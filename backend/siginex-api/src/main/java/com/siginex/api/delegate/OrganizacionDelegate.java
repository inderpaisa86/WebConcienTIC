package com.siginex.api.delegate;

import com.siginex.api.domain.Organizacion;
import com.siginex.api.domain.SiginexPrincipal;
import com.siginex.api.dto.organizacion.CrearOrganizacionRequest;

import java.util.List;

/**
 * Business coordination for organizaciones. The controller delegates here after
 * format validation. Faithful to the original {@code diagnostico-service.ts},
 * no role check is required: any valid session of the tenant may create or
 * list. The target tenant is always the actor's own tenant, taken from the
 * {@link SiginexPrincipal}; the client never supplies a {@code tenant_id}.
 * Implemented by {@link OrganizacionDelegateImpl}.
 */
public interface OrganizacionDelegate {

    Organizacion crear(SiginexPrincipal actor, CrearOrganizacionRequest request);

    List<Organizacion> listar(SiginexPrincipal actor);
}
