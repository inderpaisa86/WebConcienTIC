package com.siginex.api.controller;

import com.siginex.api.delegate.RespuestaDelegate;
import com.siginex.api.domain.RespuestaEntrada;
import com.siginex.api.domain.SiginexPrincipal;
import com.siginex.api.dto.respuesta.GuardarRespuestasRequest;
import com.siginex.api.dto.respuesta.RespuestaItem;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * REST layer for the answers of a diagnostico. Validates request format,
 * resolves the acting {@link SiginexPrincipal} from the security context, and
 * delegates the business work to {@link RespuestaDelegate}. Holds no business
 * logic.
 *
 * <p>The tenant is never read from the client; the delegate always uses the
 * authenticated actor's own tenant and returns {@code 404}
 * ({@link com.siginex.api.exception.DominioException}) when the diagnostico does
 * not belong to it. These routes fall under
 * {@code anyRequest().authenticated()} of
 * {@link com.siginex.api.config.SecurityConfig}, so a valid session is required.
 */
@RestController
@RequestMapping("/api/v1/diagnosticos/{id}/respuestas")
@RequiredArgsConstructor
public class RespuestaController {

    private final RespuestaDelegate respuestaDelegate;

    @PutMapping
    public ResponseEntity<Void> guardar(@PathVariable UUID id,
                                        @Valid @RequestBody GuardarRespuestasRequest request) {
        List<RespuestaEntrada> entradas = request.getRespuestas().stream()
                .map(RespuestaController::toEntrada)
                .toList();
        respuestaDelegate.guardar(currentPrincipal(), id, entradas);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<Map<String, String>> obtener(@PathVariable UUID id) {
        Map<String, String> respuestas = respuestaDelegate.obtener(currentPrincipal(), id);
        return ResponseEntity.ok(respuestas);
    }

    private static RespuestaEntrada toEntrada(RespuestaItem item) {
        return RespuestaEntrada.builder()
                .preguntaId(item.getPreguntaId())
                .valor(item.getValor())
                .build();
    }

    private static SiginexPrincipal currentPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof SiginexPrincipal principal) {
            return principal;
        }
        return null;
    }
}
