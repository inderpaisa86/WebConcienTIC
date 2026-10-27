package com.siginex.api.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.siginex.api.config.TenantTransactionTemplate;
import com.siginex.api.domain.RespuestaEntrada;
import com.siginex.api.domain.calculo.Benchmark;
import com.siginex.api.domain.calculo.ResultadoLectura;
import com.siginex.api.domain.calculo.TareaPlanLectura;
import com.siginex.api.service.AuditoriaService;
import com.siginex.api.service.AuditoriaServiceImpl;
import com.siginex.api.delegate.CalculoDelegate;
import com.siginex.api.delegate.CalculoDelegateImpl;
import com.siginex.api.domain.SiginexPrincipal;
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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for the READ side of the calculation orchestration
 * (FEAT-003): {@link CalculoServiceImpl#obtenerResultado},
 * {@link CalculoServiceImpl#obtenerPlanMejora},
 * {@link CalculoServiceImpl#registrarBenchmark} and
 * {@link CalculoServiceImpl#obtenerBenchmark}. Exercises the PRODUCTION
 * repositories on top of the real {@link TenantTransactionTemplate} and
 * {@link MotorCalculo}, running the REAL Flyway migrations (V1..V4) and the
 * production {@link KbSeedService} against a throwaway {@code postgres:17-alpine}
 * container.
 *
 * <p>Like {@link CalculoRlsIntegrationTest}, every tenant-scoped assertion runs
 * through the non-privileged {@code siginex_app} role
 * ({@code NOSUPERUSER NOBYPASSRLS}); the superuser connection is used only for
 * migrations, role/grants, KB seeding and the deterministic {@code TRUNCATE}
 * between tests.
 *
 * <p>Asserts (diseño §9.8 / §6):
 * <ul>
 *   <li>{@code obtenerResultado} returns the 9 pillars in the canonical module
 *       order ({@code sst, ambiental, ..., gobernanza_ia}) and the high-priority
 *       recommendations ordered by {@code (skill_id, pregunta_id)};</li>
 *   <li>{@code obtenerPlanMejora} orders by priority ({@code alta} first) and
 *       {@code skill_id};</li>
 *   <li>{@code registrarBenchmark(2)} persists {@code nivel_tecnologico=2} and
 *       returns the market benchmark (por_encima 7, promedio 3.6, rango [2,5],
 *       brecha 3); {@code obtenerBenchmark} then returns that benchmark, and
 *       {@link Optional#empty()} when no level has been declared.</li>
 * </ul>
 */
@Testcontainers
class CalculoLecturaIntegrationTest {

    private static final String APP_ROLE = "siginex_app";
    private static final String APP_PASSWORD = "siginex_app_pwd";
    private static final String KB_VERSION = "3.0.0";

    private static final List<String> ORDEN_CANONICO = List.of(
            "sst", "ambiental", "calidad", "riesgos", "cumplimiento",
            "datos", "gobierno", "esg", "gobernanza_ia");

    @Container
    static final PostgreSQLContainer POSTGRES =
            new PostgreSQLContainer(DockerImageName.parse("postgres:17-alpine"));

    private static HikariDataSource appDataSource;
    private static HikariDataSource adminDataSource;
    private static JdbcTemplate adminJdbc;
    private static JdbcTemplate appJdbc;
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
        adminJdbc.execute("TRUNCATE siginex.resultado_pilar, siginex.recomendacion, "
                + "siginex.tarea_mejora, siginex.ruta_aprendizaje, siginex.evento_auditoria, "
                + "siginex.respuesta, siginex.diagnostico, siginex.organizacion, siginex.tenant CASCADE");
    }

    @Test
    void obtenerResultadoDevuelvePilaresYRecomendacionesOrdenados() {
        UUID tenantA = seedTenant("Tenant A");
        UUID diagA = insertDiagnostico(tenantA, insertOrganizacion(tenantA, "Org A"), "2");
        // Dos brechas en módulos distintos (sst y datos) para ejercitar el orden
        // de pilares y de recomendaciones alta.
        guardarRespuestas(tenantA, diagA, List.of(
                RespuestaEntrada.builder().preguntaId("sst-1.1.1").valor("0").build(),
                RespuestaEntrada.builder().preguntaId("dat-001").valor("0").build()));
        calculoDelegate.calcular(actor(tenantA), diagA);

        ResultadoLectura resultado = calculoDelegate.obtenerResultado(actor(tenantA), diagA);

        // Cabecera leída de BD.
        assertThat(resultado.getOrganizacionNombre()).isEqualTo("Org A");
        assertThat(resultado.getScoreSgi()).isNotNull();
        assertThat(resultado.getNivelSgi()).isNotNull();
        assertThat(resultado.getCumplimientoGlobal()).isNotNull();

        // 9 pilares en el orden canónico de módulo (km.orden).
        List<String> skillIds = resultado.getPilares().stream()
                .map(ResultadoLectura.PilarLectura::getSkillId)
                .toList();
        assertThat(skillIds).containsExactlyElementsOf(ORDEN_CANONICO);

        // Recomendaciones alta: todas prioridad 'alta' y ordenadas por (skill_id, pregunta_id).
        List<ResultadoLectura.RecomendacionLectura> recos = resultado.getRecomendaciones();
        assertThat(recos).isNotEmpty();
        assertThat(recos).allMatch(r -> "alta".equals(r.getPrioridad()));
        List<String> skillOrder = recos.stream()
                .map(ResultadoLectura.RecomendacionLectura::getSkillId)
                .toList();
        assertThat(skillOrder).isSorted();
    }

    @Test
    void obtenerPlanMejoraOrdenaPorPrioridadYSkillId() {
        UUID tenantA = seedTenant("Tenant A");
        UUID diagA = insertDiagnostico(tenantA, insertOrganizacion(tenantA, "Org A"), "2");
        guardarRespuestas(tenantA, diagA, List.of(
                RespuestaEntrada.builder().preguntaId("sst-1.1.1").valor("0").build(),
                RespuestaEntrada.builder().preguntaId("dat-001").valor("0").build()));
        calculoDelegate.calcular(actor(tenantA), diagA);

        List<TareaPlanLectura> plan = calculoDelegate.obtenerPlanMejora(actor(tenantA), diagA);

        assertThat(plan).isNotEmpty();
        // Orden estable por (prioridad, skill_id): el enum prioridad_reco ordena
        // alta < media < baja, así que las 'alta' van primero y, dentro de cada
        // prioridad, por skill_id ascendente.
        for (int i = 1; i < plan.size(); i++) {
            TareaPlanLectura prev = plan.get(i - 1);
            TareaPlanLectura cur = plan.get(i);
            int p = prioridadRank(prev.getPrioridad());
            int c = prioridadRank(cur.getPrioridad());
            assertThat(p).isLessThanOrEqualTo(c);
            if (p == c) {
                assertThat(prev.getSkillId().compareTo(cur.getSkillId())).isLessThanOrEqualTo(0);
            }
        }
        // Las tareas traen la brecha (NOT NULL) y una acción.
        assertThat(plan).allSatisfy(t -> {
            assertThat(t.getBrecha()).isNotBlank();
            assertThat(t.getAccion()).isNotNull();
        });
    }

    @Test
    void registrarYObtenerBenchmark() {
        UUID tenantA = seedTenant("Tenant A");
        // Diagnóstico SIN nivel tecnológico: obtenerBenchmark -> empty.
        UUID diagA = insertDiagnosticoSinNivel(tenantA, insertOrganizacion(tenantA, "Org A"));

        assertThat(calculoDelegate.obtenerBenchmark(actor(tenantA), diagA)).isEmpty();

        // Registrar nivel 2: persiste y devuelve el benchmark correcto.
        Benchmark benchmark = calculoDelegate.registrarBenchmark(actor(tenantA), diagA, 2);
        assertThat(benchmark.getNivelEmpresa()).isEqualTo(2);
        assertThat(benchmark.getHerramientasPorEncima()).isEqualTo(7);
        assertThat(benchmark.getPromedioMercado()).isEqualTo(3.6);
        assertThat(benchmark.getRangoMercado()).containsExactly(2, 5);
        assertThat(benchmark.getBrechaAlLider()).isEqualTo(3);

        // nivel_tecnologico persistido = 2 (lectura por el superusuario, BYPASSRLS).
        String nivelPersistido = adminJdbc.queryForObject(
                "SELECT nivel_tecnologico::text FROM siginex.diagnostico WHERE id = ?",
                String.class, diagA);
        assertThat(nivelPersistido).isEqualTo("2");

        // obtenerBenchmark tras registrar devuelve el mismo benchmark.
        Optional<Benchmark> leido = calculoDelegate.obtenerBenchmark(actor(tenantA), diagA);
        assertThat(leido).isPresent();
        assertThat(leido.get().getNivelEmpresa()).isEqualTo(2);
        assertThat(leido.get().getHerramientasPorEncima()).isEqualTo(7);
        assertThat(leido.get().getBrechaAlLider()).isEqualTo(3);
    }

    // --- helpers -----------------------------------------------------------

    private static int prioridadRank(String prioridad) {
        return switch (prioridad) {
            case "alta" -> 0;
            case "media" -> 1;
            default -> 2;
        };
    }

    private UUID seedTenant(String nombre) {
        UUID id = UUID.randomUUID();
        adminJdbc.update("INSERT INTO siginex.tenant (id, nombre, slug) VALUES (?, ?, ?)",
                id, nombre, "slug-" + id);
        return id;
    }

    private UUID insertOrganizacion(UUID tenantId, String nombre) {
        return tenantTemplate.runInTenant(tenantId, () -> appJdbc.queryForObject(
                "INSERT INTO siginex.organizacion (tenant_id, nombre) VALUES (?, ?) RETURNING id",
                UUID.class, tenantId, nombre));
    }

    private UUID insertDiagnostico(UUID tenantId, UUID organizacionId, String nivelTecnologico) {
        return tenantTemplate.runInTenant(tenantId, () -> appJdbc.queryForObject(
                "INSERT INTO siginex.diagnostico (tenant_id, organizacion_id, kb_version, nivel_tecnologico) "
                        + "VALUES (?, ?, ?, CAST(? AS siginex.nivel_tecnologico)) RETURNING id",
                UUID.class, tenantId, organizacionId, KB_VERSION, nivelTecnologico));
    }

    private UUID insertDiagnosticoSinNivel(UUID tenantId, UUID organizacionId) {
        return tenantTemplate.runInTenant(tenantId, () -> appJdbc.queryForObject(
                "INSERT INTO siginex.diagnostico (tenant_id, organizacion_id, kb_version) "
                        + "VALUES (?, ?, ?) RETURNING id",
                UUID.class, tenantId, organizacionId, KB_VERSION));
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
