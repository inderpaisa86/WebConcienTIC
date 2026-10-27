package com.siginex.api.domain.calculo;

import lombok.Builder;
import lombok.Value;

/**
 * Tarea del plan de mejora leída de BD. Port 1:1 del tipo TS {@code TareaPlanUI}
 * de {@code diagnostico-service.ts::obtenerPlanMejora}. Inmutable con Lombok
 * {@code @Value @Builder} (convención del proyecto: sin {@code record}).
 *
 * <p>A diferencia de {@link TareaPlan} (que produce el motor), este value
 * refleja la fila de {@code tarea_mejora}: incluye {@code brecha} (NOT NULL en
 * la tabla) y usa {@code skillId} (columna) en lugar de {@code modulo}.
 * {@code preguntaId}/{@code normaRef}/{@code responsable}/{@code plazoDias}/
 * {@code fase}/{@code criterioCierre} son nullable en la columna.
 */
@Value
@Builder
public class TareaPlanLectura {

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
}
