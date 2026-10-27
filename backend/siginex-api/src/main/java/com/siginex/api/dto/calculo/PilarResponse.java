package com.siginex.api.dto.calculo;

import com.siginex.api.domain.calculo.ResultadoLectura;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;

/**
 * Pilar (módulo) de un resultado expuesto por la API. Espejo del
 * {@link ResultadoLectura.PilarLectura} leído de {@code resultado_pilar}.
 * Inmutable con Lombok {@code @Value @Builder} (convención del proyecto: sin
 * {@code record}).
 */
@Value
@Builder
public class PilarResponse {

    String skillId;
    BigDecimal cumplimientoPct;
    Integer nivel;
    String banda;

    /**
     * Mapea un {@link ResultadoLectura.PilarLectura} de dominio a su DTO.
     *
     * @param pilar pilar leído de BD.
     * @return el DTO de respuesta.
     */
    public static PilarResponse from(ResultadoLectura.PilarLectura pilar) {
        return PilarResponse.builder()
                .skillId(pilar.getSkillId())
                .cumplimientoPct(pilar.getCumplimientoPct())
                .nivel(pilar.getNivel())
                .banda(pilar.getBanda())
                .build();
    }
}
