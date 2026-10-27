package com.siginex.api.controller;

import com.siginex.api.delegate.OrganizacionDelegate;
import com.siginex.api.domain.Organizacion;
import com.siginex.api.domain.SiginexPrincipal;
import com.siginex.api.dto.organizacion.CrearOrganizacionRequest;
import com.siginex.api.dto.organizacion.OrganizacionResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST layer for organizaciones. Validates request format, resolves the acting
 * {@link SiginexPrincipal} from the security context, and delegates the
 * business work to {@link OrganizacionDelegate}. Holds no business logic.
 *
 * <p>The tenant is never read from the client: the delegate always uses the
 * authenticated actor's own tenant.
 */
@RestController
@RequestMapping("/api/v1/organizaciones")
@RequiredArgsConstructor
public class OrganizacionController {

    private final OrganizacionDelegate organizacionDelegate;

    @GetMapping
    public ResponseEntity<List<OrganizacionResponse>> listar() {
        List<OrganizacionResponse> cuerpo = organizacionDelegate.listar(currentPrincipal()).stream()
                .map(OrganizacionResponse::from)
                .toList();
        return ResponseEntity.ok(cuerpo);
    }

    @PostMapping
    public ResponseEntity<OrganizacionResponse> crear(@Valid @RequestBody CrearOrganizacionRequest request) {
        Organizacion creada = organizacionDelegate.crear(currentPrincipal(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(OrganizacionResponse.from(creada));
    }

    private static SiginexPrincipal currentPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof SiginexPrincipal principal) {
            return principal;
        }
        return null;
    }
}
