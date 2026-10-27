package com.siginex.api.repository;

import com.siginex.api.domain.calculo.Brecha;
import com.siginex.api.domain.calculo.ResultadoLectura;
import com.siginex.api.domain.calculo.ResultadoModulo;
import com.siginex.api.domain.calculo.RutaAprendizajePlan;
import com.siginex.api.domain.calculo.TareaPlan;
import com.siginex.api.domain.calculo.TareaPlanLectura;

import java.util.List;
import java.util.UUID;

/**
 * Reescritura <b>idempotente</b> de las tablas de resultados de un diagnóstico
 * ({@code resultado_pilar}, {@code recomendacion}, {@code tarea_mejora},
 * {@code ruta_aprendizaje}). Port de la persistencia de
 * {@code diagnostico-service.ts::calcularDiagnostico}.
 *
 * <p>Todas las operaciones corren dentro de {@code runInTenant}, de modo que la
 * RLS aísla por tenant; {@code tenant_id} se escribe explícitamente para
 * satisfacer el {@code WITH CHECK} de la política. Recalcular borra y reinserta:
 * nunca duplica filas. Implementado por {@link JdbcResultadoRepository}.
 */
public interface ResultadoRepository {

    /**
     * Borra todos los resultados de un diagnóstico en el orden del TS
     * (pilar → tarea → ruta → recomendacion) para respetar las FKs
     * ({@code tarea_mejora.recomendacion_id ON DELETE SET NULL},
     * {@code ruta_aprendizaje.recomendacion_id ON DELETE CASCADE}).
     */
    void borrarResultados(UUID tenantId, UUID diagnosticoId);

    /** Inserta un pilar ({@code score} y {@code cumplimiento_pct} guardan el mismo valor). */
    void insertarPilar(UUID tenantId, UUID diagnosticoId, ResultadoModulo pilar);

    /**
     * Inserta una recomendación y devuelve su id generado ({@code RETURNING id}).
     * {@code norma_refs} se serializa como jsonb (lista de 0 o 1 elemento).
     */
    UUID insertarRecomendacion(UUID tenantId, UUID diagnosticoId, Brecha brecha);

    /**
     * Inserta una tarea de mejora ligada a una recomendación. {@code brecha} es
     * el texto de la brecha ({@code b.brecha} del TS, igual a la pregunta) y es
     * NOT NULL en {@code tarea_mejora}; el {@link TareaPlan} no lo porta, así que
     * el service lo pasa desde la {@link Brecha} emparejada por {@code pregunta_id}.
     */
    void insertarTarea(UUID tenantId, UUID diagnosticoId, UUID recomendacionId,
                       String brecha, TareaPlan tarea);

    /** Inserta una ruta de aprendizaje. */
    void insertarRuta(UUID tenantId, UUID diagnosticoId, RutaAprendizajePlan ruta);

    /**
     * Lee los pilares de un diagnóstico en el <b>orden canónico de módulo</b>
     * (JOIN a {@code kb_modulo} por {@code skill_id}, {@code ORDER BY km.orden}):
     * {@code sst, ambiental, calidad, riesgos, cumplimiento, datos, gobierno,
     * esg, gobernanza_ia}. El TS lee {@code resultado_pilar} sin {@code ORDER BY};
     * el orden explícito es una divergencia intencional <b>solo de orden</b>
     * (no de valores), fiel al diseño §9.8.
     */
    List<ResultadoLectura.PilarLectura> listarPilares(UUID diagnosticoId);

    /**
     * Lee las recomendaciones de prioridad {@code 'alta'} de un diagnóstico con
     * orden determinista {@code ORDER BY skill_id, pregunta_id} (diseño §9.8;
     * divergencia intencional de orden respecto al TS, que lee sin {@code ORDER BY}).
     */
    List<ResultadoLectura.RecomendacionLectura> listarRecomendacionesAlta(UUID diagnosticoId);

    /**
     * Lee el plan de mejora de un diagnóstico ordenado por {@code prioridad,
     * skill_id}. El enum {@code prioridad_reco} ordena por declaración
     * ({@code alta < media < baja}), de modo que las tareas de prioridad alta van
     * primero; es <b>paridad estricta</b> con el {@code ORDER BY} del TS.
     */
    List<TareaPlanLectura> listarPlanMejora(UUID diagnosticoId);
}
