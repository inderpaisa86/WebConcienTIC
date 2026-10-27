package com.siginex.api.domain.calculo;

import lombok.Builder;
import lombok.Value;

/**
 * Ruta del plan de aprendizaje (adopción de competencias). Port 1:1 del tipo TS
 * {@code RutaAprendizajePlan}. Inmutable con Lombok {@code @Value @Builder}
 * (convención del proyecto: sin {@code record}).
 *
 * <p>{@code nivel} es {@code "Básico"} (prioridad 0) o {@code "Intermedio"}
 * (prioridad 1); {@code origen} es siempre {@code "interno"}.
 */
@Value
@Builder
public class RutaAprendizajePlan {

    String competencia;
    String modulo;
    String nivel;
    int brechas;
    String origen;
}
