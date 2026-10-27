package com.siginex.api.dto.kb;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.siginex.api.domain.KbModulo;
import lombok.Builder;
import lombok.Value;

/**
 * Response body for a KB module in {@code GET /api/v1/kb/modulos}. The JSON key
 * is {@code short} (faithful to the original bank shape); it is populated from
 * {@link KbModulo#getShortNombre()}.
 */
@Value
@Builder
public class KbModuloResponse {

    String id;
    String nombre;

    @JsonProperty("short")
    String shortNombre;
    String weight;
    String escala;

    public static KbModuloResponse from(KbModulo modulo) {
        return KbModuloResponse.builder()
                .id(modulo.getId())
                .nombre(modulo.getNombre())
                .shortNombre(modulo.getShortNombre())
                .weight(modulo.getWeight())
                .escala(modulo.getEscala())
                .build();
    }
}
