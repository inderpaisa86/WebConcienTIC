package com.siginex.api.service;

import com.siginex.api.domain.AuthUser;
import com.siginex.api.domain.SesionInfo;
import com.siginex.api.exception.AuthException;
import com.siginex.api.repository.AuthRepository;
import com.siginex.api.domain.SiginexPrincipal;
import com.siginex.api.config.TokenHasher;
import com.siginex.api.config.TenantTransactionTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;

/**
 * Default {@link AuthService} implementation: all database access and the
 * coordination of password verification, lockout, session issuing and auditing.
 *
 * <p>Enumeration resistance (D7): every attempt runs {@link PasswordEncoder}
 * {@code matches}, against the real hash or a precomputed dummy when the email
 * does not exist, and always surfaces the same generic {@link AuthException}.
 *
 * <p>Lockout (D8): after {@value #MAX_FALLOS} consecutive failures the account
 * is locked with a staged backoff; a correct login resets the counter and sets
 * {@code ultimo_login}. Passwords and hashes are never logged or audited.
 */
@Service
public class AuthServiceImpl implements AuthService {

    static final int MAX_FALLOS = 5;
    private static final Duration SESSION_TTL = Duration.ofHours(8);
    private static final int TOKEN_BYTES = 32;

    private final AuthRepository authRepository;
    private final AuditoriaService auditoriaService;
    private final TenantTransactionTemplate tenantTx;
    private final PasswordEncoder passwordEncoder;
    private final TokenHasher tokenHasher;
    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * Precomputed hash of a random throwaway password. Used to spend comparable
     * CPU time when the email does not exist, so response timing does not leak
     * account existence.
     */
    private final String dummyHash;

    public AuthServiceImpl(AuthRepository authRepository,
                           AuditoriaService auditoriaService,
                           TenantTransactionTemplate tenantTx,
                           PasswordEncoder passwordEncoder,
                           TokenHasher tokenHasher) {
        this.authRepository = authRepository;
        this.auditoriaService = auditoriaService;
        this.tenantTx = tenantTx;
        this.passwordEncoder = passwordEncoder;
        this.tokenHasher = tokenHasher;
        this.dummyHash = passwordEncoder.encode("dummy-" + System.nanoTime());
    }

    @Override
    public IssuedSession autenticar(String email, String password, String userAgent, String ip) {
        Optional<AuthUser> found = tenantTx.runWithoutTenant(() -> authRepository.findAuthByEmail(email));

        if (found.isEmpty()) {
            // Spend comparable time, then fail the same way as a wrong password.
            passwordEncoder.matches(password, dummyHash);
            auditoriaService.registrar(null, email, "login_fallido", "usuario", null,
                    Map.of("email", email, "motivo", "email_inexistente"));
            throw new AuthException("credenciales invalidas");
        }

        AuthUser user = found.get();

        if (isLocked(user)) {
            // Still verify the hash to keep timing uniform; never authenticate.
            passwordEncoder.matches(password, user.passwordHash());
            auditoriaService.registrar(user.tenantId(), email, "login_bloqueado", "usuario", user.id().toString(),
                    Map.of("email", email));
            throw new AuthException("credenciales invalidas");
        }

        boolean matches = passwordEncoder.matches(password, user.passwordHash());

        if (!matches || !user.activo()) {
            registrarFallo(user, email);
            throw new AuthException("credenciales invalidas");
        }

        authRepository.resetearFallosYLogin(user.id());

        String token = generarToken();
        String tokenHash = tokenHasher.hash(token);
        Instant expiraEn = Instant.now().plus(SESSION_TTL);
        authRepository.crearSesion(user.id(), user.tenantId(), tokenHash, expiraEn, userAgent, ip);

        auditoriaService.registrar(user.tenantId(), email, "login_exitoso", "usuario", user.id().toString(),
                Map.of("email", email));

        return new IssuedSession(token, expiraEn);
    }

    @Override
    public Optional<SiginexPrincipal> validarSesion(String tokenHash) {
        Optional<SesionInfo> found = authRepository.buscarSesionPorHash(tokenHash);
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
    public void revocar(String tokenHash, SiginexPrincipal principal) {
        authRepository.revocarSesion(tokenHash);
        if (principal != null) {
            auditoriaService.registrar(principal.tenantId(), principal.email(), "logout", "usuario",
                    principal.usuarioId().toString(), Map.of("email", principal.email()));
        }
    }

    private void registrarFallo(AuthUser user, String email) {
        int fallos = user.fallosLogin() + 1;
        Instant bloqueadoHasta = fallos >= MAX_FALLOS ? Instant.now().plus(backoff(fallos)) : null;
        authRepository.incrementarFallos(user.id(), bloqueadoHasta);
        auditoriaService.registrar(user.tenantId(), email, "login_fallido", "usuario", user.id().toString(),
                Map.of("email", email, "motivo", "credenciales"));
    }

    private static boolean isLocked(AuthUser user) {
        return user.bloqueadoHasta() != null && user.bloqueadoHasta().isAfter(Instant.now());
    }

    /** Staged backoff: 5 failures -> 1 min, 6 -> 5 min, 7+ -> 15 min. */
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
