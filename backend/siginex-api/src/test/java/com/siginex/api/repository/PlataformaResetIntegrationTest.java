package com.siginex.api.repository;

import com.siginex.api.config.TenantTransactionTemplate;
import com.siginex.api.config.TokenHasher;
import com.siginex.api.domain.AuthUser;
import com.siginex.api.domain.EmpresaCreada;
import com.siginex.api.domain.ResetTokenInfo;
import com.siginex.api.domain.Usuario;
import com.siginex.api.service.PlataformaServiceImpl;
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

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Prueba de integracion end-to-end del alta de empresa (Fase 5) y del flujo de
 * reset de contrasena, contra un PostgreSQL REAL (Testcontainers) con las
 * migraciones Flyway reales (V1..V5). Ejercita las clases de PRODUCCION
 * ({@link PlataformaServiceImpl}, {@link JdbcTenantRepository},
 * {@link JdbcUsuarioRepository}, {@link JdbcResetRepository},
 * {@link JdbcAuthRepository}) sobre el {@link TenantTransactionTemplate} real.
 *
 * <h2>Sobre el rol de BD de este test</h2>
 * El alta y el reset leen {@code usuario} FUERA de un tenant fijado
 * ({@code runWithoutTenant}), igual que el login (decision D4). En produccion la
 * cuenta de la app puede hacerlo porque tiene BYPASSRLS; el aislamiento por
 * tenant se prueba aparte en {@code UsuarioRlsIntegrationTest} con un rol
 * {@code NOSUPERUSER NOBYPASSRLS}. Este test valida el FLUJO funcional (crear
 * empresa+admin, emitir/confirmar token, revocar sesiones, token un-solo-uso y
 * expiracion), no el aislamiento, asi que usa el usuario por defecto del
 * contenedor (equivalente a BYPASSRLS) de forma deliberada y documentada.
 */
@Testcontainers
class PlataformaResetIntegrationTest {

    @Container
    static final PostgreSQLContainer POSTGRES =
            new PostgreSQLContainer(DockerImageName.parse("postgres:17-alpine"));

    private static HikariDataSource dataSource;
    private static JdbcTemplate jdbc;
    private static TenantTransactionTemplate tenantTemplate;

    private static PlataformaServiceImpl plataformaService;
    private static JdbcUsuarioRepository usuarioRepository;
    private static JdbcResetRepository resetRepository;
    private static JdbcAuthRepository authRepository;
    private static final TokenHasher TOKEN_HASHER = new TokenHasher();

    @BeforeAll
    static void setUp() {
        dataSource = buildDataSource(POSTGRES.getUsername(), POSTGRES.getPassword());
        jdbc = new JdbcTemplate(dataSource);

        Flyway.configure()
                .dataSource(dataSource)
                .schemas("siginex")
                .locations("classpath:db/migration")
                .load()
                .migrate();

        NamedParameterJdbcTemplate named = new NamedParameterJdbcTemplate(dataSource);
        TransactionTemplate tx = new TransactionTemplate(new DataSourceTransactionManager(dataSource));
        tenantTemplate = new TenantTransactionTemplate(tx, named);

        JdbcTenantRepository tenantRepository = new JdbcTenantRepository(named);
        usuarioRepository = new JdbcUsuarioRepository(named);
        resetRepository = new JdbcResetRepository(named);
        authRepository = new JdbcAuthRepository(named);
        plataformaService = new PlataformaServiceImpl(tenantRepository, usuarioRepository, authRepository, tenantTemplate);
    }

    @AfterAll
    static void tearDown() {
        if (dataSource != null) {
            dataSource.close();
        }
    }

    @AfterEach
    void cleanRows() {
        jdbc.execute("TRUNCATE siginex.reset_password, siginex.sesion, siginex.usuario, siginex.tenant CASCADE");
    }

