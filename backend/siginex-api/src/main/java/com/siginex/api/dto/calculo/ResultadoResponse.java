package com.siginex.api.dto.calculo;

import com.siginex.api.domain.calculo.ResultadoLectura;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.util.List;

/**
 * Resultado consolidado de un diagnóstico expuesto por la API ({@code POST
 * /calcular} y {@code GET /resultado}). Espejo del {@link ResultadoLectura}
 * leído de BD (diseño §9.7). Inmutable con Lombok {@code @Value @Builder}
 * (convención del proyecto: sin {@code record}).
 *
 * <p>Los numéricos ({@code score}, {@code cumplimientoGlobal}) llegan como
 * {@link BigDecimal} de las columnas {@code numeric}; pueden ser {@code null}
 * si el diagnóstico aún no se calculó. Los pilares y las recomendaciones salen
 * con el orden determinista que fija el repositorio (diseño §9.8).
 */
@Value
@Builder
public class ResultadoResponse {

    BigDecimal score;
    Integer nivelSgi;
    BigDecimal cumplimientoGlobal;
    String organizacionNombre;
    List<PilarResponse> pilares;
    List<RecomendacionResponse> recomendaciones;

    /**
     * Mapea un {@link ResultadoLectura} de dominio a su DTO de respuesta.
     *
     * @param lectura resultado leído de BD.
     * @return el DTO de respuesta.
     */
    public static ResultadoResponse from(ResultadoLectura lectura) {
        List<PilarResponse> pilares = lectura.getPilares() == null
                ? List.of()
                : lectura.getPilares().stream().map(PilarResponse::from).toList();
        List<RecomendacionResponse> recomendaciones = lectura.getRecomendaciones() == null
                ? List.of()
                : lectura.getRecomendaciones().stream().map(RecomendacionResponse::from).toList();
        return ResultadoResponse.builder()
                .score(lectura.getScoreSgi())
                .nivelSgi(lectura.getNivelSgi())
                .cumplimientoGlobal(lectura.getCumplimientoGlobal())
                .organizacionNombre(lectura.getOrganizacionNombre())
                .pilares(pilares)
                .recomendaciones(recomendaciones)
                .build();
    }
}
