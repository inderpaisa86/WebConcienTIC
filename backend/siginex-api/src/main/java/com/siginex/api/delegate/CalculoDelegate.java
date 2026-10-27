package com.siginex.api.delegate;

import com.siginex.api.domain.SiginexPrincipal;
import com.siginex.api.domain.calculo.Benchmark;
import com.siginex.api.domain.calculo.ResultadoLectura;
import com.siginex.api.domain.calculo.TareaPlanLectura;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Coordinación de negocio para el cálculo, resultado, plan de mejora y
 * benchmark de un diagnóstico. Resuelve el tenant del {@link SiginexPrincipal}
 * autenticado (nunca del cliente) y verifica que el diagnóstico pertenezca al
 * tenant del actor antes de delegar en {@link com.siginex.api.service.CalculoService}.
 * Implementado por {@link CalculoDelegateImpl}.
 */
public interface CalculoDelegate {

    /**
     * Recalcula el diagnóstico y devuelve su resultado consolidado (leído de BD
     * con orden determinista).
     *
     * @param actor         principal autenticado (aporta el tenant).
     * @param diagnosticoId id del diagnóstico.
     * @return el resultado tras recalcular.
     */
    ResultadoLectura calcular(SiginexPrincipal actor, UUID diagnosticoId);

    /**
     * Lee el resultado consolidado de un diagnóstico ya calculado.
     *
     * @param actor         principal autenticado (aporta el tenant).
     * @param diagnosticoId id del diagnóstico.
     * @return el resultado leído.
     */
    ResultadoLectura obtenerResultado(SiginexPrincipal actor, UUID diagnosticoId);

    /**
     * Lee las tareas del plan de mejora (orden por prioridad y skill_id).
     *
     * @param actor         principal autenticado (aporta el tenant).
     * @param diagnosticoId id del diagnóstico.
     * @return las tareas priorizadas (vacía si no hay).
     */
    List<TareaPlanLectura> obtenerPlanMejora(SiginexPrincipal actor, UUID diagnosticoId);

    /**
     * Registra el nivel tecnológico de la empresa ({@code 1..5}) y devuelve el
     * benchmark de mercado.
     *
     * @param actor         principal autenticado (aporta el tenant).
     * @param diagnosticoId id del diagnóstico.
     * @param nivel         nivel tecnológico {@code 1..5}.
     * @return el benchmark para el nivel registrado.
     */
    Benchmark registrarBenchmark(SiginexPrincipal actor, UUID diagnosticoId, int nivel);

    /**
     * Devuelve el benchmark si el diagnóstico ya declaró nivel tecnológico.
     *
     * @param actor         principal autenticado (aporta el tenant).
     * @param diagnosticoId id del diagnóstico.
     * @return el benchmark, o vacío si no hay nivel declarado.
     */
    Optional<Benchmark> obtenerBenchmark(SiginexPrincipal actor, UUID diagnosticoId);
}
