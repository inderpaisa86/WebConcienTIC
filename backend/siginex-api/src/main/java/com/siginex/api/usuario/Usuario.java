package com.siginex.api.usuario;

import java.time.Instant;
import java.util.UUID;

/**
 * Persistence projection of {@code siginex.usuario} as handled by the usuario
 * layer. It carries {@code passwordHash} only on the write path (insert), so it
 * must never be exposed outside the service/repository and never be logged.
 *
 * <p>Read operations (e.g. {@code buscarPorId}) return a value with
 * {@code passwordHash == null}: the hash is never read back out of the database
 * by this layer.
 */
public record Usuario(
        UUID id,
        UUID tenantId,
        String email,
        String nombre,
        String passwordHash,
        String rol,
        boolean activo,
        Instant creadoEn) {
}
