package com.siginex.api.service.calculo;

import com.siginex.api.domain.KbModulo;
import com.siginex.api.domain.KbPregunta;
import com.siginex.api.domain.calculo.Aplicabilidad;
import com.siginex.api.domain.calculo.CalculoEmpresa;
import com.siginex.api.domain.calculo.CalculoKb;
import com.siginex.api.domain.calculo.ResultadoConsolidado;
import com.siginex.api.domain.calculo.ResultadoModulo;
import com.siginex.api.domain.calculo.TareaPlan;
import com.siginex.api.domain.calculo.ValorRespuesta;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests de PARIDAD del motor puro contra {@code run.ts}. Sin Spring ni BD: el
 * KB se construye en memoria. Ningún test asevera {@code meta.generadoEn}.
 */
class MotorCalculoTest {

    private final MotorCalculo motor = new MotorCalculo();

    private static final List<String> MODULOS_ORDEN = List.of(
            "sst", "ambiental", "calidad", "riesgos", "cumplimiento",
            "datos", "gobierno", "esg", "gobernanza_ia");

    // Pesos reales de banco.json (verificados en el diseño).
    private static final Map<String, String> PESOS = Map.ofEntries(
            Map.entry("sst", "0.18"), Map.entry("ambiental", "0.13"),
            Map.entry("calidad", "0.12"), Map.entry("riesgos", "0.15"),
            Map.entry("cumplimiento", "0.15"), Map.entry("datos", "0.15"),
            Map.entry("gobierno", "0.12"), Map.entry("esg", "0.14"),
            Map.entry("gobernanza_ia", "0.12"));

    private static final Aplicabilidad TODO_APLICA = Aplicabilidad.builder()
            .permAmbiental(true).saglaft(true).ptee(true).rnbd(true).build();

    /** KB con los 9 módulos; cada módulo recibe las preguntas que se le pasen. */
    private CalculoKb kb(Map<String, List<KbPregunta>> preguntasPorModulo) {
        List<KbModulo> modulos = new ArrayList<>();
        int orden = 0;
        for (String id : MODULOS_ORDEN) {
            modulos.add(KbModulo.builder()
                    .id(id).nombre(id).shortNombre(id)
                    .weight(PESOS.get(id)).escala(null).orden(orden++)
                    .build());
        }
        return CalculoKb.from(modulos,
                mid -> preguntasPorModulo.getOrDefault(mid, List.of()));
    }

    private KbPregunta pregunta(String id, String peso, String norma,
                                Map<String, String> reco) {
        return KbPregunta.builder()
                .id(id).pregunta("pregunta " + id).norma(norma)
                .articulo("").requisito("").criterio("").peso(peso)
                .recomendacion(reco).orden(0).build();
    }

    private CalculoEmpresa empresa(Integer nivelTec) {
        return CalculoEmpresa.builder()
                .nombre("Acme").sector("x").tamano("mediana")
                .nivelTecnologico(nivelTec).aplicabilidad(TODO_APLICA).build();
    }

    @Test
    void escenarioA_moduloCalidadDosPreguntasPesoUno() {
        // calidad: 2 preguntas peso 1, respuestas 2 y 0 -> num=1, den=2 -> 50.00, nivel 3.
        Map<String, List<KbPregunta>> kb = new LinkedHashMap<>();
        kb.put("calidad", List.of(
                pregunta("c-1", "1", "ISO 9001", Map.of("0", "acc-c1-0")),
                pregunta("c-2", "1", "ISO 9001", Map.of("0", "acc-c2-0"))));

        Map<String, ValorRespuesta> answers = new LinkedHashMap<>();
        answers.put("c-1", ValorRespuesta.DOS);
        answers.put("c-2", ValorRespuesta.CERO);

        ResultadoConsolidado r = motor.run(kb(kb), empresa(null), answers);

        ResultadoModulo calidad = r.getPorModulo().stream()
                .filter(p -> p.getModulo().equals("calidad")).findFirst().orElseThrow();
        assertThat(calidad.getCumplimiento()).isEqualTo(50.00);
        assertThat(calidad.getNivel().getN()).isEqualTo(3);
        assertThat(calidad.getNivel().getNombre()).isEqualTo("En desarrollo");
        assertThat(calidad.getRespondidas()).isEqualTo(2);
        assertThat(calidad.getAplicables()).isEqualTo(2);

        // 1 brecha alta (c-2 respondida 0).
        assertThat(r.getBrechas()).hasSize(1);
        assertThat(r.getBrechas().get(0).getPreguntaId()).isEqualTo("c-2");
        assertThat(r.getBrechas().get(0).getPrioridad()).isEqualTo("alta");
        assertThat(r.getBrechas().get(0).getEvaluacion()).isZero();

        // Plan: r (c-2) / Líder de Calidad / 30 dias / fase 1.
        assertThat(r.getPlanMejora()).hasSize(1);
        TareaPlan tarea = r.getPlanMejora().get(0);
        assertThat(tarea.getPreguntaId()).isEqualTo("c-2");
        assertThat(tarea.getAccion()).isEqualTo("acc-c2-0");
        assertThat(tarea.getResponsable()).isEqualTo("Líder de Calidad");
        assertThat(tarea.getPlazoDias()).isEqualTo(30);
        assertThat(tarea.getFase()).isEqualTo(1);
        assertThat(tarea.getCriterioCierre())
                .isEqualTo("Evidencia verificable del requisito, vigente y trazable.");

        // rutaFor genérica (sin disparador que matchee) = primera competencia de calidad.
        assertThat(r.getPlanAprendizaje()).hasSize(1);
        assertThat(r.getPlanAprendizaje().get(0).getCompetencia())
                .isEqualTo("Sistema de gestión de calidad ISO 9001");
        assertThat(r.getPlanAprendizaje().get(0).getNivel()).isEqualTo("Básico");

        // Global: único módulo evaluado -> score = 50.00.
        assertThat(r.getScoreSgi()).isEqualTo(50.00);
        assertThat(r.getNivelSgi().getN()).isEqualTo(3);
        assertThat(r.getCumplimientoGlobal()).isEqualTo(50.00);

        // Sin nivel tecnológico -> sin benchmark.
        assertThat(r.getBenchmark()).isNull();
        assertThat(r.getPorModulo()).hasSize(9);
        assertThat(motor.validarContrato(r)).isEmpty();
    }

