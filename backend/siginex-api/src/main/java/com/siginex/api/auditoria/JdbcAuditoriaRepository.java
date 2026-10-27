package com.siginex.api.auditoria;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * JDBC implementation that inserts an audit event with a {@code jsonb} payload.
 */
@Repository
public class JdbcAuditoriaRepository implements AuditoriaRepository {

    private static final String INSERT_EVENTO = """
            INSERT INTO siginex.evento_auditoria (tenant_id, actor, accion, entidad, entidad_id, payload)
            VALUES (:tenantId, :actor, :accion, :entidad, :entidadId, CAST(:payload AS jsonb))
            """;

    private final NamedParameterJdbcTemplate jdbc;

    public JdbcAuditoriaRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void insertar(UUID tenantId, String actor, String accion, String entidad, String entidadId, String payloadJson) {
        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("tenantId", tenantId)
                .addValue("actor", actor)
                .addValue("accion", accion)
                .addValue("entidad", entidad)
                .addValue("entidadId", entidadId)
                .addValue("payload", payloadJson);
        jdbc.update(INSERT_EVENTO, parameters);
    }
}
