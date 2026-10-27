package com.siginex.api.domain.calculo;

import lombok.Builder;
import lombok.Value;

/**
 * Nivel de madurez (1..5) con su nombre de banda. Port 1:1 del tipo TS
 * {@code Nivel { n, nombre }} de {@code scoring.ts::levelFor}. Inmutable con
 * Lombok {@code @Value @Builder} (convención del proyecto: sin {@code record}).
 */
@Value
@Builder
public class Nivel {

    int n;
    String nombre;
}
