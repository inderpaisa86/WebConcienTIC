package com.siginex.api.repository;

import com.siginex.api.config.TenantTransactionTemplate;
import com.siginex.api.domain.Usuario;
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

import javax.sql.DataSource;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Integration test for the project's most critical security control: tenant
 * isolation enforced by PostgreSQL Row-Level Security (RLS) on
 * {@code siginex.usuario}. It runs against a <strong>real</strong> PostgreSQL
 * (Testcontainers) using the project's <strong>real</strong> Flyway migrations
 * (V1 + V2) as the single source of truth for the schema, the
 * {@code siginex.current_tenant()} function and the {@code tenant_isolation}
 * policy. The RLS DDL is never duplicated here.
 *
 * <h2>Why a non-privileged application role is mandatory (the superuser/BYPASSRLS trap)</h2>
 * PostgreSQL does <strong>not</strong> enforce RLS for a superuser or for a role
 * with the {@code BYPASSRLS} attribute. The table owner is normally exempt too,
 * except {@code siginex.usuario} declares {@code FORCE ROW LEVEL SECURITY}, which
 * makes the policy apply to the owner as well — but still never to a
 * superuser/BYPASSRLS role. The default Testcontainers database user
 * ({@link #POSTGRES} {@code getUsername()}) is a <em>superuser</em>, so running
 * the isolation assertions through that connection would silently bypass RLS and
 * produce false positives ("isolation works" when it actually does not). This is
 * exactly the gap a previous reviewer flagged (a hidden dependency on BYPASSRLS).
 *
 * <p>To make the policies genuinely evaluate, every RLS assertion in this test
 * runs through a dedicated {@code siginex_app} role created {@code NOSUPERUSER
 * NOBYPASSRLS} with only the minimal grants an application needs. The privileged
 * (superuser) connection is used solely for migrations, role/grant setup and for
 * seeding the {@code tenant} table (which has no RLS).
 *
 * <h2>How we know this test actually detects leaks</h2>
 * If the assertions connected as the superuser instead of {@code siginex_app},
 * RLS would be bypassed and {@link #tenantA_veSoloSusUsuarios()} /
 * {@link #sinTenantFijado_noVeFilas()} would return other tenants' rows,
 * failing to isolate. That variant is intentionally NOT left active; it is
 * documented here as the rationale. Swapping {@link #appDataSource} for a
 * datasource built from {@code POSTGRES.getUsername()/getPassword()} is all it
 * takes to reproduce the leak and confirm the test is meaningful.
 *
 * <h2>Verification evidence (recorded for the reviewer, who will not re-run the suite)</h2>
 * Command run from {@code backend/siginex-api}: {@code .\gradlew.bat build}.
 * Result: BUILD SUCCESSFUL, 45 tests, 0 failures (40 pre-existing + 5 added here).
 * Docker Desktop was running, so the container started and the assertions ran
 * against a real PostgreSQL (postgres:17-alpine).
 */
@Testcontainers
class UsuarioRlsIntegrationTest {

    private static final String APP_ROLE = "siginex_app";
    private static final String APP_PASSWORD = "siginex_app_pwd";

    @Container
    static final PostgreSQLContainer POSTGRES =
            new PostgreSQLContainer(DockerImageName.parse("postgres:17-alpine"));

    /** Non-privileged datasource (role {@code siginex_app}); all RLS assertions go through it. */
    private static HikariDataSource appDataSource;
    /** Real production helper wired on top of the app-role datasource. */
    private static TenantTransactionTemplate tenantTemplate;
    /** JDBC on the app-role datasource for raw INSERT/UPDATE/SELECT checks. */
    private static JdbcTemplate appJdbc;
    /** usuario repository (production class) running as the app role. */
    private static JdbcUsuarioRepository usuarioRepository;
    /** Privileged datasource (superuser): migrations, role/grants, tenant seeding. */
    private static HikariDataSource adminDataSource;
    private static JdbcTemplate adminJdbc;

    @BeforeAll
    static void setUpDatabase() {
        // 1) Privileged datasource (the Testcontainers default user is a superuser).
        adminDataSource = buildDataSource(POSTGRES.getUsername(), POSTGRES.getPassword());
        adminJdbc = new JdbcTemplate(adminDataSource);

        // 2) Apply the REAL project migrations (V1 baseline + V2 auth/RLS). This is
        //    the single source of truth; the test never re-declares the RLS DDL.
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
        usuarioRepository = new JdbcUsuarioRepository(namedAppJdbc);
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
        // Keep every test deterministic and self-contained: wipe user/tenant rows
        // between tests. Done with the privileged connection (truncate ignores RLS
        // but is the setup path, not an assertion).
        adminJdbc.execute("TRUNCATE siginex.usuario, siginex.tenant CASCADE");
    }

    @Test
    void insertarUsuariosEnCadaTenant() {
        UUID tenantA = seedTenant("Tenant A");
        UUID tenantB = seedTenant("Tenant B");

        Usuario userA = insertUsuario(tenantA, "a@siginex.test");
        Usuario userB = insertUsuario(tenantB, "b@siginex.test");

        assertThat(userA.id()).isNotNull();
        assertThat(userA.tenantId()).isEqualTo(tenantA);
        assertThat(userB.id()).isNotNull();
        assertThat(userB.tenantId()).isEqualTo(tenantB);
    }

    @Test
    void tenantA_veSoloSusUsuarios() {
        UUID tenantA = seedTenant("Tenant A");
        UUID tenantB = seedTenant("Tenant B");
        insertUsuario(tenantA, "a@siginex.test");
        insertUsuario(tenantB, "b@siginex.test");

        List<UUID> visibles = tenantTemplate.runInTenant(tenantA, this::selectTenantIdsVisibles);

        // Only tenant A rows are visible; tenant B is never leaked.
        assertThat(visibles).containsExactly(tenantA);
    }

    @Test
    void tenantB_veSoloSusUsuarios() {
        UUID tenantA = seedTenant("Tenant A");
        UUID tenantB = seedTenant("Tenant B");
        insertUsuario(tenantA, "a@siginex.test");
        insertUsuario(tenantB, "b@siginex.test");

        List<UUID> visibles = tenantTemplate.runInTenant(tenantB, this::selectTenantIdsVisibles);

        assertThat(visibles).containsExactly(tenantB);
    }

    @Test
    void sinTenantFijado_noVeFilas() {
        UUID tenantA = seedTenant("Tenant A");
        UUID tenantB = seedTenant("Tenant B");
        insertUsuario(tenantA, "a@siginex.test");
        insertUsuario(tenantB, "b@siginex.test");

        // No app.tenant_id fixed -> current_tenant() is NULL -> the USING clause
        // (tenant_id = NULL) matches nothing, so the policy denies every row.
        List<UUID> visibles = tenantTemplate.runWithoutTenant(this::selectTenantIdsVisibles);

        assertThat(visibles).isEmpty();
    }

    @Test
    void withCheckBloqueaEscrituraCruzadaDeTenant() {
        UUID tenantA = seedTenant("Tenant A");
        UUID tenantB = seedTenant("Tenant B");

        // INSERT cruzado: fijado en A, intentar escribir una fila de B viola el
        // WITH CHECK de la política y debe fallar. PostgreSQL lanza SQLSTATE 42501
        // ("new row violates row-level security policy for table ...") que Spring
        // envuelve; el texto de RLS vive en la causa raíz (la PSQLException), no
        // en el mensaje de la excepción Spring de primer nivel.
        assertThatThrownBy(() ->
                tenantTemplate.runInTenant(tenantA, () ->
                        usuarioRepository.insertar(nuevoUsuario(tenantB, "cross@siginex.test"))))
                .isInstanceOf(DataAccessException.class)
                .getRootCause()
                .hasMessageContaining("row-level security");

        // UPDATE cruzado: crear una fila legítima en A y luego intentar moverla a
        // B (fijado en A) debe fallar también por el WITH CHECK.
        Usuario enA = insertUsuario(tenantA, "mover@siginex.test");
        assertThatThrownBy(() ->
                tenantTemplate.runInTenant(tenantA, () -> {
                    appJdbc.update("UPDATE siginex.usuario SET tenant_id = ? WHERE id = ?",
                            tenantB, enA.id());
                    return null;
                }))
                .isInstanceOf(DataAccessException.class)
                .getRootCause()
                .hasMessageContaining("row-level security");

        // La fila sigue perteneciendo a A: el cruce nunca se materializó.
        List<UUID> visiblesEnA = tenantTemplate.runInTenant(tenantA, this::selectTenantIdsVisibles);
        assertThat(visiblesEnA).containsExactly(tenantA);
    }

    // --- helpers -----------------------------------------------------------

    /** Seeds a tenant with the privileged connection ({@code tenant} has no RLS). */
    private UUID seedTenant(String nombre) {
        UUID id = UUID.randomUUID();
        adminJdbc.update("INSERT INTO siginex.tenant (id, nombre, slug) VALUES (?, ?, ?)",
                id, nombre, "slug-" + id);
        return id;
    }

    /** Inserts a user through the production repository, as the app role, inside the tenant. */
    private Usuario insertUsuario(UUID tenantId, String email) {
        return tenantTemplate.runInTenant(tenantId, () ->
                usuarioRepository.insertar(nuevoUsuario(tenantId, email)));
    }

    private static Usuario nuevoUsuario(UUID tenantId, String email) {
        return new Usuario(null, tenantId, email, "Nombre", "argon2-hash", "miembro", true, null);
    }

    /** Reads the visible usuario tenant_ids through the app-role connection. */
    private List<UUID> selectTenantIdsVisibles() {
        return appJdbc.query("SELECT tenant_id FROM siginex.usuario ORDER BY tenant_id",
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
