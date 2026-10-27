package com.siginex.api.service;

import com.siginex.api.config.TenantTransactionTemplate;
import com.siginex.api.domain.DiagnosticoDetalle;
import com.siginex.api.domain.DiagnosticoResumen;
import com.siginex.api.repository.DiagnosticoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DiagnosticoServiceTest {

    private DiagnosticoRepository diagnosticoRepository;
    private TenantTransactionTemplate tenantTx;
    private DiagnosticoService service;

    private final UUID tenantId = UUID.randomUUID();

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        diagnosticoRepository = mock(DiagnosticoRepository.class);
        tenantTx = mock(TenantTransactionTemplate.class);
        // runInTenant must execute the supplier, as a real transaction would.
        when(tenantTx.runInTenant(any(UUID.class), any(Supplier.class)))
                .thenAnswer(inv -> ((Supplier<?>) inv.getArgument(1)).get());
        service = new DiagnosticoServiceImpl(diagnosticoRepository, tenantTx);
    }

    @Test
    void kbVersionVigenteRunsInsideRunInTenantWithTheGivenTenant() {
        when(diagnosticoRepository.kbVersionVigente()).thenReturn("3.0.0");

        String resultado = service.kbVersionVigente(tenantId);

        assertThat(resultado).isEqualTo("3.0.0");
        verify(tenantTx).runInTenant(eq(tenantId), any(Supplier.class));
        verify(diagnosticoRepository).kbVersionVigente();
    }

    @Test
    void crearRunsInsideRunInTenantWithTheGivenTenant() {
        UUID organizacionId = UUID.randomUUID();
        DiagnosticoDetalle persistido = DiagnosticoDetalle.builder()
                .id(UUID.randomUUID()).organizacionId(organizacionId).organizacionNombre("Acme")
                .estado("en_progreso").build();
        when(diagnosticoRepository.insertar(tenantId, organizacionId, "3.0.0")).thenReturn(persistido);

        DiagnosticoDetalle resultado = service.crear(tenantId, organizacionId, "3.0.0");

        assertThat(resultado).isEqualTo(persistido);
        verify(tenantTx).runInTenant(eq(tenantId), any(Supplier.class));
        verify(diagnosticoRepository).insertar(tenantId, organizacionId, "3.0.0");
    }

    @Test
    void listarRunsInsideRunInTenantWithTheGivenTenant() {
        DiagnosticoResumen resumen = DiagnosticoResumen.builder()
                .id(UUID.randomUUID()).organizacionId(UUID.randomUUID()).organizacionNombre("Acme")
                .estado("en_progreso").creadoEn(Instant.now()).build();
        when(diagnosticoRepository.listar()).thenReturn(List.of(resumen));

        List<DiagnosticoResumen> resultado = service.listar(tenantId);

        assertThat(resultado).containsExactly(resumen);
        verify(tenantTx).runInTenant(eq(tenantId), any(Supplier.class));
        verify(diagnosticoRepository).listar();
    }

    @Test
    void buscarPorIdRunsInsideRunInTenantWithTheGivenTenant() {
        UUID id = UUID.randomUUID();
        DiagnosticoDetalle detalle = DiagnosticoDetalle.builder()
                .id(id).organizacionId(UUID.randomUUID()).organizacionNombre("Acme")
                .estado("en_progreso").build();
        when(diagnosticoRepository.buscarPorId(id)).thenReturn(Optional.of(detalle));

        Optional<DiagnosticoDetalle> resultado = service.buscarPorId(tenantId, id);

        assertThat(resultado).contains(detalle);
        verify(tenantTx).runInTenant(eq(tenantId), any(Supplier.class));
        verify(diagnosticoRepository).buscarPorId(id);
    }
}
