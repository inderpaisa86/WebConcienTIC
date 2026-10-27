package com.siginex.api.domain.calculo;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.util.List;

/**
 * Lectura del resultado consolidado de un diagnóstico ya calculado. Port 1:1
 * del tipo TS {@code ResultadoUI} de {@code diagnostico-service.ts::obtenerResultado}.
 * Inmutable con Lombok {@code @Value @Builder} (convención del proyecto: sin
 * {@code record}).
 *
 * <p>A diferencia de {@link ResultadoConsolidado} (que produce el motor puro),
 * este value refleja lo <b>leído de BD</b>: los numéricos llegan como
 * {@link BigDecimal} (columnas {@code numeric}) y los pilares/recomendaciones
 * se materializan desde {@code resultado_pilar}/{@code recomendacion} con orden
 * determinista (diseño §9.8). Los valores pueden ser {@code null} si el
 * diagnóstico aún no se calculó.
 */
@Value
@Builder(toBuilder = true)
public class ResultadoLectura {

    BigDecimal scoreSgi;
    Integer nivelSgi;
    BigDecimal cumplimientoGlobal;
    String organizacionNombre;
    List<PilarLectura> pilares;
    List<RecomendacionLectura> recomendaciones;

    /**
     * Pilar leído de {@code resultado_pilar}. Espejo de la fila: {@code skillId},
     * {@code cumplimientoPct} ({@code numeric}, nullable), {@code nivel}
     * ({@code smallint}, nullable) y {@code banda} (texto, nullable).
     */
    @Value
    @Builder
    public static class PilarLectura {

        String skillId;
        BigDecimal cumplimientoPct;
        Integer nivel;
        String banda;
    }

    /**
     * Recomendación de prioridad alta leída de {@code recomendacion}. Espejo de
     * la fila: {@code skillId}, {@code prioridad} y {@code texto}.
     */
    @Value
    @Builder
    public static class RecomendacionLectura {

        String skillId;
        String prioridad;
        String texto;
    }
}
