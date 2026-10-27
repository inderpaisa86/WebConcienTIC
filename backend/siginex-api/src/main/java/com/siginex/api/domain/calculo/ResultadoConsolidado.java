package com.siginex.api.domain.calculo;

import lombok.Builder;
import lombok.Value;

import java.util.List;

/**
 * Resultado consolidado del diagnóstico. Port 1:1 del tipo TS
 * {@code ResultadoConsolidado} (aplanando el sub-objeto {@code resultado} del TS
 * en los campos {@code scoreSgi}/{@code nivelSgi}/{@code cumplimientoGlobal}/
 * {@code porModulo}). Inmutable con Lombok {@code @Value @Builder} (convención
 * del proyecto: sin {@code record}).
 *
 * <p>{@code scoreSgi} y {@code cumplimientoGlobal} son el mismo número (el TS
 * expone {@code score_sgi == cumplimiento_global}). {@code nivelSgi} puede ser
 * {@code null} en teoría; en la práctica {@code levelFor} siempre devuelve un
 * nivel para un {@code double} no nulo. {@code benchmark} es {@code null} si la
 * empresa no declara nivel tecnológico.
 */
@Value
@Builder
public class ResultadoConsolidado {

    double scoreSgi;
    Nivel nivelSgi;
    double cumplimientoGlobal;
    List<ResultadoModulo> porModulo;
    List<Brecha> brechas;
    List<TareaPlan> planMejora;
    List<RutaAprendizajePlan> planAprendizaje;
    Benchmark benchmark;
    List<Alerta> alertas;
    Meta meta;
}
