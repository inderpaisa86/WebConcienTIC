package com.siginex.api.domain;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;
import java.util.UUID;

/**
 * Proyección de una fila de {@code siginex.reset_password} necesaria para
 * validar un token de recuperación: a qué usuario pertenece, su tenant, si ya
 * se usó y cuándo expira. No contiene el token en claro (la tabla solo guarda
 * su hash SHA-256).
 */
@Value
@Builder
public class ResetTokenInfo {

    UUID id;
    UUID usuarioId;
    UUID tenantId;
    boolean usado;
    Instant expiraEn;
}
