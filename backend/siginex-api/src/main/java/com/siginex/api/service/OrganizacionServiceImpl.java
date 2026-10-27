package com.siginex.api.service;

import com.siginex.api.config.TenantTransactionTemplate;
import com.siginex.api.domain.Organizacion;
import com.siginex.api.repository.OrganizacionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Default {@link OrganizacionService} implementation. Every operation runs
 * inside {@link TenantTransactionTemplate#runInTenant(UUID, java.util.function.Supplier)}
 * so {@code app.tenant_id} is fixed for the transaction and PostgreSQL
 * Row-Level Security isolates the rows to the actor's tenant.
 */
@Service
@RequiredArgsConstructor
public class OrganizacionServiceImpl implements OrganizacionService {

    private final OrganizacionRepository organizacionRepository;
    private final TenantTransactionTemplate tenantTx;

    @Override
    public Organizacion crear(UUID tenantId, Organizacion org) {
        return tenantTx.runInTenant(tenantId, () -> organizacionRepository.insertar(org));
    }

    @Override
    public List<Organizacion> listar(UUID tenantId) {
        return tenantTx.runInTenant(tenantId, organizacionRepository::listar);
    }

    @Override
    public Optional<Organizacion> buscarPorId(UUID tenantId, UUID id) {
        return tenantTx.runInTenant(tenantId, () -> organizacionRepository.buscarPorId(id));
    }
}
