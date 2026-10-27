package com.siginex.api.domain.calculo;

import com.siginex.api.domain.KbPregunta;
import lombok.Getter;

import java.util.Map;

/**
 * Pregunta materializada para el motor de cálculo. Envuelve un {@link KbPregunta}
 * y expone su {@code peso} como {@code double} ya parseado <b>una sola vez</b>
 * con {@link Double#parseDouble(String)}.
 *
 * <p><b>Paridad numérica:</b> el peso se parsea desde el {@code String} (columna
 * {@code numeric}) al mismo {@code double} IEEE-754 que el {@code number} de V8
 * para ese literal decimal, que es lo que exige la réplica del camino flotante
 * del TS. Un peso no parseable lanza {@link NumberFormatException} aquí (KB
 * corrupto), de forma ruidosa al materializar y no durante el scoring.
 */
@Getter
public class CalculoPregunta {

    private final String id;
    private final String pregunta;
    private final String norma;
    private final String articulo;
    private final String requisito;
    private final double peso;
    private final Map<String, String> recomendacion;

    public CalculoPregunta(KbPregunta pregunta) {
        this.id = pregunta.getId();
        this.pregunta = pregunta.getPregunta();
        this.norma = pregunta.getNorma();
        this.articulo = pregunta.getArticulo();
        this.requisito = pregunta.getRequisito();
        // Parseo único del peso String->double; falla aquí si el KB está corrupto.
        this.peso = Double.parseDouble(pregunta.getPeso());
        this.recomendacion = pregunta.getRecomendacion();
    }
}
