package com.siginex.api.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.siginex.api.domain.KbModulo;
import com.siginex.api.domain.KbPregunta;
import com.siginex.api.service.kb.BancoJson;
import com.siginex.api.service.kb.KbSeedService;
import com.zaxxer.hikari.HikariDataSource;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for the GLOBAL knowledge bank (KB) seeded by
 * {@link KbSeedService} from {@code classpath:kb/banco.json} into the tables
 * created by migration {@code V4}. It runs the REAL Flyway migrations (V1..V4)
 * against a throwaway {@code postgres:17-alpine} container.
 *
 * <p>Asserts:
 * <ul>
 *   <li>(a) the seed loads 9 modules and 521 questions for {@code kb_version=3.0.0};</li>
 *   <li>(b) the KB is visible GLOBALLY — the non-privileged {@code siginex_app}
 *       role reads modules/questions WITHOUT fixing any tenant, proving the KB
 *       tables carry no Row-Level Security;</li>
 *   <li>idempotency — running the seed twice keeps 9/521 (no duplicates).</li>
 * </ul>
 *
 * <p>Like {@link DiagnosticoRlsIntegrationTest}, a non-privileged
 * {@code siginex_app} role ({@code NOSUPERUSER NOBYPASSRLS}) is used for the
 * read assertions so the test exercises the same role the application uses. The
 * seed itself runs through the privileged connection (it needs INSERT on the KB
 * tables at setup), which is the production analogue of the startup runner.
 *
 * <p>Verification evidence is recorded in
 * {@code .agents/tasks/fase2c/verificacion.md}.
 */
@Testcontainers
class KbSeedIntegrationTest {

    private static final String APP_ROLE = "siginex_app";
    private static final String APP_PASSWORD = "siginex_app_pwd";
    private static final String KB_VERSION = "3.0.0";
    private static final int MODULOS_ESPERADOS = 9;
    private static final int PREGUNTAS_ESPERADAS = 521;

    @Container
    static final PostgreSQLContainer POSTGRES =
            new PostgreSQLContainer(DockerImageName.parse("postgres:17-alpine"));

    private static HikariDataSource adminDataSource;
    private static JdbcTemplate adminJdbc;
    private static HikariDataSource appDataSource;
    private static JdbcTemplate appJdbc;
    private static KbSeedService kbSeedService;
    private static JdbcKbRepository kbRepository;

    @BeforeAll
    static void setUpDatabase() {
        adminDataSource = buildDataSource(POSTGRES.getUsername(), POSTGRES.getPassword());
        adminJdbc = new JdbcTemplate(adminDataSource);

        // Real project migrations (V1 baseline + V2 auth + V3 business + V4 KB).
        Flyway.configure()
                .dataSource(adminDataSource)
                .schemas("siginex")
                .locations("classpath:db/migration")
                .load()
                .migrate();

        // Non-privileged application role with minimal grants, like the RLS tests.
        adminJdbc.execute("DROP ROLE IF EXISTS " + APP_ROLE);
        adminJdbc.execute("CREATE ROLE " + APP_ROLE
                + " LOGIN PASSWORD '" + APP_PASSWORD + "' NOSUPERUSER NOBYPASSRLS");
        adminJdbc.execute("GRANT USAGE ON SCHEMA siginex TO " + APP_ROLE);
        adminJdbc.execute("GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA siginex TO " + APP_ROLE);
        adminJdbc.execute("GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA siginex TO " + APP_ROLE);
        adminJdbc.execute("GRANT EXECUTE ON FUNCTION siginex.current_tenant() TO " + APP_ROLE);

        appDataSource = buildDataSource(APP_ROLE, APP_PASSWORD);
        appJdbc = new JdbcTemplate(appDataSource);

        ObjectMapper objectMapper = new ObjectMapper();
        // Seed runs through the privileged connection (needs INSERT on KB tables).
        kbSeedService = new KbSeedService(new NamedParameterJdbcTemplate(adminDataSource), objectMapper);
        // Reads go through the app-role connection to prove the KB is global.
        kbRepository = new JdbcKbRepository(new NamedParameterJdbcTemplate(appDataSource), objectMapper);
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

    @Test
    void seedCargaNueveModulosY521Preguntas() {
        BancoJson banco = kbSeedService.seed();

        assertThat(banco.getKbVersion()).isEqualTo(KB_VERSION);

        Integer modulos = adminJdbc.queryForObject(
                "SELECT count(*) FROM siginex.kb_modulo WHERE kb_version = ?", Integer.class, KB_VERSION);
        Integer preguntas = adminJdbc.queryForObject(
                "SELECT count(*) FROM siginex.kb_pregunta WHERE kb_version = ?", Integer.class, KB_VERSION);

        assertThat(modulos).isEqualTo(MODULOS_ESPERADOS);
        assertThat(preguntas).isEqualTo(PREGUNTAS_ESPERADAS);
    }

    @Test
    void kbEsVisibleGlobalmenteSinTenant() {
        kbSeedService.seed();

        // The app role reads the KB WITHOUT fixing any tenant: the tables have no
        // RLS, so every row is visible. (If they carried RLS, current_tenant()
        // would be NULL and nothing would come back.)
        List<KbModulo> modulos = kbRepository.listarModulos();
        assertThat(modulos).hasSize(MODULOS_ESPERADOS);
        // Order is preserved from banco.json: sst is the first module.
        assertThat(modulos.get(0).getId()).isEqualTo("sst");
        assertThat(modulos.get(0).getShortNombre()).isEqualTo("SG-SST");

        List<KbPregunta> preguntasSst = kbRepository.listarPreguntas("sst");
        assertThat(preguntasSst).hasSize(60);
        KbPregunta primera = preguntasSst.get(0);
        assertThat(primera.getId()).isEqualTo("sst-1.1.1");
        // recomendacion jsonb round-trips into a {"0","1","2"} map.
        assertThat(primera.getRecomendacion()).containsKeys("0", "1", "2");
    }

    @Test
    void escalaVaciaSeNormalizaANull() {
        kbSeedService.seed();

        // esg and gobernanza_ia ship escala == "" in banco.json; the seed stores NULL.
        String escalaEsg = adminJdbc.queryForObject(
                "SELECT escala FROM siginex.kb_modulo WHERE id = ?", String.class, "esg");
        String escalaIa = adminJdbc.queryForObject(
                "SELECT escala FROM siginex.kb_modulo WHERE id = ?", String.class, "gobernanza_ia");

        assertThat(escalaEsg).isNull();
        assertThat(escalaIa).isNull();
    }

    @Test
    void seedEsIdempotente() {
        kbSeedService.seed();
        // A second run must not duplicate: the version is already loaded, so it
        // is a no-op. Counts stay at 9/521.
        kbSeedService.seed();

        Integer modulos = adminJdbc.queryForObject(
                "SELECT count(*) FROM siginex.kb_modulo WHERE kb_version = ?", Integer.class, KB_VERSION);
        Integer preguntas = adminJdbc.queryForObject(
                "SELECT count(*) FROM siginex.kb_pregunta WHERE kb_version = ?", Integer.class, KB_VERSION);

        assertThat(modulos).isEqualTo(MODULOS_ESPERADOS);
        assertThat(preguntas).isEqualTo(PREGUNTAS_ESPERADAS);
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
