package com.siginex.api.delegate;

import com.siginex.api.domain.DiagnosticoDetalle;
import com.siginex.api.domain.DiagnosticoResumen;
import com.siginex.api.domain.Organizacion;
import com.siginex.api.domain.SiginexPrincipal;
import com.siginex.api.exception.DominioException;
import com.siginex.api.service.DiagnosticoService;
import com.siginex.api.service.OrganizacionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DiagnosticoDelegateTest {

    private OrganizacionService organizacionService;
    private DiagnosticoService diagnosticoService;
    private DiagnosticoDelegate delegate;

    private final UUID actorTenant = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        organizacionService = mock(OrganizacionService.class);
        diagnosticoService = mock(DiagnosticoService.class);
        delegate = new DiagnosticoDelegateImpl(organizacionService, diagnosticoService);
    }

    private SiginexPrincipal actor() {
        return new SiginexPrincipal(UUID.randomUUID(), actorTenant, "user@x.com", "miembro");
    }

    @Test
    void crearOkResolvesKbVersionAndCreatesWithIt() {
        UUID organizacionId = UUID.randomUUID();
        Organizacion org = Organizacion.builder()
                .id(organizacionId).tenantId(actorTenant).nombre("Acme").creadoEn(Instant.now()).build();
        when(organizacionService.buscarPorId(actorTenant, organizacionId)).thenReturn(Optional.of(org));
        when(diagnosticoService.kbVersionVigente(actorTenant)).thenReturn("3.0.0");
        DiagnosticoDetalle creado = DiagnosticoDetalle.builder()
                .id(UUID.randomUUID()).organizacionId(organizacionId).organizacionNombre("Acme")
                .estado("en_progreso").build();
        when(diagnosticoService.crear(actorTenant, organizacionId, "3.0.0")).thenReturn(creado);

        DiagnosticoDetalle resultado = delegate.crear(actor(), organizacionId);

        assertThat(resultado).isEqualTo(creado);
        verify(organizacionService).buscarPorId(actorTenant, organizacionId);
        verify(diagnosticoService).kbVersionVigente(actorTenant);
        verify(diagnosticoService).crear(actorTenant, organizacionId, "3.0.0");
    }

    @Test
    void crearConOrganizacionDeOtroTenantDevuelve404YNoCrea() {
        UUID organizacionId = UUID.randomUUID();
        // RLS hides organizaciones of other tenants: buscarPorId under the actor's
        // tenant returns empty, so the organizacion is treated as not found.
        when(organizacionService.buscarPorId(actorTenant, organizacionId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> delegate.crear(actor(), organizacionId))
                .isInstanceOf(DominioException.class)
                .satisfies(ex -> assertThat(((DominioException) ex).status()).isEqualTo(HttpStatus.NOT_FOUND));

        verify(organizacionService).buscarPorId(actorTenant, organizacionId);
        verify(diagnosticoService, never()).kbVersionVigente(any());
        verify(diagnosticoService, never()).crear(any(), any(), any());
    }

    @Test
    void listarDelegatesWithActorTenant() {
        DiagnosticoResumen resumen = DiagnosticoResumen.builder()
                .id(UUID.randomUUID()).organizacionId(UUID.randomUUID()).organizacionNombre("Acme")
                .estado("en_progreso").creadoEn(Instant.now()).build();
        when(diagnosticoService.listar(actorTenant)).thenReturn(List.of(resumen));

        List<DiagnosticoResumen> resultado = delegate.listar(actor());

        assertThat(resultado).containsExactly(resumen);
        verify(diagnosticoService).listar(actorTenant);
    }

    @Test
    void obtenerExistenteDelegatesWithActorTenant() {
        UUID id = UUID.randomUUID();
        DiagnosticoDetalle detalle = DiagnosticoDetalle.builder()
                .id(id).organizacionId(UUID.randomUUID()).organizacionNombre("Acme")
                .estado("en_progreso").build();
        when(diagnosticoService.buscarPorId(actorTenant, id)).thenReturn(Optional.of(detalle));

        DiagnosticoDetalle resultado = delegate.obtener(actor(), id);

        assertThat(resultado).isEqualTo(detalle);
        verify(diagnosticoService).buscarPorId(actorTenant, id);
    }

    @Test
    void obtenerInexistenteDevuelve404() {
        UUID id = UUID.randomUUID();
        when(diagnosticoService.buscarPorId(actorTenant, id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> delegate.obtener(actor(), id))
                .isInstanceOf(DominioException.class)
                .satisfies(ex -> assertThat(((DominioException) ex).status()).isEqualTo(HttpStatus.NOT_FOUND));

        verify(diagnosticoService).buscarPorId(actorTenant, id);
    }
}