    @Test
    void escenarioB_sstTodasCeroDatosTodasDos() {
        // sst: 3 preguntas peso 1 todas 0 -> 0.00. datos: 3 preguntas peso 1 todas 2 -> 100.00.
        Map<String, List<KbPregunta>> kb = new LinkedHashMap<>();
        kb.put("sst", List.of(
                pregunta("s-1", "1", "Decreto 1072", Map.of("0", "a")),
                pregunta("s-2", "1", "Decreto 1072", Map.of("0", "a")),
                pregunta("s-3", "1", "Decreto 1072", Map.of("0", "a"))));
        kb.put("datos", List.of(
                pregunta("d-1", "1", "Ley 1581", Map.of()),
                pregunta("d-2", "1", "Ley 1581", Map.of()),
                pregunta("d-3", "1", "Ley 1581", Map.of())));

        Map<String, ValorRespuesta> answers = new LinkedHashMap<>();
        answers.put("s-1", ValorRespuesta.CERO);
        answers.put("s-2", ValorRespuesta.CERO);
        answers.put("s-3", ValorRespuesta.CERO);
        answers.put("d-1", ValorRespuesta.DOS);
        answers.put("d-2", ValorRespuesta.DOS);
        answers.put("d-3", ValorRespuesta.DOS);

        ResultadoConsolidado r = motor.run(kb(kb), empresa(null), answers);

        // Global: gw=0.18+0.15=0.33, gs=0*0.18+100*0.15=15.0 -> round2(45.4545..)=45.45.
        assertThat(r.getScoreSgi()).isEqualTo(45.45);
        assertThat(r.getNivelSgi().getN()).isEqualTo(3);
    }

    @Test
    void escenarioD_moduloDenCeroExcluidoYTodosNullScoreCero() {
        // Caso 1: un módulo con den==0 (todas NA) se excluye del global.
        Map<String, List<KbPregunta>> kb1 = new LinkedHashMap<>();
        kb1.put("calidad", List.of(
                pregunta("c-1", "1", "ISO 9001", Map.of()),
                pregunta("c-2", "1", "ISO 9001", Map.of())));
        kb1.put("datos", List.of(pregunta("d-1", "1", "Ley 1581", Map.of())));

        Map<String, ValorRespuesta> answers1 = new LinkedHashMap<>();
        answers1.put("c-1", ValorRespuesta.NA);
        answers1.put("c-2", ValorRespuesta.NA);
        answers1.put("d-1", ValorRespuesta.DOS);

        ResultadoConsolidado r1 = motor.run(kb(kb1), empresa(null), answers1);

        ResultadoModulo calidad = r1.getPorModulo().stream()
                .filter(p -> p.getModulo().equals("calidad")).findFirst().orElseThrow();
        assertThat(calidad.getCumplimiento()).isNull();
        assertThat(calidad.getNivel()).isNull();
        assertThat(calidad.getRespondidas()).isEqualTo(2);  // NA cuenta como respondida
        // Global ignora calidad (den==0): solo datos (100.00) -> score 100.00.
        assertThat(r1.getScoreSgi()).isEqualTo(100.00);

        // Caso 2: todos los módulos sin dato -> gw==0 -> score 0.0, nivel 1.
        Map<String, List<KbPregunta>> kb2 = new LinkedHashMap<>();
        Map<String, ValorRespuesta> answers2 = new LinkedHashMap<>();
        ResultadoConsolidado r2 = motor.run(kb(kb2), empresa(null), answers2);
        assertThat(r2.getScoreSgi()).isEqualTo(0.0);
        assertThat(r2.getNivelSgi().getN()).isEqualTo(1);
        assertThat(r2.getNivelSgi().getNombre()).isEqualTo("Inicial");
        // Todos los módulos con cumplimiento null.
        assertThat(r2.getPorModulo()).allSatisfy(p -> assertThat(p.getCumplimiento()).isNull());
    }

    @Test
    void benchmarkSoloSiNivelTecnologicoPresente() {
        ResultadoConsolidado conNivel = motor.run(kb(new LinkedHashMap<>()), empresa(2),
                new LinkedHashMap<>());
        assertThat(conNivel.getBenchmark()).isNotNull();
        assertThat(conNivel.getBenchmark().getNivelEmpresa()).isEqualTo(2);
    }

    @Test
    void validarContratoDetectaNumeroModulosDistintoDeNueve() {
        ResultadoConsolidado malo = ResultadoConsolidado.builder()
                .porModulo(List.of()).build();
        assertThat(motor.validarContrato(malo))
                .containsExactly("por_modulo.length !== 9");
    }
}
