package com.siginex.api.domain;

import lombok.Builder;
import lombok.Value;

import java.util.Map;

/**
 * A question of a KB module. Immutable value built with Lombok
 * {@code @Value @Builder} (project convention: no {@code record}).
 *
 * <p>The KB is GLOBAL and shared by every tenant (no {@code tenantId}, no RLS).
 * {@code recomendacion} is the {@code {"0":..,"1":..,"2":..}} map of per-answer
 * recommendation texts. {@code orden} preserves the question order inside its
 * module from {@code banco.json}.
 */
@Value
@Builder
public class KbPregunta {

    String id;
    String pregunta;
    String norma;
    String articulo;
    String requisito;
    String criterio;
    String peso;
    Map<String, String> recomendacion;
    Integer orden;
}
