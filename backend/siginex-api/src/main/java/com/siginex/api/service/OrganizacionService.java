package com.siginex.api.service;

import com.siginex.api.domain.Organizacion;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Data coordination for organizaciones. Every operation runs under the actor's
 * tenant so PostgreSQL Row-Level Security isolates the rows. Implemented by
 * {@link OrganizacionServiceImpl}.
 *
 * <p>This layer holds no business rules; the {@code tenantId} it receives is
 * always resolved server-side by the delegate from the authenticated principal.
 * {@link #buscarPorId(UUID, UUID)} exists so FEAT-002 can verify that an
 * organizacion belongs to the actor's tenant before creating a diagnostico.
 */
public interface OrganizacionService {

    Organizacion crear(UUID tenantId, Organizacion org);

    List<Organizacion> listar(UUID tenantId);

    Optional<Organizacion> buscarPorId(UUID tenantId, UUID id);
}
