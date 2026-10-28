package com.siginex.api.repository;

import com.siginex.api.domain.ResetTokenInfo;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * Acceso a datos de {@code siginex.reset_password} (Fase 5). Igual que
 * {@code sesion}, se accede FUERA de RLS por {@code token_hash}/{@code usuario_id}
 * (la búsqueda ocurre antes de conocer el tenant). SQL only; la transacción la
 * gobierna el service.
 */
public interface ResetRepository {

    /** Marca como usados todos los tokens vigentes del usuario (al emitir uno nuevo). */
    void invalidarVigentesDeUsuario(UUID usuarioId);

    /** Inserta un nuevo token (solo su hash) con expiración. */
    void insertar(UUID usuarioId, String tokenHash, Instant expiraEn);

    /**
     * Busca un token por su hash, con el {@code tenant_id} del usuario (join a
     * {@code usuario}). Vacío si no existe.
     */
    Optional<ResetTokenInfo> buscarPorHash(String tokenHash);

    /** Marca un token como usado por su id (un solo uso). */
    void marcarUsado(UUID id);
}
