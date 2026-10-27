package com.siginex.api.service.calculo;

import com.siginex.api.domain.calculo.Benchmark;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests de PARIDAD de {@code calcularBenchmark} contra {@code motores.ts}.
 * {@code herramientas_por_encima} se CALCULA (no se transcribe): para
 * BENCH_NIVELES={3,3,2,2,4,4,5,4,5} y niveles 1..5 da 9/7/5/2/0, verificado
 * contra el runtime Node del TS (ver nota de FEAT en findings: el "4" del
 * enunciado para nivel 3 es una errata; el conteo real {@code count(x>3)} sobre
 * {4,4,5,4,5} es 5).
 */
class CalculoBenchmarkTest {

    @Test
    void herramientasPorEncimaPorNivel() {
        assertThat(CalculoBenchmark.calcularBenchmark(1).getHerramientasPorEncima()).isEqualTo(9);
        assertThat(CalculoBenchmark.calcularBenchmark(2).getHerramientasPorEncima()).isEqualTo(7);
        assertThat(CalculoBenchmark.calcularBenchmark(3).getHerramientasPorEncima()).isEqualTo(5);
        assertThat(CalculoBenchmark.calcularBenchmark(4).getHerramientasPorEncima()).isEqualTo(2);
        assertThat(CalculoBenchmark.calcularBenchmark(5).getHerramientasPorEncima()).isZero();
    }

    @Test
    void promedioRangoYBrechaConstantesDelMercado() {
        Benchmark b = CalculoBenchmark.calcularBenchmark(2);
        assertThat(b.getNivelEmpresa()).isEqualTo(2);
        assertThat(b.getPromedioMercado()).isEqualTo(3.6);  // Math.round(32/9*10)/10 = 36/10
        assertThat(b.getRangoMercado()).containsExactly(2, 5);
        assertThat(b.getBrechaAlLider()).isEqualTo(3);       // 5 - 2
        assertThat(b.getHerramientasPorEncima()).isEqualTo(7);
        assertThat(b.getRecomendacion())
                .isEqualTo("Consolide en una sola plataforma integrada (nivel 3) y elimine silos.");
    }

    @Test
    void recomendacionesPorNivel() {
        assertThat(CalculoBenchmark.calcularBenchmark(1).getRecomendacion()).startsWith("Digitalice");
        assertThat(CalculoBenchmark.calcularBenchmark(3).getRecomendacion()).startsWith("Automatice");
        assertThat(CalculoBenchmark.calcularBenchmark(4).getRecomendacion()).startsWith("Incorpore");
        assertThat(CalculoBenchmark.calcularBenchmark(5).getRecomendacion()).startsWith("Manténgase");
    }
}
