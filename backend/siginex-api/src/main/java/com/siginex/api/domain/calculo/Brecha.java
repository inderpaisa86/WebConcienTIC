package com.siginex.api.domain.calculo;

import lombok.Builder;
import lombok.Value;

/**
 * Brecha de cumplimiento (pregunta respondida 0 o 1). Port 1:1 del tipo TS
 * {@code Brecha}. Inmutable con Lombok {@code @Value @Builder} (convención del
 * proyecto: sin {@code record}).
 *
 * <p>{@code evaluacion} es 0 o 1 (las respuestas 2 y {@code na} no generan
 * brecha). {@code prioridad} es {@code "alta"} o {@code "media"}. La lista de
 * brechas <b>no se reordena</b>: conserva el orden de recorrido del KB.
 */
@Value
@Builder
public class Brecha {

    String modulo;
    String preguntaId;
    String brecha;
    String norma;
    int evaluacion;
    String prioridad;
}
