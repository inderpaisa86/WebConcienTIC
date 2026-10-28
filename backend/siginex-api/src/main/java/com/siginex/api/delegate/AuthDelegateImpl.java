package com.siginex.api.delegate;

import com.siginex.api.config.TokenHasher;
import com.siginex.api.domain.AuthUser;
import com.siginex.api.domain.SesionInfo;
import com.siginex.api.domain.SiginexPrincipal;
import com.siginex.api.exception.AuthException;
import com.siginex.api.service.AuthService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;

/**
 * Implementación por defecto de {@link AuthDelegate}. Dueño de las REGLAS DE
 * NEGOCIO y la ORQUESTACIÓN de la autenticación; el {@link AuthService} quedó
 * reducido a acceso a datos.
 *
 * <p>Comportamiento de seguridad preservado EXACTAMENTE del antiguo
 * {@code AuthServiceImpl} (refactor de capas, no de funcionalidad):
 * <ul>
 *   <li><b>Resistencia a enumeración (D7):</b> cada intento ejecuta
 *       {@link PasswordEncoder#matches} —contra el hash real o un dummy
 *       precomputado cuando el email no existe— y SIEMPRE surface el mismo
 *       {@link AuthException} genérico.</li>
 *   <li><b>Bloqueo (D8):</b> tras {@value #MAX_FALLOS} fallos consecutivos la
 *       cuenta se bloquea con backoff escalonado; un login correcto resetea el
 *       contador y fija {@code ultimo_login}.</li>
 *   <li>Contraseñas y hashes NUNCA se registran ni auditan.</li>
 *   <li>La verificación de credenciales ocurre FUERA de RLS (el Service busca
 *       por email en {@code runWithoutTenant}).</li>
 * </ul>
 */
@Component
public class AuthDelegateImpl implements AuthDelegate {

    static final int MAX_FALLOS = 5;
    private static final Duration SESSION_TTL = Duration.ofHours(8);
    private static final int TOKEN_BYTES = 32;

    private final AuthService authService;
    private final PasswordEncoder passwordEncoder;
    private final TokenHasher tokenHasher;
    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * Hash precomputado de una contraseña aleatoria desechable. Se usa para
     * gastar un tiempo de CPU comparable cuando el email no existe, de modo que
     * el tiempo de respuesta no filtre la existencia de la cuenta.
     */
    private final String dummyHash;

    public AuthDelegateImpl(AuthService authService,
                            PasswordEncoder passwordEncoder,
                            TokenHasher tokenHasher) {
        this.authService = authService;
        this.passwordEncoder = passwordEncoder;
        this.tokenHasher = tokenHasher;
        this.dummyHash = passwordEncoder.encode("dummy-" + System.nanoTime());
    }

    @Override
    public IssuedSession login(String email, String password, String userAgent, String ip) {
        Optional<AuthUser> found = authService.buscarAuthPorEmail(email);

        if (found.isEmpty()) {
            // Gastar tiempo comparable y fallar igual que una contraseña errónea.
            passwordEncoder.matches(password, dummyHash);
            authService.registrarEvento(null, email, "login_fallido", "usuario", null,
                    Map.of("email", email, "motivo", "email_inexistente"));
            throw new AuthException("credenciales invalidas");
        }

        AuthUser user = found.get();

        if (isLocked(user)) {
            // Aún se verifica el hash para mantener el timing uniforme; nunca autentica.
            passwordEncoder.matches(password, user.passwordHash());
            authService.registrarEvento(user.tenantId(), email, "login_bloqueado", "usuario", user.id().toString(),
                    Map.of("email", email));
            throw new AuthException("credenciales invalidas");
        }

        boolean matches = passwordEncoder.matches(password, user.passwordHash());

        if (!matches || !user.activo()) {
            registrarFallo(user, email);
            throw new AuthException("credenciales invalidas");
        }

        authService.resetearFallosYLogin(user.id());

        String token = generarToken();
        String tokenHash = tokenHasher.hash(token);
        Instant expiraEn = Instant.now().plus(SESSION_TTL);
        authService.crearSesion(user.id(), user.tenantId(), tokenHash, expiraEn, userAgent, ip);

        authService.registrarEvento(user.tenantId(), email, "login_exitoso", "usuario", user.id().toString(),
                Map.of("email", email));

        return new IssuedSession(token, expiraEn);
    }

    @Override
    public Optional<SiginexPrincipal> validarSesion(String tokenHash) {
        Optional<SesionInfo> found = authService.buscarSesionPorHash(tokenHash);
        if (found.isEmpty()) {
            return Optional.empty();
        }
        SesionInfo sesion = found.get();
        if (sesion.revocada() || !sesion.usuarioActivo()) {
            return Optional.empty();
        }
        if (sesion.expiraEn() == null || sesion.expiraEn().isBefore(Instant.now())) {
            return Optional.empty();
        }
        return Optional.of(new SiginexPrincipal(
                sesion.usuarioId(), sesion.tenantId(), sesion.email(), sesion.rol()));
    }

    @Override
    public void logout(String tokenHash, SiginexPrincipal principal) {
        authService.revocarSesion(tokenHash);
        if (principal != null) {
            authService.registrarEvento(principal.tenantId(), principal.email(), "logout", "usuario",
                    principal.usuarioId().toString(), Map.of("email", principal.email()));
        }
    }

    private void registrarFallo(AuthUser user, String email) {
        int fallos = user.fallosLogin() + 1;
        Instant bloqueadoHasta = fallos >= MAX_FALLOS ? Instant.now().plus(backoff(fallos)) : null;
        authService.incrementarFallos(user.id(), bloqueadoHasta);
        authService.registrarEvento(user.tenantId(), email, "login_fallido", "usuario", user.id().toString(),
                Map.of("email", email, "motivo", "credenciales"));
    }

    private static boolean isLocked(AuthUser user) {
        return user.bloqueadoHasta() != null && user.bloqueadoHasta().isAfter(Instant.now());
    }

    /** Backoff escalonado: 5 fallos -> 1 min, 6 -> 5 min, 7+ -> 15 min. */
    private static Duration backoff(int fallos) {
        if (fallos <= MAX_FALLOS) {
            return Duration.ofMinutes(1);
        }
        if (fallos == MAX_FALLOS + 1) {
            return Duration.ofMinutes(5);
        }
        return Duration.ofMinutes(15);
    }

    private String generarToken() {
        byte[] bytes = new byte[TOKEN_BYTES];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
