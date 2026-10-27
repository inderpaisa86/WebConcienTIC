package com.siginex.api.service.calculo;

import com.siginex.api.domain.calculo.ValorRespuesta;

import java.util.Optional;

/**
 * Normaliza el enum {@code valor_respuesta} de BD al tipo del motor. Port 1:1 de
 * {@code diagnostico-service.ts::aValorOrquestador}. Clase de utilidad, no
 * instanciable.
 *
 * <p>Tabla: {@code "0"->CERO}, {@code "1"->UNO}, {@code "2"->DOS},
 * {@code "si"->DOS}, {@code "no"->CERO}, {@code "na"->NA}. {@code "sin_dato"},
 * cualquier otro valor y {@code null} devuelven {@link Optional#empty()} y
 * <b>no</b> entran al mapa {@code answers} (estado "sin responder").
 */
public final class ValorRespuestaMapper {

    private ValorRespuestaMapper() {
    }

    /**
     * Mapea el valor de BD al {@link ValorRespuesta} del motor.
     *
     * @param valor valor del enum {@code valor_respuesta} (puede ser {@code null}).
     * @return el valor del motor, o {@link Optional#empty()} si no se responde.
     */
    public static Optional<ValorRespuesta> aValorOrquestador(String valor) {
        if (valor == null) {
            return Optional.empty();
        }
        return switch (valor) {
            case "0" -> Optional.of(ValorRespuesta.CERO);
            case "1" -> Optional.of(ValorRespuesta.UNO);
            case "2", "si" -> Optional.of(ValorRespuesta.DOS);
            case "no" -> Optional.of(ValorRespuesta.CERO);
            case "na" -> Optional.of(ValorRespuesta.NA);
            default -> Optional.empty();
        };
    }
}
