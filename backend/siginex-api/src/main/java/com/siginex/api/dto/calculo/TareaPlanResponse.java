package com.siginex.api.dto.calculo;

import com.siginex.api.domain.calculo.TareaPlanLectura;
import lombok.Builder;
import lombok.Value;

/**
 * Tarea del plan de mejora expuesta por la API ({@code GET /plan-mejora}).
 * Espejo del {@link TareaPlanLectura} leído de {@code tarea_mejora} (diseño
 * §9.7). Inmutable con Lombok {@code @Value @Builder} (convención del proyecto:
 * sin {@code record}).
 */
@Value
@Builder
public class TareaPlanResponse {

    String skillId;
    String preguntaId;
    String brecha;
    String accion;
    String normaRef;
    String prioridad;
    String responsable;
    Integer plazoDias;
    Integer fase;
    String criterioCierre;

    /**
     * Mapea una {@link TareaPlanLectura} de dominio a su DTO de respuesta.
     *
     * @param tarea tarea leída de BD.
     * @return el DTO de respuesta.
     */
    public static TareaPlanResponse from(TareaPlanLectura tarea) {
        return TareaPlanResponse.builder()
                .skillId(tarea.getSkillId())
                .preguntaId(tarea.getPreguntaId())
                .brecha(tarea.getBrecha())
                .accion(tarea.getAccion())
                .normaRef(tarea.getNormaRef())
                .prioridad(tarea.getPrioridad())
                .responsable(tarea.getResponsable())
                .plazoDias(tarea.getPlazoDias())
                .fase(tarea.getFase())
                .criterioCierre(tarea.getCriterioCierre())
                .build();
    }
}
