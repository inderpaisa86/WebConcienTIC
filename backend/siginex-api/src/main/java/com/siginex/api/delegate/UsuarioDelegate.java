package com.siginex.api.delegate;

import com.siginex.api.domain.Usuario;
import com.siginex.api.domain.SiginexPrincipal;
import com.siginex.api.dto.usuario.CrearUsuarioRequest;

import java.util.List;
import java.util.UUID;

/**
 * Business rules and authorization for admin-managed users. The controller
 * delegates here after format validation; this layer decides whether the use
 * case may proceed and then hands the data work to the service layer.
 * Implemented by {@link UsuarioDelegateImpl}.
 *
 * <p>Authorization: only an actor with role {@code admin} may create or
 * activate/deactivate users. The target tenant is always the admin's own
 * tenant, taken from the {@link SiginexPrincipal}; the client never supplies a
 * {@code tenant_id}.
 */
public interface UsuarioDelegate {

    Usuario crear(SiginexPrincipal actor, CrearUsuarioRequest request);

    List<Usuario> listar(SiginexPrincipal actor);

    Usuario activar(SiginexPrincipal actor, UUID usuarioId);

    Usuario desactivar(SiginexPrincipal actor, UUID usuarioId);
}
