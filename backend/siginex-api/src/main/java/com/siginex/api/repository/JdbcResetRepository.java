package com.siginex.api.repository;

import com.siginex.api.domain.ResetTokenInfo;
import lombok.RequiredArgsConstructor;
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
 * Implementación JDBC de {@link ResetRepository} con
 * {@link NamedParameterJdbcTemplate}, patrón {@code JdbcXxxRepository}. SQL
 * only. Igual que la búsqueda de login (decisión D4), la lectura por token se
 * hace sin tenant fijado (el service la invoca en {@code runWithoutTenant}); el
 * {@code tenant_id} del usuario se obtiene con un join a {@code usuario}. El
 * token en claro nunca se guarda: solo su hash SHA-256.
 */
@Repository
@RequiredArgsConstructor
public class JdbcResetRepository implements ResetRepository {

    private static final String INVALIDAR_VIGENTES = """
            UPDATE siginex.reset_password SET usado = true
            WHERE usuario_id = :usuarioId AND usado = false
            """;

    private static final String INSERT_RESET = """
            INSERT INTO siginex.reset_password (usuario_id, token_hash, expira_en)
            VALUES (:usuarioId, :tokenHash, :expiraEn)
            """;

    private static final String FIND_BY_HASH = """
            SELECT r.id, r.usuario_id, u.tenant_id, r.usado, r.expira_en
            FROM siginex.reset_password r
            JOIN siginex.usuario u ON u.id = r.usuario_id
            WHERE r.token_hash = :tokenHash
            """;

    private static final String MARCAR_USADO = """
            UPDATE siginex.reset_password SET usado = true WHERE id = :id
            """;

    private final NamedParameterJdbcTemplate jdbc;

    @Override
    public void invalidarVigentesDeUsuario(UUID usuarioId) {
        jdbc.update(INVALIDAR_VIGENTES, new MapSqlParameterSource("usuarioId", usuarioId));
    }

    @Override
    public void insertar(UUID usuarioId, String tokenHash, Instant expiraEn) {
        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("usuarioId", usuarioId)
                .addValue("tokenHash", tokenHash)
                .addValue("expiraEn", Timestamp.from(expiraEn));
        jdbc.update(INSERT_RESET, parameters);
    }

    @Override
    public Optional<ResetTokenInfo> buscarPorHash(String tokenHash) {
        try {
            ResetTokenInfo info = jdbc.queryForObject(FIND_BY_HASH,
                    new MapSqlParameterSource("tokenHash", tokenHash), resetRowMapper());
            return Optional.ofNullable(info);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public void marcarUsado(UUID id) {
        jdbc.update(MARCAR_USADO, new MapSqlParameterSource("id", id));
    }

    private static RowMapper<ResetTokenInfo> resetRowMapper() {
        return (rs, rowNum) -> ResetTokenInfo.builder()
                .id(rs.getObject("id", UUID.class))
                .usuarioId(rs.getObject("usuario_id", UUID.class))
                .tenantId(rs.getObject("tenant_id", UUID.class))
                .usado(rs.getBoolean("usado"))
                .expiraEn(instant(rs, "expira_en"))
                .build();
    }

    private static Instant instant(ResultSet rs, String column) throws SQLException {
        Timestamp value = rs.getTimestamp(column);
        return value == null ? null : value.toInstant();
    }
}
