package com.siginex.api.dto.auth;

import java.util.UUID;

/**
 * Current-user response for {@code GET /auth/me}. Exposes no secret.
 */
public record MeResponse(UUID usuarioId, UUID tenantId, String email, String rol) {
}
