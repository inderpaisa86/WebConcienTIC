package com.siginex.api.dto.usuario;

import com.siginex.api.domain.Usuario;

import java.time.Instant;
import java.util.UUID;

/**
 * User response body. Never exposes {@code password_hash} or any other secret.
 */
public record UsuarioResponse(
        UUID id,
        UUID tenantId,
        String email,
        String nombre,
        String rol,
        boolean activo,
        Instant creadoEn) {

    public static UsuarioResponse from(Usuario usuario) {
        return new UsuarioResponse(
                usuario.id(),
                usuario.tenantId(),
                usuario.email(),
                usuario.nombre(),
                usuario.rol(),
                usuario.activo(),
                usuario.creadoEn());
    }
}
