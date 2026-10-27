package com.siginex.api.repository;

import com.siginex.api.domain.DiagnosticoDetalle;
import com.siginex.api.domain.DiagnosticoResumen;
import com.siginex.api.domain.calculo.ResultadoLectura;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
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
            SELECT d.id, d.organizacion_id, o.nombre AS organizacion_nombre, d.estado,
                   d.nivel_tecnologico
            FROM siginex.diagnostico d
            INNER JOIN siginex.organizacion o ON d.organizacion_id = o.id
            WHERE d.id = :id
            """;

    // Fase 2d: reescritura idempotente del cálculo. Marca el diagnostico como
    // 'calculado' y refresca score/nivel/cumplimiento y completado_en. El CAST
    // del estado al enum siginex.estado_diag sigue el patrón de los repos.
    private static final String ACTUALIZAR_RESULTADO = """
            UPDATE siginex.diagnostico
            SET estado = CAST('calculado' AS siginex.estado_diag),
                score_sgi = :score,
                nivel_sgi = :nivel,
                cumplimiento_global = :cumplimiento,
                completado_en = now()
            WHERE id = :id
            """;

    // Fase 2d: registro del benchmark. Fija nivel_tecnologico (enum
    // siginex.nivel_tecnologico '1'..'5') casteando desde el entero validado.
    private static final String ACTUALIZAR_NIVEL_TECNOLOGICO = """
            UPDATE siginex.diagnostico
            SET nivel_tecnologico = CAST(:nivel AS siginex.nivel_tecnologico)
            WHERE id = :id
            """;

    private static final String LISTAR = """
            SELECT d.id, d.organizacion_id, o.nombre AS organizacion_nombre, d.estado,
                   d.score_sgi, d.nivel_sgi, d.creado_en
            FROM siginex.diagnostico d
            INNER JOIN siginex.organizacion o ON d.organizacion_id = o.id
            ORDER BY d.creado_en DESC
            """;

    // Fase 2d: cabecera agregada para obtenerResultado (score/nivel/cumplimiento
    // + nombre de la organizacion). Filtra solo por id: la RLS acota al tenant.
    private static final String SELECT_CABECERA_RESULTADO = """
            SELECT d.score_sgi, d.nivel_sgi, d.cumplimiento_global,
                   o.nombre AS organizacion_nombre
            FROM siginex.diagnostico d
            INNER JOIN siginex.organizacion o ON d.organizacion_id = o.id
            WHERE d.id = :id
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

    @Override
    public void actualizarResultado(UUID tenantId, UUID diagnosticoId,
                                    BigDecimal scoreSgi, Integer nivelSgi,
                                    BigDecimal cumplimientoGlobal) {
        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("id", diagnosticoId)
                .addValue("score", scoreSgi)
                .addValue("nivel", nivelSgi)
                .addValue("cumplimiento", cumplimientoGlobal);
        jdbc.update(ACTUALIZAR_RESULTADO, parameters);
    }

    @Override
    public void actualizarNivelTecnologico(UUID tenantId, UUID diagnosticoId, int nivel) {
        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("id", diagnosticoId)
                // El enum usa etiquetas '1'..'5'; se pasa el entero como texto.
                .addValue("nivel", Integer.toString(nivel));
        jdbc.update(ACTUALIZAR_NIVEL_TECNOLOGICO, parameters);
    }

    @Override
    public Optional<ResultadoLectura> buscarCabeceraResultado(UUID diagnosticoId) {
        try {
            ResultadoLectura cabecera = jdbc.queryForObject(SELECT_CABECERA_RESULTADO,
                    new MapSqlParameterSource("id", diagnosticoId), cabeceraResultadoRowMapper());
            return Optional.ofNullable(cabecera);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    private static RowMapper<ResultadoLectura> cabeceraResultadoRowMapper() {
        // Pilares y recomendaciones los completa el service; aquí solo la cabecera.
        return (rs, rowNum) -> ResultadoLectura.builder()
                .scoreSgi(rs.getBigDecimal("score_sgi"))
                .nivelSgi(integer(rs, "nivel_sgi"))
                .cumplimientoGlobal(rs.getBigDecimal("cumplimiento_global"))
                .organizacionNombre(rs.getString("organizacion_nombre"))
                .build();
    }

    private static RowMapper<DiagnosticoDetalle> detalleRowMapper() {
        return (rs, rowNum) -> DiagnosticoDetalle.builder()
                .id(rs.getObject("id", UUID.class))
                .organizacionId(rs.getObject("organizacion_id", UUID.class))
                .organizacionNombre(rs.getString("organizacion_nombre"))
                .estado(rs.getString("estado"))
                .nivelTecnologico(nivelTecnologico(rs))
                .build();
    }

    /**
     * Lee {@code nivel_tecnologico} (enum {@code siginex.nivel_tecnologico} con
     * etiquetas {@code '1'..'5'}) como {@link Integer}, o {@code null} si la
     * columna es nula. El valor llega como texto ({@code getString}) y se
     * convierte con {@link Integer#parseInt}.
     */
    private static Integer nivelTecnologico(ResultSet rs) throws SQLException {
        String value = rs.getString("nivel_tecnologico");
        return value == null ? null : Integer.valueOf(value);
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
