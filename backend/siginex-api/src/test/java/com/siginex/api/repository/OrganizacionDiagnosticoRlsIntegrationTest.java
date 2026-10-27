package com.siginex.api.repository;

import com.siginex.api.config.TenantTransactionTemplate;
import com.siginex.api.domain.DiagnosticoDetalle;
import com.siginex.api.domain.DiagnosticoResumen;
import com.siginex.api.domain.Organizacion;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * End-to-end integration test that exercises the <strong>production</strong>
 * repositories ({@link JdbcOrganizacionRepository} and
 * {@link JdbcDiagnosticoRepository}) through the real
 * {@link TenantTransactionTemplate}, over a PostgreSQL container with the actual
 * Flyway migrations (V1 + V2 + V3). It proves that creating an organizacion and
 * a diagnostico in one tenant stays invisible to another tenant: PostgreSQL
 * Row-Level Security isolates both business tables end-to-end, from the
 * repository SQL down to the policy.
 *
 * <p>It mirrors {@link DiagnosticoRlsIntegrationTest} but, instead of raw
 * {@code appJdbc} statements, it drives the production repository classes so the
 * whole vertical slice (organizacion + diagnostico) is validated together.
 *
 * <h2>Why a non-privileged application role is mandatory (the superuser/BYPASSRLS trap)</h2>
 * PostgreSQL does <strong>not</strong> enforce RLS for a superuser or a
 * {@code BYPASSRLS} role. The default Testcontainers user is a superuser, so the
 * isolation assertions run through a dedicated {@code siginex_app} role created
 * {@code NOSUPERUSER NOBYPASSRLS} with only minimal grants. The privileged
 * connection is used solely for migrations, role/grant setup, seeding the
 * non-RLS {@code tenant} rows and the deterministic {@code TRUNCATE} between
 * tests ({@code kb_version} is global, seeded by V3, and never truncated).
 */
@Testcontainers
class OrganizacionDiagnosticoRlsIntegrationTest {

    private static final String APP_ROLE = "siginex_app";
    private static final String APP_PASSWORD = "siginex_app_pwd";

    @Container
    static final PostgreSQLContainer POSTGRES =
            new PostgreSQLContainer(DockerImageName.parse("postgres:17-alpine"));

    /** Non-privileged datasource (role {@code siginex_app}); all RLS assertions go through it. */
    private static HikariDataSource appDataSource;
    /** Real production helper wired on top of the app-role datasource. */
    private static TenantTransactionTemplate tenantTemplate;
    /** Production repositories under test, wired on the app-role datasource. */
    private static JdbcOrganizacionRepository organizacionRepository;
    private static JdbcDiagnosticoRepository diagnosticoRepository;
    /** Privileged datasource (superuser): migrations, role/grants, seeding, truncate. */
    private static HikariDataSource adminDataSource;
    private static JdbcTemplate adminJdbc;

