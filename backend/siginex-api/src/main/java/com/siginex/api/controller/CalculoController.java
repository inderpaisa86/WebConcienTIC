package com.siginex.api.controller;

import com.siginex.api.delegate.CalculoDelegate;
import com.siginex.api.domain.SiginexPrincipal;
import com.siginex.api.dto.calculo.BenchmarkResponse;
import com.siginex.api.dto.calculo.RegistrarBenchmarkRequest;
import com.siginex.api.dto.calculo.ResultadoResponse;
import com.siginex.api.dto.calculo.TareaPlanResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
 * Capa REST del cálculo de un diagnóstico: recálculo, lectura de resultado,
 * plan de mejora y benchmark. Valida el formato del request, resuelve el
 * {@link SiginexPrincipal} actuante del contexto de seguridad y delega el
 * trabajo de negocio en {@link CalculoDelegate}. No contiene lógica de negocio.
 *
 * <p>El tenant nunca se lee del cliente; el delegate usa siempre el tenant del
 * actor autenticado y devuelve {@code 404}
 * ({@link com.siginex.api.exception.DominioException}) cuando el diagnóstico no
 * le pertenece. Estas rutas caen bajo {@code anyRequest().authenticated()} de
 * {@link com.siginex.api.config.SecurityConfig}, así que requieren sesión válida.
 */
@RestController
@RequestMapping("/api/v1/diagnosticos/{id}")
@RequiredArgsConstructor
public class CalculoController {

    private final CalculoDelegate calculoDelegate;

    @PostMapping("/calcular")
    public ResponseEntity<ResultadoResponse> calcular(@PathVariable UUID id) {
        ResultadoResponse body = ResultadoResponse.from(
                calculoDelegate.calcular(currentPrincipal(), id));
        return ResponseEntity.ok(body);
    }

    @GetMapping("/resultado")
    public ResponseEntity<ResultadoResponse> obtenerResultado(@PathVariable UUID id) {
        ResultadoResponse body = ResultadoResponse.from(
                calculoDelegate.obtenerResultado(currentPrincipal(), id));
        return ResponseEntity.ok(body);
    }

    @GetMapping("/plan-mejora")
    public ResponseEntity<List<TareaPlanResponse>> obtenerPlanMejora(@PathVariable UUID id) {
        List<TareaPlanResponse> body = calculoDelegate.obtenerPlanMejora(currentPrincipal(), id).stream()
                .map(TareaPlanResponse::from)
                .toList();
        return ResponseEntity.ok(body);
    }

    @PostMapping("/benchmark")
    public ResponseEntity<BenchmarkResponse> registrarBenchmark(@PathVariable UUID id,
                                                                @Valid @RequestBody RegistrarBenchmarkRequest request) {
        BenchmarkResponse body = BenchmarkResponse.from(
                calculoDelegate.registrarBenchmark(currentPrincipal(), id, request.getNivel()));
        return ResponseEntity.ok(body);
    }

    @GetMapping("/benchmark")
    public ResponseEntity<BenchmarkResponse> obtenerBenchmark(@PathVariable UUID id) {
        return calculoDelegate.obtenerBenchmark(currentPrincipal(), id)
                .map(BenchmarkResponse::from)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    private static SiginexPrincipal currentPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof SiginexPrincipal principal) {
            return principal;
        }
        return null;
    }
}
