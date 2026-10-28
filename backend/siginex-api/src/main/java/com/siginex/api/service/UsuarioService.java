package com.siginex.api.service;

import com.siginex.api.domain.Usuario;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Data coordination for admin-managed users. Every operation runs under the
 * admin's tenant so PostgreSQL Row-Level Security isolates the rows.
 * Implemented by {@link UsuarioServiceImpl}.
 *
 * <p>This layer holds no business rules: authorization and validation live in
 * the delegate. The {@code tenantId} it receives is always the admin's own
 * tenant, resolved server-side by the delegate.
 */
public interface UsuarioService {

    Usuario crear(UUID tenantId, Usuario usuario);

    /** Lists the users of the admin's tenant (RLS scopes them). */
    List<Usuario> listar(UUID tenantId);

    Optional<Usuario> buscarPorId(UUID tenantId, UUID usuarioId);

    boolean existeEmail(UUID tenantId, String email);

    void cambiarActivo(UUID tenantId, UUID usuarioId, boolean activo);
}
