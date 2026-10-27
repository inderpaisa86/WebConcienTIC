package com.siginex.api.delegate;

import com.siginex.api.domain.DiagnosticoDetalle;
import com.siginex.api.domain.DiagnosticoResumen;
import com.siginex.api.domain.SiginexPrincipal;
import com.siginex.api.exception.DominioException;
import com.siginex.api.service.DiagnosticoService;
import com.siginex.api.service.OrganizacionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Default {@link DiagnosticoDelegate} implementation.
 *
 * <p>Faithful to the original {@code diagnostico-service.ts}: no admin role is
 * required, any valid session of the tenant may create, list or get. The target
 * tenant is always the actor's own tenant, taken from the
 * {@link SiginexPrincipal}; the client never supplies a {@code tenant_id}.
 *
 * <p>{@code crear} verifies the organizacion belongs to the actor's tenant via
 * {@link OrganizacionService#buscarPorId(UUID, UUID)}. Because that lookup runs
 * under the actor's tenant, Row-Level Security hides organizaciones of other
 * tenants, so an organizacion from another tenant yields {@code 404}
 * ({@link DominioException} NOT_FOUND) exactly like a non-existent one.
 */
@Component
@RequiredArgsConstructor
public class DiagnosticoDelegateImpl implements DiagnosticoDelegate {

    private final OrganizacionService organizacionService;
    private final DiagnosticoService diagnosticoService;

    @Override
    public DiagnosticoDetalle crear(SiginexPrincipal actor, UUID organizacionId) {
        UUID tenantId = actor.tenantId();
        organizacionService.buscarPorId(tenantId, organizacionId)
                .orElseThrow(() -> new DominioException(HttpStatus.NOT_FOUND, "organizacion no encontrada"));
        String version = diagnosticoService.kbVersionVigente(tenantId);
        return diagnosticoService.crear(tenantId, organizacionId, version);
    }

    @Override
    public List<DiagnosticoResumen> listar(SiginexPrincipal actor) {
        return diagnosticoService.listar(actor.tenantId());
    }

    @Override
    public DiagnosticoDetalle obtener(SiginexPrincipal actor, UUID id) {
        return diagnosticoService.buscarPorId(actor.tenantId(), id)
                .orElseThrow(() -> new DominioException(HttpStatus.NOT_FOUND, "diagnostico no encontrado"));
    }
}
