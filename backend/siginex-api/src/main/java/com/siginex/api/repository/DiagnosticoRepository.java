package com.siginex.api.repository;

import com.siginex.api.domain.DiagnosticoDetalle;
import com.siginex.api.domain.DiagnosticoResumen;
import com.siginex.api.domain.calculo.ResultadoLectura;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Data access for diagnosticos. Every method runs under the actor's tenant: the
 * service wraps the calls in
 * {@code TenantTransactionTemplate.runInTenant(tenantId, ...)} so PostgreSQL
 * Row-Level Security isolates the rows. SQL only; transaction and tenant
 * scoping are the service's concern.
 *
 * <p>{@link #kbVersionVigente()} reads the global {@code kb_version} table
 * (no RLS) and returns {@code 3.0.0} when it is empty, faithful to the original
 * {@code kbVersionVigente}.
 */
public interface DiagnosticoRepository {

    String kbVersionVigente();

    DiagnosticoDetalle insertar(UUID tenantId, UUID organizacionId, String kbVersion);

    List<DiagnosticoResumen> listar();

    Optional<DiagnosticoDetalle> buscarPorId(UUID id);

    /**
     * Fase 2d: marca el diagnostico como {@code 'calculado'} y refresca el
     * resultado agregado tras recalcular. Corre dentro de {@code runInTenant},
     * de modo que la RLS exige que la fila pertenezca al tenant del actor. Los
     * numéricos se pasan como {@link BigDecimal} construido desde el
     * {@code double} ya redondeado a 2 decimales (frontera JDBC); {@code null}
     * persiste {@code NULL}.
     *
     * @param tenantId           tenant del actor (RLS).
     * @param diagnosticoId      id del diagnostico a actualizar.
     * @param scoreSgi           score SGI redondeado (o {@code null}).
     * @param nivelSgi           nivel SGI {@code 1..5} (o {@code null}).
     * @param cumplimientoGlobal cumplimiento global redondeado (o {@code null}).
     */
    void actualizarResultado(UUID tenantId, UUID diagnosticoId,
                             BigDecimal scoreSgi, Integer nivelSgi,
                             BigDecimal cumplimientoGlobal);

    /**
     * Fase 2d: registra el {@code nivel_tecnologico} de la empresa en el
     * diagnostico ({@code registrarBenchmark}). Idempotente: sobrescribe el
     * nivel. Corre dentro de {@code runInTenant}, de modo que la RLS exige que
     * la fila pertenezca al tenant del actor. El {@code nivel} (1..5) se castea
     * al enum {@code siginex.nivel_tecnologico} siguiendo el patrón de los repos.
     *
     * @param tenantId      tenant del actor (RLS).
     * @param diagnosticoId id del diagnostico a actualizar.
     * @param nivel         nivel tecnológico {@code 1..5} (validado en el service).
     */
    void actualizarNivelTecnologico(UUID tenantId, UUID diagnosticoId, int nivel);

    /**
     * Fase 2d: lee la cabecera agregada del diagnóstico para
     * {@code obtenerResultado} ({@code score_sgi}, {@code nivel_sgi},
     * {@code cumplimiento_global} y el nombre de la organización vía JOIN), port
     * del primer {@code SELECT} de {@code diagnostico-service.ts::obtenerResultado}.
     * Corre dentro de {@code runInTenant}, de modo que la RLS exige pertenencia al
     * tenant; devuelve {@link Optional#empty()} si no existe en el tenant. Los
     * pilares y recomendaciones del {@link ResultadoLectura} los completa el
     * service desde {@link ResultadoRepository}.
     */
    Optional<ResultadoLectura> buscarCabeceraResultado(UUID diagnosticoId);
}
