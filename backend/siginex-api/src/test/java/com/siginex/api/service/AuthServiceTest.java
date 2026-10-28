package com.siginex.api.service;

import com.siginex.api.config.TenantTransactionTemplate;
import com.siginex.api.domain.AuthUser;
import com.siginex.api.repository.AuthRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Pruebas del {@link AuthServiceImpl} tras el refactor de capas: ahora es SOLO
 * acceso a datos (sin reglas de negocio). Verifica que cada método delega en el
 * {@link AuthRepository}/{@link AuditoriaService} correcto y que la búsqueda por
 * email corre FUERA de un tenant fijado ({@code runWithoutTenant}, decisión D4).
 * La lógica de negocio se prueba en {@code AuthDelegateTest}.
 */
class AuthServiceTest {

    private AuthRepository authRepository;
    private AuditoriaService auditoriaService;
    private TenantTransactionTemplate tenantTx;
    private AuthService service;

    private final UUID usuarioId = UUID.randomUUID();
    private final UUID tenantId = UUID.randomUUID();

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        authRepository = mock(AuthRepository.class);
        auditoriaService = mock(AuditoriaService.class);
        tenantTx = mock(TenantTransactionTemplate.class);
        // runWithoutTenant debe ejecutar el supplier, como una transacción real.
        when(tenantTx.runWithoutTenant(any())).thenAnswer(inv -> ((Supplier<?>) inv.getArgument(0)).get());
        service = new AuthServiceImpl(authRepository, auditoriaService, tenantTx);
    }

    @Test
    void buscarAuthPorEmailRunsWithoutTenantAndDelegatesToRepository() {
        AuthUser user = new AuthUser(usuarioId, tenantId, "hash", true, "admin", (short) 0, null);
        when(authRepository.findAuthByEmail("a@x.com")).thenReturn(Optional.of(user));

        Optional<AuthUser> result = service.buscarAuthPorEmail("a@x.com");

        assertThat(result).containsSame(user);
        // Corre dentro de runWithoutTenant (no fija tenant).
        verify(tenantTx).runWithoutTenant(any());
        verify(authRepository).findAuthByEmail("a@x.com");
    }

    @Test
    void crearSesionDelegatesToRepository() {
        Instant expira = Instant.now().plusSeconds(3600);
        service.crearSesion(usuarioId, tenantId, "token-hash", expira, "agent", "1.2.3.4");
        verify(authRepository).crearSesion(usuarioId, tenantId, "token-hash", expira, "agent", "1.2.3.4");
    }

    @Test
    void buscarSesionPorHashDelegatesToRepository() {
        when(authRepository.buscarSesionPorHash("h")).thenReturn(Optional.empty());
        assertThat(service.buscarSesionPorHash("h")).isEmpty();
        verify(authRepository).buscarSesionPorHash("h");
    }

    @Test
    void revocarSesionDelegatesToRepository() {
        service.revocarSesion("h");
        verify(authRepository).revocarSesion("h");
    }

    @Test
    void incrementarFallosDelegatesToRepository() {
        Instant lock = Instant.now().plusSeconds(60);
        service.incrementarFallos(usuarioId, lock);
        verify(authRepository).incrementarFallos(usuarioId, lock);
    }

    @Test
    void resetearFallosYLoginDelegatesToRepository() {
        service.resetearFallosYLogin(usuarioId);
        verify(authRepository).resetearFallosYLogin(usuarioId);
    }

    @Test
    void registrarEventoDelegatesToAuditoria() {
        service.registrarEvento(tenantId, "a@x.com", "login_exitoso", "usuario", usuarioId.toString(),
                Map.of("email", "a@x.com"));
        verify(auditoriaService).registrar(eq(tenantId), eq("a@x.com"), eq("login_exitoso"),
                eq("usuario"), anyString(), any());
    }
}
