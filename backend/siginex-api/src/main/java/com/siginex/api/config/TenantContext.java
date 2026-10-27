package com.siginex.api.config;

import java.util.UUID;

/**
 * Per-request holder for the tenant resolved from the session.
 *
 * <p>The {@code SessionAuthenticationFilter} resolves the authenticated user's
 * {@code tenant_id} and stores it here so that any layer serving that request
 * can fix it on the database transaction (via {@link TenantTransactionTemplate})
 * without the tenant ever being supplied by the client.
 *
 * <p>Backed by a {@link ThreadLocal}: Spring MVC serves each request on a single
 * thread, so the value is scoped to the request. The filter clears it at the end
 * of the request to avoid leaking the tenant into a pooled thread.
 */
public final class TenantContext {

    private static final ThreadLocal<UUID> CURRENT_TENANT = new ThreadLocal<>();

    private TenantContext() {
    }

    public static void setTenantId(UUID tenantId) {
        CURRENT_TENANT.set(tenantId);
    }

    public static UUID getTenantId() {
        return CURRENT_TENANT.get();
    }

    public static void clear() {
        CURRENT_TENANT.remove();
    }
}
