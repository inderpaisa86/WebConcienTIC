package com.siginex.api.delegate;

import com.siginex.api.domain.AuthUser;
import com.siginex.api.domain.SesionInfo;
import com.siginex.api.domain.SiginexPrincipal;
import com.siginex.api.exception.AuthException;
import com.siginex.api.service.AuthService;
import com.siginex.api.config.TokenHasher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

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

/**
 * Pruebas de la lógica de negocio de autenticación, que tras el refactor de
 * capas vive en {@link AuthDelegateImpl} (antes estaba en {@code AuthServiceImpl}).
 * El {@link AuthService} se mockea como mero acceso a datos. Cubre: login
 * correcto, contraseña incorrecta, bloqueo tras N intentos, usuario inactivo,
 * email inexistente (no-enumeración con dummy hash), y las reglas de validez de
 * sesión (válida / revocada / expirada) y de logout.
 *
 * <p>Es el mismo conjunto de aserciones que tenía el antiguo {@code AuthServiceTest},
 * reapuntado a la capa donde ahora reside la lógica.
 */
class AuthDelegateTest {

    private AuthService authService;
    private PasswordEncoder passwordEncoder;
    private TokenHasher tokenHasher;
    private AuthDelegate delegate;

    private final UUID usuarioId = UUID.randomUUID();
    private final UUID tenantId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        authService = mock(AuthService.class);
        passwordEncoder = mock(PasswordEncoder.class);
        tokenHasher = mock(TokenHasher.class);

        // El constructor precomputa un dummy hash; valor estable.
        when(passwordEncoder.encode(anyString())).thenReturn("dummy-hash");
        when(tokenHasher.hash(anyString())).thenReturn("token-hash");

