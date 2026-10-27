package com.siginex.api.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.siginex.api.domain.calculo.Brecha;
import com.siginex.api.domain.calculo.Nivel;
import com.siginex.api.domain.calculo.ResultadoLectura;
import com.siginex.api.domain.calculo.ResultadoModulo;
import com.siginex.api.domain.calculo.RutaAprendizajePlan;
import com.siginex.api.domain.calculo.TareaPlan;
import com.siginex.api.domain.calculo.TareaPlanLectura;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

/**
 * Implementación JDBC de {@link ResultadoRepository} con
 * {@link NamedParameterJdbcTemplate}. SQL portado de la persistencia de
 * {@code diagnostico-service.ts::calcularDiagnostico}.
 *
 * <p>Corre dentro de {@code runInTenant}, de modo que la RLS aísla por tenant;
 * {@code tenant_id} se escribe explícitamente para el {@code WITH CHECK}. Los
 * enums Postgres se castean con {@code CAST(:x AS siginex.<enum>)} y
 * {@code norma_refs} (jsonb) con {@code CAST(:refs AS jsonb)}, igual que
 * {@link JdbcAuditoriaRepository}.
 *
 * <p><b>Paridad numérica en la frontera JDBC:</b> los {@code double} del motor
 * ya redondeados a 2 decimales se convierten a {@link BigDecimal} con
 * {@code new BigDecimal(Double.toString(x))} (NUNCA {@code new BigDecimal(double)},
 * que reintroduciría ruido binario); {@code null} persiste {@code NULL}.
 */
@Repository
@RequiredArgsConstructor
public class JdbcResultadoRepository implements ResultadoRepository {

    // Orden de borrado fiel al TS: pilar -> tarea -> ruta -> recomendacion.
    private static final String DELETE_PILAR =
            "DELETE FROM siginex.resultado_pilar WHERE diagnostico_id = :diagnosticoId";
    private static final String DELETE_TAREA =
            "DELETE FROM siginex.tarea_mejora WHERE diagnostico_id = :diagnosticoId";
    private static final String DELETE_RUTA =
            "DELETE FROM siginex.ruta_aprendizaje WHERE diagnostico_id = :diagnosticoId";
    private static final String DELETE_RECOMENDACION =
            "DELETE FROM siginex.recomendacion WHERE diagnostico_id = :diagnosticoId";

    private static final String INSERT_PILAR = """
            INSERT INTO siginex.resultado_pilar
                (tenant_id, diagnostico_id, skill_id, score, nivel, cumplimiento_pct, banda)
            VALUES
                (:tenantId, :diagnosticoId, :skillId, :score, :nivel, :cumplimientoPct, :banda)
            """;

    private static final String INSERT_RECOMENDACION = """
            INSERT INTO siginex.recomendacion
                (tenant_id, diagnostico_id, skill_id, pregunta_id, prioridad, texto, norma_refs)
            VALUES
                (:tenantId, :diagnosticoId, :skillId, :preguntaId,
                 CAST(:prioridad AS siginex.prioridad_reco), :texto, CAST(:normaRefs AS jsonb))
            RETURNING id
            """;

    private static final String INSERT_TAREA = """
            INSERT INTO siginex.tarea_mejora
                (tenant_id, diagnostico_id, recomendacion_id, skill_id, pregunta_id, brecha,
                 accion, norma_ref, prioridad, responsable, plazo_dias, fase, criterio_cierre)
            VALUES
                (:tenantId, :diagnosticoId, :recomendacionId, :skillId, :preguntaId, :brecha,
                 :accion, :normaRef, CAST(:prioridad AS siginex.prioridad_reco), :responsable,
                 :plazoDias, :fase, :criterioCierre)
            """;

    private static final String INSERT_RUTA = """
            INSERT INTO siginex.ruta_aprendizaje
                (tenant_id, diagnostico_id, skill_id, brecha, origen)
            VALUES
                (:tenantId, :diagnosticoId, :skillId, :brecha, CAST(:origen AS siginex.origen_ruta))
            """;

