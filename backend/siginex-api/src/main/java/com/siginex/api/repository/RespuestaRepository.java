package com.siginex.api.repository;

import com.siginex.api.domain.RespuestaEntrada;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Data access for the per-tenant {@code respuesta} table. Every method runs
 * under the actor's tenant because the service wraps the calls in
 * {@code TenantTransactionTemplate.runInTenant(tenantId, ...)}, so PostgreSQL
 * Row-Level Security isolates the rows. Implemented by
 * {@link JdbcRespuestaRepository}.
 */
public interface RespuestaRepository {

    /**
     * Upserts a batch of answers for a diagnostico. Faithful to the original
     * {@code guardarRespuestas}: for each entry the {@code skillId} is derived
     * from {@code preguntaId}, {@code tipo = 'cero_uno_dos'}, and the write is
     * {@code ON CONFLICT (diagnostico_id, pregunta_id) DO UPDATE} refreshing the
     * value and {@code actualizado_en}.
     */
    void upsertLote(UUID tenantId, UUID diagnosticoId, List<RespuestaEntrada> entradas);

    /**
     * Lists the answers of a diagnostico as a {@code preguntaId -> valor} map,
     * including only non-null values, faithful to {@code obtenerDiagnostico}.
     */
    Map<String, String> listarPorDiagnostico(UUID diagnosticoId);
}
