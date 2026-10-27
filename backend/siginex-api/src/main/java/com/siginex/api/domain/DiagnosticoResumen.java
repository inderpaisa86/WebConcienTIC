package com.siginex.api.domain;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;
import java.util.UUID;

/**
 * Summary row of a diagnostico for listing. Immutable value built with Lombok
 * {@code @Value @Builder} (Fase 2b convention: no {@code record}).
 *
 * <p>Reflects {@code listarDiagnosticos} of the original
 * {@code diagnostico-service.ts}: it carries the organizacion name resolved by a
 * JOIN. {@code scoreSgi} is modeled as {@link String} (the column is
 * {@code numeric}) and {@code nivelSgi} may be {@code null}. Every access runs
 * under the actor's tenant, so Row-Level Security isolates the rows.
 */
@Value
@Builder
public class DiagnosticoResumen {

    UUID id;
    UUID organizacionId;
    String organizacionNombre;
    String estado;
    String scoreSgi;
    Integer nivelSgi;
    Instant creadoEn;
}
