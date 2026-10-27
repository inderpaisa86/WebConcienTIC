package com.siginex.api.domain.calculo;

import lombok.Builder;
import lombok.Value;

import java.util.Map;

/**
 * Alerta derivada del diagnóstico (p. ej. brechas críticas). Port 1:1 del tipo
 * TS {@code Alerta}. Inmutable con Lombok {@code @Value @Builder} (convención
 * del proyecto: sin {@code record}).
 *
 * <p>{@code detalle} es un mapa libre ({@code Record<string, unknown>} del TS).
 */
@Value
@Builder
public class Alerta {

    String tipo;
    String severidad;
    String titulo;
    Map<String, Object> detalle;
}
