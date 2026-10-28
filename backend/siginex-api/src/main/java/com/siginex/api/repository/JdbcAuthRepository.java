package com.siginex.api.repository;


import com.siginex.api.domain.AuthUser;
import com.siginex.api.domain.SesionInfo;
import com.siginex.api.domain.Usuario;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * JDBC implementation of {@link AuthRepository} using
 * {@link NamedParameterJdbcTemplate}, following the {@code JdbcXxxRepository}
 * pattern. SQL only; transaction and tenant scoping are the service's concern.
 */
@Repository
public class JdbcAuthRepository implements AuthRepository {

    private static final String FIND_AUTH_BY_EMAIL = """
            SELECT id, tenant_id, password_hash, activo, rol, fallos_login, bloqueado_hasta
            FROM siginex.usuario
            WHERE lower(email) = lower(:email)
            """;

    private static final String INSERT_SESION = """
            INSERT INTO siginex.sesion (usuario_id, tenant_id, token_hash, expira_en, user_agent, ip)
            VALUES (:usuarioId, :tenantId, :tokenHash, :expiraEn, :userAgent, CAST(:ip AS inet))
            """;

    private static final String FIND_SESION_BY_HASH = """
            SELECT s.id AS sesion_id, s.usuario_id, s.tenant_id, s.revocada, s.expira_en,
                   u.email, u.rol, u.activo AS usuario_activo
            FROM siginex.sesion s
            JOIN siginex.usuario u ON u.id = s.usuario_id
            WHERE s.token_hash = :tokenHash
            """;

    private static final String REVOKE_SESION = """
            UPDATE siginex.sesion SET revocada = true WHERE token_hash = :tokenHash
            """;

    private static final String INCREMENT_FALLOS = """
            UPDATE siginex.usuario
            SET fallos_login = fallos_login + 1, bloqueado_hasta = :bloqueadoHasta
            WHERE id = :usuarioId
            """;

    private static final String RESET_FALLOS_Y_LOGIN = """
            UPDATE siginex.usuario
            SET fallos_login = 0, bloqueado_hasta = NULL, ultimo_login = now()
            WHERE id = :usuarioId
            """;

    private static final String REVOKE_ALL_SESIONES = """
            UPDATE siginex.sesion SET revocada = true WHERE usuario_id = :usuarioId AND revocada = false
            """;

    private static final String UPDATE_PASSWORD_HASH = """
            UPDATE siginex.usuario SET password_hash = :passwordHash WHERE id = :usuarioId
            """;

    private final NamedParameterJdbcTemplate jdbc;

    public JdbcAuthRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Optional<AuthUser> findAuthByEmail(String email) {
        try {
            AuthUser user = jdbc.queryForObject(FIND_AUTH_BY_EMAIL,
                    new MapSqlParameterSource("email", email), authUserRowMapper());
            return Optional.ofNullable(user);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public void crearSesion(UUID usuarioId, UUID tenantId, String tokenHash, Instant expiraEn, String userAgent, String ip) {
        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("usuarioId", usuarioId)
                .addValue("tenantId", tenantId)
                .addValue("tokenHash", tokenHash)
                .addValue("expiraEn", Timestamp.from(expiraEn))
                .addValue("userAgent", userAgent)
                .addValue("ip", ip);
        jdbc.update(INSERT_SESION, parameters);
    }

    @Override
    public Optional<SesionInfo> buscarSesionPorHash(String tokenHash) {
        try {
            SesionInfo sesion = jdbc.queryForObject(FIND_SESION_BY_HASH,
                    new MapSqlParameterSource("tokenHash", tokenHash), sesionRowMapper());
            return Optional.ofNullable(sesion);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public void revocarSesion(String tokenHash) {
        jdbc.update(REVOKE_SESION, new MapSqlParameterSource("tokenHash", tokenHash));
    }

    @Override
    public void incrementarFallos(UUID usuarioId, Instant bloqueadoHasta) {
        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("usuarioId", usuarioId)
                .addValue("bloqueadoHasta", bloqueadoHasta == null ? null : Timestamp.from(bloqueadoHasta));
        jdbc.update(INCREMENT_FALLOS, parameters);
    }

    @Override
    public void resetearFallosYLogin(UUID usuarioId) {
        jdbc.update(RESET_FALLOS_Y_LOGIN, new MapSqlParameterSource("usuarioId", usuarioId));
    }

    @Override
    public void revocarTodasLasSesiones(UUID usuarioId) {
        jdbc.update(REVOKE_ALL_SESIONES, new MapSqlParameterSource("usuarioId", usuarioId));
    }

    @Override
    public void actualizarPasswordHash(UUID usuarioId, String passwordHash) {
        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("usuarioId", usuarioId)
                .addValue("passwordHash", passwordHash);
        jdbc.update(UPDATE_PASSWORD_HASH, parameters);
    }

    private static RowMapper<AuthUser> authUserRowMapper() {
        return (rs, rowNum) -> new AuthUser(
                rs.getObject("id", UUID.class),
                rs.getObject("tenant_id", UUID.class),
                rs.getString("password_hash"),
                rs.getBoolean("activo"),
                rs.getString("rol"),
                rs.getShort("fallos_login"),
                instant(rs, "bloqueado_hasta"));
    }

    private static RowMapper<SesionInfo> sesionRowMapper() {
        return (rs, rowNum) -> new SesionInfo(
                rs.getObject("sesion_id", UUID.class),
                rs.getObject("usuario_id", UUID.class),
                rs.getObject("tenant_id", UUID.class),
                rs.getString("email"),
                rs.getString("rol"),
                rs.getBoolean("usuario_activo"),
                rs.getBoolean("revocada"),
                instant(rs, "expira_en"));
    }

    private static Instant instant(ResultSet rs, String column) throws SQLException {
        Timestamp value = rs.getTimestamp(column);
        return value == null ? null : value.toInstant();
    }
}
