package com.siginex.api.service.calculo;

import com.siginex.api.domain.calculo.Nivel;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests de PARIDAD de {@code levelFor} y {@code round2} contra {@code scoring.ts}.
 * El redondeo recorre el camino {@code double} (Math.round(x*100)/100), NO
 * BigDecimal: los casos frontera {@code .xx5} lo blindan.
 *
 * <p><b>Nota de paridad (crítica):</b> los valores esperados de {@code round2}
 * se verificaron ejecutando el mismo {@code Math.round(x*100)/100} en el runtime
 * Node del TS (fuente de verdad). Para esos literales el {@code double} de
 * {@code x*100} es EXACTAMENTE {@code .5} ({@code 2.675*100==267.5},
 * {@code 0.005*100==0.5}) tanto en Node como en la JVM, así que
 * {@code round2(2.675)==2.68} y {@code round2(0.005)==0.01}. Los valores
 * 2.67/0.0 que menciona el enunciado del FEAT corresponden a la semántica de
 * {@code toFixed}/BigDecimal, NO a {@code Math.round}; asumirlos haría divergir
 * el motor del TS (ver findings de FEAT-001).
 */
class ScoringParidadTest {

    private final MotorCalculo motor = new MotorCalculo();

    @Test
    void levelForBordesEstrictos() {
        // Umbrales estrictos <21/<41/<61/<81.
        assertThat(nivelN(20.99)).isEqualTo(1);
        assertThat(nivelN(21.0)).isEqualTo(2);   // 21 NO es < 21
        assertThat(nivelN(40.99)).isEqualTo(2);
        assertThat(nivelN(41.0)).isEqualTo(3);
        assertThat(nivelN(60.99)).isEqualTo(3);
        assertThat(nivelN(61.0)).isEqualTo(4);
        assertThat(nivelN(80.99)).isEqualTo(4);
        assertThat(nivelN(81.0)).isEqualTo(5);
        assertThat(nivelN(100.0)).isEqualTo(5);
    }

    @Test
    void levelForNullDevuelveNull() {
        assertThat(motor.levelFor(null)).isNull();
    }

    @Test
    void round2CasosFronteraPorCaminoDouble() {
        // 0.125*100 == 12.5 exacto -> Math.round -> 13 -> 0.13 (JS y JVM coinciden).
        assertThat(motor.round2(0.125)).isEqualTo(0.13);
        // 2.675*100 == 267.5 exacto (verificado en Node) -> Math.round -> 268 -> 2.68.
        assertThat(motor.round2(2.675)).isEqualTo(2.68);
        // 0.005*100 == 0.5 exacto (verificado en Node) -> Math.round -> 1 -> 0.01.
        assertThat(motor.round2(0.005)).isEqualTo(0.01);
    }

    private int nivelN(double pct) {
        Nivel n = motor.levelFor(pct);
        return n.getN();
    }
}
