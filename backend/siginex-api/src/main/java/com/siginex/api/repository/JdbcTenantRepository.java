package com.siginex.api.repository;

import com.siginex.api.domain.EmpresaCreada;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Implementación JDBC de {@link TenantRepository} con
 * {@link NamedParameterJdbcTemplate}, siguiendo el patrón {@code JdbcXxxRepository}.
 * SQL only. La tabla {@code tenant} no tiene RLS, así que estas sentencias
 * corren fuera de un tenant fijado (el service usa {@code runWithoutTenant}).
 */
@Repository
@RequiredArgsConstructor
public class JdbcTenantRepository implements TenantRepository {

    private static final String INSERT_TENANT = """
            INSERT INTO siginex.tenant (nombre, slug)
            VALUES (:nombre, :slug)
            RETURNING id, nombre, slug
            """;

    private static final String EXISTE_SLUG = """
            SELECT EXISTS (SELECT 1 FROM siginex.tenant WHERE slug = :slug)
            """;

    private final NamedParameterJdbcTemplate jdbc;

    @Override
    public EmpresaCreada insertarTenant(String nombre, String slug) {
        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("nombre", nombre)
                .addValue("slug", slug);
        return jdbc.queryForObject(INSERT_TENANT, parameters, tenantRowMapper());
    }

    @Override
    public boolean existeSlug(String slug) {
        Boolean exists = jdbc.queryForObject(EXISTE_SLUG,
                new MapSqlParameterSource("slug", slug), Boolean.class);
        return Boolean.TRUE.equals(exists);
    }

    /** Mapea la fila del tenant recién insertado (aún sin usuario admin). */
    private static RowMapper<EmpresaCreada> tenantRowMapper() {
        return (rs, rowNum) -> EmpresaCreada.builder()
                .tenantId(rs.getObject("id", UUID.class))
                .tenantNombre(rs.getString("nombre"))
                .tenantSlug(rs.getString("slug"))
                .build();
    }
}
