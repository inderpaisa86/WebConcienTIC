package com.siginex.api.delegate;

import com.siginex.api.domain.SiginexPrincipal;

import java.time.Instant;
import java.util.Optional;

/**
 * Orquestación de negocio de la autenticación. Dueño de TODAS las reglas y
 * decisiones (tras el refactor de capas): verificación de credenciales con
 * Argon2id, resistencia a enumeración (timing uniforme con dummy hash), bloqueo
 * por intentos (MAX_FALLOS / backoff), emisión del token de sesión, y las reglas
 * de validez de sesión (no revocada, no expirada, usuario activo) y de logout.
 * Pide los datos y las escrituras al {@link com.siginex.api.service.AuthService}.
 * Implementado por {@link AuthDelegateImpl}.
 */
public interface AuthDelegate {

    /**
     * Autentica por email + contraseña y, en éxito, emite un token de sesión
     * opaco. Falla SIEMPRE con el mismo error genérico
     * ({@link com.siginex.api.exception.AuthException}) —credenciales inválidas,
     * cuenta inactiva o bloqueada— sin revelar el motivo (no-enumeración).
     */
    IssuedSession login(String email, String password, String userAgent, String ip);

    /**
     * Valida una sesión por el hash SHA-256 de su token opaco. Devuelve el
     * principal solo si la sesión existe, no está revocada, no ha expirado y el
     * usuario está activo.
     */
    Optional<SiginexPrincipal> validarSesion(String tokenHash);

    /** Revoca la sesión identificada por el hash del token y audita el logout. */
    void logout(String tokenHash, SiginexPrincipal principal);

    /** Token opaco (en claro, para la cookie) más su expiración absoluta. */
    record IssuedSession(String token, Instant expiraEn) {
    }
}
