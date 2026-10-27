package com.siginex.api.usuario;

import com.siginex.api.tenant.TenantTransactionTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

/**
 * Data coordination for admin-managed users. Every operation runs inside
 * {@link TenantTransactionTemplate#runInTenant(UUID, java.util.function.Supplier)}
 * so {@code app.tenant_id} is fixed for the transaction and PostgreSQL
 * Row-Level Security isolates the rows to the admin's tenant.
 *
 * <p>This layer holds no business rules: authorization and validation live in
 * {@link UsuarioDelegate}. The {@code tenantId} it receives is always the
 * admin's own tenant, resolved server-side by the delegate.
 */
@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final TenantTransactionTemplate tenantTx;

    public UsuarioService(UsuarioRepository usuarioRepository, TenantTransactionTemplate tenantTx) {
        this.usuarioRepository = usuarioRepository;
        this.tenantTx = tenantTx;
    }

    public Usuario crear(UUID tenantId, Usuario usuario) {
        return tenantTx.runInTenant(tenantId, () -> usuarioRepository.insertar(usuario));
    }

    public Optional<Usuario> buscarPorId(UUID tenantId, UUID usuarioId) {
        return tenantTx.runInTenant(tenantId, () -> usuarioRepository.buscarPorId(usuarioId));
    }

    public boolean existeEmail(UUID tenantId, String email) {
        return tenantTx.runInTenant(tenantId, () -> usuarioRepository.existeEmail(email));
    }

    public void cambiarActivo(UUID tenantId, UUID usuarioId, boolean activo) {
        tenantTx.runInTenant(tenantId, () -> {
            usuarioRepository.cambiarActivo(usuarioId, activo);
            return null;
        });
    }
}
