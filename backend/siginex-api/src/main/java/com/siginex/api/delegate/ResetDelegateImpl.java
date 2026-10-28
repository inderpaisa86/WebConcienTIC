package com.siginex.api.delegate;

import com.siginex.api.config.TokenHasher;
import com.siginex.api.domain.AuthUser;
import com.siginex.api.domain.ResetTokenInfo;
import com.siginex.api.dto.reset.ConfirmarResetRequest;
import com.siginex.api.dto.reset.SolicitarResetRequest;
import com.siginex.api.exception.AuthException;
import com.siginex.api.service.AuditoriaService;
import com.siginex.api.service.ResetService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;

/**
 * Implementación por defecto de {@link ResetDelegate}.
 *
 * <p>Token: 32 bytes de {@link SecureRandom} en Base64 URL-safe (mismo patrón
 * que la sesión). Solo se persiste su hash SHA-256 ({@link TokenHasher}); el
 * token en claro viaja solo en el enlace del correo y jamás se loguea.
 * Expiración corta (30 min), un solo uso.
 *
 * <p>No-enumeración: {@link #solicitar} responde siempre igual exista o no el
 * email, y siempre consume un trabajo comparable (genera token + hash incluso
 * si el usuario no existe) para no filtrar por temporización. Un fallo de
 * correo tampoco se propaga como señal al cliente.
 *
 * <p>{@link #confirmar} nunca distingue entre "token inexistente", "usado" o
 * "expirado": todas producen el mismo {@link AuthException} genérico (401).
 */
@Component
@Slf4j
public class ResetDelegateImpl implements ResetDelegate {

    private static final int TOKEN_BYTES = 32;
    private static final int MIN_PASSWORD = 12;
    private static final Duration TOKEN_TTL = Duration.ofMinutes(30);
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final ResetService resetService;
    private final PasswordEncoder passwordEncoder;
    private final TokenHasher tokenHasher;
    private final AuditoriaService auditoriaService;

    /** Base del front para construir el enlace, p. ej. https://concientic.com */
    @Value("${siginex.reset.front-base-url:http://localhost:3000}")
    private String frontBaseUrl;

    public ResetDelegateImpl(ResetService resetService,
                             PasswordEncoder passwordEncoder,
                             TokenHasher tokenHasher,
                             AuditoriaService auditoriaService) {
        this.resetService = resetService;
        this.passwordEncoder = passwordEncoder;
        this.tokenHasher = tokenHasher;
        this.auditoriaService = auditoriaService;
    }

    @Override
    public void solicitar(SolicitarResetRequest request) {
        String email = request.email();

        // Generamos token + hash SIEMPRE, exista o no el usuario, para que el
        // coste (y el tiempo) sea comparable: no-enumeración por temporización.
        String token = generarToken();
        String tokenHash = tokenHasher.hash(token);
        Instant expiraEn = Instant.now().plus(TOKEN_TTL);

        Optional<AuthUser> usuario = resetService.buscarUsuarioPorEmail(email);

        if (usuario.isPresent() && usuario.get().activo()) {
            AuthUser u = usuario.get();
            String enlace = construirEnlace(token);
            try {
                resetService.emitirYEnviar(u.id(), tokenHash, expiraEn, email, enlace);
            } catch (RuntimeException ex) {
                // No revelar el fallo al cliente (no-enumeración). Dejar
                // constancia SIN token ni enlace.
                log.warn("[reset] fallo al emitir/enviar recuperacion para un usuario existente");
            }
            auditoriaService.registrar(u.tenantId(), email, "reset_solicitado", "usuario",
                    u.id().toString(), Map.of("email", email));
        } else {
            // Usuario inexistente o inactivo: no se hace nada observable. Se
            // audita sin tenant (puede no haberlo) y sin señal al cliente.
            auditoriaService.registrar(null, email, "reset_solicitado_sin_cuenta", "usuario", null,
                    Map.of("email", email));
        }
        // Siempre termina igual: el controller devuelve 204 en cualquier caso.
    }

    @Override
    public void confirmar(ConfirmarResetRequest request) {
        // Política de contraseña (defensa en profundidad; el DTO ya valida @Size).
        if (request.nuevaPassword() == null || request.nuevaPassword().length() < MIN_PASSWORD) {
            throw new AuthException("solicitud invalida");
        }

        String tokenHash = tokenHasher.hash(request.token());
        ResetTokenInfo token = resetService.buscarTokenPorHash(tokenHash)
                .orElseThrow(() -> new AuthException("token invalido"));

        // Mismo error genérico para usado/expirado: no se distingue el motivo.
        if (token.isUsado()) {
            throw new AuthException("token invalido");
        }
        if (token.getExpiraEn() == null || token.getExpiraEn().isBefore(Instant.now())) {
            throw new AuthException("token invalido");
        }

        String nuevoHash = passwordEncoder.encode(request.nuevaPassword());
        resetService.confirmar(token, nuevoHash);

        auditoriaService.registrar(token.getTenantId(), null, "reset_completado", "usuario",
                token.getUsuarioId().toString(), Map.of("via", "reset_password"));
    }

    private String construirEnlace(String token) {
        String base = frontBaseUrl.endsWith("/")
                ? frontBaseUrl.substring(0, frontBaseUrl.length() - 1)
                : frontBaseUrl;
        String tokenParam = URLEncoder.encode(token, StandardCharsets.UTF_8);
        return base + "/siginex/restablecer?token=" + tokenParam;
    }

    private static String generarToken() {
        byte[] bytes = new byte[TOKEN_BYTES];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
