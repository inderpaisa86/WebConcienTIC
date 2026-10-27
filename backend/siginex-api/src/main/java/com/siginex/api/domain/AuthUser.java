package com.siginex.api.domain;


import com.siginex.api.service.AuthService;
import java.time.Instant;
import java.util.UUID;

/**
 * Minimal authentication projection of {@code siginex.usuario} used only inside
 * the auth layer during login. Holds the password hash, so it must never leave
 * {@link AuthService} and must never be logged.
 */
public record AuthUser(
        UUID id,
        UUID tenantId,
        String passwordHash,
        boolean activo,
        String rol,
        short fallosLogin,
        Instant bloqueadoHasta) {
}
