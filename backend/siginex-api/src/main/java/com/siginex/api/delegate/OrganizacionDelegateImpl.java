package com.siginex.api.delegate;

import com.siginex.api.domain.Organizacion;
import com.siginex.api.domain.SiginexPrincipal;
import com.siginex.api.dto.organizacion.CrearOrganizacionRequest;
import com.siginex.api.service.OrganizacionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Default {@link OrganizacionDelegate} implementation.
 *
 * <p>Faithful to the original {@code diagnostico-service.ts}: no admin role is
 * required, any valid session of the tenant may create or list. The target
 * tenant is always the actor's own tenant, taken from the
 * {@link SiginexPrincipal}; the client never supplies a {@code tenant_id}.
 * Because every data access runs under that tenant via {@code runInTenant},
 * Row-Level Security makes organizaciones of other tenants invisible.
 */
@Component
@RequiredArgsConstructor
public class OrganizacionDelegateImpl implements OrganizacionDelegate {

    private final OrganizacionService organizacionService;

    @Override
    public Organizacion crear(SiginexPrincipal actor, CrearOrganizacionRequest request) {
        UUID tenantId = actor.tenantId();
        Organizacion aInsertar = Organizacion.builder()
                .id(null)
                .tenantId(tenantId)
                .nombre(request.getNombre())
                .sector(request.getSector())
                .tamano(request.getTamano())
                .build();
        return organizacionService.crear(tenantId, aInsertar);
    }

    @Override
    public List<Organizacion> listar(SiginexPrincipal actor) {
        return organizacionService.listar(actor.tenantId());
    }
}
