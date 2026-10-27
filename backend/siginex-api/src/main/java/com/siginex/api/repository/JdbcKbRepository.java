package com.siginex.api.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.siginex.api.domain.KbModulo;
import com.siginex.api.domain.KbPregunta;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * JDBC implementation of {@link KbRepository} using
 * {@link NamedParameterJdbcTemplate}. The KB tables ({@code kb_modulo},
 * {@code kb_pregunta}) are GLOBAL: they have no tenant and no RLS, so the reads
 * run without fixing any tenant. {@code recomendacion} ({@code jsonb}) is parsed
 * back into a {@code Map<String,String>} with the shared {@link ObjectMapper}.
 */
@Repository
@RequiredArgsConstructor
public class JdbcKbRepository implements KbRepository {

    private static final String LISTAR_MODULOS = """
            SELECT id, nombre, short, weight, escala, orden
            FROM siginex.kb_modulo
            ORDER BY orden
            """;

    private static final String LISTAR_PREGUNTAS = """
            SELECT id, pregunta, norma, articulo, requisito, criterio, peso, recomendacion, orden
            FROM siginex.kb_pregunta
            WHERE modulo_id = :moduloId
            ORDER BY orden
            """;

    private final NamedParameterJdbcTemplate jdbc;
    private final ObjectMapper objectMapper;

    @Override
    public List<KbModulo> listarModulos() {
        return jdbc.query(LISTAR_MODULOS, moduloRowMapper());
    }

    @Override
    public List<KbPregunta> listarPreguntas(String moduloId) {
        return jdbc.query(LISTAR_PREGUNTAS,
                new MapSqlParameterSource("moduloId", moduloId), preguntaRowMapper());
    }

    private static RowMapper<KbModulo> moduloRowMapper() {
        return (rs, rowNum) -> KbModulo.builder()
                .id(rs.getString("id"))
                .nombre(rs.getString("nombre"))
                .shortNombre(rs.getString("short"))
                .weight(rs.getString("weight"))
                .escala(rs.getString("escala"))
                .orden(integer(rs, "orden"))
                .build();
    }

    private RowMapper<KbPregunta> preguntaRowMapper() {
        return (rs, rowNum) -> KbPregunta.builder()
                .id(rs.getString("id"))
                .pregunta(rs.getString("pregunta"))
                .norma(rs.getString("norma"))
                .articulo(rs.getString("articulo"))
                .requisito(rs.getString("requisito"))
                .criterio(rs.getString("criterio"))
                .peso(rs.getString("peso"))
                .recomendacion(parseRecomendacion(rs.getString("recomendacion")))
                .orden(integer(rs, "orden"))
                .build();
    }

    private Map<String, String> parseRecomendacion(String json) {
        if (json == null || json.isBlank()) {
            return Map.of();
        }
        try {
            return objectMapper.readValue(json,
                    objectMapper.getTypeFactory().constructMapType(Map.class, String.class, String.class));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("recomendacion jsonb invalida en kb_pregunta", e);
        }
    }

    private static Integer integer(ResultSet rs, String column) throws SQLException {
        int value = rs.getInt(column);
        return rs.wasNull() ? null : value;
    }
}
