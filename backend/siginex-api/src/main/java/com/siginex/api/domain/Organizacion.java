package com.siginex.api.domain;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;
import java.util.UUID;

/**
 * Persistence projection of {@code siginex.organizacion}. Immutable value built
 * with Lombok {@code @Value @Builder} (Fase 2b convention: no {@code record}).
 *
 * <p>{@code tamano} is modeled as a {@link String} because the column is the
 * PostgreSQL enum {@code siginex.tamano_empresa}; the cast lives in the SQL of
 * {@code JdbcOrganizacionRepository}. Every access runs under the actor's
 * tenant, so Row-Level Security isolates the rows.
 *
 * <p>{@code aplicabilidad} (Fase 2d) is the raw {@code jsonb} column as text; it
 * is parsed into the engine's {@code Aplicabilidad} by the calculation layer
 * (via {@code AplicabilidadParser}). It is {@code null}-friendly: the column
 * defaults to {@code '{}'}.
 */
@Value
@Builder
public class Organizacion {

    UUID id;
    UUID tenantId;
    String nombre;
    String sector;
    String tamano;
    String aplicabilidad;
    Instant creadoEn;
}
