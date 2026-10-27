package com.siginex.api.service;

import com.siginex.api.domain.calculo.Benchmark;
import com.siginex.api.domain.calculo.ContextoCalculo;
import com.siginex.api.domain.calculo.ResultadoConsolidado;
import com.siginex.api.domain.calculo.ResultadoLectura;
import com.siginex.api.domain.calculo.TareaPlanLectura;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Acceso a datos del cálculo del diagnóstico SIGINEX: ESTRICTAMENTE lecturas y
 * escrituras a base de datos. No contiene validaciones de negocio ni ejecuta el
 * motor de cálculo: esa orquestación/validación vive en
 * {@link com.siginex.api.delegate.CalculoDelegate}. Implementado por
 * {@link com.siginex.api.service.CalculoServiceImpl}.
 */
public interface CalculoService {

    /**
     * LECTURA. Carga el contexto de entrada del cálculo de un diagnóstico dentro
     * de {@code runInTenant}: cabecera del diagnóstico (RLS exige pertenencia al
     * tenant), organización dueña, respuestas normalizadas al tipo del motor, y
     * el KB de cálculo (global). Si la cabecera o la organización no existen en
     * el tenant → {@link com.siginex.api.exception.DominioException} 404.
     *
     * @param tenantId      tenant del actor (RLS); nunca del cliente.
     * @param diagnosticoId id del diagnóstico.
     * @return el contexto (empresa + KB + respuestas) listo para el motor.
     */
    ContextoCalculo cargarContextoCalculo(UUID tenantId, UUID diagnosticoId);

    /**
     * ESCRITURA ATÓMICA. Reescribe idempotentemente los resultados ya calculados
     * de un diagnóstico dentro de una ÚNICA transacción por tenant
     * ({@code runInTenant}): borra pilares/recomendaciones/tareas/rutas previos,
     * reinserta los nuevos, actualiza la cabecera ({@code estado='calculado'},
     * score/nivel/cumplimiento/completado_en) y registra el evento de auditoría
     * {@code 'diagnostico.calculado'} (actor {@code 'api'}). No valida ni calcula:
     * recibe el {@link ResultadoConsolidado} ya producido y validado por el
     * Delegate. La atomicidad garantiza que un fallo no deje resultados a medias.
     *
     * @param tenantId      tenant del actor (RLS).
     * @param diagnosticoId id del diagnóstico.
     * @param resultado     resultado consolidado ya calculado y con contrato válido.
     */
    void persistirResultado(UUID tenantId, UUID diagnosticoId, ResultadoConsolidado resultado);

    /**
     * LECTURA. Lee el resultado consolidado de un diagnóstico ya calculado dentro
     * de {@code runInTenant}: pilares en orden canónico de módulo y
     * recomendaciones de prioridad alta con orden determinista (diseño §9.8). Si
     * la cabecera no existe en el tenant → {@link com.siginex.api.exception.DominioException} 404.
     */
    ResultadoLectura obtenerResultado(UUID tenantId, UUID diagnosticoId);

    /**
     * LECTURA. Lee las tareas del plan de mejora de un diagnóstico, ordenadas por
     * prioridad ({@code alta} primero) y {@code skill_id}. Corre dentro de
     * {@code runInTenant}.
     */
    List<TareaPlanLectura> obtenerPlanMejora(UUID tenantId, UUID diagnosticoId);

    /**
     * ESCRITURA. Persiste el nivel tecnológico ({@code 1..5}) del diagnóstico
     * (idempotente: sobrescribe) dentro de {@code runInTenant}. NO valida el
     * rango: esa validación de negocio la hace el Delegate antes de llamar aquí.
     */
    void guardarNivelTecnologico(UUID tenantId, UUID diagnosticoId, int nivel);

    /**
     * LECTURA. Devuelve el {@code nivel_tecnologico} declarado del diagnóstico, o
     * {@link Optional#empty()} si no hay (o si la RLS no ve el diagnóstico). Corre
     * dentro de {@code runInTenant}.
     */
    Optional<Integer> obtenerNivelTecnologico(UUID tenantId, UUID diagnosticoId);
}
