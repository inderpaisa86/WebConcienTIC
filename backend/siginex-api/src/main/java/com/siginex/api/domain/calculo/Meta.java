package com.siginex.api.domain.calculo;

import lombok.Builder;
import lombok.Value;

/**
 * Metadatos del resultado consolidado. Port 1:1 del campo {@code meta} del TS.
 * Inmutable con Lombok {@code @Value @Builder} (convención del proyecto: sin
 * {@code record}).
 *
 * <p>{@code generadoEn} es el instante de generación en ISO-8601 (equivalente a
 * {@code new Date().toISOString()}). Ningún test de paridad lo asevera.
 */
@Value
@Builder
public class Meta {

    int preguntasAplicables;
    int respondidas;
    String generadoEn;
}
