package com.siginex.api.auth;

import java.time.Instant;
import java.util.UUID;

/**
 * Session row joined with its user, as returned when validating a session by
 * token hash. Carries no secret beyond the already-hashed lookup key, which is
 * not retained here.
 */
public record SesionInfo(
        UUID sesionId,
        UUID usuarioId,
        UUID tenantId,
        String email,
        String rol,
        boolean usuarioActivo,
        boolean revocada,
        Instant expiraEn) {
}
