package com.siginex.api.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.siginex.api.config.TenantTransactionTemplate;
import com.siginex.api.domain.DiagnosticoDetalle;
import com.siginex.api.domain.Organizacion;
import com.siginex.api.domain.calculo.Aplicabilidad;
import com.siginex.api.domain.calculo.Benchmark;
import com.siginex.api.domain.calculo.Brecha;
import com.siginex.api.domain.calculo.CalculoEmpresa;
import com.siginex.api.domain.calculo.CalculoKb;
import com.siginex.api.domain.calculo.ResultadoConsolidado;
import com.siginex.api.domain.calculo.ResultadoLectura;
import com.siginex.api.domain.calculo.ResultadoModulo;
import com.siginex.api.domain.calculo.RutaAprendizajePlan;
import com.siginex.api.domain.calculo.TareaPlan;
import com.siginex.api.domain.calculo.TareaPlanLectura;
import com.siginex.api.domain.calculo.ValorRespuesta;
import com.siginex.api.exception.DominioException;
import com.siginex.api.repository.DiagnosticoRepository;
import com.siginex.api.repository.KbRepository;
import com.siginex.api.repository.OrganizacionRepository;
import com.siginex.api.repository.RespuestaRepository;
import com.siginex.api.repository.ResultadoRepository;
import com.siginex.api.service.calculo.AplicabilidadParser;
import com.siginex.api.service.calculo.CalculoBenchmark;
import com.siginex.api.service.calculo.MotorCalculo;
import com.siginex.api.service.calculo.ValorRespuestaMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementación por defecto de {@link CalculoService}. Port de
 * {@code diagnostico-service.ts::calcularDiagnostico}. Toda la orquestación
 * corre dentro de una única transacción por tenant
 * ({@link TenantTransactionTemplate#runInTenant}), de modo que la reescritura es
 * atómica y la RLS aísla por tenant.
 *
 * <p>El KB ({@code kb_modulo}/{@code kb_pregunta}) es global (sin RLS); se lee
 * dentro de la misma unidad de trabajo para mantener una sola ruta de datos. El
 * contrato se valida ANTES de cualquier {@code DELETE}/{@code INSERT}/
 * {@code UPDATE} y antes del evento de auditoría: si falla, se lanza
 * {@link DominioException} y la transacción revierte sin dejar filas ni evento.
 */
@Service
@RequiredArgsConstructor
public class CalculoServiceImpl implements CalculoService {

    private final TenantTransactionTemplate tenantTx;
    private final DiagnosticoRepository diagnosticoRepository;
    private final OrganizacionRepository organizacionRepository;
    private final RespuestaRepository respuestaRepository;
    private final KbRepository kbRepository;
    private final ResultadoRepository resultadoRepository;
    private final MotorCalculo motorCalculo;
    private final AuditoriaService auditoriaService;
    private final ObjectMapper objectMapper;

    @Override
    public ResultadoConsolidado calcular(UUID tenantId, UUID diagnosticoId) {
        return tenantTx.runInTenant(tenantId, () -> {
            // (1) Cabecera del diagnostico (RLS exige pertenencia al tenant).
            DiagnosticoDetalle diag = diagnosticoRepository.buscarPorId(diagnosticoId)
                    .orElseThrow(() -> new DominioException(HttpStatus.NOT_FOUND,
                            "Diagnóstico no encontrado."));

            // (2) Organizacion dueña del diagnostico.
            Organizacion org = organizacionRepository.buscarPorId(diag.getOrganizacionId())
                    .orElseThrow(() -> new DominioException(HttpStatus.NOT_FOUND,
                            "Organización no encontrada."));

            // (3) Respuestas normalizadas al tipo del motor (omitiendo las vacías).
            Map<String, String> crudas = respuestaRepository.listarPorDiagnostico(diagnosticoId);
            Map<String, ValorRespuesta> answers = new java.util.LinkedHashMap<>();
            for (Map.Entry<String, String> e : crudas.entrySet()) {
                Optional<ValorRespuesta> v = ValorRespuestaMapper.aValorOrquestador(e.getValue());
                v.ifPresent(valor -> answers.put(e.getKey(), valor));
            }

            // (4) Aplicabilidad desde el jsonb de la organizacion.
            Aplicabilidad aplicabilidad = AplicabilidadParser.parse(parseJsonbObjeto(org.getAplicabilidad()));

            // (5) KB de cálculo (global, sin RLS).
            CalculoKb kb = CalculoKb.from(kbRepository.listarModulos(), kbRepository::listarPreguntas);

            // (6) Perfil de la empresa.
            CalculoEmpresa empresa = CalculoEmpresa.builder()
                    .nombre(org.getNombre())
                    .sector(org.getSector())
                    .tamano(org.getTamano())
                    .nivelTecnologico(diag.getNivelTecnologico())
                    .aplicabilidad(aplicabilidad)
                    .build();

            // (7) Motor puro.
            ResultadoConsolidado resultado = motorCalculo.run(kb, empresa, answers);

            // (8) Validación de contrato ANTES de tocar tablas o auditoría.
            List<String> faltan = motorCalculo.validarContrato(resultado);
            if (!faltan.isEmpty()) {
                throw new DominioException(HttpStatus.UNPROCESSABLE_CONTENT,
                        "Contrato inválido: " + String.join(", ", faltan));
            }

            // (9) Reescritura idempotente: borrar resultados previos.
            resultadoRepository.borrarResultados(tenantId, diagnosticoId);

            // (10) Pilares por cada módulo.
            for (ResultadoModulo pilar : resultado.getPorModulo()) {
                resultadoRepository.insertarPilar(tenantId, diagnosticoId, pilar);
            }

            // (11) Recomendaciones por brecha + tarea emparejada por pregunta_id.
            for (Brecha brecha : resultado.getBrechas()) {
                UUID recomendacionId =
                        resultadoRepository.insertarRecomendacion(tenantId, diagnosticoId, brecha);
                TareaPlan tarea = primeraTareaDe(resultado.getPlanMejora(), brecha.getPreguntaId());
                if (tarea != null) {
                    resultadoRepository.insertarTarea(tenantId, diagnosticoId, recomendacionId,
                            brecha.getBrecha(), tarea);
                }
            }

            // (12) Rutas de aprendizaje.
            for (RutaAprendizajePlan ruta : resultado.getPlanAprendizaje()) {
                resultadoRepository.insertarRuta(tenantId, diagnosticoId, ruta);
            }

            // (13) Actualizar la cabecera del diagnostico.
            BigDecimal score = numeric(resultado.getScoreSgi());
            Integer nivelSgi = resultado.getNivelSgi() != null ? resultado.getNivelSgi().getN() : null;
            BigDecimal cumplimiento = numeric(resultado.getCumplimientoGlobal());
            diagnosticoRepository.actualizarResultado(tenantId, diagnosticoId, score, nivelSgi, cumplimiento);

            // (14) Evento de auditoría (actor 'api', sin secretos).
            auditoriaService.registrar(tenantId, "api", "diagnostico.calculado", "diagnostico",
                    diagnosticoId.toString(),
                    Map.of("score_sgi", resultado.getScoreSgi(),
                            "brechas", resultado.getBrechas().size()));

            return resultado;
        });
    }

    @Override
    public ResultadoLectura obtenerResultado(UUID tenantId, UUID diagnosticoId) {
        return tenantTx.runInTenant(tenantId, () -> {
            // Cabecera (score/nivel/cumplimiento + nombre de la organizacion). Si la
            // RLS no la ve (otro tenant) o no existe -> 404, como el TS (return null
            // -> notFound() en la vista; aquí lo elevamos a DominioException).
            ResultadoLectura cabecera = diagnosticoRepository.buscarCabeceraResultado(diagnosticoId)
                    .orElseThrow(() -> new DominioException(HttpStatus.NOT_FOUND,
                            "Diagnóstico no encontrado."));

            // Pilares en orden canónico de módulo y recomendaciones alta ordenadas
            // (diseño §9.8); se completan sobre la cabecera leída.
            return cabecera.toBuilder()
                    .pilares(resultadoRepository.listarPilares(diagnosticoId))
                    .recomendaciones(resultadoRepository.listarRecomendacionesAlta(diagnosticoId))
                    .build();
        });
    }

    @Override
    public List<TareaPlanLectura> obtenerPlanMejora(UUID tenantId, UUID diagnosticoId) {
        return tenantTx.runInTenant(tenantId,
                () -> resultadoRepository.listarPlanMejora(diagnosticoId));
    }

    @Override
    public Benchmark registrarBenchmark(UUID tenantId, UUID diagnosticoId, int nivel) {
        // Validación de nivel (fiel al TS: entero 1..5). Defensa en profundidad:
        // el controller también valida con Bean Validation (diseño §9.6/§10).
        if (nivel < 1 || nivel > 5) {
            throw new DominioException(HttpStatus.UNPROCESSABLE_CONTENT,
                    "El nivel tecnológico debe ser un entero de 1 a 5.");
        }
        return tenantTx.runInTenant(tenantId, () -> {
            // Idempotente: sobrescribe el nivel. La RLS acota al tenant del actor.
            diagnosticoRepository.actualizarNivelTecnologico(tenantId, diagnosticoId, nivel);
            return CalculoBenchmark.calcularBenchmark(nivel);
        });
    }

    @Override
    public Optional<Benchmark> obtenerBenchmark(UUID tenantId, UUID diagnosticoId) {
        return tenantTx.runInTenant(tenantId, () -> {
            // Lee el nivel vigente de la cabecera; empty si no hay nivel declarado
            // (o si la RLS no ve el diagnostico), igual que el TS (return null).
            Integer nivel = diagnosticoRepository.buscarPorId(diagnosticoId)
                    .map(DiagnosticoDetalle::getNivelTecnologico)
                    .orElse(null);
            if (nivel == null) {
                return Optional.empty();
            }
            return Optional.of(CalculoBenchmark.calcularBenchmark(nivel));
        });
    }

    /**
     * Devuelve la primera tarea del plan cuyo {@code pregunta_id} coincide con el
     * dado (fiel al {@code .find} del TS), o {@code null} si no hay ninguna.
     */
    private static TareaPlan primeraTareaDe(List<TareaPlan> plan, String preguntaId) {
        for (TareaPlan t : plan) {
            if (preguntaId != null && preguntaId.equals(t.getPreguntaId())) {
                return t;
            }
        }
        return null;
    }

    /**
     * Deserializa el jsonb de {@code organizacion.aplicabilidad} a un
     * {@code Map<String,Object>} para el {@link AplicabilidadParser}. Un jsonb
     * ausente/vacío o que no represente un objeto se trata como mapa vacío (el
     * parser aplica entonces los 4 defaults en {@code true}).
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> parseJsonbObjeto(String json) {
        if (json == null || json.isBlank()) {
            return Map.of();
        }
        try {
            Object parsed = objectMapper.readValue(json, Object.class);
            return parsed instanceof Map ? (Map<String, Object>) parsed : Map.of();
        } catch (JsonProcessingException e) {
            // jsonb malformado: se usan los defaults (no se interrumpe el cálculo).
            return Map.of();
        }
    }

    /**
     * Convierte un {@code double} ya redondeado a 2 decimales a {@link BigDecimal}
     * en la frontera JDBC sin reintroducir ruido binario ({@code Double.toString}).
     */
    private static BigDecimal numeric(double value) {
        return new BigDecimal(Double.toString(value));
    }
}
