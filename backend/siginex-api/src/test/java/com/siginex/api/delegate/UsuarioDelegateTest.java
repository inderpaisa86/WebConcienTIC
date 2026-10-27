package com.siginex.api.delegate;


import com.siginex.api.delegate.UsuarioDelegate;
import com.siginex.api.domain.Usuario;
import com.siginex.api.exception.UsuarioException;
import com.siginex.api.service.UsuarioService;
import com.siginex.api.service.AuditoriaService;
import com.siginex.api.domain.SiginexPrincipal;
import com.siginex.api.dto.usuario.CrearUsuarioRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UsuarioDelegateTest {

    private UsuarioService usuarioService;
    private PasswordEncoder passwordEncoder;
    private AuditoriaService auditoriaService;
    private UsuarioDelegate delegate;

    private final UUID adminTenant = UUID.randomUUID();
    private final UUID otroTenant = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        usuarioService = mock(UsuarioService.class);
        passwordEncoder = mock(PasswordEncoder.class);
        auditoriaService = mock(AuditoriaService.class);
        delegate = new UsuarioDelegateImpl(usuarioService, passwordEncoder, auditoriaService);
    }

    private SiginexPrincipal admin() {
        return new SiginexPrincipal(UUID.randomUUID(), adminTenant, "admin@x.com", "admin");
    }

    private SiginexPrincipal miembro() {
        return new SiginexPrincipal(UUID.randomUUID(), adminTenant, "miembro@x.com", "miembro");
    }

    private CrearUsuarioRequest request() {
        return new CrearUsuarioRequest("nuevo@x.com", "Nuevo", "contrasena-larga", "miembro");
    }

    @Test
    void nonAdminCannotCreateUser() {
        assertThatThrownBy(() -> delegate.crear(miembro(), request()))
                .isInstanceOf(UsuarioException.class)
                .extracting(e -> ((UsuarioException) e).status())
                .isEqualTo(HttpStatus.FORBIDDEN);

        verify(usuarioService, never()).crear(any(), any());
    }

    @Test
    void createFixesAdminTenantAndHashesPasswordNeverStoringPlaintext() {
        when(usuarioService.existeEmail(adminTenant, "nuevo@x.com")).thenReturn(false);
        when(passwordEncoder.encode("contrasena-larga")).thenReturn("argon2-hash");
        UUID nuevoId = UUID.randomUUID();
        when(usuarioService.crear(eq(adminTenant), any())).thenAnswer(inv -> {
            Usuario u = inv.getArgument(1);
            return new Usuario(nuevoId, u.tenantId(), u.email(), u.nombre(), u.passwordHash(),
                    u.rol(), u.activo(), Instant.now());
        });

        Usuario creado = delegate.crear(admin(), request());

        ArgumentCaptor<Usuario> inserted = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioService).crear(eq(adminTenant), inserted.capture());
        Usuario aInsertar = inserted.getValue();
        assertThat(aInsertar.tenantId()).isEqualTo(adminTenant);
        assertThat(aInsertar.passwordHash()).isEqualTo("argon2-hash");
        assertThat(aInsertar.passwordHash()).isNotEqualTo("contrasena-larga");
        assertThat(creado.id()).isEqualTo(nuevoId);
    }

    @Test
    void duplicateEmailIsRejectedWithConflict() {
        when(usuarioService.existeEmail(adminTenant, "nuevo@x.com")).thenReturn(true);

        assertThatThrownBy(() -> delegate.crear(admin(), request()))
                .isInstanceOf(UsuarioException.class)
                .extracting(e -> ((UsuarioException) e).status())
                .isEqualTo(HttpStatus.CONFLICT);

        verify(usuarioService, never()).crear(any(), any());
    }

    @Test
    void createAuditsWithoutSecretKeys() {
        when(usuarioService.existeEmail(adminTenant, "nuevo@x.com")).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("argon2-hash");
        UUID nuevoId = UUID.randomUUID();
        when(usuarioService.crear(eq(adminTenant), any())).thenAnswer(inv -> {
            Usuario u = inv.getArgument(1);
            return new Usuario(nuevoId, u.tenantId(), u.email(), u.nombre(), u.passwordHash(),
                    u.rol(), u.activo(), Instant.now());
        });

        delegate.crear(admin(), request());

        ArgumentCaptor<Map<String, ?>> payload = ArgumentCaptor.forClass(Map.class);
        verify(auditoriaService).registrar(eq(adminTenant), eq("admin@x.com"), eq("usuario_creado"),
                eq("usuario"), eq(nuevoId.toString()), payload.capture());
        assertThat(payload.getValue()).doesNotContainKeys("password", "passwordHash", "hash");
    }

    @Test
    void nonAdminCannotActivateOrDeactivate() {
        UUID target = UUID.randomUUID();

        assertThatThrownBy(() -> delegate.activar(miembro(), target))
                .isInstanceOf(UsuarioException.class)
                .extracting(e -> ((UsuarioException) e).status())
                .isEqualTo(HttpStatus.FORBIDDEN);
        assertThatThrownBy(() -> delegate.desactivar(miembro(), target))
                .isInstanceOf(UsuarioException.class)
                .extracting(e -> ((UsuarioException) e).status())
                .isEqualTo(HttpStatus.FORBIDDEN);

        verify(usuarioService, never()).cambiarActivo(any(), any(), anyBoolean());
    }

    @Test
    void adminActivatesUserInOwnTenant() {
        UUID target = UUID.randomUUID();
        Usuario inactivo = new Usuario(target, adminTenant, "u@x.com", "U", null, "miembro", false, Instant.now());
        Usuario activo = new Usuario(target, adminTenant, "u@x.com", "U", null, "miembro", true, Instant.now());
        when(usuarioService.buscarPorId(adminTenant, target))
                .thenReturn(Optional.of(inactivo))
                .thenReturn(Optional.of(activo));

        Usuario resultado = delegate.activar(admin(), target);

        verify(usuarioService).cambiarActivo(adminTenant, target, true);
        assertThat(resultado.activo()).isTrue();
        verify(auditoriaService).registrar(eq(adminTenant), eq("admin@x.com"), eq("usuario_activado"),
                eq("usuario"), eq(target.toString()), any());
    }

    @Test
    void adminCannotOperateOnUserOfAnotherTenant() {
        UUID target = UUID.randomUUID();
        // RLS scopes the lookup to the admin's tenant, so a foreign user is not found.
        when(usuarioService.buscarPorId(adminTenant, target)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> delegate.desactivar(admin(), target))
                .isInstanceOf(UsuarioException.class)
                .extracting(e -> ((UsuarioException) e).status())
                .isEqualTo(HttpStatus.NOT_FOUND);

        // The lookup used the admin's tenant, never a foreign one.
        verify(usuarioService).buscarPorId(adminTenant, target);
        verify(usuarioService, never()).buscarPorId(eq(otroTenant), any());
        verify(usuarioService, never()).cambiarActivo(any(), any(), anyBoolean());
    }
}
