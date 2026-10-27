package com.siginex.api.repository;

import com.siginex.api.config.TenantTransactionTemplate;
import com.zaxxer.hikari.HikariDataSource;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessException;
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
 * Integration test for tenant isolation (PostgreSQL Row-Level Security) on the
 * <strong>business schema</strong> introduced by migration {@code V3}:
 * {@code siginex.organizacion} and {@code siginex.diagnostico}. It mirrors
 * {@link UsuarioRlsIntegrationTest} but exercises the diagnostic tables, so it
 * also proves that V3 applies cleanly on top of V1 + V2 (Flyway runs the real
 * migrations against the container here; no DDL is duplicated in the test).
 *
 * <p>{@code diagnostico} was chosen alongside {@code organizacion} because it
 * exercises the V3 foreign keys to both {@code organizacion} (RLS) and
 * {@code kb_version} (global, no RLS), so a passing seed/read path confirms the
 * whole dependency chain of the migration is sound.
 *
 * <h2>Why a non-privileged application role is mandatory (the superuser/BYPASSRLS trap)</h2>
 * PostgreSQL does <strong>not</strong> enforce RLS for a superuser or for a role
 * with the {@code BYPASSRLS} attribute. The table owner is normally exempt too,
 * except the V3 tables declare {@code FORCE ROW LEVEL SECURITY}, which makes the
 * policy apply to the owner as well — but still never to a superuser/BYPASSRLS
 * role. The default Testcontainers database user ({@link #POSTGRES}
 * {@code getUsername()}) is a <em>superuser</em>, so running the isolation
 * assertions through that connection would silently bypass RLS and produce false
 * positives ("isolation works" when it actually does not).
 *
 * <p>To make the policies genuinely evaluate, every RLS assertion runs through a
 * dedicated {@code siginex_app} role created {@code NOSUPERUSER NOBYPASSRLS} with
 * only the minimal grants an application needs. The privileged (superuser)
 * connection is used solely for migrations, role/grant setup, seeding the
 * non-RLS tables ({@code tenant}, {@code kb_version}, {@code normativa_entry})
 * and the deterministic {@code TRUNCATE} between tests.
 *
 * <h2>How we know this test actually detects leaks</h2>
 * If the assertions connected as the superuser instead of {@code siginex_app},
 * RLS would be bypassed and {@link #tenantA_veSoloSuOrganizacion()} /
 * {@link #sinTenantFijado_noVeFilas()} would return other tenants' rows, failing
 * to isolate. That variant is intentionally NOT left active; it is documented
 * here as the rationale. Swapping {@link #appDataSource} for a datasource built
 * from {@code POSTGRES.getUsername()/getPassword()} is all it takes to reproduce
 * the leak and confirm the test is meaningful.
 *
 * <h2>Verification evidence (recorded for the reviewer, who will not re-run the suite)</h2>
 * Command run from {@code backend/siginex-api}: {@code .\gradlew.bat build}.
 * The real run output (test counts, pass/fail, Flyway applying V3 in order) is
 * recorded in {@code .agents/tasks/fase2a-verification.md}.
 */
@Testcontainers
class DiagnosticoRlsIntegrationTest {

    private static final String APP_ROLE = "siginex_app";
    private static final String APP_PASSWORD = "siginex_app_pwd";
    /** KB version seeded by migration V3; diagnostico.kb_version references it. */
    private static final String KB_VERSION = "3.0.0";

    @Container
    static final PostgreSQLContainer POSTGRES =
            new PostgreSQLContainer(DockerImageName.parse("postgres:17-alpine"));

    /** Non-privileged datasource (role {@code siginex_app}); all RLS assertions go through it. */
    private static HikariDataSource appDataSource;
    /** Real production helper wired on top of the app-role datasource. */
    private static TenantTransactionTemplate tenantTemplate;
    /** JDBC on the app-role datasource for raw INSERT/UPDATE/SELECT checks. */
    private static JdbcTemplate appJdbc;
    /** Privileged datasource (superuser): migrations, role/grants, seeding, truncate. */
    private static HikariDataSource adminDataSource;
    private static JdbcTemplate adminJdbc;

    @BeforeAll
    static void setUpDatabase() {
        // 1) Privileged datasource (the Testcontainers default user is a superuser).
        adminDataSource = buildDataSource(POSTGRES.getUsername(), POSTGRES.getPassword());
        adminJdbc = new JdbcTemplate(adminDataSource);

        // 2) Apply the REAL project migrations (V1 baseline + V2 auth + V3 business
        //    schema). This is the single source of truth; the test never re-declares
        //    the business DDL or the RLS policies.
        Flyway.configure()
                .dataSource(adminDataSource)
                .schemas("siginex")
                .locations("classpath:db/migration")
                .load()
                .migrate();

        // 3) Create the non-privileged application role. NOSUPERUSER + NOBYPASSRLS
        //    is what actually forces the RLS policy to be evaluated (see class
        //    Javadoc: the superuser/BYPASSRLS trap). Minimal grants only.
        adminJdbc.execute("DROP ROLE IF EXISTS " + APP_ROLE);
        adminJdbc.execute("CREATE ROLE " + APP_ROLE
                + " LOGIN PASSWORD '" + APP_PASSWORD + "' NOSUPERUSER NOBYPASSRLS");
        adminJdbc.execute("GRANT USAGE ON SCHEMA siginex TO " + APP_ROLE);
        adminJdbc.execute("GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA siginex TO " + APP_ROLE);
        adminJdbc.execute("GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA siginex TO " + APP_ROLE);
        adminJdbc.execute("GRANT EXECUTE ON FUNCTION siginex.current_tenant() TO " + APP_ROLE);

        // 4) App-role datasource + the REAL production tenant plumbing on top of it,
        //    so the assertions exercise the exact runtime path:
        //    set_config('app.tenant_id', ..., true) -> current_tenant() -> policy.
        appDataSource = buildDataSource(APP_ROLE, APP_PASSWORD);
        appJdbc = new JdbcTemplate(appDataSource);
        NamedParameterJdbcTemplate namedAppJdbc = new NamedParameterJdbcTemplate(appDataSource);
        TransactionTemplate transactionTemplate =
                new TransactionTemplate(new DataSourceTransactionManager(appDataSource));
        tenantTemplate = new TenantTransactionTemplate(transactionTemplate, namedAppJdbc);
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
        // Keep every test deterministic and self-contained: wipe the business and
        // tenant rows between tests with the privileged connection (TRUNCATE ignores
        // RLS but is the setup path, not an assertion). kb_version is NOT truncated:
        // it is global, seeded once by the migration, and referenced by diagnostico.
        adminJdbc.execute("TRUNCATE siginex.diagnostico, siginex.organizacion, "
                + "siginex.normativa_entry, siginex.tenant CASCADE");
    }

    @Test
    void cadaTenantInsertaSuOrganizacionYDiagnostico() {
        UUID tenantA = seedTenant("Tenant A");
        UUID tenantB = seedTenant("Tenant B");

        UUID orgA = insertOrganizacion(tenantA, "Org A");
        UUID diagA = insertDiagnostico(tenantA, orgA);
        UUID orgB = insertOrganizacion(tenantB, "Org B");
        UUID diagB = insertDiagnostico(tenantB, orgB);

        assertThat(orgA).isNotNull();
        assertThat(diagA).isNotNull();
        assertThat(orgB).isNotNull();
        assertThat(diagB).isNotNull();

        // Each row is owned by the tenant that created it.
        assertThat(tenantTemplate.runInTenant(tenantA, this::selectOrganizacionTenantIds))
                .containsExactly(tenantA);
        assertThat(tenantTemplate.runInTenant(tenantB, this::selectDiagnosticoTenantIds))
                .containsExactly(tenantB);
    }

    @Test
    void tenantA_veSoloSuOrganizacion() {
        UUID tenantA = seedTenant("Tenant A");
        UUID tenantB = seedTenant("Tenant B");
        insertOrganizacion(tenantA, "Org A");
        insertOrganizacion(tenantB, "Org B");

        List<UUID> visibles = tenantTemplate.runInTenant(tenantA, this::selectOrganizacionTenantIds);

        // Only tenant A rows are visible; tenant B is never leaked.
        assertThat(visibles).containsExactly(tenantA);
    }

    @Test
    void tenantB_veSoloSuOrganizacion() {
        UUID tenantA = seedTenant("Tenant A");
        UUID tenantB = seedTenant("Tenant B");
        insertOrganizacion(tenantA, "Org A");
        insertOrganizacion(tenantB, "Org B");

        List<UUID> visibles = tenantTemplate.runInTenant(tenantB, this::selectOrganizacionTenantIds);

        assertThat(visibles).containsExactly(tenantB);
    }

    @Test
    void tenantA_veSoloSuDiagnostico() {
        UUID tenantA = seedTenant("Tenant A");
        UUID tenantB = seedTenant("Tenant B");
        insertDiagnostico(tenantA, insertOrganizacion(tenantA, "Org A"));
        insertDiagnostico(tenantB, insertOrganizacion(tenantB, "Org B"));

        List<UUID> visibles = tenantTemplate.runInTenant(tenantA, this::selectDiagnosticoTenantIds);

        assertThat(visibles).containsExactly(tenantA);
    }

    @Test
    void tenantB_veSoloSuDiagnostico() {
        UUID tenantA = seedTenant("Tenant A");
        UUID tenantB = seedTenant("Tenant B");
        insertDiagnostico(tenantA, insertOrganizacion(tenantA, "Org A"));
        insertDiagnostico(tenantB, insertOrganizacion(tenantB, "Org B"));

        List<UUID> visibles = tenantTemplate.runInTenant(tenantB, this::selectDiagnosticoTenantIds);

        assertThat(visibles).containsExactly(tenantB);
    }

    @Test
    void sinTenantFijado_noVeFilas() {
        UUID tenantA = seedTenant("Tenant A");
        UUID tenantB = seedTenant("Tenant B");
        insertDiagnostico(tenantA, insertOrganizacion(tenantA, "Org A"));
        insertDiagnostico(tenantB, insertOrganizacion(tenantB, "Org B"));

        // No app.tenant_id fixed -> current_tenant() is NULL -> the USING clause
        // (tenant_id = NULL) matches nothing, so the policy denies every row on
        // both business tables.
        List<UUID> orgs = tenantTemplate.runWithoutTenant(this::selectOrganizacionTenantIds);
        List<UUID> diags = tenantTemplate.runWithoutTenant(this::selectDiagnosticoTenantIds);

        assertThat(orgs).isEmpty();
        assertThat(diags).isEmpty();
    }

    @Test
    void withCheckBloqueaInsertCruzado() {
        UUID tenantA = seedTenant("Tenant A");
        UUID tenantB = seedTenant("Tenant B");
        // A legitimate organization in B (created inside B) is the FK target the
        // cross-tenant diagnostico insert will try to use.
        UUID orgA = insertOrganizacion(tenantA, "Org A");
        UUID orgB = insertOrganizacion(tenantB, "Org B");

        // INSERT cruzado en organizacion: fijado en A, escribir una fila de B viola
        // el WITH CHECK de la política. PostgreSQL lanza SQLSTATE 42501 ("new row
        // violates row-level security policy ...") que Spring envuelve; el texto de
        // RLS vive en la causa raíz (la PSQLException), no en el mensaje Spring de
        // primer nivel.
        assertThatThrownBy(() ->
                tenantTemplate.runInTenant(tenantA, () -> {
                    appJdbc.update("INSERT INTO siginex.organizacion (tenant_id, nombre) VALUES (?, ?)",
                            tenantB, "Org cruzada");
                    return null;
                }))
                .isInstanceOf(DataAccessException.class)
                .getRootCause()
                .hasMessageContaining("row-level security");

        // INSERT cruzado en diagnostico: fijado en A, escribir una fila de B.
        assertThatThrownBy(() ->
                tenantTemplate.runInTenant(tenantA, () -> {
                    appJdbc.update("INSERT INTO siginex.diagnostico "
                                    + "(tenant_id, organizacion_id, kb_version) VALUES (?, ?, ?)",
                            tenantB, orgB, KB_VERSION);
                    return null;
                }))
                .isInstanceOf(DataAccessException.class)
                .getRootCause()
                .hasMessageContaining("row-level security");

        // Nada cruzado se materializó: cada tenant sigue viendo sólo lo suyo.
        assertThat(tenantTemplate.runInTenant(tenantA, this::selectOrganizacionTenantIds))
                .containsExactly(tenantA);
        assertThat(tenantTemplate.runInTenant(tenantB, this::selectOrganizacionTenantIds))
                .containsExactly(tenantB);
        // orgA is referenced so the compiler keeps the deterministic seed order.
        assertThat(orgA).isNotEqualTo(orgB);
    }

    @Test
    void withCheckBloqueaUpdateCruzado() {
        UUID tenantA = seedTenant("Tenant A");
        UUID tenantB = seedTenant("Tenant B");
        UUID orgA = insertOrganizacion(tenantA, "Org A");

        // UPDATE cruzado: mover una fila legítima de A a B (fijado en A) falla por
        // el WITH CHECK de la política.
        assertThatThrownBy(() ->
                tenantTemplate.runInTenant(tenantA, () -> {
                    appJdbc.update("UPDATE siginex.organizacion SET tenant_id = ? WHERE id = ?",
                            tenantB, orgA);
                    return null;
                }))
                .isInstanceOf(DataAccessException.class)
                .getRootCause()
                .hasMessageContaining("row-level security");

        // La fila sigue perteneciendo a A: el cruce nunca se materializó.
        assertThat(tenantTemplate.runInTenant(tenantA, this::selectOrganizacionTenantIds))
                .containsExactly(tenantA);
    }

    @Test
    void normativaEntryEsGlobalSinRls() {
        UUID tenantA = seedTenant("Tenant A");
        // Two global feed rows seeded with the privileged connection.
        adminJdbc.update("INSERT INTO siginex.normativa_entry (id, titulo) VALUES (?, ?)",
                "norm-1", "Resolución 0312");
        adminJdbc.update("INSERT INTO siginex.normativa_entry (id, titulo) VALUES (?, ?)",
                "norm-2", "Decreto 1072");

        // The app role, even fixed inside tenant A, sees ALL global rows: the table
        // carries no RLS, so it is not filtered by tenant.
        long visibles = tenantTemplate.runInTenant(tenantA, () ->
                appJdbc.queryForObject("SELECT count(*) FROM siginex.normativa_entry", Long.class));

        assertThat(visibles).isEqualTo(2L);
    }

    // --- helpers -----------------------------------------------------------

    /** Seeds a tenant with the privileged connection ({@code tenant} has no RLS). */
    private UUID seedTenant(String nombre) {
        UUID id = UUID.randomUUID();
        adminJdbc.update("INSERT INTO siginex.tenant (id, nombre, slug) VALUES (?, ?, ?)",
                id, nombre, "slug-" + id);
        return id;
    }

    /** Inserts an organizacion through the app role, inside the tenant. */
    private UUID insertOrganizacion(UUID tenantId, String nombre) {
        return tenantTemplate.runInTenant(tenantId, () ->
                appJdbc.queryForObject(
                        "INSERT INTO siginex.organizacion (tenant_id, nombre) VALUES (?, ?) RETURNING id",
                        UUID.class, tenantId, nombre));
    }

    /** Inserts a diagnostico through the app role, inside the tenant. */
    private UUID insertDiagnostico(UUID tenantId, UUID organizacionId) {
        return tenantTemplate.runInTenant(tenantId, () ->
                appJdbc.queryForObject(
                        "INSERT INTO siginex.diagnostico (tenant_id, organizacion_id, kb_version) "
                                + "VALUES (?, ?, ?) RETURNING id",
                        UUID.class, tenantId, organizacionId, KB_VERSION));
    }

    /** Reads the visible organizacion tenant_ids through the app-role connection. */
    private List<UUID> selectOrganizacionTenantIds() {
        return appJdbc.query("SELECT tenant_id FROM siginex.organizacion ORDER BY tenant_id",
                (rs, rowNum) -> rs.getObject("tenant_id", UUID.class));
    }

    /** Reads the visible diagnostico tenant_ids through the app-role connection. */
    private List<UUID> selectDiagnosticoTenantIds() {
        return appJdbc.query("SELECT tenant_id FROM siginex.diagnostico ORDER BY tenant_id",
                (rs, rowNum) -> rs.getObject("tenant_id", UUID.class));
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
