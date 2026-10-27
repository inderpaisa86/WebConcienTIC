package com.siginex.api.delegate;

import com.siginex.api.domain.SiginexPrincipal;
import com.siginex.api.domain.calculo.Benchmark;
import com.siginex.api.domain.calculo.ResultadoLectura;
import com.siginex.api.domain.calculo.TareaPlanLectura;
import com.siginex.api.exception.DominioException;
import com.siginex.api.service.CalculoService;
import com.siginex.api.service.DiagnosticoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementación por defecto de {@link CalculoDelegate}.
 *
 * <p>Antes de calcular o leer verifica que el diagnóstico pertenezca al tenant
 * del actor vía {@link DiagnosticoService#buscarPorId(UUID, UUID)}. Como ese
 * lookup corre bajo el tenant del actor, la Row-Level Security oculta los
 * diagnósticos de otros tenants, de modo que un diagnóstico de otro tenant
 * produce {@code 404} ({@link DominioException} NOT_FOUND) igual que uno
 * inexistente (mismo contrato que {@link RespuestaDelegateImpl}). El tenant es
 * siempre el del propio actor, tomado del {@link SiginexPrincipal}; el cliente
 * nunca suministra un {@code tenant_id}.
 */
@Component
@RequiredArgsConstructor
public class CalculoDelegateImpl implements CalculoDelegate {

    private final DiagnosticoService diagnosticoService;
    private final CalculoService calculoService;

    @Override
    public ResultadoLectura calcular(SiginexPrincipal actor, UUID diagnosticoId) {
        UUID tenantId = actor.tenantId();
        verificarPertenencia(tenantId, diagnosticoId);
        // Recalcula y persiste; luego lee el resultado ya persistido con el
        // orden determinista (diseño §9.8), de modo que POST /calcular y
        // GET /resultado devuelvan exactamente la misma forma.
        calculoService.calcular(tenantId, diagnosticoId);
        return calculoService.obtenerResultado(tenantId, diagnosticoId);
    }

    @Override
    public ResultadoLectura obtenerResultado(SiginexPrincipal actor, UUID diagnosticoId) {
        UUID tenantId = actor.tenantId();
        verificarPertenencia(tenantId, diagnosticoId);
        return calculoService.obtenerResultado(tenantId, diagnosticoId);
    }

    @Override
    public List<TareaPlanLectura> obtenerPlanMejora(SiginexPrincipal actor, UUID diagnosticoId) {
        UUID tenantId = actor.tenantId();
        verificarPertenencia(tenantId, diagnosticoId);
        return calculoService.obtenerPlanMejora(tenantId, diagnosticoId);
    }

    @Override
    public Benchmark registrarBenchmark(SiginexPrincipal actor, UUID diagnosticoId, int nivel) {
        UUID tenantId = actor.tenantId();
        verificarPertenencia(tenantId, diagnosticoId);
        return calculoService.registrarBenchmark(tenantId, diagnosticoId, nivel);
    }

    @Override
    public Optional<Benchmark> obtenerBenchmark(SiginexPrincipal actor, UUID diagnosticoId) {
        UUID tenantId = actor.tenantId();
        verificarPertenencia(tenantId, diagnosticoId);
        return calculoService.obtenerBenchmark(tenantId, diagnosticoId);
    }

    private void verificarPertenencia(UUID tenantId, UUID diagnosticoId) {
        diagnosticoService.buscarPorId(tenantId, diagnosticoId)
                .orElseThrow(() -> new DominioException(HttpStatus.NOT_FOUND, "diagnostico no encontrado"));
    }
}
