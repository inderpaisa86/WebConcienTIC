package com.siginex.api.domain.calculo;

import lombok.Value;

import java.util.Map;

/**
 * Datos de entrada del cálculo de un diagnóstico, ya leídos de la base de datos.
 *
 * <p>Separa la responsabilidad de capas: el {@code CalculoService} LEE este
 * contexto (acceso a BD), y el {@code CalculoDelegate} lo usa para ORQUESTAR el
 * procesamiento (invocar el {@code MotorCalculo} y validar el contrato). Así el
 * Service no contiene lógica de negocio ni ejecuta el motor.
 *
 * <p>Inmutable, construido con Lombok {@code @Value}.
 */
@Value
public class ContextoCalculo {

    /** Perfil de la empresa dueña del diagnóstico (nombre, sector, tamaño, nivel, aplicabilidad). */
    CalculoEmpresa empresa;

    /** Banco de conocimiento de cálculo (global), con pesos ya parseados a double. */
    CalculoKb kb;

    /** Respuestas del diagnóstico normalizadas al tipo del motor (orden de inserción preservado). */
    Map<String, ValorRespuesta> respuestas;
}
