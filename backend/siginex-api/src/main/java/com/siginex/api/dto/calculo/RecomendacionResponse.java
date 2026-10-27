package com.siginex.api.dto.calculo;

import com.siginex.api.domain.calculo.ResultadoLectura;
import lombok.Builder;
import lombok.Value;

/**
 * Recomendación de prioridad alta expuesta por la API. Espejo del
 * {@link ResultadoLectura.RecomendacionLectura} leído de {@code recomendacion}.
 * Inmutable con Lombok {@code @Value @Builder} (convención del proyecto: sin
 * {@code record}).
 */
@Value
@Builder
public class RecomendacionResponse {

    String skillId;
    String prioridad;
    String texto;

    /**
     * Mapea una {@link ResultadoLectura.RecomendacionLectura} de dominio a su DTO.
     *
     * @param reco recomendación leída de BD.
     * @return el DTO de respuesta.
     */
    public static RecomendacionResponse from(ResultadoLectura.RecomendacionLectura reco) {
        return RecomendacionResponse.builder()
                .skillId(reco.getSkillId())
                .prioridad(reco.getPrioridad())
                .texto(reco.getTexto())
                .build();
    }
}
