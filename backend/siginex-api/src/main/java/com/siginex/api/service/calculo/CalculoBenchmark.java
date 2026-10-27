package com.siginex.api.service.calculo;

import com.siginex.api.domain.calculo.Benchmark;

/**
 * Cálculo del benchmark de mercado. Port 1:1 de {@code motores.ts::calcularBenchmark}.
 * Clase de utilidad, no instanciable.
 *
 * <p><b>Paridad numérica:</b> el promedio se calcula en {@code double} con el
 * mismo camino del TS ({@code prom = sum/9.0}, {@code Math.round(prom*10)/10.0});
 * {@link Math#round(double)} replica el half-up hacia +∞ de {@code Math.round}
 * de JS para valores no negativos. <b>No</b> valida que {@code nivel} esté en
 * 1..5 (igual que el TS: lo asume).
 */
public final class CalculoBenchmark {

    private CalculoBenchmark() {
    }

    /**
     * Calcula el benchmark para un nivel tecnológico. {@code herramientas_por_encima}
     * y {@code brecha_al_lider} se <b>calculan</b> (no se transcriben) a partir de
     * {@code BENCH_NIVELES}.
     *
     * @param nivel nivel tecnológico de la empresa (se asume 1..5, sin validar).
     * @return el benchmark consolidado.
     */
    public static Benchmark calcularBenchmark(int nivel) {
        int[] niveles = ConstantesCalculo.BENCH_NIVELES;

        // prom = sum/9.0 en double, idéntico a BENCH_NIVELES.reduce(a+b)/length.
        double suma = 0.0;
        for (int x : niveles) {
            suma += x;
        }
        double prom = suma / niveles.length;

        int min = niveles[0];
        int max = niveles[0];
        int porEncima = 0;
        for (int x : niveles) {
            if (x < min) {
                min = x;
            }
            if (x > max) {
                max = x;
            }
            if (x > nivel) {
                porEncima += 1;
            }
        }

        return Benchmark.builder()
                .nivelEmpresa(nivel)
                // Math.round(prom*10)/10.0 -> half-up hacia +∞ como Math.round de JS.
                .promedioMercado(Math.round(prom * 10) / 10.0)
                .rangoMercado(new int[]{min, max})
                .herramientasPorEncima(porEncima)
                .brechaAlLider(max - nivel)
                .recomendacion(ConstantesCalculo.BENCH_RECO.getOrDefault(nivel, ""))
                .build();
    }
}
