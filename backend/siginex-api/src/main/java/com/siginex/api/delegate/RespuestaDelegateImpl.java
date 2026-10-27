package com.siginex.api.delegate;

import com.siginex.api.domain.RespuestaEntrada;
import com.siginex.api.domain.SiginexPrincipal;
import com.siginex.api.exception.DominioException;
import com.siginex.api.service.DiagnosticoService;
import com.siginex.api.service.RespuestaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Default {@link RespuestaDelegate} implementation.
 *
 * <p>Before saving or reading answers it verifies the diagnostico belongs to
 * the actor's tenant via {@link DiagnosticoService#buscarPorId(UUID, UUID)}.
 * Because that lookup runs under the actor's tenant, Row-Level Security hides
 * diagnosticos of other tenants, so a diagnostico from another tenant yields
 * {@code 404} ({@link DominioException} NOT_FOUND) exactly like a non-existent
 * one — same contract as {@link DiagnosticoDelegateImpl#obtener}. The tenant is
 * always the actor's own, taken from the {@link SiginexPrincipal}; the client
 * never supplies a {@code tenant_id}.
 */
@Component
@RequiredArgsConstructor
public class RespuestaDelegateImpl implements RespuestaDelegate {

    private final DiagnosticoService diagnosticoService;
    private final RespuestaService respuestaService;

    @Override
    public void guardar(SiginexPrincipal actor, UUID diagnosticoId, List<RespuestaEntrada> entradas) {
        UUID tenantId = actor.tenantId();
        verificarPertenencia(tenantId, diagnosticoId);
        respuestaService.guardar(tenantId, diagnosticoId, entradas);
    }

    @Override
    public Map<String, String> obtener(SiginexPrincipal actor, UUID diagnosticoId) {
        UUID tenantId = actor.tenantId();
        verificarPertenencia(tenantId, diagnosticoId);
        return respuestaService.listar(tenantId, diagnosticoId);
    }

    private void verificarPertenencia(UUID tenantId, UUID diagnosticoId) {
        diagnosticoService.buscarPorId(tenantId, diagnosticoId)
                .orElseThrow(() -> new DominioException(HttpStatus.NOT_FOUND, "diagnostico no encontrado"));
    }
}
