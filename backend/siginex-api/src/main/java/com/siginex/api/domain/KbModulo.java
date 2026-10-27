package com.siginex.api.domain;

import lombok.Builder;
import lombok.Value;

/**
 * A module of the knowledge bank (KB), e.g. {@code sst}, {@code ambiental}.
 * Immutable value built with Lombok {@code @Value @Builder} (project convention:
 * no {@code record}).
 *
 * <p>The KB is GLOBAL and shared by every tenant; it carries no {@code tenantId}
 * and is read without Row-Level Security. {@code escala} may be {@code null}
 * (the {@code esg} and {@code gobernanza_ia} modules ship an empty string that
 * the seed normalizes to {@code null}). {@code orden} preserves the module order
 * from {@code banco.json}.
 */
@Value
@Builder
public class KbModulo {

    String id;
    String nombre;
    String shortNombre;
    String weight;
    String escala;
    Integer orden;
}
