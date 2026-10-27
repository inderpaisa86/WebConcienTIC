package com.siginex.api.domain.calculo;

import lombok.Builder;
import lombok.Value;

/**
 * Tarea del plan de mejora generada por una brecha. Port 1:1 del tipo TS
 * {@code TareaPlan}. Inmutable con Lombok {@code @Value @Builder} (convención
 * del proyecto: sin {@code record}).
 *
 * <p>El plan se ordena de forma <b>estable</b> por prioridad ({@code "alta"}
 * antes que {@code "media"}); el resto de campos conserva el valor de la brecha.
 */
@Value
@Builder
public class TareaPlan {

    String modulo;
    String preguntaId;
    String accion;
    String responsable;
    String prioridad;
    int plazoDias;
    int fase;
    String norma;
    String criterioCierre;
}