    @BeforeAll
    static void setUpDatabase() {
        // 1) Privileged datasource (the Testcontainers default user is a superuser).
        adminDataSource = buildDataSource(POSTGRES.getUsername(), POSTGRES.getPassword());
        adminJdbc = new JdbcTemplate(adminDataSource);

        // 2) Apply the REAL project migrations (V1 + V2 + V3). Single source of truth.
        Flyway.configure()
                .dataSource(adminDataSource)
                .schemas("siginex")
                .locations("classpath:db/migration")
                .load()
                .migrate();

        // 3) Non-privileged application role (NOSUPERUSER + NOBYPASSRLS forces the
        //    policy to be evaluated). Minimal grants only.
        adminJdbc.execute("DROP ROLE IF EXISTS " + APP_ROLE);
        adminJdbc.execute("CREATE ROLE " + APP_ROLE
                + " LOGIN PASSWORD '" + APP_PASSWORD + "' NOSUPERUSER NOBYPASSRLS");
        adminJdbc.execute("GRANT USAGE ON SCHEMA siginex TO " + APP_ROLE);
        adminJdbc.execute("GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA siginex TO " + APP_ROLE);
        adminJdbc.execute("GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA siginex TO " + APP_ROLE);
        adminJdbc.execute("GRANT EXECUTE ON FUNCTION siginex.current_tenant() TO " + APP_ROLE);

        // 4) App-role datasource + the REAL production plumbing and repositories on
        //    top of it, so the assertions exercise the exact runtime path.
        appDataSource = buildDataSource(APP_ROLE, APP_PASSWORD);
        NamedParameterJdbcTemplate namedAppJdbc = new NamedParameterJdbcTemplate(appDataSource);
        TransactionTemplate transactionTemplate =
                new TransactionTemplate(new DataSourceTransactionManager(appDataSource));
        tenantTemplate = new TenantTransactionTemplate(transactionTemplate, namedAppJdbc);
        organizacionRepository = new JdbcOrganizacionRepository(namedAppJdbc);
        diagnosticoRepository = new JdbcDiagnosticoRepository(namedAppJdbc);
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
        // Deterministic, self-contained tests. kb_version is global and NOT truncated.
        adminJdbc.execute("TRUNCATE siginex.diagnostico, siginex.organizacion, siginex.tenant CASCADE");
    }

    @Test
    void tenantAVeSuOrganizacionYDiagnosticoYTenantBNoLosVe() {
        UUID tenantA = seedTenant("Tenant A");
        UUID tenantB = seedTenant("Tenant B");

        // Create an organizacion and a diagnostico in tenant A using the PRODUCTION
        // repositories, inside runInTenant(A, ...).
        Organizacion orgA = tenantTemplate.runInTenant(tenantA, () ->
                organizacionRepository.insertar(Organizacion.builder()
                        .tenantId(tenantA).nombre("Org A").sector("tecnologia").tamano("mediana").build()));
        String version = tenantTemplate.runInTenant(tenantA, () -> diagnosticoRepository.kbVersionVigente());
        DiagnosticoDetalle diagA = tenantTemplate.runInTenant(tenantA, () ->
                diagnosticoRepository.insertar(tenantA, orgA.getId(), version));

        assertThat(orgA.getId()).isNotNull();
        assertThat(diagA.getId()).isNotNull();
        assertThat(diagA.getEstado()).isEqualTo("en_progreso");
        assertThat(diagA.getOrganizacionNombre()).isEqualTo("Org A");

        // Tenant A sees what it created.
        List<Organizacion> orgsA = tenantTemplate.runInTenant(tenantA, () -> organizacionRepository.listar());
        List<DiagnosticoResumen> diagsA = tenantTemplate.runInTenant(tenantA, () -> diagnosticoRepository.listar());
        assertThat(orgsA).extracting(Organizacion::getId).containsExactly(orgA.getId());
        assertThat(diagsA).extracting(DiagnosticoResumen::getId).containsExactly(diagA.getId());

        // Tenant B sees nothing: RLS isolates both business tables end-to-end.
        List<Organizacion> orgsB = tenantTemplate.runInTenant(tenantB, () -> organizacionRepository.listar());
        List<DiagnosticoResumen> diagsB = tenantTemplate.runInTenant(tenantB, () -> diagnosticoRepository.listar());
        assertThat(orgsB).isEmpty();
        assertThat(diagsB).isEmpty();

        // Tenant B cannot fetch tenant A's diagnostico by id either (RLS hides it).
        assertThat(tenantTemplate.runInTenant(tenantB, () -> diagnosticoRepository.buscarPorId(diagA.getId())))
                .isEmpty();
    }

    // --- helpers -----------------------------------------------------------

    /** Seeds a tenant with the privileged connection ({@code tenant} has no RLS). */
    private UUID seedTenant(String nombre) {
        UUID id = UUID.randomUUID();
        adminJdbc.update("INSERT INTO siginex.tenant (id, nombre, slug) VALUES (?, ?, ?)",
                id, nombre, "slug-" + id);
        return id;
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
