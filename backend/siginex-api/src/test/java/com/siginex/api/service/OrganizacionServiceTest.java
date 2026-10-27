package com.siginex.api.service;

import com.siginex.api.config.TenantTransactionTemplate;
import com.siginex.api.domain.Organizacion;
import com.siginex.api.repository.OrganizacionRepository;
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

class OrganizacionServiceTest {

    private OrganizacionRepository organizacionRepository;
    private TenantTransactionTemplate tenantTx;
    private OrganizacionService service;

    private final UUID tenantId = UUID.randomUUID();

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        organizacionRepository = mock(OrganizacionRepository.class);
        tenantTx = mock(TenantTransactionTemplate.class);
        // runInTenant must execute the supplier, as a real transaction would.
        when(tenantTx.runInTenant(any(UUID.class), any(Supplier.class)))
                .thenAnswer(inv -> ((Supplier<?>) inv.getArgument(1)).get());
        service = new OrganizacionServiceImpl(organizacionRepository, tenantTx);
    }

    @Test
    void crearRunsInsideRunInTenantWithTheGivenTenant() {
        UUID id = UUID.randomUUID();
        Organizacion aInsertar = Organizacion.builder().tenantId(tenantId).nombre("Acme").build();
        Organizacion persistida = Organizacion.builder()
                .id(id).tenantId(tenantId).nombre("Acme").creadoEn(Instant.now()).build();
        when(organizacionRepository.insertar(aInsertar)).thenReturn(persistida);

        Organizacion resultado = service.crear(tenantId, aInsertar);

        assertThat(resultado).isEqualTo(persistida);
        verify(tenantTx).runInTenant(eq(tenantId), any(Supplier.class));
        verify(organizacionRepository).insertar(aInsertar);
    }

    @Test
    void listarRunsInsideRunInTenantWithTheGivenTenant() {
        Organizacion org = Organizacion.builder()
                .id(UUID.randomUUID()).tenantId(tenantId).nombre("Acme").creadoEn(Instant.now()).build();
        when(organizacionRepository.listar()).thenReturn(List.of(org));

        List<Organizacion> resultado = service.listar(tenantId);

        assertThat(resultado).containsExactly(org);
        verify(tenantTx).runInTenant(eq(tenantId), any(Supplier.class));
        verify(organizacionRepository).listar();
    }

    @Test
    void buscarPorIdRunsInsideRunInTenantWithTheGivenTenant() {
        UUID id = UUID.randomUUID();
        Organizacion org = Organizacion.builder()
                .id(id).tenantId(tenantId).nombre("Acme").creadoEn(Instant.now()).build();
        when(organizacionRepository.buscarPorId(id)).thenReturn(Optional.of(org));

        Optional<Organizacion> resultado = service.buscarPorId(tenantId, id);

        assertThat(resultado).contains(org);
        verify(tenantTx).runInTenant(eq(tenantId), any(Supplier.class));
        verify(organizacionRepository).buscarPorId(id);
    }
}
