package com.siginex.api.repository;


import com.siginex.api.config.TenantTransactionTemplate;
import com.siginex.api.domain.AuthUser;
import com.siginex.api.domain.SesionInfo;
import com.siginex.api.domain.Usuario;
import com.siginex.api.service.AuthService;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * Data access for authentication and sessions.
 *
 * <p>{@link #findAuthByEmail(String)} is the single access to {@code usuario}
 * outside Row-Level Security (decision D4): it runs before the tenant is known
 * and returns only the columns required to authenticate. The caller
 * ({@link AuthService}) invokes it inside
 * {@code TenantTransactionTemplate.runWithoutTenant}. Session rows are keyed by
 * {@code token_hash}/{@code usuario_id} and are not under RLS.
 */
public interface AuthRepository {

    Optional<AuthUser> findAuthByEmail(String email);

    void crearSesion(UUID usuarioId, UUID tenantId, String tokenHash, Instant expiraEn, String userAgent, String ip);

    Optional<SesionInfo> buscarSesionPorHash(String tokenHash);

    void revocarSesion(String tokenHash);

    void incrementarFallos(UUID usuarioId, Instant bloqueadoHasta);

    void resetearFallosYLogin(UUID usuarioId);
}
