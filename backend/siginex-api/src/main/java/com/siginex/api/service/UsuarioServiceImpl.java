package com.siginex.api.service;

import com.siginex.api.domain.Usuario;
import com.siginex.api.repository.UsuarioRepository;
import com.siginex.api.config.TenantTransactionTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

/**
 * Default {@link UsuarioService} implementation. Every operation runs inside
 * {@link TenantTransactionTemplate#runInTenant(UUID, java.util.function.Supplier)}
 * so {@code app.tenant_id} is fixed for the transaction and PostgreSQL
 * Row-Level Security isolates the rows to the admin's tenant.
 */
@Service
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final TenantTransactionTemplate tenantTx;

    public UsuarioServiceImpl(UsuarioRepository usuarioRepository, TenantTransactionTemplate tenantTx) {
        this.usuarioRepository = usuarioRepository;
        this.tenantTx = tenantTx;
    }

    @Override
    public Usuario crear(UUID tenantId, Usuario usuario) {
        return tenantTx.runInTenant(tenantId, () -> usuarioRepository.insertar(usuario));
    }

    @Override
    public Optional<Usuario> buscarPorId(UUID tenantId, UUID usuarioId) {
        return tenantTx.runInTenant(tenantId, () -> usuarioRepository.buscarPorId(usuarioId));
    }

    @Override
    public boolean existeEmail(UUID tenantId, String email) {
        return tenantTx.runInTenant(tenantId, () -> usuarioRepository.existeEmail(email));
    }

    @Override
    public void cambiarActivo(UUID tenantId, UUID usuarioId, boolean activo) {
        tenantTx.runInTenant(tenantId, () -> {
            usuarioRepository.cambiarActivo(usuarioId, activo);
            return null;
        });
    }
}