    // Lecturas (Fase 2d, diseño §9.8). Pilares en el ORDEN CANÓNICO de módulo:
    // JOIN a kb_modulo (global, sin RLS) por skill_id y ORDER BY km.orden. Es una
    // divergencia intencional SOLO de orden respecto al TS (que lee sin ORDER BY);
    // no altera ningún valor.
    private static final String LISTAR_PILARES = """
            SELECT rp.skill_id, rp.cumplimiento_pct, rp.nivel, rp.banda
            FROM siginex.resultado_pilar rp
            INNER JOIN siginex.kb_modulo km ON km.id = rp.skill_id
            WHERE rp.diagnostico_id = :diagnosticoId
            ORDER BY km.orden
            """;

    // Recomendaciones de prioridad alta con orden determinista (skill_id, pregunta_id):
    // ambos son estables por fila dentro del diagnóstico (creado_en no desempata al
    // compartir transacción). Divergencia intencional de orden respecto al TS.
    private static final String LISTAR_RECOMENDACIONES_ALTA = """
            SELECT skill_id, prioridad, texto
            FROM siginex.recomendacion
            WHERE diagnostico_id = :diagnosticoId AND prioridad = 'alta'
            ORDER BY skill_id, pregunta_id
            """;

    // Plan de mejora ordenado por prioridad, skill_id. El enum prioridad_reco
    // ordena por declaración (alta < media < baja): es PARIDAD ESTRICTA con el
    // ORDER BY del TS (obtenerPlanMejora).
    private static final String LISTAR_PLAN_MEJORA = """
            SELECT skill_id, pregunta_id, brecha, accion, norma_ref, prioridad,
                   responsable, plazo_dias, fase, criterio_cierre
            FROM siginex.tarea_mejora
            WHERE diagnostico_id = :diagnosticoId
            ORDER BY prioridad, skill_id
            """;

    private final NamedParameterJdbcTemplate jdbc;
    private final ObjectMapper objectMapper;

    @Override
    public void borrarResultados(UUID tenantId, UUID diagnosticoId) {
        MapSqlParameterSource parameters = new MapSqlParameterSource("diagnosticoId", diagnosticoId);
        jdbc.update(DELETE_PILAR, parameters);
        jdbc.update(DELETE_TAREA, parameters);
        jdbc.update(DELETE_RUTA, parameters);
        jdbc.update(DELETE_RECOMENDACION, parameters);
    }

    @Override
    public void insertarPilar(UUID tenantId, UUID diagnosticoId, ResultadoModulo pilar) {
        Nivel nivel = pilar.getNivel();
        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("tenantId", tenantId)
                .addValue("diagnosticoId", diagnosticoId)
                .addValue("skillId", pilar.getModulo())
                // score y cumplimiento_pct guardan el MISMO valor (fiel al TS).
                .addValue("score", numeric(pilar.getCumplimiento()))
                .addValue("cumplimientoPct", numeric(pilar.getCumplimiento()))
                .addValue("nivel", nivel != null ? nivel.getN() : null)
                .addValue("banda", nivel != null ? nivel.getNombre() : null);
        jdbc.update(INSERT_PILAR, parameters);
    }

    @Override
    public UUID insertarRecomendacion(UUID tenantId, UUID diagnosticoId, Brecha brecha) {
        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("tenantId", tenantId)
                .addValue("diagnosticoId", diagnosticoId)
                .addValue("skillId", brecha.getModulo())
                .addValue("preguntaId", brecha.getPreguntaId())
                .addValue("prioridad", brecha.getPrioridad())
                .addValue("texto", brecha.getBrecha())
                .addValue("normaRefs", normaRefs(brecha.getNorma()));
        return jdbc.queryForObject(INSERT_RECOMENDACION, parameters, UUID.class);
    }

    @Override
    public void insertarTarea(UUID tenantId, UUID diagnosticoId, UUID recomendacionId,
                              String brecha, TareaPlan tarea) {
        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("tenantId", tenantId)
                .addValue("diagnosticoId", diagnosticoId)
                .addValue("recomendacionId", recomendacionId)
                .addValue("skillId", tarea.getModulo())
                .addValue("preguntaId", tarea.getPreguntaId())
                .addValue("brecha", brecha)
                .addValue("accion", tarea.getAccion())
                .addValue("normaRef", tarea.getNorma())
                .addValue("prioridad", tarea.getPrioridad())
                .addValue("responsable", tarea.getResponsable())
                .addValue("plazoDias", tarea.getPlazoDias())
                .addValue("fase", tarea.getFase())
                .addValue("criterioCierre", tarea.getCriterioCierre());
        jdbc.update(INSERT_TAREA, parameters);
    }

