package com.siginex.api.dto.diagnostico;

import com.siginex.api.domain.DiagnosticoResumen;
import lombok.Builder;
import lombok.Value;

import java.time.Instant;
import java.util.UUID;

/**
 * Diagnostico summary response body. Same role as
 * {@code OrganizacionResponse.from(...)}: maps the domain value to the shape
 * exposed by the API.
 */
@Value
@Builder
public class DiagnosticoResumenResponse {

    UUID id;
    UUID organizacionId;
    String organizacionNombre;
    String estado;
    String scoreSgi;
    Integer nivelSgi;
    Instant creadoEn;

    public static DiagnosticoResumenResponse from(DiagnosticoResumen resumen) {
        return DiagnosticoResumenResponse.builder()
                .id(resumen.getId())
                .organizacionId(resumen.getOrganizacionId())
                .organizacionNombre(resumen.getOrganizacionNombre())
                .estado(resumen.getEstado())
                .scoreSgi(resumen.getScoreSgi())
                .nivelSgi(resumen.getNivelSgi())
                .creadoEn(resumen.getCreadoEn())
                .build();
    }
}
