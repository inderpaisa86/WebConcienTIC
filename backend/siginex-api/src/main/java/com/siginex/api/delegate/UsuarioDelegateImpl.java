package com.siginex.api.delegate;

import com.siginex.api.domain.Usuario;
import com.siginex.api.exception.UsuarioException;
import com.siginex.api.service.UsuarioService;
import com.siginex.api.service.AuditoriaService;
import com.siginex.api.domain.SiginexPrincipal;
import com.siginex.api.dto.usuario.CrearUsuarioRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Default {@link UsuarioDelegate} implementation.
 *
 * <p>Authorization: only an actor with role {@code admin} may create or
 * activate/deactivate users (otherwise 403). The target tenant is always the
 * admin's own tenant, taken from the {@link SiginexPrincipal}; the client never
 * supplies a {@code tenant_id}. Because every data access runs under that
 * tenant via {@code runInTenant}, Row-Level Security makes users of other
 * tenants invisible, so an admin cannot see or alter them (a lookup returns
 * empty and the delegate answers 404).
 *
 * <p>Email uniqueness is checked with {@code lower(email)} to match the unique
 * index. The password is hashed with {@link PasswordEncoder} (Argon2id) before
 * persisting; the plaintext password and the hash are never logged or audited.
 */
@Component
public class UsuarioDelegateImpl implements UsuarioDelegate {

    private static final String ROL_ADMIN = "admin";

    private final UsuarioService usuarioService;
    private final PasswordEncoder passwordEncoder;
    private final AuditoriaService auditoriaService;

    public UsuarioDelegateImpl(UsuarioService usuarioService,
                               PasswordEncoder passwordEncoder,
                               AuditoriaService auditoriaService) {
        this.usuarioService = usuarioService;
        this.passwordEncoder = passwordEncoder;
        this.auditoriaService = auditoriaService;
    }

    @Override
    public Usuario crear(SiginexPrincipal actor, CrearUsuarioRequest request) {
        exigirAdmin(actor);
        UUID tenantId = actor.tenantId();

        if (usuarioService.existeEmail(tenantId, request.email())) {
            throw new UsuarioException(HttpStatus.CONFLICT, "el email ya esta registrado");
        }

        Usuario aInsertar = new Usuario(
                null,
                tenantId,
                request.email(),
                request.nombre(),
                passwordEncoder.encode(request.password()),
                request.rol(),
                true,
                null);

        Usuario creado = usuarioService.crear(tenantId, aInsertar);

        auditoriaService.registrar(tenantId, actor.email(), "usuario_creado", "usuario",
                creado.id().toString(), Map.of("email", creado.email(), "rol", creado.rol()));
        return creado;
    }

    @Override
    public List<Usuario> listar(SiginexPrincipal actor) {
        exigirAdmin(actor);
        return usuarioService.listar(actor.tenantId());
    }

    @Override
    public Usuario activar(SiginexPrincipal actor, UUID usuarioId) {
        return cambiarActivo(actor, usuarioId, true, "usuario_activado");
    }

    @Override
    public Usuario desactivar(SiginexPrincipal actor, UUID usuarioId) {
        return cambiarActivo(actor, usuarioId, false, "usuario_desactivado");
    }

    private Usuario cambiarActivo(SiginexPrincipal actor, UUID usuarioId, boolean activo, String accion) {
        exigirAdmin(actor);
        UUID tenantId = actor.tenantId();

        // RLS scopes the lookup to the admin's tenant: a user of another tenant
        // is simply not found, so the admin cannot operate on it (404).
        usuarioService.buscarPorId(tenantId, usuarioId)
                .orElseThrow(() -> new UsuarioException(HttpStatus.NOT_FOUND, "usuario no encontrado"));

        usuarioService.cambiarActivo(tenantId, usuarioId, activo);

        Usuario actualizado = usuarioService.buscarPorId(tenantId, usuarioId)
                .orElseThrow(() -> new UsuarioException(HttpStatus.NOT_FOUND, "usuario no encontrado"));

        auditoriaService.registrar(tenantId, actor.email(), accion, "usuario",
                usuarioId.toString(), Map.of("email", actualizado.email()));
        return actualizado;
    }

    private static void exigirAdmin(SiginexPrincipal actor) {
        if (actor == null || !ROL_ADMIN.equals(actor.rol())) {
            throw new UsuarioException(HttpStatus.FORBIDDEN, "se requiere rol admin");
        }
    }
}
