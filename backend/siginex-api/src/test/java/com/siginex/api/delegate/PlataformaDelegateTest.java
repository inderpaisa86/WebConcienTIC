package com.siginex.api.delegate;

import com.siginex.api.domain.EmpresaCreada;
import com.siginex.api.domain.Usuario;
import com.siginex.api.dto.plataforma.CrearEmpresaRequest;
import com.siginex.api.exception.PlataformaException;
import com.siginex.api.service.AuditoriaService;
import com.siginex.api.service.PlataformaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Pruebas de las reglas del alta de empresa: clave de plataforma ausente/incorrecta
 * -> 401, clave no configurada -> 503, email duplicado -> 409, y camino feliz que
 * hashea con Argon2id (nunca almacena la contraseña en claro) y crea tenant+admin.
 */
class PlataformaDelegateTest {

    private PlataformaService plataformaService;
    private PasswordEncoder passwordEncoder;
    private AuditoriaService auditoriaService;
    private PlataformaDelegateImpl delegate;

    private static final String CLAVE = "clave-de-plataforma-super-secreta";

    @BeforeEach
    void setUp() {
        plataformaService = mock(PlataformaService.class);
        passwordEncoder = mock(PasswordEncoder.class);
        auditoriaService = mock(AuditoriaService.class);
        delegate = new PlataformaDelegateImpl(plataformaService, passwordEncoder, auditoriaService);
        ReflectionTestUtils.setField(delegate, "adminKeyEsperada", CLAVE);
    }

    private CrearEmpresaRequest request() {
        return new CrearEmpresaRequest("Acme S.A.S.", "admin@acme.com", "Admin Acme", "contrasena-larga-1");
    }

    @Test
    void claveAusente_lanza401() {
        assertThatThrownBy(() -> delegate.crearEmpresa(null, request()))
                .isInstanceOf(PlataformaException.class)
                .extracting(e -> ((PlataformaException) e).status())
                .isEqualTo(HttpStatus.UNAUTHORIZED);
        verify(plataformaService, never()).crearEmpresaConAdmin(anyString(), anyString(), any());
    }

    @Test
    void claveIncorrecta_lanza401() {
        assertThatThrownBy(() -> delegate.crearEmpresa("otra-clave", request()))
                .isInstanceOf(PlataformaException.class)
                .extracting(e -> ((PlataformaException) e).status())
                .isEqualTo(HttpStatus.UNAUTHORIZED);
        verify(plataformaService, never()).crearEmpresaConAdmin(anyString(), anyString(), any());
    }

    @Test
    void claveNoConfigurada_lanza503() {
        ReflectionTestUtils.setField(delegate, "adminKeyEsperada", "");

        assertThatThrownBy(() -> delegate.crearEmpresa(CLAVE, request()))
                .isInstanceOf(PlataformaException.class)
                .extracting(e -> ((PlataformaException) e).status())
                .isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
    }

    @Test
    void emailDuplicado_lanza409() {
        when(plataformaService.existeEmailGlobal("admin@acme.com")).thenReturn(true);

        assertThatThrownBy(() -> delegate.crearEmpresa(CLAVE, request()))
                .isInstanceOf(PlataformaException.class)
                .extracting(e -> ((PlataformaException) e).status())
                .isEqualTo(HttpStatus.CONFLICT);
        verify(plataformaService, never()).crearEmpresaConAdmin(anyString(), anyString(), any());
    }

    @Test
    void passwordCorta_lanza400() {
        CrearEmpresaRequest corta = new CrearEmpresaRequest("Acme", "admin@acme.com", "Admin", "corta");

        assertThatThrownBy(() -> delegate.crearEmpresa(CLAVE, corta))
                .isInstanceOf(PlataformaException.class)
                .extracting(e -> ((PlataformaException) e).status())
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void caminoFeliz_hasheaArgon2YCreaTenantConAdmin() {
        when(plataformaService.existeEmailGlobal("admin@acme.com")).thenReturn(false);
        when(plataformaService.existeSlug(anyString())).thenReturn(false);
        when(passwordEncoder.encode("contrasena-larga-1")).thenReturn("argon2-hash");
        UUID tenantId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();
        when(plataformaService.crearEmpresaConAdmin(eq("Acme S.A.S."), anyString(), any()))
                .thenReturn(EmpresaCreada.builder()
                        .tenantId(tenantId).tenantNombre("Acme S.A.S.").tenantSlug("acme-s-a-s")
                        .adminUsuarioId(adminId).adminEmail("admin@acme.com").build());

        EmpresaCreada creada = delegate.crearEmpresa(CLAVE, request());

        ArgumentCaptor<Usuario> admin = ArgumentCaptor.forClass(Usuario.class);
        verify(plataformaService).crearEmpresaConAdmin(eq("Acme S.A.S."), anyString(), admin.capture());
        // El admin a insertar lleva el hash Argon2id, nunca la contraseña en claro.
        assertThat(admin.getValue().passwordHash()).isEqualTo("argon2-hash");
        assertThat(admin.getValue().passwordHash()).isNotEqualTo("contrasena-larga-1");
        assertThat(admin.getValue().rol()).isEqualTo("admin");
        assertThat(creada.getTenantId()).isEqualTo(tenantId);
        assertThat(creada.getAdminUsuarioId()).isEqualTo(adminId);
    }
}
