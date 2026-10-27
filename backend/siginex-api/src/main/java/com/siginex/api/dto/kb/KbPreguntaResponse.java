package com.siginex.api.dto.kb;

import com.siginex.api.domain.KbPregunta;
import lombok.Builder;
import lombok.Value;

import java.util.Map;

/**
 * Response body for a KB question in
 * {@code GET /api/v1/kb/modulos/{moduloId}/preguntas}. {@code recomendacion} is
 * the {@code {"0":..,"1":..,"2":..}} map of per-answer recommendation texts.
 */
@Value
@Builder
public class KbPreguntaResponse {

    String id;
    String pregunta;
    String norma;
    String articulo;
    String requisito;
    String criterio;
    String peso;
    Map<String, String> recomendacion;

    public static KbPreguntaResponse from(KbPregunta pregunta) {
        return KbPreguntaResponse.builder()
                .id(pregunta.getId())
                .pregunta(pregunta.getPregunta())
                .norma(pregunta.getNorma())
                .articulo(pregunta.getArticulo())
                .requisito(pregunta.getRequisito())
                .criterio(pregunta.getCriterio())
                .peso(pregunta.getPeso())
                .recomendacion(pregunta.getRecomendacion())
                .build();
    }
}
