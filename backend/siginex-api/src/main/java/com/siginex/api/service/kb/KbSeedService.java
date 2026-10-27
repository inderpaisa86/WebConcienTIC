package com.siginex.api.service.kb;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * Idempotent seed of the GLOBAL knowledge bank (KB) from
 * {@code classpath:kb/banco.json} into {@code siginex.kb_modulo} /
 * {@code siginex.kb_pregunta}.
 *
 * <p>The KB is global and shared by every tenant, so this service reads the
 * {@link NamedParameterJdbcTemplate} directly and never uses
 * {@code runInTenant}: there is no tenant and no Row-Level Security on these
 * tables.
 *
 * <p>Idempotency: {@link #seed()} does nothing when the bank's {@code kb_version}
 * already has modules loaded; otherwise it upserts every module and question
 * ({@code ON CONFLICT (id) DO UPDATE}). Re-running it never duplicates rows.
 * {@code escala == ""} is normalized to {@code null}; {@code recomendacion} is
 * written as {@code jsonb} via an explicit CAST. The module/question order of
 * {@code banco.json} is preserved in the {@code orden} columns.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class KbSeedService {

    private static final String BANCO_RESOURCE = "kb/banco.json";

    private static final String COUNT_MODULOS =
            "SELECT count(*) FROM siginex.kb_modulo WHERE kb_version = :kbVersion";

    private static final String UPSERT_MODULO = """
            INSERT INTO siginex.kb_modulo (id, kb_version, nombre, short, weight, escala, orden)
            VALUES (:id, :kbVersion, :nombre, :short, :weight, :escala, :orden)
            ON CONFLICT (id) DO UPDATE SET
                kb_version = EXCLUDED.kb_version,
                nombre     = EXCLUDED.nombre,
                short      = EXCLUDED.short,
                weight     = EXCLUDED.weight,
                escala     = EXCLUDED.escala,
                orden      = EXCLUDED.orden
            """;

    private static final String UPSERT_PREGUNTA = """
            INSERT INTO siginex.kb_pregunta
                (id, modulo_id, kb_version, pregunta, norma, articulo, requisito,
                 criterio, peso, recomendacion, orden)
            VALUES
                (:id, :moduloId, :kbVersion, :pregunta, :norma, :articulo, :requisito,
                 :criterio, :peso, CAST(:recomendacion AS jsonb), :orden)
            ON CONFLICT (id) DO UPDATE SET
                modulo_id     = EXCLUDED.modulo_id,
                kb_version    = EXCLUDED.kb_version,
                pregunta      = EXCLUDED.pregunta,
                norma         = EXCLUDED.norma,
                articulo      = EXCLUDED.articulo,
                requisito     = EXCLUDED.requisito,
                criterio      = EXCLUDED.criterio,
                peso          = EXCLUDED.peso,
                recomendacion = EXCLUDED.recomendacion,
                orden         = EXCLUDED.orden
            """;

    private final NamedParameterJdbcTemplate jdbc;
    private final ObjectMapper objectMapper;

    /**
     * Loads {@code banco.json} and upserts it when the bank's {@code kb_version}
     * is not yet present. Global operation (no tenant, no RLS). Returns the bank
     * actually read so callers can log/verify counts.
     */
    public BancoJson seed() {
        BancoJson banco = readBanco();
        String kbVersion = banco.getKbVersion();

        Integer existentes = jdbc.queryForObject(COUNT_MODULOS,
                new MapSqlParameterSource("kbVersion", kbVersion), Integer.class);
        if (existentes != null && existentes > 0) {
            log.info("KB seed: kb_version {} already loaded ({} modulos); skipping.", kbVersion, existentes);
            return banco;
        }

        List<BancoModuloJson> modulos = banco.getModulos();
        int preguntas = 0;
        for (int m = 0; m < modulos.size(); m++) {
            BancoModuloJson modulo = modulos.get(m);
            jdbc.update(UPSERT_MODULO, moduloParams(modulo, kbVersion, m));

            List<BancoPreguntaJson> preguntasModulo = modulo.getPreguntas();
            for (int p = 0; p < preguntasModulo.size(); p++) {
                jdbc.update(UPSERT_PREGUNTA,
                        preguntaParams(preguntasModulo.get(p), modulo.getId(), kbVersion, p));
                preguntas++;
            }
        }
        log.info("KB seed: loaded kb_version {} -> {} modulos, {} preguntas.",
                kbVersion, modulos.size(), preguntas);
        return banco;
    }

    private BancoJson readBanco() {
        try (InputStream in = new ClassPathResource(BANCO_RESOURCE).getInputStream()) {
            return objectMapper.readValue(in, BancoJson.class);
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo leer el banco KB desde " + BANCO_RESOURCE, e);
        }
    }

    private static MapSqlParameterSource moduloParams(BancoModuloJson modulo, String kbVersion, int orden) {
        return new MapSqlParameterSource()
                .addValue("id", modulo.getId())
                .addValue("kbVersion", kbVersion)
                .addValue("nombre", modulo.getNombre())
                .addValue("short", modulo.getShortNombre())
                .addValue("weight", modulo.getWeight())
                .addValue("escala", normalizar(modulo.getEscala()))
                .addValue("orden", orden);
    }

    private MapSqlParameterSource preguntaParams(BancoPreguntaJson pregunta, String moduloId,
                                                 String kbVersion, int orden) {
        return new MapSqlParameterSource()
                .addValue("id", pregunta.getId())
                .addValue("moduloId", moduloId)
                .addValue("kbVersion", kbVersion)
                .addValue("pregunta", pregunta.getPregunta())
                .addValue("norma", pregunta.getNorma())
                .addValue("articulo", pregunta.getArticulo())
                .addValue("requisito", pregunta.getRequisito())
                .addValue("criterio", pregunta.getCriterio())
                .addValue("peso", pregunta.getPeso())
                .addValue("recomendacion", toJson(pregunta.getRecomendacion()))
                .addValue("orden", orden);
    }

    /** Empty strings (e.g. the escala of esg / gobernanza_ia) become NULL. */
    private static String normalizar(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : value;
    }

    private String toJson(Map<String, String> recomendacion) {
        Map<String, String> value = recomendacion == null ? Map.of() : recomendacion;
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("No se pudo serializar recomendacion del banco KB", e);
        }
    }
}
