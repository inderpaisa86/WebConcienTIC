package com.siginex.api.auth;

import com.siginex.api.auditoria.AuditoriaService;
import com.siginex.api.security.SiginexPrincipal;
import com.siginex.api.security.TokenHasher;
import com.siginex.api.tenant.TenantTransactionTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthServiceTest {

    private AuthRepository authRepository;
    private AuditoriaService auditoriaService;
    private TenantTransactionTemplate tenantTx;
    private PasswordEncoder passwordEncoder;
    private TokenHasher tokenHasher;
    private AuthService service;

    private final UUID usuarioId = UUID.randomUUID();
    private final UUID tenantId = UUID.randomUUID();

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        authRepository = mock(AuthRepository.class);
        auditoriaService = mock(AuditoriaService.class);
        tenantTx = mock(TenantTransactionTemplate.class);
        passwordEncoder = mock(PasswordEncoder.class);
        tokenHasher = mock(TokenHasher.class);

        // The constructor precomputes a dummy hash; return a stable value.
        when(passwordEncoder.encode(anyString())).thenReturn("dummy-hash");
        when(tokenHasher.hash(anyString())).thenReturn("token-hash");
        // runWithoutTenant must execute the supplier, as a real transaction would.
        when(tenantTx.runWithoutTenant(any())).thenAnswer(inv -> ((Supplier<?>) inv.getArgument(0)).get());

        service = new AuthService(authRepository, auditoriaService, tenantTx, passwordEncoder, tokenHasher);
    }

    private AuthUser activeUser(short fallos, Instant bloqueadoHasta) {
        return new AuthUser(usuarioId, tenantId, "real-hash", true, "admin", fallos, bloqueadoHasta);
    }

    @Test
    void successfulLoginCreatesSessionAndResetsFailures() {
        when(authRepository.findAuthByEmail("a@x.com")).thenReturn(Optional.of(activeUser((short) 0, null)));
        when(passwordEncoder.matches("secret", "real-hash")).thenReturn(true);

        AuthService.IssuedSession issued = service.autenticar("a@x.com", "secret", "agent", "1.2.3.4");

        assertThat(issued.token()).isNotBlank();
        assertThat(issued.expiraEn()).isAfter(Instant.now());
        verify(authRepository).resetearFallosYLogin(usuarioId);
        verify(authRepository).crearSesion(eq(usuarioId), eq(tenantId), eq("token-hash"), any(), eq("agent"), eq("1.2.3.4"));
        verify(auditoriaService).registrar(eq(tenantId), eq("a@x.com"), eq("login_exitoso"), anyString(), anyString(), any());
    }

    @Test
    void wrongPasswordThrowsGenericErrorAndIncrementsFailures() {
        when(authRepository.findAuthByEmail("a@x.com")).thenReturn(Optional.of(activeUser((short) 0, null)));
        when(passwordEncoder.matches("bad", "real-hash")).thenReturn(false);

        assertThatThrownBy(() -> service.autenticar("a@x.com", "bad", null, null))
                .isInstanceOf(AuthException.class)
                .hasMessage("credenciales invalidas");

        verify(authRepository).incrementarFallos(eq(usuarioId), isNull());
        verify(authRepository, never()).crearSesion(any(), any(), any(), any(), any(), any());
    }

    @Test
    void fifthFailureSetsLockUntil() {
        when(authRepository.findAuthByEmail("a@x.com")).thenReturn(Optional.of(activeUser((short) 4, null)));
        when(passwordEncoder.matches("bad", "real-hash")).thenReturn(false);

        assertThatThrownBy(() -> service.autenticar("a@x.com", "bad", null, null))
                .isInstanceOf(AuthException.class);

        var lockUntil = ArgumentCaptor.forClass(Instant.class);
        verify(authRepository).incrementarFallos(eq(usuarioId), lockUntil.capture());
        assertThat(lockUntil.getValue()).isNotNull();
        assertThat(lockUntil.getValue()).isAfter(Instant.now());
    }

    @Test
    void lockedAccountDoesNotAuthenticateEvenWithCorrectPassword() {
        when(authRepository.findAuthByEmail("a@x.com"))
                .thenReturn(Optional.of(activeUser((short) 5, Instant.now().plusSeconds(60))));
        when(passwordEncoder.matches("secret", "real-hash")).thenReturn(true);

        assertThatThrownBy(() -> service.autenticar("a@x.com", "secret", null, null))
                .isInstanceOf(AuthException.class);

        verify(authRepository, never()).crearSesion(any(), any(), any(), any(), any(), any());
        verify(authRepository, never()).resetearFallosYLogin(any());
        // Hash still verified to keep timing uniform.
        verify(passwordEncoder, atLeastOnce()).matches(eq("secret"), eq("real-hash"));
    }

    @Test
    void unknownEmailThrowsSameErrorAndRunsDummyHashVerification() {
        when(authRepository.findAuthByEmail("missing@x.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.autenticar("missing@x.com", "whatever", null, null))
                .isInstanceOf(AuthException.class)
                .hasMessage("credenciales invalidas");

        // A hash verification runs against the precomputed dummy hash.
        verify(passwordEncoder).matches("whatever", "dummy-hash");
        verify(authRepository, never()).crearSesion(any(), any(), any(), any(), any(), any());
    }

    @Test
    void inactiveUserDoesNotAuthenticate() {
        AuthUser inactive = new AuthUser(usuarioId, tenantId, "real-hash", false, "miembro", (short) 0, null);
        when(authRepository.findAuthByEmail("a@x.com")).thenReturn(Optional.of(inactive));
        when(passwordEncoder.matches("secret", "real-hash")).thenReturn(true);

        assertThatThrownBy(() -> service.autenticar("a@x.com", "secret", null, null))
                .isInstanceOf(AuthException.class);

        verify(authRepository, never()).crearSesion(any(), any(), any(), any(), any(), any());
    }

    @Test
    void validarSesionReturnsPrincipalForValidSession() {
        SesionInfo sesion = new SesionInfo(UUID.randomUUID(), usuarioId, tenantId, "a@x.com", "admin",
                true, false, Instant.now().plusSeconds(3600));
        when(authRepository.buscarSesionPorHash("h")).thenReturn(Optional.of(sesion));

        Optional<SiginexPrincipal> principal = service.validarSesion("h");

        assertThat(principal).isPresent();
        assertThat(principal.get().usuarioId()).isEqualTo(usuarioId);
        assertThat(principal.get().tenantId()).isEqualTo(tenantId);
        assertThat(principal.get().rol()).isEqualTo("admin");
    }

    @Test
    void validarSesionRejectsRevokedSession() {
        SesionInfo revoked = new SesionInfo(UUID.randomUUID(), usuarioId, tenantId, "a@x.com", "admin",
                true, true, Instant.now().plusSeconds(3600));
        when(authRepository.buscarSesionPorHash("h")).thenReturn(Optional.of(revoked));

        assertThat(service.validarSesion("h")).isEmpty();
    }

    @Test
    void validarSesionRejectsExpiredSession() {
        SesionInfo expired = new SesionInfo(UUID.randomUUID(), usuarioId, tenantId, "a@x.com", "admin",
                true, false, Instant.now().minusSeconds(1));
        when(authRepository.buscarSesionPorHash("h")).thenReturn(Optional.of(expired));

        assertThat(service.validarSesion("h")).isEmpty();
    }

    @Test
    void revocarMarksSessionRevokedAndAudits() {
        SiginexPrincipal principal = new SiginexPrincipal(usuarioId, tenantId, "a@x.com", "admin");

        service.revocar("token-hash", principal);

        verify(authRepository).revocarSesion("token-hash");
        verify(auditoriaService).registrar(eq(tenantId), eq("a@x.com"), eq("logout"), anyString(), anyString(), any());
    }
}
