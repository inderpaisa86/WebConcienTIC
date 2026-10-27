package com.siginex.api.service;

import com.siginex.api.domain.calculo.Benchmark;
import com.siginex.api.domain.calculo.ResultadoConsolidado;
import com.siginex.api.domain.calculo.ResultadoLectura;
import com.siginex.api.domain.calculo.TareaPlanLectura;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Orquestación de alto nivel del cálculo del diagnóstico SIGINEX. Port de
 * {@code diagnostico-service.ts::calcularDiagnostico}. Implementado por
 * {@link com.siginex.api.service.CalculoServiceImpl}.
 */
public interface CalculoService {

    /**
     * Recalcula el diagnóstico y reescribe idempotentemente sus resultados
     * dentro de una única transacción por tenant ({@code runInTenant}): lee
     * respuestas + KB + aplicabilidad, corre el motor puro, valida el contrato
     * ANTES de tocar tablas, reescribe pilares/recomendaciones/tareas/rutas,
     * actualiza la cabecera ({@code estado='calculado'}, score/nivel/
     * cumplimiento/completado_en) y registra el evento de auditoría
     * {@code 'diagnostico.calculado'} con actor {@code 'api'}.
     *
     * @param tenantId      tenant del actor (RLS); nunca del cliente.
     * @param diagnosticoId id del diagnóstico a calcular.
     * @return el resultado consolidado recién calculado.
     */
    ResultadoConsolidado calcular(UUID tenantId, UUID diagnosticoId);

    /**
     * Lee el resultado consolidado de un diagnóstico ya calculado. Port de
     * {@code diagnostico-service.ts::obtenerResultado}. Corre dentro de
     * {@code runInTenant}: los pilares salen en el orden canónico de módulo y
     * las recomendaciones de prioridad alta con orden determinista (diseño §9.8).
     * Si la cabecera no existe en el tenant → {@link DominioException} 404.
     *
     * @param tenantId      tenant del actor (RLS).
     * @param diagnosticoId id del diagnóstico.
     * @return el resultado leído.
     */
    ResultadoLectura obtenerResultado(UUID tenantId, UUID diagnosticoId);

    /**
     * Lee las tareas del plan de mejora de un diagnóstico, ordenadas por
     * prioridad ({@code alta} primero) y {@code skill_id}. Port de
     * {@code diagnostico-service.ts::obtenerPlanMejora} (paridad estricta de
     * orden). Corre dentro de {@code runInTenant}.
     *
     * @param tenantId      tenant del actor (RLS).
     * @param diagnosticoId id del diagnóstico.
     * @return las tareas priorizadas (vacía si no hay).
     */
    List<TareaPlanLectura> obtenerPlanMejora(UUID tenantId, UUID diagnosticoId);

    /**
     * Registra el nivel tecnológico de la empresa (1..5) en el diagnóstico y
     * devuelve el benchmark de mercado. Port de
     * {@code diagnostico-service.ts::registrarBenchmark}. Idempotente: sobrescribe
     * el nivel. Valida {@code 1 <= nivel <= 5} y corre dentro de {@code runInTenant}.
     *
     * @param tenantId      tenant del actor (RLS).
     * @param diagnosticoId id del diagnóstico.
     * @param nivel         nivel tecnológico {@code 1..5}.
     * @return el benchmark para el nivel registrado.
     */
    Benchmark registrarBenchmark(UUID tenantId, UUID diagnosticoId, int nivel);

    /**
     * Devuelve el benchmark de mercado si el diagnóstico ya tiene
     * {@code nivel_tecnologico} declarado. Port de
     * {@code diagnostico-service.ts::obtenerBenchmark}: {@link Optional#empty()}
     * si no hay nivel. Corre dentro de {@code runInTenant}.
     *
     * @param tenantId      tenant del actor (RLS).
     * @param diagnosticoId id del diagnóstico.
     * @return el benchmark, o vacío si no hay nivel declarado.
     */
    Optional<Benchmark> obtenerBenchmark(UUID tenantId, UUID diagnosticoId);
}
