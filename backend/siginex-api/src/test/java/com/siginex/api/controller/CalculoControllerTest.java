package com.siginex.api.controller;

import com.siginex.api.config.SecurityConfig;
import com.siginex.api.config.SessionAuthenticationFilter;
import com.siginex.api.config.TokenHasher;
import com.siginex.api.delegate.CalculoDelegate;
import com.siginex.api.domain.SiginexPrincipal;
import com.siginex.api.domain.calculo.Benchmark;
import com.siginex.api.domain.calculo.ResultadoLectura;
import com.siginex.api.domain.calculo.TareaPlanLectura;
import com.siginex.api.exception.DominioException;
import com.siginex.api.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Web-slice de {@link CalculoController}: valida rutas, códigos y formas JSON
 * con el {@link CalculoDelegate} mockeado. Importa la cadena real
 * {@link SecurityConfig} para probar que las rutas exigen sesión; el
 * {@link SessionAuthenticationFilter} se neutraliza mockeando sus colaboradores
 * ({@link AuthService}/{@link TokenHasher}), de modo que la autenticación se
 * inyecta en el test con el post-processor {@code authentication(...)} de
 * Spring Security Test (el tenant sale del principal, nunca del cliente).
 */
@WebMvcTest(CalculoController.class)
@Import(SecurityConfig.class)
class CalculoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CalculoDelegate calculoDelegate;

    // Colaboradores del SessionAuthenticationFilter (es @Component, entra en el
    // slice); mockeados para que el filtro no autentique por sí mismo.
    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private TokenHasher tokenHasher;

    private final UUID diagnosticoId = UUID.randomUUID();

    private static UsernamePasswordAuthenticationToken principal() {
        SiginexPrincipal p = new SiginexPrincipal(
                UUID.randomUUID(), UUID.randomUUID(), "user@x.com", "miembro");
        return new UsernamePasswordAuthenticationToken(
                p, null, List.of(new SimpleGrantedAuthority("ROLE_MIEMBRO")));
    }

    private static ResultadoLectura resultado() {
        return ResultadoLectura.builder()
                .scoreSgi(new BigDecimal("45.45"))
                .nivelSgi(3)
                .cumplimientoGlobal(new BigDecimal("45.45"))
                .organizacionNombre("Org A")
                .pilares(List.of(ResultadoLectura.PilarLectura.builder()
                        .skillId("sst").cumplimientoPct(new BigDecimal("50.00"))
                        .nivel(3).banda("Definido").build()))
                .recomendaciones(List.of(ResultadoLectura.RecomendacionLectura.builder()
                        .skillId("sst").prioridad("alta").texto("Cerrar brecha").build()))
                .build();
    }

    private static Benchmark benchmark() {
        return Benchmark.builder()
                .nivelEmpresa(2)
                .promedioMercado(3.6)
                .rangoMercado(new int[]{2, 5})
                .herramientasPorEncima(7)
                .brechaAlLider(3)
                .recomendacion("Reco nivel 2")
                .build();
    }

    @Test
    void calcularDevuelve200ConResultado() throws Exception {
        when(calculoDelegate.calcular(any(), eq(diagnosticoId))).thenReturn(resultado());

        mockMvc.perform(post("/api/v1/diagnosticos/{id}/calcular", diagnosticoId)
                        .with(authentication(principal())).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.score").value(45.45))
                .andExpect(jsonPath("$.nivelSgi").value(3))
                .andExpect(jsonPath("$.organizacionNombre").value("Org A"))
                .andExpect(jsonPath("$.pilares[0].skillId").value("sst"))
                .andExpect(jsonPath("$.recomendaciones[0].prioridad").value("alta"));
    }

    @Test
    void obtenerResultadoDevuelve200() throws Exception {
        when(calculoDelegate.obtenerResultado(any(), eq(diagnosticoId))).thenReturn(resultado());

        mockMvc.perform(get("/api/v1/diagnosticos/{id}/resultado", diagnosticoId)
                        .with(authentication(principal())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cumplimientoGlobal").value(45.45));
    }

    @Test
    void obtenerResultadoDeOtroTenantDevuelve404() throws Exception {
        when(calculoDelegate.obtenerResultado(any(), eq(diagnosticoId)))
                .thenThrow(new DominioException(HttpStatus.NOT_FOUND, "diagnostico no encontrado"));

        mockMvc.perform(get("/api/v1/diagnosticos/{id}/resultado", diagnosticoId)
                        .with(authentication(principal())))
                .andExpect(status().isNotFound());
    }

    @Test
    void obtenerPlanMejoraDevuelve200ConLista() throws Exception {
        when(calculoDelegate.obtenerPlanMejora(any(), eq(diagnosticoId)))
                .thenReturn(List.of(TareaPlanLectura.builder()
                        .skillId("sst").preguntaId("sst-1.1.1").brecha("Falta evidencia")
                        .accion("Documentar").prioridad("alta").plazoDias(30).fase(1).build()));

        mockMvc.perform(get("/api/v1/diagnosticos/{id}/plan-mejora", diagnosticoId)
                        .with(authentication(principal())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].skillId").value("sst"))
                .andExpect(jsonPath("$[0].preguntaId").value("sst-1.1.1"))
                .andExpect(jsonPath("$[0].plazoDias").value(30));
    }

    @Test
    void registrarBenchmarkDevuelve200EnSnakeCase() throws Exception {
        when(calculoDelegate.registrarBenchmark(any(), eq(diagnosticoId), eq(2)))
                .thenReturn(benchmark());

        mockMvc.perform(post("/api/v1/diagnosticos/{id}/benchmark", diagnosticoId)
                        .with(authentication(principal())).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nivel\":2}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nivel_empresa").value(2))
                .andExpect(jsonPath("$.promedio_mercado").value(3.6))
                .andExpect(jsonPath("$.rango_mercado[0]").value(2))
                .andExpect(jsonPath("$.rango_mercado[1]").value(5))
                .andExpect(jsonPath("$.herramientas_por_encima").value(7))
                .andExpect(jsonPath("$.brecha_al_lider").value(3))
                .andExpect(jsonPath("$.recomendacion").value("Reco nivel 2"));
    }

    @Test
    void registrarBenchmarkConNivelFueraDeRangoDevuelve400() throws Exception {
        mockMvc.perform(post("/api/v1/diagnosticos/{id}/benchmark", diagnosticoId)
                        .with(authentication(principal())).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nivel\":0}"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/v1/diagnosticos/{id}/benchmark", diagnosticoId)
                        .with(authentication(principal())).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nivel\":6}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void obtenerBenchmarkSinNivelDevuelve204() throws Exception {
        when(calculoDelegate.obtenerBenchmark(any(), eq(diagnosticoId)))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/diagnosticos/{id}/benchmark", diagnosticoId)
                        .with(authentication(principal())))
                .andExpect(status().isNoContent());
    }

    @Test
    void obtenerBenchmarkConNivelDevuelve200() throws Exception {
        when(calculoDelegate.obtenerBenchmark(any(), eq(diagnosticoId)))
                .thenReturn(Optional.of(benchmark()));

        mockMvc.perform(get("/api/v1/diagnosticos/{id}/benchmark", diagnosticoId)
                        .with(authentication(principal())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nivel_empresa").value(2));
    }

    @Test
    void rutasExigenSesion() throws Exception {
        mockMvc.perform(get("/api/v1/diagnosticos/{id}/resultado", diagnosticoId))
                .andExpect(status().is4xxClientError());
    }
}