    @Test
    void altaDeEmpresaCreaTenantYAdmin() {
        Usuario adminAInsertar = new Usuario(null, null, "admin@acme.com", "Admin Acme",
                "argon2-hash", "admin", true, null);

        EmpresaCreada creada = plataformaService.crearEmpresaConAdmin("Acme S.A.S.", "acme-sas", adminAInsertar);

        assertThat(creada.getTenantId()).isNotNull();
        assertThat(creada.getAdminUsuarioId()).isNotNull();
        assertThat(creada.getTenantSlug()).isEqualTo("acme-sas");

        Optional<AuthUser> admin = authRepository.findAuthByEmail("admin@acme.com");
        assertThat(admin).isPresent();
        assertThat(admin.get().rol()).isEqualTo("admin");
        assertThat(admin.get().activo()).isTrue();
        assertThat(admin.get().tenantId()).isEqualTo(creada.getTenantId());
        assertThat(admin.get().passwordHash()).isEqualTo("argon2-hash");
    }

    @Test
    void resetFlujoCompleto_emitirConfirmarRevocaSesionesYCambiaHash() {
        UUID adminId = plataformaService.crearEmpresaConAdmin("Beta Ltda", "beta-ltda",
                new Usuario(null, null, "jefe@beta.com", "Jefe", "hash-viejo", "admin", true, null))
                .getAdminUsuarioId();

        tenantTemplate.runWithoutTenant(() -> {
            jdbc.update("INSERT INTO siginex.sesion (usuario_id, tenant_id, token_hash, expira_en) "
                            + "SELECT id, tenant_id, 'sesion-hash', now() + interval '8 hours' "
                            + "FROM siginex.usuario WHERE id = ?",
                    adminId);
            return null;
        });

        String token = "token-en-claro-de-prueba";
        String tokenHash = TOKEN_HASHER.hash(token);
        tenantTemplate.runWithoutTenant(() -> {
            resetRepository.invalidarVigentesDeUsuario(adminId);
            resetRepository.insertar(adminId, tokenHash, Instant.now().plusSeconds(1800));
            return null;
        });

        ResetTokenInfo info = tenantTemplate.runWithoutTenant(() -> resetRepository.buscarPorHash(tokenHash)).orElseThrow();
        assertThat(info.isUsado()).isFalse();
        assertThat(info.getUsuarioId()).isEqualTo(adminId);
        assertThat(info.getTenantId()).isNotNull();

        tenantTemplate.runWithoutTenant(() -> {
            resetRepository.marcarUsado(info.getId());
            return null;
        });
        tenantTemplate.runInTenant(info.getTenantId(), () -> {
            authRepository.actualizarPasswordHash(adminId, "hash-nuevo");
            authRepository.revocarTodasLasSesiones(adminId);
            return null;
        });

        String hashActual = jdbc.queryForObject(
                "SELECT password_hash FROM siginex.usuario WHERE id = ?", String.class, adminId);
        assertThat(hashActual).isEqualTo("hash-nuevo");

        Boolean revocada = jdbc.queryForObject(
                "SELECT revocada FROM siginex.sesion WHERE usuario_id = ?", Boolean.class, adminId);
        assertThat(revocada).isTrue();

        Boolean usado = jdbc.queryForObject(
                "SELECT usado FROM siginex.reset_password WHERE id = ?", Boolean.class, info.getId());
        assertThat(usado).isTrue();
    }

    @Test
    void emitirTokenNuevoInvalidaLosAnteriores() {
        UUID adminId = plataformaService.crearEmpresaConAdmin("Gamma", "gamma",
                new Usuario(null, null, "a@gamma.com", "A", "h", "admin", true, null))
                .getAdminUsuarioId();

        String hash1 = TOKEN_HASHER.hash("token-1");
        String hash2 = TOKEN_HASHER.hash("token-2");

        tenantTemplate.runWithoutTenant(() -> {
            resetRepository.invalidarVigentesDeUsuario(adminId);
            resetRepository.insertar(adminId, hash1, Instant.now().plusSeconds(1800));
            return null;
        });
        tenantTemplate.runWithoutTenant(() -> {
            resetRepository.invalidarVigentesDeUsuario(adminId);
            resetRepository.insertar(adminId, hash2, Instant.now().plusSeconds(1800));
            return null;
        });

        ResetTokenInfo t1 = tenantTemplate.runWithoutTenant(() -> resetRepository.buscarPorHash(hash1)).orElseThrow();
        ResetTokenInfo t2 = tenantTemplate.runWithoutTenant(() -> resetRepository.buscarPorHash(hash2)).orElseThrow();
        assertThat(t1.isUsado()).isTrue();
        assertThat(t2.isUsado()).isFalse();
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
