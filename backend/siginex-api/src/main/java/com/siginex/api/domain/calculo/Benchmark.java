package com.siginex.api.domain.calculo;

import lombok.Builder;
import lombok.Value;

/**
 * Resultado del benchmark de mercado para un nivel tecnológico (1..5). Port 1:1
 * del tipo TS {@code Benchmark} / {@code BenchmarkResult}. Inmutable con Lombok
 * {@code @Value @Builder} (convención del proyecto: sin {@code record}).
 *
 * <p>{@code rangoMercado} es {@code [min, max]} de los niveles del mercado
 * ({@code BENCH_NIVELES}); se modela como {@code int[]} de longitud 2.
 */
@Value
@Builder
public class Benchmark {

    int nivelEmpresa;
    double promedioMercado;
    int[] rangoMercado;
    int herramientasPorEncima;
    int brechaAlLider;
    String recomendacion;
}
