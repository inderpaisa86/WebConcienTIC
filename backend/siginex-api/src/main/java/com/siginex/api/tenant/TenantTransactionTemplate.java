package com.siginex.api.tenant;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.UUID;
import java.util.function.Supplier;

/**
 * Java equivalent of the original {@code withTenant} helper (src/lib/db/tenant.ts).
 *
 * <p>{@link #runInTenant(UUID, Supplier)} opens a transaction and fixes
 * {@code app.tenant_id} for it with {@code set_config(..., is_local => true)} so
 * PostgreSQL Row-Level Security isolates the data. Because the setting is local
 * to the transaction, it never leaks when the Hikari pool reuses the connection.
 * The RLS policy compares {@code tenant_id = siginex.current_tenant()}, and
 * {@code current_tenant()} reads that setting.
 *
 * <p>{@link #runWithoutTenant(Supplier)} runs work in a transaction without
 * fixing any tenant. It exists only for the login lookup (decision D4), which
 * must find the user before the tenant is known; that lookup is the single
 * access to {@code usuario} outside RLS and stays encapsulated in the auth
 * repository.
 */
@Component
public class TenantTransactionTemplate {

    private static final String SET_TENANT_SQL = "SELECT set_config('app.tenant_id', :tenantId, true)";

    private final TransactionTemplate transactionTemplate;
    private final NamedParameterJdbcTemplate jdbc;

    public TenantTransactionTemplate(TransactionTemplate transactionTemplate, NamedParameterJdbcTemplate jdbc) {
        this.transactionTemplate = transactionTemplate;
        this.jdbc = jdbc;
    }

    public <T> T runInTenant(UUID tenantId, Supplier<T> work) {
        if (tenantId == null) {
            throw new IllegalArgumentException("tenantId is required to run inside a tenant");
        }
        return transactionTemplate.execute(status -> {
            jdbc.queryForObject(SET_TENANT_SQL,
                    new MapSqlParameterSource("tenantId", tenantId.toString()), String.class);
            return work.get();
        });
    }

    public <T> T runWithoutTenant(Supplier<T> work) {
        return transactionTemplate.execute(status -> work.get());
    }
}
