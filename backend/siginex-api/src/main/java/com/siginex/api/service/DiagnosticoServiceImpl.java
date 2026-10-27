package com.siginex.api.service;

import com.siginex.api.config.TenantTransactionTemplate;
import com.siginex.api.domain.DiagnosticoDetalle;
import com.siginex.api.domain.DiagnosticoResumen;
import com.siginex.api.repository.DiagnosticoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Default {@link DiagnosticoService} implementation. Every operation runs
 * inside {@link TenantTransactionTemplate#runInTenant(UUID, java.util.function.Supplier)}
 * so {@code app.tenant_id} is fixed for the transaction and PostgreSQL
 * Row-Level Security isolates the rows to the actor's tenant.
 */
@Service
@RequiredArgsConstructor
public class DiagnosticoServiceImpl implements DiagnosticoService {

    private final DiagnosticoRepository diagnosticoRepository;
    private final TenantTransactionTemplate tenantTx;

    @Override
    public String kbVersionVigente(UUID tenantId) {
        return tenantTx.runInTenant(tenantId, diagnosticoRepository::kbVersionVigente);
    }

    @Override
    public DiagnosticoDetalle crear(UUID tenantId, UUID organizacionId, String kbVersion) {
        return tenantTx.runInTenant(tenantId,
                () -> diagnosticoRepository.insertar(tenantId, organizacionId, kbVersion));
    }

    @Override
    public List<DiagnosticoResumen> listar(UUID tenantId) {
        return tenantTx.runInTenant(tenantId, diagnosticoRepository::listar);
    }

    @Override
    public Optional<DiagnosticoDetalle> buscarPorId(UUID tenantId, UUID id) {
        return tenantTx.runInTenant(tenantId, () -> diagnosticoRepository.buscarPorId(id));
    }
}
