package com.siginex.api.repository;


import com.siginex.api.config.TenantTransactionTemplate;
import com.siginex.api.domain.Usuario;
import java.util.Optional;
import java.util.UUID;

/**
 * Data access for admin-managed users. Every method runs under the admin's
 * tenant: the service wraps the calls in
 * {@code TenantTransactionTemplate.runInTenant(tenantId, ...)} so PostgreSQL
 * Row-Level Security isolates the rows. SQL only; transaction and tenant
 * scoping are the service's concern.
 */
public interface UsuarioRepository {

    Usuario insertar(Usuario usuario);

    Optional<Usuario> buscarPorId(UUID id);

    void cambiarActivo(UUID id, boolean activo);

    boolean existeEmail(String email);
}
