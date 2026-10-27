package com.siginex.api.dto.diagnostico;

import com.siginex.api.domain.DiagnosticoDetalle;
import lombok.Builder;
import lombok.Value;

import java.util.UUID;

/**
 * Diagnostico header response body returned by create and get-by-id. Same role
 * as {@code OrganizacionResponse.from(...)}: maps the domain value to the shape
 * exposed by the API. The answers map is deferred to Fase 2c.
 */
@Value
@Builder
public class DiagnosticoDetalleResponse {

    UUID id;
    UUID organizacionId;
    String organizacionNombre;
    String estado;

    public static DiagnosticoDetalleResponse from(DiagnosticoDetalle detalle) {
        return DiagnosticoDetalleResponse.builder()
                .id(detalle.getId())
                .organizacionId(detalle.getOrganizacionId())
                .organizacionNombre(detalle.getOrganizacionNombre())
                .estado(detalle.getEstado())
                .build();
    }
}
