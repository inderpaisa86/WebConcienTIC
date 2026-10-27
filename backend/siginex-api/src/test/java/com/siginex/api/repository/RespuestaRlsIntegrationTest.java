package com.siginex.api.repository;

import com.siginex.api.config.TenantTransactionTemplate;
import com.siginex.api.domain.RespuestaEntrada;
import com.zaxxer.hikari.HikariDataSource;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
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
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for tenant isolation (PostgreSQL Row-Level Security) on the
 * {@code siginex.respuesta} table, exercising the PRODUCTION
 * {@link JdbcRespuestaRepository} on top of the real
 * {@link TenantTransactionTemplate}. Runs the REAL Flyway migrations (V1..V4)
 * against a throwaway {@code postgres:17-alpine} container.
 *
 * <p>Asserts:
 * <ul>
 *   <li>(c) answers saved in tenant A's diagnostico are NOT visible to tenant B
 *       (RLS on {@code respuesta});</li>
 *   <li>(d) the upsert is idempotent — saving the same {@code preguntaId} twice
 *       updates the value in place (1 row, new value), it never duplicates.</li>
 * </ul>
 *
 * <p>As in {@link DiagnosticoRlsIntegrationTest}, every RLS assertion runs
 * through the non-privileged {@code siginex_app} role
 * ({@code NOSUPERUSER NOBYPASSRLS}); the superuser connection is used only for
 * migrations, role/grants, seeding the non-RLS rows and the deterministic
 * {@code TRUNCATE} between tests. Verification evidence is recorded in
 * {@code .agents/tasks/fase2c/verificacion.md}.
 */
@Testcontainers
class RespuestaRlsIntegrationTest {

    private static final String APP_ROLE = "siginex_app";
    private static final String APP_PASSWORD = "siginex_app_pwd";
    private static final String KB_VERSION = "3.0.0";

    @Container
    static final PostgreSQLContainer POSTGRES =
            new PostgreSQLContainer(DockerImageName.parse("postgres:17-alpine"));

    private static HikariDataSource appDataSource;
    private static TenantTransactionTemplate tenantTemplate;
    private static JdbcTemplate appJdbc;
    private static RespuestaRepository respuestaRepository;
    private static HikariDataSource adminDataSource;
    private static JdbcTemplate adminJdbc;

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

        appDataSource = buildDataSource(APP_ROLE, APP_PASSWORD);
        appJdbc = new JdbcTemplate(appDataSource);
        NamedParameterJdbcTemplate namedAppJdbc = new NamedParameterJdbcTemplate(appDataSource);
        TransactionTemplate transactionTemplate =
                new TransactionTemplate(new DataSourceTransactionManager(appDataSource));
        tenantTemplate = new TenantTransactionTemplate(transactionTemplate, namedAppJdbc);
        respuestaRepository = new JdbcRespuestaRepository(namedAppJdbc);
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

    @AfterEach
    void cleanRows() {
        adminJdbc.execute("TRUNCATE siginex.respuesta, siginex.diagnostico, siginex.organizacion, "
                + "siginex.tenant CASCADE");
    }

    @Test
    void respuestasDeA_noSonVisiblesParaB() {
        UUID tenantA = seedTenant("Tenant A");
        UUID tenantB = seedTenant("Tenant B");
        UUID diagA = insertDiagnostico(tenantA, insertOrganizacion(tenantA, "Org A"));

        // Save two answers in A's diagnostico through the production repository.
        List<RespuestaEntrada> lote = List.of(
                RespuestaEntrada.builder().preguntaId("sst-1.1.1").valor("2").build(),
                RespuestaEntrada.builder().preguntaId("ambiental-2.3").valor("na").build());
        tenantTemplate.runInTenant(tenantA, () -> {
            respuestaRepository.upsertLote(tenantA, diagA, lote);
            return null;
        });

        // A sees its two answers.
        Map<String, String> vistoPorA =
                tenantTemplate.runInTenant(tenantA, () -> respuestaRepository.listarPorDiagnostico(diagA));
        assertThat(vistoPorA).containsOnly(
                Map.entry("sst-1.1.1", "2"),
                Map.entry("ambiental-2.3", "na"));

        // B, fixed in its own tenant, sees nothing for A's diagnostico: RLS hides
        // the rows. skillId derivation is also exercised by the upsert above.
        Map<String, String> vistoPorB =
                tenantTemplate.runInTenant(tenantB, () -> respuestaRepository.listarPorDiagnostico(diagA));
        assertThat(vistoPorB).isEmpty();

        // Cross-check at the raw level: no respuesta row is visible to B at all.
        Long filasB = tenantTemplate.runInTenant(tenantB, () ->
                appJdbc.queryForObject("SELECT count(*) FROM siginex.respuesta", Long.class));
        assertThat(filasB).isZero();
    }

    @Test
    void upsertEsIdempotente_actualizaNoDuplica() {
        UUID tenantA = seedTenant("Tenant A");
        UUID diagA = insertDiagnostico(tenantA, insertOrganizacion(tenantA, "Org A"));

        // First save: value 1.
        tenantTemplate.runInTenant(tenantA, () -> {
            respuestaRepository.upsertLote(tenantA, diagA,
                    List.of(RespuestaEntrada.builder().preguntaId("sst-1.1.1").valor("1").build()));
            return null;
        });
        // Second save of the SAME preguntaId with a different value: updates in place.
        tenantTemplate.runInTenant(tenantA, () -> {
            respuestaRepository.upsertLote(tenantA, diagA,
                    List.of(RespuestaEntrada.builder().preguntaId("sst-1.1.1").valor("2").build()));
            return null;
        });

        // Exactly one row, with the latest value.
        Long filas = tenantTemplate.runInTenant(tenantA, () ->
                appJdbc.queryForObject(
                        "SELECT count(*) FROM siginex.respuesta WHERE diagnostico_id = ? AND pregunta_id = ?",
                        Long.class, diagA, "sst-1.1.1"));
        assertThat(filas).isEqualTo(1L);

        Map<String, String> vistoPorA =
                tenantTemplate.runInTenant(tenantA, () -> respuestaRepository.listarPorDiagnostico(diagA));
        assertThat(vistoPorA).containsExactly(Map.entry("sst-1.1.1", "2"));

        // skillId was derived from the preguntaId and persisted.
        String skillId = tenantTemplate.runInTenant(tenantA, () ->
                appJdbc.queryForObject(
                        "SELECT skill_id FROM siginex.respuesta WHERE diagnostico_id = ? AND pregunta_id = ?",
                        String.class, diagA, "sst-1.1.1"));
        assertThat(skillId).isEqualTo("sst");
    }

    // --- helpers -----------------------------------------------------------

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

    private UUID insertDiagnostico(UUID tenantId, UUID organizacionId) {
        return tenantTemplate.runInTenant(tenantId, () ->
                appJdbc.queryForObject(
                        "INSERT INTO siginex.diagnostico (tenant_id, organizacion_id, kb_version) "
                                + "VALUES (?, ?, ?) RETURNING id",
                        UUID.class, tenantId, organizacionId, KB_VERSION));
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
