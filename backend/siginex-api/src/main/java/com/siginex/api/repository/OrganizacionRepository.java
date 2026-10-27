package com.siginex.api.repository;

import com.siginex.api.domain.Organizacion;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Data access for organizaciones. Every method runs under the actor's tenant:
 * the service wraps the calls in
 * {@code TenantTransactionTemplate.runInTenant(tenantId, ...)} so PostgreSQL
 * Row-Level Security isolates the rows. SQL only; transaction and tenant
 * scoping are the service's concern.
 */
public interface OrganizacionRepository {

    Organizacion insertar(Organizacion org);

    List<Organizacion> listar();

    Optional<Organizacion> buscarPorId(UUID id);
}
