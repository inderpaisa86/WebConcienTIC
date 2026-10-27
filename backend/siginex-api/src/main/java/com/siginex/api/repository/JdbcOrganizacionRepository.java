package com.siginex.api.repository;

import com.siginex.api.domain.Organizacion;
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
 * JDBC implementation of {@link OrganizacionRepository} using
 * {@link NamedParameterJdbcTemplate}. SQL ported faithfully from
 * {@code crearOrganizacion} / {@code listarOrganizaciones} of the original
 * {@code diagnostico-service.ts}. All statements run under the actor's tenant
 * because the service invokes them inside {@code runInTenant}, so RLS enforces
 * isolation. {@code tamano} is cast to {@code siginex.tamano_empresa} and the
 * default {@code aplicabilidad} is an empty jsonb object.
 */
@Repository
@RequiredArgsConstructor
public class JdbcOrganizacionRepository implements OrganizacionRepository {

    private static final String INSERT_ORGANIZACION = """
            INSERT INTO siginex.organizacion (tenant_id, nombre, sector, tamano, aplicabilidad)
            VALUES (:tenantId, :nombre, :sector, CAST(:tamano AS siginex.tamano_empresa), CAST('{}' AS jsonb))
            RETURNING id, tenant_id, nombre, sector, tamano, creado_en
            """;

    private static final String LISTAR = """
            SELECT id, tenant_id, nombre, sector, tamano, creado_en
            FROM siginex.organizacion
            ORDER BY creado_en DESC
            """;

    private static final String FIND_BY_ID = """
            SELECT id, tenant_id, nombre, sector, tamano, creado_en
            FROM siginex.organizacion
            WHERE id = :id
            """;

    private final NamedParameterJdbcTemplate jdbc;

    @Override
    public Organizacion insertar(Organizacion org) {
        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("tenantId", org.getTenantId())
                .addValue("nombre", org.getNombre())
                .addValue("sector", org.getSector())
                .addValue("tamano", org.getTamano());
        return jdbc.queryForObject(INSERT_ORGANIZACION, parameters, organizacionRowMapper());
    }

    @Override
    public List<Organizacion> listar() {
        return jdbc.query(LISTAR, organizacionRowMapper());
    }

    @Override
    public Optional<Organizacion> buscarPorId(UUID id) {
        try {
            Organizacion org = jdbc.queryForObject(FIND_BY_ID,
                    new MapSqlParameterSource("id", id), organizacionRowMapper());
            return Optional.ofNullable(org);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    private static RowMapper<Organizacion> organizacionRowMapper() {
        return (rs, rowNum) -> Organizacion.builder()
                .id(rs.getObject("id", UUID.class))
                .tenantId(rs.getObject("tenant_id", UUID.class))
                .nombre(rs.getString("nombre"))
                .sector(rs.getString("sector"))
                .tamano(rs.getString("tamano"))
                .creadoEn(instant(rs, "creado_en"))
                .build();
    }

    private static Instant instant(ResultSet rs, String column) throws SQLException {
        Timestamp value = rs.getTimestamp(column);
        return value == null ? null : value.toInstant();
    }
}
