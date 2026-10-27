package com.siginex.api.delegate;

import com.siginex.api.domain.SiginexPrincipal;
import com.siginex.api.domain.calculo.Benchmark;
import com.siginex.api.domain.calculo.ContextoCalculo;
import com.siginex.api.domain.calculo.ResultadoConsolidado;
import com.siginex.api.domain.calculo.ResultadoLectura;
import com.siginex.api.domain.calculo.TareaPlanLectura;
import com.siginex.api.exception.DominioException;
import com.siginex.api.service.CalculoService;
import com.siginex.api.service.DiagnosticoService;
import com.siginex.api.service.calculo.CalculoBenchmark;
import com.siginex.api.service.calculo.MotorCalculo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementación por defecto de {@link CalculoDelegate}. Dueño de las
 * VALIDACIONES DE NEGOCIO y de la ORQUESTACIÓN del procesamiento del cálculo
 * (port de {@code diagnostico-service.ts::calcularDiagnostico}):
 *
 * <ul>
 *   <li>resuelve el tenant del {@link SiginexPrincipal} (nunca del cliente) y
 *       verifica la pertenencia del diagnóstico (404 vía RLS);</li>
 *   <li>pide al {@link CalculoService} el contexto de BD, ejecuta el
 *       {@link MotorCalculo} (procesamiento puro), VALIDA el contrato del
 *       resultado ANTES de persistir, y ordena la escritura atómica;</li>
 *   <li>valida el rango {@code 1..5} del nivel de benchmark antes de guardarlo.</li>
 * </ul>
 *
 * <p>El {@link CalculoService} queda reducido a acceso a datos: el Delegate
 * orquesta y decide; el Service lee y escribe. La atomicidad de la reescritura
 * la garantiza {@link CalculoService#persistirResultado} (una sola transacción).
 */
@Component
@RequiredArgsConstructor
public class CalculoDelegateImpl implements CalculoDelegate {

    private final DiagnosticoService diagnosticoService;
    private final CalculoService calculoService;
    private final MotorCalculo motorCalculo;

    @Override
    public ResultadoLectura calcular(SiginexPrincipal actor, UUID diagnosticoId) {
        UUID tenantId = actor.tenantId();
        verificarPertenencia(tenantId, diagnosticoId);

        // 1) Cargar contexto desde BD (Service).
        ContextoCalculo contexto = calculoService.cargarContextoCalculo(tenantId, diagnosticoId);

        // 2) Procesamiento puro: correr el motor (orquestación del Delegate).
        ResultadoConsolidado resultado =
                motorCalculo.run(contexto.getKb(), contexto.getEmpresa(), contexto.getRespuestas());

        // 3) Validación de negocio: el contrato debe cumplirse ANTES de persistir.
        List<String> faltan = motorCalculo.validarContrato(resultado);
        if (!faltan.isEmpty()) {
            throw new DominioException(HttpStatus.UNPROCESSABLE_CONTENT,
                    "Contrato inválido: " + String.join(", ", faltan));
        }

        // 4) Escritura atómica (Service, una sola transacción).
        calculoService.persistirResultado(tenantId, diagnosticoId, resultado);

        // 5) Leer el resultado persistido con el orden determinista (diseño §9.8),
        //    de modo que POST /calcular y GET /resultado devuelvan la misma forma.
        return calculoService.obtenerResultado(tenantId, diagnosticoId);
    }

    @Override
    public ResultadoLectura obtenerResultado(SiginexPrincipal actor, UUID diagnosticoId) {
        UUID tenantId = actor.tenantId();
        verificarPertenencia(tenantId, diagnosticoId);
        return calculoService.obtenerResultado(tenantId, diagnosticoId);
    }

    @Override
    public List<TareaPlanLectura> obtenerPlanMejora(SiginexPrincipal actor, UUID diagnosticoId) {
        UUID tenantId = actor.tenantId();
        verificarPertenencia(tenantId, diagnosticoId);
        return calculoService.obtenerPlanMejora(tenantId, diagnosticoId);
    }

    @Override
    public Benchmark registrarBenchmark(SiginexPrincipal actor, UUID diagnosticoId, int nivel) {
        UUID tenantId = actor.tenantId();
        verificarPertenencia(tenantId, diagnosticoId);

        // Validación de negocio: nivel entero 1..5 (fiel al TS).
        if (nivel < 1 || nivel > 5) {
            throw new DominioException(HttpStatus.UNPROCESSABLE_CONTENT,
                    "El nivel tecnológico debe ser un entero de 1 a 5.");
        }

        // Escritura (Service) + cálculo del benchmark (procesamiento del Delegate).
        calculoService.guardarNivelTecnologico(tenantId, diagnosticoId, nivel);
        return CalculoBenchmark.calcularBenchmark(nivel);
    }

    @Override
    public Optional<Benchmark> obtenerBenchmark(SiginexPrincipal actor, UUID diagnosticoId) {
        UUID tenantId = actor.tenantId();
        verificarPertenencia(tenantId, diagnosticoId);
        // Lee el nivel vigente (Service) y calcula el benchmark (Delegate); vacío
        // si no hay nivel declarado, igual que el TS (return null).
        return calculoService.obtenerNivelTecnologico(tenantId, diagnosticoId)
                .map(CalculoBenchmark::calcularBenchmark);
    }

    private void verificarPertenencia(UUID tenantId, UUID diagnosticoId) {
        diagnosticoService.buscarPorId(tenantId, diagnosticoId)
                .orElseThrow(() -> new DominioException(HttpStatus.NOT_FOUND, "diagnostico no encontrado"));
    }
}
