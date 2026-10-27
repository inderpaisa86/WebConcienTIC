package com.siginex.api.dto.calculo;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.siginex.api.domain.calculo.Benchmark;
import lombok.Builder;
import lombok.Value;

/**
 * Benchmark de mercado expuesto por la API ({@code GET}/{@code POST
 * /benchmark}). Espejo del {@link Benchmark} de dominio. Inmutable con Lombok
 * {@code @Value @Builder} (convención del proyecto: sin {@code record}).
 *
 * <p>Se serializa en <b>snake_case</b> vía {@link JsonProperty} (diseño §9.7)
 * para alinear con el contrato ya conocido del front ({@code Benchmark.tsx}):
 * {@code nivel_empresa}, {@code promedio_mercado}, {@code rango_mercado},
 * {@code herramientas_por_encima}, {@code brecha_al_lider}, {@code recomendacion}.
 */
@Value
@Builder
public class BenchmarkResponse {

    @JsonProperty("nivel_empresa")
    int nivelEmpresa;

    @JsonProperty("promedio_mercado")
    double promedioMercado;

    @JsonProperty("rango_mercado")
    int[] rangoMercado;

    @JsonProperty("herramientas_por_encima")
    int herramientasPorEncima;

    @JsonProperty("brecha_al_lider")
    int brechaAlLider;

    @JsonProperty("recomendacion")
    String recomendacion;

    /**
     * Mapea un {@link Benchmark} de dominio a su DTO de respuesta.
     *
     * @param benchmark benchmark de dominio.
     * @return el DTO de respuesta.
     */
    public static BenchmarkResponse from(Benchmark benchmark) {
        return BenchmarkResponse.builder()
                .nivelEmpresa(benchmark.getNivelEmpresa())
                .promedioMercado(benchmark.getPromedioMercado())
                .rangoMercado(benchmark.getRangoMercado())
                .herramientasPorEncima(benchmark.getHerramientasPorEncima())
                .brechaAlLider(benchmark.getBrechaAlLider())
                .recomendacion(benchmark.getRecomendacion())
                .build();
    }
}
