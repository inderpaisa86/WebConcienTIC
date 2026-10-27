package com.siginex.api.usuario;

import com.siginex.api.tenant.TenantTransactionTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Instant;
import java.util.UUID;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UsuarioServiceTest {

    private UsuarioRepository usuarioRepository;
    private TenantTransactionTemplate tenantTx;
    private UsuarioService service;

    private final UUID tenantId = UUID.randomUUID();

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        usuarioRepository = mock(UsuarioRepository.class);
        tenantTx = mock(TenantTransactionTemplate.class);
        // runInTenant must execute the supplier, as a real transaction would.
        when(tenantTx.runInTenant(any(UUID.class), any(Supplier.class)))
                .thenAnswer(inv -> ((Supplier<?>) inv.getArgument(1)).get());
        service = new UsuarioService(usuarioRepository, tenantTx);
    }

    @Test
    void crearRunsInsideRunInTenantWithTheGivenTenant() {
        UUID id = UUID.randomUUID();
        Usuario aInsertar = new Usuario(null, tenantId, "u@x.com", "U", "hash", "miembro", true, null);
        Usuario persistido = new Usuario(id, tenantId, "u@x.com", "U", null, "miembro", true, Instant.now());
        when(usuarioRepository.insertar(aInsertar)).thenReturn(persistido);

        Usuario resultado = service.crear(tenantId, aInsertar);

        assertThat(resultado).isEqualTo(persistido);
        verify(tenantTx).runInTenant(eq(tenantId), any(Supplier.class));
        verify(usuarioRepository).insertar(aInsertar);
    }

    @Test
    void cambiarActivoRunsInsideRunInTenantWithTheGivenTenant() {
        UUID usuarioId = UUID.randomUUID();

        service.cambiarActivo(tenantId, usuarioId, false);

        verify(tenantTx).runInTenant(eq(tenantId), any(Supplier.class));
        verify(usuarioRepository).cambiarActivo(usuarioId, false);
    }

    @Test
    void existeEmailRunsInsideRunInTenantWithTheGivenTenant() {
        when(usuarioRepository.existeEmail("u@x.com")).thenReturn(true);

        boolean existe = service.existeEmail(tenantId, "u@x.com");

        assertThat(existe).isTrue();
        ArgumentCaptor<UUID> tenant = ArgumentCaptor.forClass(UUID.class);
        verify(tenantTx).runInTenant(tenant.capture(), any(Supplier.class));
        assertThat(tenant.getValue()).isEqualTo(tenantId);
    }
}
