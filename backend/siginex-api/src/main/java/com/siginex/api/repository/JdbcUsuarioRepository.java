package com.siginex.api.repository;


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
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * JDBC implementation of {@link UsuarioRepository} using
 * {@link NamedParameterJdbcTemplate}, following the {@code JdbcXxxRepository}
 * pattern. SQL only; all statements run under the admin's tenant because the
 * service invokes them inside {@code runInTenant}, so RLS enforces isolation.
 */
@Repository
public class JdbcUsuarioRepository implements UsuarioRepository {

    private static final String INSERT_USUARIO = """
            INSERT INTO siginex.usuario (tenant_id, email, nombre, password_hash, rol, activo)
            VALUES (:tenantId, :email, :nombre, :passwordHash, :rol, :activo)
            RETURNING id, tenant_id, email, nombre, rol, activo, creado_en
            """;

    private static final String FIND_BY_ID = """
            SELECT id, tenant_id, email, nombre, rol, activo, creado_en
            FROM siginex.usuario
            WHERE id = :id
            """;

    private static final String LISTAR = """
            SELECT id, tenant_id, email, nombre, rol, activo, creado_en
            FROM siginex.usuario
            ORDER BY creado_en DESC
            """;

    private static final String CAMBIAR_ACTIVO = """
            UPDATE siginex.usuario SET activo = :activo WHERE id = :id
            """;

    private static final String EXISTE_EMAIL = """
            SELECT EXISTS (SELECT 1 FROM siginex.usuario WHERE lower(email) = lower(:email))
            """;

    private final NamedParameterJdbcTemplate jdbc;

    public JdbcUsuarioRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Usuario insertar(Usuario usuario) {
        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("tenantId", usuario.tenantId())
                .addValue("email", usuario.email())
                .addValue("nombre", usuario.nombre())
                .addValue("passwordHash", usuario.passwordHash())
                .addValue("rol", usuario.rol())
                .addValue("activo", usuario.activo());
        return jdbc.queryForObject(INSERT_USUARIO, parameters, usuarioRowMapper());
    }

    @Override
    public List<Usuario> listar() {
        return jdbc.query(LISTAR, usuarioRowMapper());
    }

    @Override
    public Optional<Usuario> buscarPorId(UUID id) {
        try {
            Usuario usuario = jdbc.queryForObject(FIND_BY_ID,
                    new MapSqlParameterSource("id", id), usuarioRowMapper());
            return Optional.ofNullable(usuario);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public void cambiarActivo(UUID id, boolean activo) {
        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("id", id)
                .addValue("activo", activo);
        jdbc.update(CAMBIAR_ACTIVO, parameters);
    }

    @Override
    public boolean existeEmail(String email) {
        Boolean exists = jdbc.queryForObject(EXISTE_EMAIL,
                new MapSqlParameterSource("email", email), Boolean.class);
        return Boolean.TRUE.equals(exists);
    }

    /**
     * Maps a user row. The {@code password_hash} column is never selected, so
     * the hash never leaves the database through this layer.
     */
    private static RowMapper<Usuario> usuarioRowMapper() {
        return (rs, rowNum) -> new Usuario(
                rs.getObject("id", UUID.class),
                rs.getObject("tenant_id", UUID.class),
                rs.getString("email"),
                rs.getString("nombre"),
                null,
                rs.getString("rol"),
                rs.getBoolean("activo"),
                instant(rs, "creado_en"));
    }

    private static Instant instant(ResultSet rs, String column) throws SQLException {
        Timestamp value = rs.getTimestamp(column);
        return value == null ? null : value.toInstant();
    }
}
