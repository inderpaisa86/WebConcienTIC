package com.siginex.api.controller;


import com.siginex.api.delegate.UsuarioDelegate;
import com.siginex.api.domain.Usuario;
import com.siginex.api.domain.SiginexPrincipal;
import com.siginex.api.dto.usuario.CrearUsuarioRequest;
import com.siginex.api.dto.usuario.UsuarioResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * REST layer for admin-managed users. Validates request format, resolves the
 * acting {@link SiginexPrincipal} from the security context, and delegates the
 * business decision to {@link UsuarioDelegate}. Holds no business logic and
 * never exposes {@code password_hash} (responses are {@link UsuarioResponse}).
 *
 * <p>The tenant is never read from the client: the delegate always uses the
 * authenticated admin's own tenant.
 */
@RestController
@RequestMapping("/api/v1/usuarios")
public class UsuarioController {

    private final UsuarioDelegate usuarioDelegate;

    public UsuarioController(UsuarioDelegate usuarioDelegate) {
        this.usuarioDelegate = usuarioDelegate;
    }

    @PostMapping
    public ResponseEntity<UsuarioResponse> crear(@Valid @RequestBody CrearUsuarioRequest request) {
        Usuario creado = usuarioDelegate.crear(currentPrincipal(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(UsuarioResponse.from(creado));
    }

    @PostMapping("/{id}/activar")
    public ResponseEntity<UsuarioResponse> activar(@PathVariable UUID id) {
        Usuario usuario = usuarioDelegate.activar(currentPrincipal(), id);
        return ResponseEntity.ok(UsuarioResponse.from(usuario));
    }

    @PostMapping("/{id}/desactivar")
    public ResponseEntity<UsuarioResponse> desactivar(@PathVariable UUID id) {
        Usuario usuario = usuarioDelegate.desactivar(currentPrincipal(), id);
        return ResponseEntity.ok(UsuarioResponse.from(usuario));
    }

    private static SiginexPrincipal currentPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof SiginexPrincipal principal) {
            return principal;
        }
        return null;
    }
}
