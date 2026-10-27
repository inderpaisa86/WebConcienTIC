package com.siginex.api.dto.organizacion;

import com.siginex.api.domain.Organizacion;
import lombok.Builder;
import lombok.Value;

import java.time.Instant;
import java.util.UUID;

/**
 * Organizacion response body. Same role as {@code UsuarioResponse.from(...)}:
 * maps the domain value to the shape exposed by the API.
 */
@Value
@Builder
public class OrganizacionResponse {

    UUID id;
    UUID tenantId;
    String nombre;
    String sector;
    String tamano;
    Instant creadoEn;

    public static OrganizacionResponse from(Organizacion org) {
        return OrganizacionResponse.builder()
                .id(org.getId())
                .tenantId(org.getTenantId())
                .nombre(org.getNombre())
                .sector(org.getSector())
                .tamano(org.getTamano())
                .creadoEn(org.getCreadoEn())
                .build();
    }
}
