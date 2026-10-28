package com.siginex.api.delegate;

import com.siginex.api.config.TokenHasher;
import com.siginex.api.domain.AuthUser;
import com.siginex.api.domain.ResetTokenInfo;
import com.siginex.api.dto.reset.ConfirmarResetRequest;
import com.siginex.api.dto.reset.SolicitarResetRequest;
import com.siginex.api.exception.AuthException;
import com.siginex.api.service.AuditoriaService;
import com.siginex.api.service.ResetService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Pruebas de las reglas de negocio del reset de contraseña: token
 * inexistente/usado/expirado -> el mismo error genérico; política de
 * contraseña; y no-enumeración (solicitar un email inexistente no falla, no
 * emite token, pero tampoco revela nada). El token/enlace nunca se exponen.
 */
class ResetDelegateTest {

    private ResetService resetService;
    private PasswordEncoder passwordEncoder;
    private TokenHasher tokenHasher;
    private AuditoriaService auditoriaService;
    private ResetDelegateImpl delegate;

    private final UUID usuarioId = UUID.randomUUID();
    private final UUID tenantId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        resetService = mock(ResetService.class);
        passwordEncoder = mock(PasswordEncoder.class);
        tokenHasher = mock(TokenHasher.class);
        auditoriaService = mock(AuditoriaService.class);
        delegate = new ResetDelegateImpl(resetService, passwordEncoder, tokenHasher, auditoriaService);
        ReflectionTestUtils.setField(delegate, "frontBaseUrl", "https://concientic.com");
        when(tokenHasher.hash(anyString())).thenReturn("hash-del-token");
    }

    private AuthUser usuarioActivo() {
        return new AuthUser(usuarioId, tenantId, "hash", true, "miembro", (short) 0, null);
    }

    private ResetTokenInfo token(boolean usado, Instant expiraEn) {
        return ResetTokenInfo.builder()
                .id(UUID.randomUUID())
                .usuarioId(usuarioId)
                .tenantId(tenantId)
                .usado(usado)
                .expiraEn(expiraEn)
                .build();
    }

    // ---- solicitar: no-enumeración -----------------------------------------

    @Test
    void solicitarConEmailExistente_emiteYEnvia() {
        when(resetService.buscarUsuarioPorEmail("a@x.com")).thenReturn(Optional.of(usuarioActivo()));

        delegate.solicitar(new SolicitarResetRequest("a@x.com"));

        verify(resetService).emitirYEnviar(any(), anyString(), any(), anyString(), anyString());
    }

    @Test
    void solicitarConEmailInexistente_noEmitePeroNoFalla() {
        when(resetService.buscarUsuarioPorEmail("missing@x.com")).thenReturn(Optional.empty());

        // No lanza (misma respuesta que el caso existente: 204 en el controller).
        assertThatCode(() -> delegate.solicitar(new SolicitarResetRequest("missing@x.com")))
                .doesNotThrowAnyException();
        // No se emite token para un email inexistente.
        verify(resetService, never()).emitirYEnviar(any(), anyString(), any(), anyString(), anyString());
    }

    @Test
    void solicitarConUsuarioInactivo_noEmite() {
        AuthUser inactivo = new AuthUser(usuarioId, tenantId, "hash", false, "miembro", (short) 0, null);
        when(resetService.buscarUsuarioPorEmail("a@x.com")).thenReturn(Optional.of(inactivo));

        delegate.solicitar(new SolicitarResetRequest("a@x.com"));

        verify(resetService, never()).emitirYEnviar(any(), anyString(), any(), anyString(), anyString());
    }

    @Test
    void solicitar_fallaDeCorreoNoSePropaga() {
        when(resetService.buscarUsuarioPorEmail("a@x.com")).thenReturn(Optional.of(usuarioActivo()));
        org.mockito.Mockito.doThrow(new IllegalStateException("smtp caido"))
                .when(resetService).emitirYEnviar(any(), anyString(), any(), anyString(), anyString());

        // El fallo de envío no debe revelarse al cliente (no-enumeración).
        assertThatCode(() -> delegate.solicitar(new SolicitarResetRequest("a@x.com")))
                .doesNotThrowAnyException();
    }

    // ---- confirmar: validación del token -----------------------------------

    @Test
    void confirmarConTokenInexistente_lanzaGenerico() {
        when(resetService.buscarTokenPorHash("hash-del-token")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> delegate.confirmar(new ConfirmarResetRequest("tok", "contrasena-larga-1")))
                .isInstanceOf(AuthException.class);
        verify(resetService, never()).confirmar(any(), anyString());
    }

    @Test
    void confirmarConTokenUsado_lanzaGenerico() {
        when(resetService.buscarTokenPorHash("hash-del-token"))
                .thenReturn(Optional.of(token(true, Instant.now().plusSeconds(600))));

        assertThatThrownBy(() -> delegate.confirmar(new ConfirmarResetRequest("tok", "contrasena-larga-1")))
                .isInstanceOf(AuthException.class);
        verify(resetService, never()).confirmar(any(), anyString());
    }

    @Test
    void confirmarConTokenExpirado_lanzaGenerico() {
        when(resetService.buscarTokenPorHash("hash-del-token"))
                .thenReturn(Optional.of(token(false, Instant.now().minusSeconds(1))));

        assertThatThrownBy(() -> delegate.confirmar(new ConfirmarResetRequest("tok", "contrasena-larga-1")))
                .isInstanceOf(AuthException.class);
        verify(resetService, never()).confirmar(any(), anyString());
    }

    @Test
    void confirmarConPasswordCorta_lanzaGenerico() {
        assertThatThrownBy(() -> delegate.confirmar(new ConfirmarResetRequest("tok", "corta")))
                .isInstanceOf(AuthException.class);
        // Ni siquiera llega a buscar el token.
        verify(resetService, never()).buscarTokenPorHash(anyString());
    }

    @Test
    void confirmarValido_reHasheaYConfirma() {
        when(resetService.buscarTokenPorHash("hash-del-token"))
                .thenReturn(Optional.of(token(false, Instant.now().plusSeconds(600))));
        when(passwordEncoder.encode("contrasena-larga-1")).thenReturn("argon2-nuevo");

        delegate.confirmar(new ConfirmarResetRequest("tok", "contrasena-larga-1"));

        verify(passwordEncoder).encode("contrasena-larga-1");
        verify(resetService).confirmar(any(ResetTokenInfo.class),
                org.mockito.ArgumentMatchers.eq("argon2-nuevo"));
    }
}