    @Override
    public void insertarRuta(UUID tenantId, UUID diagnosticoId, RutaAprendizajePlan ruta) {
        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("tenantId", tenantId)
                .addValue("diagnosticoId", diagnosticoId)
                .addValue("skillId", ruta.getModulo())
                .addValue("brecha", ruta.getCompetencia())
                .addValue("origen", ruta.getOrigen());
        jdbc.update(INSERT_RUTA, parameters);
    }

    @Override
    public List<ResultadoLectura.PilarLectura> listarPilares(UUID diagnosticoId) {
        return jdbc.query(LISTAR_PILARES,
                new MapSqlParameterSource("diagnosticoId", diagnosticoId), PILAR_ROW_MAPPER);
    }

    @Override
    public List<ResultadoLectura.RecomendacionLectura> listarRecomendacionesAlta(UUID diagnosticoId) {
        return jdbc.query(LISTAR_RECOMENDACIONES_ALTA,
                new MapSqlParameterSource("diagnosticoId", diagnosticoId), RECOMENDACION_ROW_MAPPER);
    }

    @Override
    public List<TareaPlanLectura> listarPlanMejora(UUID diagnosticoId) {
        return jdbc.query(LISTAR_PLAN_MEJORA,
                new MapSqlParameterSource("diagnosticoId", diagnosticoId), TAREA_ROW_MAPPER);
    }

    private static final RowMapper<ResultadoLectura.PilarLectura> PILAR_ROW_MAPPER = (rs, rowNum) ->
            ResultadoLectura.PilarLectura.builder()
                    .skillId(rs.getString("skill_id"))
                    .cumplimientoPct(rs.getBigDecimal("cumplimiento_pct"))
                    .nivel(integer(rs, "nivel"))
                    .banda(rs.getString("banda"))
                    .build();

    private static final RowMapper<ResultadoLectura.RecomendacionLectura> RECOMENDACION_ROW_MAPPER =
            (rs, rowNum) -> ResultadoLectura.RecomendacionLectura.builder()
                    .skillId(rs.getString("skill_id"))
                    .prioridad(rs.getString("prioridad"))
                    .texto(rs.getString("texto"))
                    .build();

    private static final RowMapper<TareaPlanLectura> TAREA_ROW_MAPPER = (rs, rowNum) ->
            TareaPlanLectura.builder()
                    .skillId(rs.getString("skill_id"))
                    .preguntaId(rs.getString("pregunta_id"))
                    .brecha(rs.getString("brecha"))
                    .accion(rs.getString("accion"))
                    .normaRef(rs.getString("norma_ref"))
                    .prioridad(rs.getString("prioridad"))
                    .responsable(rs.getString("responsable"))
                    .plazoDias(integer(rs, "plazo_dias"))
                    .fase(integer(rs, "fase"))
                    .criterioCierre(rs.getString("criterio_cierre"))
                    .build();

    /** Lee una columna entera nullable ({@code smallint}/{@code integer}) como {@link Integer}. */
    private static Integer integer(ResultSet rs, String column) throws SQLException {
        int value = rs.getInt(column);
        return rs.wasNull() ? null : value;
    }

    /**
     * Convierte un {@code Double} ya redondeado a 2 decimales a {@link BigDecimal}
     * sin reintroducir ruido binario ({@code Double.toString}); {@code null}
     * persiste {@code NULL}.
     */
    private static BigDecimal numeric(Double value) {
        return value == null ? null : new BigDecimal(Double.toString(value));
    }

    /**
     * Serializa {@code norma_refs} como jsonb: lista vacía si la norma es nula,
     * o con un único elemento (fiel a {@code b.norma ? [b.norma] : []} del TS).
     */
    private String normaRefs(String norma) {
        List<String> refs = norma != null ? List.of(norma) : List.of();
        try {
            return objectMapper.writeValueAsString(refs);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("No se pudo serializar norma_refs a jsonb", e);
        }
    }
}