        delegate = new AuthDelegateImpl(authService, passwordEncoder, tokenHasher);
    }

    private AuthUser activeUser(short fallos, Instant bloqueadoHasta) {
        return new AuthUser(usuarioId, tenantId, "real-hash", true, "admin", fallos, bloqueadoHasta);
    }

    @Test
    void successfulLoginCreatesSessionAndResetsFailures() {
        when(authService.buscarAuthPorEmail("a@x.com")).thenReturn(Optional.of(activeUser((short) 0, null)));
        when(passwordEncoder.matches("secret", "real-hash")).thenReturn(true);

        AuthDelegate.IssuedSession issued = delegate.login("a@x.com", "secret", "agent", "1.2.3.4");

        assertThat(issued.token()).isNotBlank();
        assertThat(issued.expiraEn()).isAfter(Instant.now());
        verify(authService).resetearFallosYLogin(usuarioId);
        verify(authService).crearSesion(eq(usuarioId), eq(tenantId), eq("token-hash"), any(), eq("agent"), eq("1.2.3.4"));
        verify(authService).registrarEvento(eq(tenantId), eq("a@x.com"), eq("login_exitoso"), anyString(), anyString(), any());
    }

    @Test
    void wrongPasswordThrowsGenericErrorAndIncrementsFailures() {
        when(authService.buscarAuthPorEmail("a@x.com")).thenReturn(Optional.of(activeUser((short) 0, null)));
        when(passwordEncoder.matches("bad", "real-hash")).thenReturn(false);

        assertThatThrownBy(() -> delegate.login("a@x.com", "bad", null, null))
                .isInstanceOf(AuthException.class)
                .hasMessage("credenciales invalidas");

        verify(authService).incrementarFallos(eq(usuarioId), isNull());
        verify(authService, never()).crearSesion(any(), any(), any(), any(), any(), any());
    }

    @Test
    void fifthFailureSetsLockUntil() {
        when(authService.buscarAuthPorEmail("a@x.com")).thenReturn(Optional.of(activeUser((short) 4, null)));
        when(passwordEncoder.matches("bad", "real-hash")).thenReturn(false);

        assertThatThrownBy(() -> delegate.login("a@x.com", "bad", null, null))
                .isInstanceOf(AuthException.class);

        var lockUntil = ArgumentCaptor.forClass(Instant.class);
        verify(authService).incrementarFallos(eq(usuarioId), lockUntil.capture());
        assertThat(lockUntil.getValue()).isNotNull();
        assertThat(lockUntil.getValue()).isAfter(Instant.now());
    }

    @Test
    void lockedAccountDoesNotAuthenticateEvenWithCorrectPassword() {
        when(authService.buscarAuthPorEmail("a@x.com"))
                .thenReturn(Optional.of(activeUser((short) 5, Instant.now().plusSeconds(60))));
        when(passwordEncoder.matches("secret", "real-hash")).thenReturn(true);

        assertThatThrownBy(() -> delegate.login("a@x.com", "secret", null, null))
                .isInstanceOf(AuthException.class);

        verify(authService, never()).crearSesion(any(), any(), any(), any(), any(), any());
        verify(authService, never()).resetearFallosYLogin(any());
        // El hash se sigue verificando para mantener el timing uniforme.
        verify(passwordEncoder, atLeastOnce()).matches(eq("secret"), eq("real-hash"));
    }

    @Test
    void unknownEmailThrowsSameErrorAndRunsDummyHashVerification() {
        when(authService.buscarAuthPorEmail("missing@x.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> delegate.login("missing@x.com", "whatever", null, null))
                .isInstanceOf(AuthException.class)
                .hasMessage("credenciales invalidas");

        // Se ejecuta una verificación de hash contra el dummy precomputado.
        verify(passwordEncoder).matches("whatever", "dummy-hash");
        verify(authService, never()).crearSesion(any(), any(), any(), any(), any(), any());
    }

    @Test
    void inactiveUserDoesNotAuthenticate() {
        AuthUser inactive = new AuthUser(usuarioId, tenantId, "real-hash", false, "miembro", (short) 0, null);
        when(authService.buscarAuthPorEmail("a@x.com")).thenReturn(Optional.of(inactive));
        when(passwordEncoder.matches("secret", "real-hash")).thenReturn(true);

        assertThatThrownBy(() -> delegate.login("a@x.com", "secret", null, null))
                .isInstanceOf(AuthException.class);

        verify(authService, never()).crearSesion(any(), any(), any(), any(), any(), any());
    }

    @Test
    void validarSesionReturnsPrincipalForValidSession() {
        SesionInfo sesion = new SesionInfo(UUID.randomUUID(), usuarioId, tenantId, "a@x.com", "admin",
                true, false, Instant.now().plusSeconds(3600));
        when(authService.buscarSesionPorHash("h")).thenReturn(Optional.of(sesion));

        Optional<SiginexPrincipal> principal = delegate.validarSesion("h");

        assertThat(principal).isPresent();
        assertThat(principal.get().usuarioId()).isEqualTo(usuarioId);
        assertThat(principal.get().tenantId()).isEqualTo(tenantId);
        assertThat(principal.get().rol()).isEqualTo("admin");
    }

    @Test
    void validarSesionRejectsRevokedSession() {
        SesionInfo revoked = new SesionInfo(UUID.randomUUID(), usuarioId, tenantId, "a@x.com", "admin",
                true, true, Instant.now().plusSeconds(3600));
        when(authService.buscarSesionPorHash("h")).thenReturn(Optional.of(revoked));

        assertThat(delegate.validarSesion("h")).isEmpty();
    }

    @Test
    void validarSesionRejectsExpiredSession() {
        SesionInfo expired = new SesionInfo(UUID.randomUUID(), usuarioId, tenantId, "a@x.com", "admin",
                true, false, Instant.now().minusSeconds(1));
        when(authService.buscarSesionPorHash("h")).thenReturn(Optional.of(expired));

        assertThat(delegate.validarSesion("h")).isEmpty();
    }

    @Test
    void validarSesionRejectsInactiveUser() {
        SesionInfo inactiveUser = new SesionInfo(UUID.randomUUID(), usuarioId, tenantId, "a@x.com", "admin",
                false, false, Instant.now().plusSeconds(3600));
        when(authService.buscarSesionPorHash("h")).thenReturn(Optional.of(inactiveUser));

        assertThat(delegate.validarSesion("h")).isEmpty();
    }

    @Test
    void logoutRevokesSessionAndAudits() {
        SiginexPrincipal principal = new SiginexPrincipal(usuarioId, tenantId, "a@x.com", "admin");

        delegate.logout("token-hash", principal);

        verify(authService).revocarSesion("token-hash");
        verify(authService).registrarEvento(eq(tenantId), eq("a@x.com"), eq("logout"), anyString(), anyString(), any());
    }
}
