package com.siginex.api.tenant;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TenantTransactionTemplateTest {

    @Test
    void runInTenantSetsTenantConfigBeforeRunningTheWork() {
        var jdbc = mock(NamedParameterJdbcTemplate.class);
        var transactionTemplate = mock(TransactionTemplate.class);
        // Execute the callback synchronously, like a real transaction would.
        when(transactionTemplate.execute(any())).thenAnswer(invocation ->
                invocation.<TransactionCallback<?>>getArgument(0).doInTransaction(null));
        var template = new TenantTransactionTemplate(transactionTemplate, jdbc);
        UUID tenantId = UUID.randomUUID();

        String result = template.runInTenant(tenantId, () -> {
            jdbc.queryForObject("SELECT 1", new MapSqlParameterSource(), String.class);
            return "done";
        });

        assertThat(result).isEqualTo("done");

        var sqlCaptor = ArgumentCaptor.forClass(String.class);
        var paramsCaptor = ArgumentCaptor.forClass(MapSqlParameterSource.class);
        // Two queryForObject calls happened: set_config first, then the work's query.
        verify(jdbc, times(2)).queryForObject(sqlCaptor.capture(), paramsCaptor.capture(), eq(String.class));

        assertThat(sqlCaptor.getAllValues().get(0)).contains("set_config('app.tenant_id'");
        assertThat(sqlCaptor.getAllValues().get(1)).isEqualTo("SELECT 1");
        assertThat(paramsCaptor.getAllValues().get(0).getValue("tenantId")).isEqualTo(tenantId.toString());
    }

    @Test
    void runInTenantRejectsNullTenant() {
        var template = new TenantTransactionTemplate(mock(TransactionTemplate.class), mock(NamedParameterJdbcTemplate.class));

        try {
            template.runInTenant(null, () -> "x");
            assertThat(false).as("expected IllegalArgumentException").isTrue();
        } catch (IllegalArgumentException expected) {
            assertThat(expected).hasMessageContaining("tenantId");
        }
    }

    @Test
    void runWithoutTenantDoesNotSetTenantConfig() {
        var jdbc = mock(NamedParameterJdbcTemplate.class);
        var transactionTemplate = mock(TransactionTemplate.class);
        when(transactionTemplate.execute(any())).thenAnswer(invocation ->
                invocation.<TransactionCallback<?>>getArgument(0).doInTransaction(null));
        var template = new TenantTransactionTemplate(transactionTemplate, jdbc);

        String result = template.runWithoutTenant(() -> "login-lookup");

        assertThat(result).isEqualTo("login-lookup");
        org.mockito.Mockito.verifyNoInteractions(jdbc);
    }
}
