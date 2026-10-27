package com.siginex.api.service.calculo;

import com.siginex.api.domain.KbPregunta;
import com.siginex.api.domain.calculo.Aplicabilidad;
import com.siginex.api.domain.calculo.CalculoPregunta;
import com.siginex.api.domain.calculo.ValorRespuesta;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests de PARIDAD de los helpers de normalización y filtrado:
 * {@code aplica}/{@code esLegal}/{@code rutaFor} (de {@code MotorCalculo}),
 * el parser de aplicabilidad y {@code aValorOrquestador}.
 */
class AplicabilidadParidadTest {

    private final MotorCalculo motor = new MotorCalculo();

    private static final Aplicabilidad TODO_FALSE = Aplicabilidad.builder()
            .permAmbiental(false).saglaft(false).ptee(false).rnbd(false).build();
    private static final Aplicabilidad TODO_TRUE = Aplicabilidad.builder()
            .permAmbiental(true).saglaft(true).ptee(true).rnbd(true).build();

    private CalculoPregunta pregunta(String norma, String articulo, String requisito,
                                     String texto) {
        return new CalculoPregunta(KbPregunta.builder()
                .id("q").pregunta(texto).norma(norma).articulo(articulo)
                .requisito(requisito).criterio("").peso("1").recomendacion(Map.of())
                .orden(0).build());
    }

    // -- aplica --------------------------------------------------------------

    @Test
    void aplicaConPerfilCompletoTodoAplica() {
        assertThat(motor.aplica(pregunta("Decreto 631", "", "", "vertimiento"), TODO_TRUE)).isTrue();
    }

    @Test
    void aplicaExcluyeAmbientalPorToken631OVertimiento() {
        assertThat(motor.aplica(pregunta("Resolución 631", "", "", "x"), TODO_FALSE)).isFalse();
        assertThat(motor.aplica(pregunta("x", "", "", "control de vertimiento"), TODO_FALSE)).isFalse();
    }

    @Test
    void aplicaExcluyeSaglaftPorCualquierToken() {
        assertThat(motor.aplica(pregunta("x", "", "", "SARLAFT"), TODO_FALSE)).isFalse();
        assertThat(motor.aplica(pregunta("x", "", "", "riesgo LA/FT"), TODO_FALSE)).isFalse();
        assertThat(motor.aplica(pregunta("x", "", "", "Supersociedades"), TODO_FALSE)).isFalse();
        assertThat(motor.aplica(pregunta("x", "", "", "prevención de lavado"), TODO_FALSE)).isFalse();
    }

    @Test
    void aplicaExcluyePteeYRnbd() {
        assertThat(motor.aplica(pregunta("x", "", "", "programa PTEE"), TODO_FALSE)).isFalse();
        assertThat(motor.aplica(pregunta("x", "", "", "registro RNBD"), TODO_FALSE)).isFalse();
    }

    @Test
    void aplicaCamposNullTratadosComoVacio() {
        // norma/articulo/requisito null; nada que matchee -> aplica.
        assertThat(motor.aplica(pregunta(null, null, null, "pregunta neutra"), TODO_FALSE)).isTrue();
    }

    // -- esLegal -------------------------------------------------------------

    @Test
    void esLegalDetectaPalabrasClaveIncluyendoTildeYSinTilde() {
        assertThat(motor.esLegal("Ley 1581")).isTrue();
        assertThat(motor.esLegal("Decreto 1072")).isTrue();
        assertThat(motor.esLegal("Resolución 0312")).isTrue();   // con tilde
        assertThat(motor.esLegal("resolucion interna")).isTrue(); // sin tilde
        assertThat(motor.esLegal("Circular 100")).isTrue();
        assertThat(motor.esLegal("ISO 9001")).isFalse();
        assertThat(motor.esLegal(null)).isFalse();
    }

    // -- rutaFor -------------------------------------------------------------

