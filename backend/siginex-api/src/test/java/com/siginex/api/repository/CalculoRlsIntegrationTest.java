package com.siginex.api.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.siginex.api.config.TenantTransactionTemplate;
import com.siginex.api.delegate.CalculoDelegate;
import com.siginex.api.delegate.CalculoDelegateImpl;
import com.siginex.api.domain.RespuestaEntrada;
import com.siginex.api.domain.SiginexPrincipal;
import com.siginex.api.domain.calculo.ResultadoLectura;
import com.siginex.api.exception.DominioException;
import com.siginex.api.service.AuditoriaService;
import com.siginex.api.service.AuditoriaServiceImpl;
import com.siginex.api.service.CalculoService;
import com.siginex.api.service.CalculoServiceImpl;
import com.siginex.api.service.DiagnosticoServiceImpl;
import com.siginex.api.service.calculo.MotorCalculo;
import com.siginex.api.service.kb.KbSeedService;
import com.zaxxer.hikari.HikariDataSource;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Integration test for the idempotent calculation orchestration
 * ({@link CalculoServiceImpl#calcular}) and tenant isolation (PostgreSQL
 * Row-Level Security), exercising the PRODUCTION repositories on top of the real
 * {@link TenantTransactionTemplate} and {@link MotorCalculo}. Runs the REAL
 * Flyway migrations (V1..V4) and the production {@link KbSeedService} (banco.json,
 * 9 modules / 521 questions) against a throwaway {@code postgres:17-alpine}
 * container.
 *
 * <p>Like {@link RespuestaRlsIntegrationTest}, every tenant-scoped assertion runs
 * through the non-privileged {@code siginex_app} role
 * ({@code NOSUPERUSER NOBYPASSRLS}); the superuser connection is used only for
 * migrations, role/grants, KB seeding (needs INSERT on the global KB tables) and
 * the deterministic {@code TRUNCATE} between tests.
 *
 * <p>Asserts:
 * <ul>
 *   <li>end-to-end: {@code calcular} writes 9 {@code resultado_pilar} rows plus
 *       {@code recomendacion}/{@code tarea_mejora}/{@code ruta_aprendizaje} rows,
 *       updates {@code diagnostico} ({@code estado='calculado'}, score/nivel/
 *       cumplimiento/completado_en) and records the
 *       {@code 'diagnostico.calculado'} audit event with actor {@code 'api'};</li>
 *   <li>IDEMPOTENCY: running twice yields identical counts and values (no
 *       duplicated rows);</li>
 *   <li>RLS CROSS-TENANT: calculating tenant A's diagnostico under tenant B
 *       throws {@link DominioException} 404 and leaves no rows for B.</li>
 * </ul>
 */
@Testcontainers
class CalculoRlsIntegrationTest {

    private static final String APP_ROLE = "siginex_app";
    private static final String APP_PASSWORD = "siginex_app_pwd";
    private static final String KB_VERSION = "3.0.0";

    @Container
    static final PostgreSQLContainer POSTGRES =
            new PostgreSQLContainer(DockerImageName.parse("postgres:17-alpine"));

    private static HikariDataSource appDataSource;
    private static HikariDataSource adminDataSource;
    private static JdbcTemplate appJdbc;
    private static JdbcTemplate adminJdbc;
    private static TenantTransactionTemplate tenantTemplate;
    private static CalculoService calculoService;
    private static CalculoDelegate calculoDelegate;

    @BeforeAll
    static void setUpDatabase() {
        adminDataSource = buildDataSource(POSTGRES.getUsername(), POSTGRES.getPassword());
        adminJdbc = new JdbcTemplate(adminDataSource);

        Flyway.configure()
                .dataSource(adminDataSource)
                .schemas("siginex")
                .locations("classpath:db/migration")
                .load()
                .migrate();

        adminJdbc.execute("DROP ROLE IF EXISTS " + APP_ROLE);
        adminJdbc.execute("CREATE ROLE " + APP_ROLE
                + " LOGIN PASSWORD '" + APP_PASSWORD + "' NOSUPERUSER NOBYPASSRLS");
        adminJdbc.execute("GRANT USAGE ON SCHEMA siginex TO " + APP_ROLE);
        adminJdbc.execute("GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA siginex TO " + APP_ROLE);
        adminJdbc.execute("GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA siginex TO " + APP_ROLE);
        adminJdbc.execute("GRANT EXECUTE ON FUNCTION siginex.current_tenant() TO " + APP_ROLE);

        ObjectMapper objectMapper = new ObjectMapper();
        // KB seed runs through the privileged connection (needs INSERT on KB tables).
        new KbSeedService(new NamedParameterJdbcTemplate(adminDataSource), objectMapper).seed();

        appDataSource = buildDataSource(APP_ROLE, APP_PASSWORD);
        appJdbc = new JdbcTemplate(appDataSource);
        NamedParameterJdbcTemplate namedAppJdbc = new NamedParameterJdbcTemplate(appDataSource);
        TransactionTemplate transactionTemplate =
                new TransactionTemplate(new DataSourceTransactionManager(appDataSource));
        tenantTemplate = new TenantTransactionTemplate(transactionTemplate, namedAppJdbc);

        // Production repositories + motor wired manually on the app-role connection.
        var diagnosticoRepository = new JdbcDiagnosticoRepository(namedAppJdbc);
        var organizacionRepository = new JdbcOrganizacionRepository(namedAppJdbc);
        var respuestaRepository = new JdbcRespuestaRepository(namedAppJdbc);
        var kbRepository = new JdbcKbRepository(namedAppJdbc, objectMapper);
        var resultadoRepository = new JdbcResultadoRepository(namedAppJdbc, objectMapper);
        var auditoriaRepository = new JdbcAuditoriaRepository(namedAppJdbc);
        AuditoriaService auditoriaService = new AuditoriaServiceImpl(auditoriaRepository);
        MotorCalculo motorCalculo = new MotorCalculo();
        // CalculoService queda como acceso a datos; la orquestación/validación del
        // cálculo vive en CalculoDelegate (Delegate orquesta, Service solo BD).
        calculoService = new CalculoServiceImpl(tenantTemplate, diagnosticoRepository,
                organizacionRepository, respuestaRepository, kbRepository, resultadoRepository,
                auditoriaService, objectMapper);
        var diagnosticoService = new DiagnosticoServiceImpl(diagnosticoRepository, tenantTemplate);
        calculoDelegate = new CalculoDelegateImpl(diagnosticoService, calculoService, motorCalculo);
    }

    /** Principal de prueba para un tenant (solo el tenantId importa para el cálculo). */
    private static SiginexPrincipal actor(UUID tenantId) {
        return new SiginexPrincipal(UUID.randomUUID(), tenantId, "actor@siginex.test", "miembro");
    }

    @AfterAll
    static void tearDown() {
        if (appDataSource != null) {
            appDataSource.close();
        }
        if (adminDataSource != null) {
            adminDataSource.close();
        }
    }

    @BeforeEach
    @AfterEach
    void cleanRows() {
        // KB tables are global and preserved; only the per-tenant rows are reset.
        adminJdbc.execute("TRUNCATE siginex.resultado_pilar, siginex.recomendacion, "
                + "siginex.tarea_mejora, siginex.ruta_aprendizaje, siginex.evento_auditoria, "
                + "siginex.respuesta, siginex.diagnostico, siginex.organizacion, siginex.tenant CASCADE");
    }

    @Test
    void calcularPersisteResultadosYEventoDeAuditoria() {
        UUID tenantA = seedTenant("Tenant A");
        UUID diagA = insertDiagnostico(tenantA, insertOrganizacion(tenantA, "Org A"), "2");
        // Una respuesta con brecha (0) y otra en conformidad (2).
        guardarRespuestas(tenantA, diagA, List.of(
                RespuestaEntrada.builder().preguntaId("sst-1.1.1").valor("0").build(),
                RespuestaEntrada.builder().preguntaId("sst-1.1.2").valor("2").build()));

        ResultadoLectura resultado = calculoDelegate.calcular(actor(tenantA), diagA);

        // La lectura devuelve exactamente 9 pilares (uno por módulo del KB) y la
        // cabecera con score calculado. Las recomendaciones alta reflejan la brecha.
        assertThat(resultado.getPilares()).hasSize(9);
        assertThat(resultado.getScoreSgi()).isNotNull();

        // 9 pilares persistidos (uno por módulo del KB).
        assertThat(contar(tenantA, "resultado_pilar", diagA)).isEqualTo(9L);
        // Al menos una recomendación, una tarea y una ruta (de la brecha sst-1.1.1).
        long recos = contar(tenantA, "recomendacion", diagA);
        long tareas = contar(tenantA, "tarea_mejora", diagA);
        long rutas = contar(tenantA, "ruta_aprendizaje", diagA);
        assertThat(recos).isPositive();
        assertThat(tareas).isPositive();
        assertThat(rutas).isPositive();

        // Cabecera actualizada: estado 'calculado', numéricos y completado_en no nulos.
        tenantTemplate.runInTenant(tenantA, () -> {
            String estado = appJdbc.queryForObject(
                    "SELECT estado FROM siginex.diagnostico WHERE id = ?", String.class, diagA);
            assertThat(estado).isEqualTo("calculado");
            Integer completados = appJdbc.queryForObject(
                    "SELECT count(*) FROM siginex.diagnostico "
                            + "WHERE id = ? AND completado_en IS NOT NULL "
                            + "AND score_sgi IS NOT NULL AND nivel_sgi IS NOT NULL "
                            + "AND cumplimiento_global IS NOT NULL", Integer.class, diagA);
            assertThat(completados).isEqualTo(1);
            return null;
        });

        // Evento de auditoría 'diagnostico.calculado' con actor 'api'.
        Integer eventos = tenantTemplate.runInTenant(tenantA, () -> appJdbc.queryForObject(
                "SELECT count(*) FROM siginex.evento_auditoria "
                        + "WHERE entidad_id = ? AND accion = 'diagnostico.calculado' AND actor = 'api'",
                Integer.class, diagA.toString()));
        assertThat(eventos).isEqualTo(1);
    }

    @Test
    void recalcularEsIdempotente_noDuplicaFilas() {
        UUID tenantA = seedTenant("Tenant A");
        UUID diagA = insertDiagnostico(tenantA, insertOrganizacion(tenantA, "Org A"), "3");
        guardarRespuestas(tenantA, diagA, List.of(
                RespuestaEntrada.builder().preguntaId("sst-1.1.1").valor("0").build(),
                RespuestaEntrada.builder().preguntaId("sst-1.1.2").valor("1").build()));

        ResultadoLectura primero = calculoDelegate.calcular(actor(tenantA), diagA);
        long pilares1 = contar(tenantA, "resultado_pilar", diagA);
        long recos1 = contar(tenantA, "recomendacion", diagA);
        long tareas1 = contar(tenantA, "tarea_mejora", diagA);
        long rutas1 = contar(tenantA, "ruta_aprendizaje", diagA);

        ResultadoLectura segundo = calculoDelegate.calcular(actor(tenantA), diagA);

        // Mismos conteos tras recalcular: borrar+insertar no duplica.
        assertThat(contar(tenantA, "resultado_pilar", diagA)).isEqualTo(pilares1);
        assertThat(contar(tenantA, "recomendacion", diagA)).isEqualTo(recos1);
        assertThat(contar(tenantA, "tarea_mejora", diagA)).isEqualTo(tareas1);
        assertThat(contar(tenantA, "ruta_aprendizaje", diagA)).isEqualTo(rutas1);

        // Mismos valores globales (determinismo del motor), leídos de BD.
        assertThat(segundo.getScoreSgi()).isEqualByComparingTo(primero.getScoreSgi());
        assertThat(segundo.getRecomendaciones()).hasSameSizeAs(primero.getRecomendaciones());

        // Un único evento de auditoría por recálculo (2 ejecuciones -> 2 eventos).
        Integer eventos = tenantTemplate.runInTenant(tenantA, () -> appJdbc.queryForObject(
                "SELECT count(*) FROM siginex.evento_auditoria "
                        + "WHERE entidad_id = ? AND accion = 'diagnostico.calculado'",
                Integer.class, diagA.toString()));
        assertThat(eventos).isEqualTo(2);
    }

    @Test
    void calcularConTenantEquivocado_lanza404YNoDejaFilas() {
        UUID tenantA = seedTenant("Tenant A");
        UUID tenantB = seedTenant("Tenant B");
        UUID diagA = insertDiagnostico(tenantA, insertOrganizacion(tenantA, "Org A"), "2");
        guardarRespuestas(tenantA, diagA, List.of(
                RespuestaEntrada.builder().preguntaId("sst-1.1.1").valor("0").build()));

        // Calcular el diagnóstico de A con el actor del tenant B: el Delegate
        // verifica la pertenencia y, como la RLS esconde la cabecera de A para B,
        // lanza 404 ANTES de calcular o persistir. No se deja ninguna fila.
        assertThatThrownBy(() -> calculoDelegate.calcular(actor(tenantB), diagA))
                .isInstanceOf(DominioException.class)
                .satisfies(ex -> assertThat(((DominioException) ex).status()).isEqualTo(HttpStatus.NOT_FOUND));

        // No se escribió ningún resultado para A (el cálculo nunca llegó a persistir).
        assertThat(contar(tenantA, "resultado_pilar", diagA)).isZero();
        assertThat(contar(tenantA, "recomendacion", diagA)).isZero();

        // Y el cálculo legítimo de A sigue funcionando después del intento fallido.
        ResultadoLectura resultado = calculoDelegate.calcular(actor(tenantA), diagA);
        assertThat(resultado.getPilares()).hasSize(9);
        assertThat(contar(tenantA, "resultado_pilar", diagA)).isEqualTo(9L);
    }

    // --- helpers -----------------------------------------------------------

    private long contar(UUID tenantId, String tabla, UUID diagnosticoId) {
        return tenantTemplate.runInTenant(tenantId, () -> appJdbc.queryForObject(
                "SELECT count(*) FROM siginex." + tabla + " WHERE diagnostico_id = ?",
                Long.class, diagnosticoId));
    }

    private UUID seedTenant(String nombre) {
        UUID id = UUID.randomUUID();
        adminJdbc.update("INSERT INTO siginex.tenant (id, nombre, slug) VALUES (?, ?, ?)",
                id, nombre, "slug-" + id);
        return id;
    }

    private UUID insertOrganizacion(UUID tenantId, String nombre) {
        return tenantTemplate.runInTenant(tenantId, () ->
                appJdbc.queryForObject(
                        "INSERT INTO siginex.organizacion (tenant_id, nombre) VALUES (?, ?) RETURNING id",
                        UUID.class, tenantId, nombre));
    }

    private UUID insertDiagnostico(UUID tenantId, UUID organizacionId, String nivelTecnologico) {
        return tenantTemplate.runInTenant(tenantId, () ->
                appJdbc.queryForObject(
                        "INSERT INTO siginex.diagnostico (tenant_id, organizacion_id, kb_version, nivel_tecnologico) "
                                + "VALUES (?, ?, ?, CAST(? AS siginex.nivel_tecnologico)) RETURNING id",
                        UUID.class, tenantId, organizacionId, KB_VERSION, nivelTecnologico));
    }

    private void guardarRespuestas(UUID tenantId, UUID diagnosticoId, List<RespuestaEntrada> entradas) {
        tenantTemplate.runInTenant(tenantId, () -> {
            new JdbcRespuestaRepository(new NamedParameterJdbcTemplate(appDataSource))
                    .upsertLote(tenantId, diagnosticoId, entradas);
            return null;
        });
    }

    private static HikariDataSource buildDataSource(String username, String password) {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(POSTGRES.getJdbcUrl());
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        dataSource.setDriverClassName(POSTGRES.getDriverClassName());
        dataSource.setMaximumPoolSize(2);
        return dataSource;
    }
}
