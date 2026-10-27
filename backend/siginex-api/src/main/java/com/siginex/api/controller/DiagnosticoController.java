package com.siginex.api.controller;

import com.siginex.api.delegate.DiagnosticoDelegate;
import com.siginex.api.domain.DiagnosticoDetalle;
import com.siginex.api.domain.SiginexPrincipal;
import com.siginex.api.dto.diagnostico.CrearDiagnosticoRequest;
import com.siginex.api.dto.diagnostico.DiagnosticoDetalleResponse;
import com.siginex.api.dto.diagnostico.DiagnosticoResumenResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * REST layer for diagnosticos. Validates request format, resolves the acting
 * {@link SiginexPrincipal} from the security context, and delegates the
 * business work to {@link DiagnosticoDelegate}. Holds no business logic.
 *
 * <p>The tenant is never read from the client: the delegate always uses the
 * authenticated actor's own tenant. A {@code 404} is produced by the delegate
 * via {@link com.siginex.api.exception.DominioException}.
 */
@RestController
@RequestMapping("/api/v1/diagnosticos")
@RequiredArgsConstructor
public class DiagnosticoController {

    private final DiagnosticoDelegate diagnosticoDelegate;

    @GetMapping
    public ResponseEntity<List<DiagnosticoResumenResponse>> listar() {
        List<DiagnosticoResumenResponse> cuerpo = diagnosticoDelegate.listar(currentPrincipal()).stream()
                .map(DiagnosticoResumenResponse::from)
                .toList();
        return ResponseEntity.ok(cuerpo);
    }

    @PostMapping
    public ResponseEntity<DiagnosticoDetalleResponse> crear(@Valid @RequestBody CrearDiagnosticoRequest request) {
        DiagnosticoDetalle creado = diagnosticoDelegate.crear(currentPrincipal(), request.getOrganizacionId());
        return ResponseEntity.status(HttpStatus.CREATED).body(DiagnosticoDetalleResponse.from(creado));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DiagnosticoDetalleResponse> obtener(@PathVariable UUID id) {
        DiagnosticoDetalle detalle = diagnosticoDelegate.obtener(currentPrincipal(), id);
        return ResponseEntity.ok(DiagnosticoDetalleResponse.from(detalle));
    }

    private static SiginexPrincipal currentPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof SiginexPrincipal principal) {
            return principal;
        }
        return null;
    }
}
