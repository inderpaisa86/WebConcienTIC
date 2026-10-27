package com.siginex.api.delegate;

import com.siginex.api.domain.DiagnosticoDetalle;
import com.siginex.api.domain.RespuestaEntrada;
import com.siginex.api.domain.SiginexPrincipal;
import com.siginex.api.exception.DominioException;
import com.siginex.api.service.DiagnosticoService;
import com.siginex.api.service.RespuestaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RespuestaDelegateTest {

    private DiagnosticoService diagnosticoService;
    private RespuestaService respuestaService;
    private RespuestaDelegate delegate;

    private final UUID actorTenant = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        diagnosticoService = mock(DiagnosticoService.class);
        respuestaService = mock(RespuestaService.class);
        delegate = new RespuestaDelegateImpl(diagnosticoService, respuestaService);
    }

    private SiginexPrincipal actor() {
        return new SiginexPrincipal(UUID.randomUUID(), actorTenant, "user@x.com", "miembro");
    }

    private DiagnosticoDetalle detalle(UUID id) {
        return DiagnosticoDetalle.builder()
                .id(id).organizacionId(UUID.randomUUID()).organizacionNombre("Acme")
                .estado("en_progreso").build();
    }

    @Test
    void guardarEnDiagnosticoDeOtroTenantDevuelve404YNoGuarda() {
        UUID diagnosticoId = UUID.randomUUID();
        // RLS hides diagnosticos of other tenants: buscarPorId under the actor's
        // tenant returns empty, so the diagnostico is treated as not found.
        when(diagnosticoService.buscarPorId(actorTenant, diagnosticoId)).thenReturn(Optional.empty());
        List<RespuestaEntrada> lote = List.of(
                RespuestaEntrada.builder().preguntaId("sst-1.1.1").valor("2").build());

        assertThatThrownBy(() -> delegate.guardar(actor(), diagnosticoId, lote))
                .isInstanceOf(DominioException.class)
                .satisfies(ex -> assertThat(((DominioException) ex).status()).isEqualTo(HttpStatus.NOT_FOUND));

        verify(diagnosticoService).buscarPorId(actorTenant, diagnosticoId);
        verify(respuestaService, never()).guardar(any(), any(), any());
    }

    @Test
    void guardarOkDelegaConTenantDelActorYElLote() {
        UUID diagnosticoId = UUID.randomUUID();
        when(diagnosticoService.buscarPorId(actorTenant, diagnosticoId))
                .thenReturn(Optional.of(detalle(diagnosticoId)));
        List<RespuestaEntrada> lote = List.of(
                RespuestaEntrada.builder().preguntaId("sst-1.1.1").valor("2").build(),
                RespuestaEntrada.builder().preguntaId("ambiental-2.3").valor("na").build());

        delegate.guardar(actor(), diagnosticoId, lote);

        verify(diagnosticoService).buscarPorId(actorTenant, diagnosticoId);
        verify(respuestaService).guardar(actorTenant, diagnosticoId, lote);
    }

    @Test
    void obtenerEnDiagnosticoDeOtroTenantDevuelve404YNoLista() {
        UUID diagnosticoId = UUID.randomUUID();
        when(diagnosticoService.buscarPorId(actorTenant, diagnosticoId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> delegate.obtener(actor(), diagnosticoId))
                .isInstanceOf(DominioException.class)
                .satisfies(ex -> assertThat(((DominioException) ex).status()).isEqualTo(HttpStatus.NOT_FOUND));

        verify(diagnosticoService).buscarPorId(actorTenant, diagnosticoId);
        verify(respuestaService, never()).listar(any(), any());
    }

    @Test
    void obtenerOkDelegaConTenantDelActorYDevuelveMapa() {
        UUID diagnosticoId = UUID.randomUUID();
        when(diagnosticoService.buscarPorId(actorTenant, diagnosticoId))
                .thenReturn(Optional.of(detalle(diagnosticoId)));
        Map<String, String> mapa = Map.of("sst-1.1.1", "2", "ambiental-2.3", "na");
        when(respuestaService.listar(actorTenant, diagnosticoId)).thenReturn(mapa);

        Map<String, String> resultado = delegate.obtener(actor(), diagnosticoId);

        assertThat(resultado).isEqualTo(mapa);
        verify(diagnosticoService).buscarPorId(actorTenant, diagnosticoId);
        verify(respuestaService).listar(actorTenant, diagnosticoId);
    }
}
