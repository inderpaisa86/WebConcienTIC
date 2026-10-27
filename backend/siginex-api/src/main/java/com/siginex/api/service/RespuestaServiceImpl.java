package com.siginex.api.service;

import com.siginex.api.config.TenantTransactionTemplate;
import com.siginex.api.domain.RespuestaEntrada;
import com.siginex.api.repository.RespuestaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Default {@link RespuestaService} implementation. Every operation runs inside
 * {@link TenantTransactionTemplate#runInTenant(UUID, java.util.function.Supplier)}
 * so {@code app.tenant_id} is fixed for the transaction and PostgreSQL
 * Row-Level Security isolates the answers to the actor's tenant.
 */
@Service
@RequiredArgsConstructor
public class RespuestaServiceImpl implements RespuestaService {

    private final RespuestaRepository respuestaRepository;
    private final TenantTransactionTemplate tenantTx;

    @Override
    public void guardar(UUID tenantId, UUID diagnosticoId, List<RespuestaEntrada> entradas) {
        tenantTx.runInTenant(tenantId, () -> {
            respuestaRepository.upsertLote(tenantId, diagnosticoId, entradas);
            return null;
        });
    }

    @Override
    public Map<String, String> listar(UUID tenantId, UUID diagnosticoId) {
        return tenantTx.runInTenant(tenantId,
                () -> respuestaRepository.listarPorDiagnostico(diagnosticoId));
    }
}
