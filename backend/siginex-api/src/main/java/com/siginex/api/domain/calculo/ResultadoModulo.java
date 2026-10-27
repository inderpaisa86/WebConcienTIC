package com.siginex.api.domain.calculo;

import lombok.Builder;
import lombok.Value;

/**
 * Resultado de cumplimiento de un módulo. Port 1:1 del tipo TS
 * {@code ResultadoModulo}. Inmutable con Lombok {@code @Value @Builder}
 * (convención del proyecto: sin {@code record}).
 *
 * <p><b>Tri-estado (crítico para la paridad):</b> {@code cumplimiento} y
 * {@code nivel} son <b>nullable</b> ({@link Double}/{@link Nivel}). Regla
 * invariante: {@code cumplimiento == null} ⟺ {@code den == 0} (ningún
 * aplicable respondido con 0/1/2). {@code 0.0} significa "cumplimiento cero
 * real" (todas 0) y debe distinguirse de "sin dato". Un módulo con
 * {@code cumplimiento == null} se excluye del score global.
 */
@Value
@Builder
public class ResultadoModulo {

    String modulo;
    String nombre;
    Double cumplimiento;
    Nivel nivel;
    int respondidas;
    int aplicables;
}