    @Test
    void rutaForGenericaSiNingunDisparadorMatchea() {
        CalculoPregunta q = pregunta("x", "", "requisito neutro", "pregunta neutra");
        assertThat(motor.rutaFor("sst", q)).isEqualTo("Fundamentos del SG-SST");
    }

    @Test
    void rutaForRompeAlPrimerMatch() {
        // "iper" dispara la segunda competencia de sst.
        CalculoPregunta q = pregunta("x", "", "matriz IPER", "identificación de peligros");
        assertThat(motor.rutaFor("sst", q))
                .isEqualTo("Identificación de peligros y valoración de riesgos (IPER)");
    }

    @Test
    void rutaForDisparadorAbreviadoConTilde() {
        // "emisi" (abreviado) dispara "Gestión de riesgos ASG y clima" en esg.
        CalculoPregunta q = pregunta("x", "", "", "reducción de emisiones de carbono");
        assertThat(motor.rutaFor("esg", q)).isEqualTo("Gestión de riesgos ASG y clima");
    }

    @Test
    void rutaForModuloDesconocidoDevuelveNull() {
        assertThat(motor.rutaFor("inexistente", pregunta("x", "", "", "y"))).isNull();
    }

    // -- AplicabilidadParser -------------------------------------------------

    @Test
    void parserNullYVacioDan4DefaultsTrue() {
        assertThat(AplicabilidadParser.parse(null)).isEqualTo(TODO_TRUE);
        assertThat(AplicabilidadParser.parse(Map.of())).isEqualTo(TODO_TRUE);
    }

    @Test
    void parserMergeSoloClavesBooleanasPresentes() {
        Map<String, Object> jsonb = new HashMap<>();
        jsonb.put("saglaft", false);
        jsonb.put("rnbd", false);
        Aplicabilidad a = AplicabilidadParser.parse(jsonb);
        assertThat(a.isPermAmbiental()).isTrue();   // ausente -> default true
        assertThat(a.isSaglaft()).isFalse();
        assertThat(a.isPtee()).isTrue();
        assertThat(a.isRnbd()).isFalse();
    }

    @Test
    void parserClaveReconocidaNoBooleanaDescartaTodo() {
        Map<String, Object> jsonb = new HashMap<>();
        jsonb.put("saglaft", "false");  // String, no boolean -> safeParse falla
        jsonb.put("ptee", false);
        // Todo se descarta -> 4 defaults true.
        assertThat(AplicabilidadParser.parse(jsonb)).isEqualTo(TODO_TRUE);
    }

    @Test
    void parserIgnoraClavesDesconocidas() {
        Map<String, Object> jsonb = new HashMap<>();
        jsonb.put("permAmbiental", false);
        jsonb.put("otra", 123);         // desconocida -> ignorada
        Aplicabilidad a = AplicabilidadParser.parse(jsonb);
        assertThat(a.isPermAmbiental()).isFalse();
        assertThat(a.isSaglaft()).isTrue();
    }

    // -- ValorRespuestaMapper ------------------------------------------------

    @Test
    void aValorOrquestadorTablaCompleta() {
        assertThat(ValorRespuestaMapper.aValorOrquestador("0")).contains(ValorRespuesta.CERO);
        assertThat(ValorRespuestaMapper.aValorOrquestador("1")).contains(ValorRespuesta.UNO);
        assertThat(ValorRespuestaMapper.aValorOrquestador("2")).contains(ValorRespuesta.DOS);
        assertThat(ValorRespuestaMapper.aValorOrquestador("si")).contains(ValorRespuesta.DOS);
        assertThat(ValorRespuestaMapper.aValorOrquestador("no")).contains(ValorRespuesta.CERO);
        assertThat(ValorRespuestaMapper.aValorOrquestador("na")).contains(ValorRespuesta.NA);
        assertThat(ValorRespuestaMapper.aValorOrquestador("sin_dato")).isEmpty();
        assertThat(ValorRespuestaMapper.aValorOrquestador("otro")).isEmpty();
        assertThat(ValorRespuestaMapper.aValorOrquestador(null)).isEmpty();
    }
}
