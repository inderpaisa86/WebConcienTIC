package com.siginex.api.domain;

import lombok.Builder;
import lombok.Value;

import java.util.UUID;

/**
 * Header of a diagnostico returned by {@code crearDiagnostico} and
 * {@code obtenerDiagnostico}. Immutable value built with Lombok
 * {@code @Value @Builder} (Fase 2b convention: no {@code record}).
 *
 * <p>The answers map ({@code preguntaId -> valor}) of the original
 * {@code obtenerDiagnostico} is deferred to Fase 2c and is not exposed here.
 * {@code organizacionNombre} is resolved by a JOIN to {@code organizacion}.
 */
@Value
@Builder
public class DiagnosticoDetalle {

    UUID id;
    UUID organizacionId;
    String organizacionNombre;
    String estado;
}
