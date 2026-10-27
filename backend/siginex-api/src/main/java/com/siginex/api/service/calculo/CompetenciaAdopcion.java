package com.siginex.api.service.calculo;

import lombok.Value;

import java.util.List;

/**
 * Entrada del catálogo de adopción {@code ADOPT}: una competencia y sus
 * disparadores (tokens que, de aparecer en el texto de la pregunta, seleccionan
 * la competencia en {@code rutaFor}). Port del tuple TS
 * {@code [competencia, disparadores[]]}. Inmutable con Lombok {@code @Value}
 * (convención del proyecto: sin {@code record}).
 */
@Value
public class CompetenciaAdopcion {

    String competencia;
    List<String> disparadores;
}
