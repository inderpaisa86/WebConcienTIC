package com.siginex.api.repository;

import com.siginex.api.domain.RespuestaEntrada;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * JDBC implementation of {@link RespuestaRepository} using
 * {@link NamedParameterJdbcTemplate}. SQL ported faithfully from
 * {@code guardarRespuestas} / {@code obtenerDiagnostico} of the original
 * {@code diagnostico-service.ts}.
 *
 * <p>All statements run under the actor's tenant because the service invokes
 * them inside {@code runInTenant}, so RLS isolates the rows. {@code tenant_id}
 * is written explicitly so the policy {@code WITH CHECK} is satisfied.
 * {@code skillId} is the part of {@code preguntaId} before the first {@code '-'}
 * (or the whole id when there is none), {@code tipo = 'cero_uno_dos'}, and
 * {@code valor} is cast to the {@code siginex.valor_respuesta} enum.
 */
@Repository
@RequiredArgsConstructor
public class JdbcRespuestaRepository implements RespuestaRepository {

    private static final String TIPO = "cero_uno_dos";

    private static final String UPSERT = """
            INSERT INTO siginex.respuesta
                (tenant_id, diagnostico_id, pregunta_id, skill_id, tipo, valor)
            VALUES
                (:tenantId, :diagnosticoId, :preguntaId, :skillId,
                 CAST(:tipo AS siginex.tipo_pregunta), CAST(:valor AS siginex.valor_respuesta))
            ON CONFLICT (diagnostico_id, pregunta_id) DO UPDATE SET
                valor = EXCLUDED.valor,
                actualizado_en = now()
            """;

    private static final String LISTAR = """
            SELECT pregunta_id, valor
            FROM siginex.respuesta
            WHERE diagnostico_id = :diagnosticoId
            """;

    private final NamedParameterJdbcTemplate jdbc;

    @Override
    public void upsertLote(UUID tenantId, UUID diagnosticoId, List<RespuestaEntrada> entradas) {
        for (RespuestaEntrada entrada : entradas) {
            MapSqlParameterSource parameters = new MapSqlParameterSource()
                    .addValue("tenantId", tenantId)
                    .addValue("diagnosticoId", diagnosticoId)
                    .addValue("preguntaId", entrada.getPreguntaId())
                    .addValue("skillId", skillId(entrada.getPreguntaId()))
                    .addValue("tipo", TIPO)
                    .addValue("valor", entrada.getValor());
            jdbc.update(UPSERT, parameters);
        }
    }

    @Override
    public Map<String, String> listarPorDiagnostico(UUID diagnosticoId) {
        Map<String, String> respuestas = new LinkedHashMap<>();
        jdbc.query(LISTAR, new MapSqlParameterSource("diagnosticoId", diagnosticoId), rs -> {
            String valor = rs.getString("valor");
            // Faithful to obtenerDiagnostico: only non-null values are kept.
            if (valor != null) {
                respuestas.put(rs.getString("pregunta_id"), valor);
            }
        });
        return respuestas;
    }

    /**
     * Derives the skillId from the preguntaId, faithful to the original
     * {@code guardarRespuestas}: the part before the first {@code '-'}, or the
     * whole id when there is no {@code '-'} (or it is the first character).
     */
    static String skillId(String preguntaId) {
        int i = preguntaId.indexOf('-');
        return i > 0 ? preguntaId.substring(0, i) : preguntaId;
    }
}
