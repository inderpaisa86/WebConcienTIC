package com.siginex.api.repository;

import com.siginex.api.domain.DiagnosticoDetalle;
import com.siginex.api.domain.DiagnosticoResumen;
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
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * JDBC implementation of {@link DiagnosticoRepository} using
 * {@link NamedParameterJdbcTemplate}. SQL ported faithfully from
 * {@code kbVersionVigente} / {@code crearDiagnostico} / {@code listarDiagnosticos}
 * / {@code obtenerDiagnostico} of the original {@code diagnostico-service.ts}.
 *
 * <p>All tenant-scoped statements run under the actor's tenant because the
 * service invokes them inside {@code runInTenant}, so RLS enforces isolation.
 * {@code kb_version} is global (no RLS). The INSERT lets the database apply the
 * default {@code estado 'en_progreso'}; the header is then re-read by id with a
 * JOIN to {@code organizacion} so {@code organizacionNombre} is populated,
 * coherent with {@link #buscarPorId(UUID)}.
 */
@Repository
@RequiredArgsConstructor
public class JdbcDiagnosticoRepository implements DiagnosticoRepository {

    private static final String KB_VERSION_DEFAULT = "3.0.0";

    private static final String KB_VERSION_VIGENTE = """
            SELECT version
            FROM siginex.kb_version
            ORDER BY publicado_en DESC
            LIMIT 1
            """;

    private static final String INSERT_DIAGNOSTICO = """
            INSERT INTO siginex.diagnostico (tenant_id, organizacion_id, kb_version)
            VALUES (:tenantId, :organizacionId, :kbVersion)
            RETURNING id
            """;

    private static final String SELECT_DETALLE = """
            SELECT d.id, d.organizacion_id, o.nombre AS organizacion_nombre, d.estado
            FROM siginex.diagnostico d
            INNER JOIN siginex.organizacion o ON d.organizacion_id = o.id
            WHERE d.id = :id
            """;

    private static final String LISTAR = """
            SELECT d.id, d.organizacion_id, o.nombre AS organizacion_nombre, d.estado,
                   d.score_sgi, d.nivel_sgi, d.creado_en
            FROM siginex.diagnostico d
            INNER JOIN siginex.organizacion o ON d.organizacion_id = o.id
            ORDER BY d.creado_en DESC
            """;

    private final NamedParameterJdbcTemplate jdbc;

    @Override
    public String kbVersionVigente() {
        try {
            return jdbc.queryForObject(KB_VERSION_VIGENTE, new MapSqlParameterSource(), String.class);
        } catch (EmptyResultDataAccessException e) {
            return KB_VERSION_DEFAULT;
        }
    }

    @Override
    public DiagnosticoDetalle insertar(UUID tenantId, UUID organizacionId, String kbVersion) {
        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("tenantId", tenantId)
                .addValue("organizacionId", organizacionId)
                .addValue("kbVersion", kbVersion);
        UUID id = jdbc.queryForObject(INSERT_DIAGNOSTICO, parameters, UUID.class);
        // Re-read the header with the JOIN so organizacionNombre is populated,
        // coherent with buscarPorId (RETURNING cannot bring the joined name).
        return jdbc.queryForObject(SELECT_DETALLE,
                new MapSqlParameterSource("id", id), detalleRowMapper());
    }

    @Override
    public List<DiagnosticoResumen> listar() {
        return jdbc.query(LISTAR, resumenRowMapper());
    }

    @Override
    public Optional<DiagnosticoDetalle> buscarPorId(UUID id) {
        try {
            DiagnosticoDetalle detalle = jdbc.queryForObject(SELECT_DETALLE,
                    new MapSqlParameterSource("id", id), detalleRowMapper());
            return Optional.ofNullable(detalle);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    private static RowMapper<DiagnosticoDetalle> detalleRowMapper() {
        return (rs, rowNum) -> DiagnosticoDetalle.builder()
                .id(rs.getObject("id", UUID.class))
                .organizacionId(rs.getObject("organizacion_id", UUID.class))
                .organizacionNombre(rs.getString("organizacion_nombre"))
                .estado(rs.getString("estado"))
                .build();
    }

    private static RowMapper<DiagnosticoResumen> resumenRowMapper() {
        return (rs, rowNum) -> DiagnosticoResumen.builder()
                .id(rs.getObject("id", UUID.class))
                .organizacionId(rs.getObject("organizacion_id", UUID.class))
                .organizacionNombre(rs.getString("organizacion_nombre"))
                .estado(rs.getString("estado"))
                .scoreSgi(rs.getString("score_sgi"))
                .nivelSgi(integer(rs, "nivel_sgi"))
                .creadoEn(instant(rs, "creado_en"))
                .build();
    }

    private static Integer integer(ResultSet rs, String column) throws SQLException {
        int value = rs.getInt(column);
        return rs.wasNull() ? null : value;
    }

    private static Instant instant(ResultSet rs, String column) throws SQLException {
        Timestamp value = rs.getTimestamp(column);
        return value == null ? null : value.toInstant();
    }
}
