package com.siginex.api.security;

import java.util.UUID;

/**
 * Authenticated user bound to the request by {@link SessionAuthenticationFilter}.
 *
 * <p>Carries only what later layers need to authorize and to resolve the tenant
 * server-side: the user id, the tenant id, the email and the role. No secret
 * (password hash, session token) is ever held here.
 */
public record SiginexPrincipal(UUID usuarioId, UUID tenantId, String email, String rol) {
}
