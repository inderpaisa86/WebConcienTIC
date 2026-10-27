package com.siginex.api.delegate;

import com.siginex.api.domain.Organizacion;
import com.siginex.api.domain.SiginexPrincipal;
import com.siginex.api.dto.organizacion.CrearOrganizacionRequest;
import com.siginex.api.service.OrganizacionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OrganizacionDelegateTest {

    private OrganizacionService organizacionService;
    private OrganizacionDelegate delegate;

    private final UUID actorTenant = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        organizacionService = mock(OrganizacionService.class);
        delegate = new OrganizacionDelegateImpl(organizacionService);
    }

    private SiginexPrincipal actor() {
        return new SiginexPrincipal(UUID.randomUUID(), actorTenant, "user@x.com", "miembro");
    }

    private CrearOrganizacionRequest request() {
        CrearOrganizacionRequest req = new CrearOrganizacionRequest();
        req.setNombre("Acme");
        req.setSector("tecnologia");
        req.setTamano("mediana");
        return req;
    }

    @Test
    void crearFixesActorTenantAndNeverAcceptsClientTenant() {
        UUID nuevoId = UUID.randomUUID();
        when(organizacionService.crear(eq(actorTenant), any())).thenAnswer(inv -> {
            Organizacion o = inv.getArgument(1);
            return Organizacion.builder()
                    .id(nuevoId)
                    .tenantId(o.getTenantId())
                    .nombre(o.getNombre())
                    .sector(o.getSector())
                    .tamano(o.getTamano())
                    .creadoEn(Instant.now())
                    .build();
        });

        Organizacion creada = delegate.crear(actor(), request());

        ArgumentCaptor<Organizacion> aInsertar = ArgumentCaptor.forClass(Organizacion.class);
        verify(organizacionService).crear(eq(actorTenant), aInsertar.capture());
        Organizacion enviada = aInsertar.getValue();
        assertThat(enviada.getTenantId()).isEqualTo(actorTenant);
        assertThat(enviada.getId()).isNull();
        assertThat(enviada.getNombre()).isEqualTo("Acme");
        assertThat(enviada.getSector()).isEqualTo("tecnologia");
        assertThat(enviada.getTamano()).isEqualTo("mediana");
        assertThat(creada.getId()).isEqualTo(nuevoId);
    }

    @Test
    void listarDelegatesWithActorTenant() {
        Organizacion org = Organizacion.builder()
                .id(UUID.randomUUID())
                .tenantId(actorTenant)
                .nombre("Acme")
                .creadoEn(Instant.now())
                .build();
        when(organizacionService.listar(actorTenant)).thenReturn(List.of(org));

        List<Organizacion> resultado = delegate.listar(actor());

        assertThat(resultado).containsExactly(org);
        verify(organizacionService).listar(actorTenant);
    }
}
