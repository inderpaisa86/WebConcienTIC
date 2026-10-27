package com.siginex.api.domain.calculo;

/**
 * Valor de respuesta que consume el motor de cálculo. Port 1:1 del tipo TS
 * {@code ValorRespuesta = 0 | 1 | 2 | "na"} de {@code orquestador/tipos.ts}.
 *
 * <p>El motor distingue tres estados por pregunta: respondida con 0/1/2
 * ({@link #CERO}/{@link #UNO}/{@link #DOS}), respondida con {@link #NA} (cuenta
 * como respondida pero no acumula), y <b>ausente del mapa</b> (sin responder).
 * La ausencia se modela por la propia falta de la clave en el {@code Map} de
 * respuestas, nunca con un valor centinela.
 */
public enum ValorRespuesta {

    CERO(0),
    UNO(1),
    DOS(2),
    NA(-1);

    private final int numero;

    ValorRespuesta(int numero) {
        this.numero = numero;
    }

    /**
     * Valor numérico 0/1/2 de la respuesta. Para {@link #NA} no aplica y devuelve
     * {@code -1}; el motor nunca lo consulta en ese caso (salta con {@code continue}).
     *
     * @return 0, 1 o 2 para respuestas numéricas; {@code -1} para {@link #NA}.
     */
    public int numero() {
        return numero;
    }
}
