package com.siginex.api.service.calculo;

import com.siginex.api.domain.calculo.Alerta;
import com.siginex.api.domain.calculo.Aplicabilidad;
import com.siginex.api.domain.calculo.Benchmark;
import com.siginex.api.domain.calculo.Brecha;
import com.siginex.api.domain.calculo.CalculoEmpresa;
import com.siginex.api.domain.calculo.CalculoKb;
import com.siginex.api.domain.calculo.CalculoModulo;
import com.siginex.api.domain.calculo.CalculoPregunta;
import com.siginex.api.domain.calculo.Meta;
import com.siginex.api.domain.calculo.Nivel;
import com.siginex.api.domain.calculo.ResultadoConsolidado;
import com.siginex.api.domain.calculo.ResultadoModulo;
import com.siginex.api.domain.calculo.RutaAprendizajePlan;
import com.siginex.api.domain.calculo.TareaPlan;
import com.siginex.api.domain.calculo.ValorRespuesta;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Motor de cálculo <b>puro</b> del diagnóstico SIGINEX. Port 1:1 de
 * {@code orquestador/run.ts} (pipeline + {@code validarContrato}), con los
 * helpers de {@code scoring.ts} ({@link #levelFor}/{@link #round2}) y
 * {@code motores.ts} ({@link #esLegal}/{@link #rutaFor}) y
 * {@code aplicabilidad.ts} ({@link #aplica}).
 *
 * <p>No toca la BD ni inyecta repositorios; su única dependencia es un
 * {@link Clock} (para {@code meta.generadoEn}), por defecto {@link Clock#systemUTC()}.
 *
 * <p><b>Paridad numérica (VINCULANTE):</b> todo el scoring es {@code double}
 * primitivo, replicando el camino flotante del TS operación por operación. La
 * acumulación de {@code num}/{@code den} y de {@code gw}/{@code gs} usa un bucle
 * {@code for} <b>secuencial en el orden del KB</b>; está <b>prohibido</b>
 * {@code DoubleStream.sum()}/{@code reduce} o cualquier suma reasociada/paralela.
 * El nivel por módulo se evalúa sobre el cumplimiento <b>crudo</b>; el nivel
 * global sobre el score <b>ya redondeado</b>; el global pondera cumplimientos
 * <b>ya redondeados</b>. {@code den == 0} ⇒ cumplimiento/nivel {@code null} y el
 * módulo se excluye del global; {@code gw == 0} ⇒ score {@code 0.0}.
 */
@Component
public class MotorCalculo {

    private final Clock clock;

    /** Constructor por defecto: reloj UTC del sistema. */
    public MotorCalculo() {
        this(Clock.systemUTC());
    }

    /**
     * Constructor con reloj inyectable (para fijar {@code meta.generadoEn} en tests).
     *
     * @param clock reloj a usar; no debe ser {@code null}.
     */
    public MotorCalculo(Clock clock) {
        this.clock = clock;
    }

    /**
     * Ejecuta el pipeline de diagnóstico. Determinista: mismo
     * {@code (kb, empresa, answers)} → mismo resultado (salvo {@code meta.generadoEn}).
     *
     * @param kb      KB de cálculo materializado (9 módulos en orden).
     * @param empresa perfil de la organización.
     * @param answers respuestas por pregunta ({@code preguntaId -> valor}); una
     *                clave ausente significa "sin responder".
     * @return el resultado consolidado.
     */
    public ResultadoConsolidado run(CalculoKb kb, CalculoEmpresa empresa,
                                    Map<String, ValorRespuesta> answers) {
        Aplicabilidad profile = empresa.getAplicabilidad();

        List<ResultadoModulo> perModulo = new ArrayList<>();
        List<Brecha> brechas = new ArrayList<>();
        List<TareaPlan> plan = new ArrayList<>();
        List<Alerta> alertas = new ArrayList<>();
        // Orden de inserción = orden del KB; LinkedHashMap para que el sort estable desempate.
        Map<String, RutaAcc> aprend = new LinkedHashMap<>();

        int answeredTot = 0;
        int aplicTot = 0;

        for (CalculoModulo m : kb.getModulos()) {
            // Preguntas aplicables, en el orden del KB.
            List<CalculoPregunta> apq = new ArrayList<>();
            for (CalculoPregunta q : m.getPreguntas()) {
                if (aplica(q, profile)) {
                    apq.add(q);
                }
            }
            aplicTot += apq.size();

            double num = 0.0;
            double den = 0.0;
            int answered = 0;

            for (CalculoPregunta q : apq) {
                ValorRespuesta v = answers.get(q.getId());
                if (v == null) {
                    continue;                 // no respondida: se omite
                }
                answered += 1;
                if (v == ValorRespuesta.NA) {
                    continue;                 // respondida pero no acumula
                }

                int valor = v.numero();        // 0, 1 o 2
                num += (valor / 2.0) * q.getPeso();   // bucle secuencial, orden del KB
                den += q.getPeso();

                if (valor < 2) {
                    boolean legal = esLegal(q.getNorma());
                    // prioridad: alta si 0 o norma legal; media en el resto con 1.
                    String prioridad = (valor == 0 || legal) ? "alta" : "media";

                    brechas.add(Brecha.builder()
                            .modulo(m.getId())
                            .preguntaId(q.getId())
                            .brecha(q.getPregunta())
                            .norma(q.getNorma())
                            .evaluacion(valor)
                            .prioridad(prioridad)
                            .build());

                    int dias = "alta".equals(prioridad) ? 30 : 90;
                    Map<String, String> reco = q.getRecomendacion();
                    String accion = reco != null ? reco.getOrDefault(String.valueOf(valor), "") : "";
                    plan.add(TareaPlan.builder()
                            .modulo(m.getId())
                            .preguntaId(q.getId())
                            .accion(accion)
                            .responsable(ConstantesCalculo.RESP.get(m.getId()))
                            .prioridad(prioridad)
                            .plazoDias(dias)
                            .fase("alta".equals(prioridad) ? 1 : 2)
                            .norma(q.getNorma())
                            .criterioCierre(ConstantesCalculo.CRITERIO_CIERRE)
                            .build());

                    String comp = rutaFor(m.getId(), q);
                    if (comp != null) {
                        RutaAcc existente = aprend.get(comp);
                        if (existente == null) {
                            aprend.put(comp, new RutaAcc(comp, m.getId(), 1, valor == 0 ? 0 : 1));
                        } else {
                            existente.brechas += 1;
                            if (valor == 0) {
                                existente.prioridad = 0;
                            }
                        }
                    }
                }
            }

            answeredTot += answered;
            Double cumplimiento;
            Nivel nivel;
            if (den > 0.0) {
                double compPct = (num / den) * 100.0;   // crudo, double
                cumplimiento = round2(compPct);          // mostrado, redondeado
                nivel = levelFor(compPct);               // nivel del CRUDO
            } else {
                cumplimiento = null;                      // den == 0: sin dato
                nivel = null;
            }
            perModulo.add(ResultadoModulo.builder()
                    .modulo(m.getId())
                    .nombre(m.nombreResuelto())
                    .cumplimiento(cumplimiento)
                    .nivel(nivel)
                    .respondidas(answered)
                    .aplicables(apq.size())
                    .build());
        }

        // Global ponderado por peso de módulo, renormalizado sobre los evaluados.
        double gw = 0.0;
        double gs = 0.0;
        for (ResultadoModulo p : perModulo) {
            if (p.getCumplimiento() == null) {
                continue;                                 // excluye den == 0
            }
            double peso = kb.pesoModulo(p.getModulo());
            gw += peso;                                   // orden = orden del KB
            gs += p.getCumplimiento() * peso;             // cumplimiento YA redondeado
        }
        double score = gw > 0.0 ? round2(gs / gw) : 0.0;  // 0.0 si no hay evaluados
        Nivel nivelSgi = levelFor(score);                 // nivel del REDONDEADO

        // Ordenar plan: estable, prioridad alta antes que media.
        plan.sort((a, b) -> prioridadOrden(a.getPrioridad()) - prioridadOrden(b.getPrioridad()));

        // Plan de aprendizaje: orden de inserción, sort estable prioridad asc, luego brechas desc.
        List<RutaAcc> rutas = new ArrayList<>(aprend.values());
        rutas.sort((a, b) -> {
            int porPrioridad = Integer.compare(a.prioridad, b.prioridad);
            if (porPrioridad != 0) {
                return porPrioridad;
            }
            return Integer.compare(b.brechas, a.brechas);
        });
        List<RutaAprendizajePlan> planAprendizaje = new ArrayList<>(rutas.size());
        for (RutaAcc r : rutas) {
            planAprendizaje.add(RutaAprendizajePlan.builder()
                    .competencia(r.competencia)
                    .modulo(r.modulo)
                    .nivel(r.prioridad == 0 ? "Básico" : "Intermedio")
                    .brechas(r.brechas)
                    .origen("interno")
                    .build());
        }

        // Benchmark (opcional): solo si la empresa declara nivel tecnológico.
        Integer nt = empresa.getNivelTecnologico();
        Benchmark benchmark = nt != null ? CalculoBenchmark.calcularBenchmark(nt) : null;

        // Alertas (brechas críticas).
        int criticas = 0;
        for (Brecha b : brechas) {
            if ("alta".equals(b.getPrioridad())) {
                criticas += 1;
            }
        }
        if (criticas > 0) {
            alertas.add(Alerta.builder()
                    .tipo("plan")
                    .severidad("alta")
                    .titulo("Brechas de alta prioridad detectadas")
                    .detalle(Map.of("cantidad", criticas))
                    .build());
        }

        return ResultadoConsolidado.builder()
                .scoreSgi(score)
                .nivelSgi(nivelSgi)
                .cumplimientoGlobal(score)
                .porModulo(perModulo)
                .brechas(brechas)
                .planMejora(plan)
                .planAprendizaje(planAprendizaje)
                .benchmark(benchmark)
                .alertas(alertas)
                .meta(Meta.builder()
                        .preguntasAplicables(aplicTot)
                        .respondidas(answeredTot)
                        .generadoEn(clock.instant().toString())
                        .build())
                .build();
    }

    /**
     * Valida la integridad del contrato de salida. Port 1:1 del chequeo
     * {@code por_modulo.length !== 9} de {@code run.ts::validarContrato}.
     *
     * @param r resultado consolidado.
     * @return lista con {@code "por_modulo.length !== 9"} (literal exacto) si el
     *         número de módulos no es 9; lista vacía en caso contrario.
     */
    public List<String> validarContrato(ResultadoConsolidado r) {
        if (r.getPorModulo().size() != 9) {
            return List.of("por_modulo.length !== 9");
        }
        return List.of();
    }

    // -- Helpers (port de aplicabilidad.ts / scoring.ts / motores.ts) --------

    /**
     * Resolver de aplicabilidad. Port 1:1 de {@code aplicabilidad.ts::aplica}.
     * Construye {@code s = lower(norma + " " + articulo + " " + requisito + " " +
     * pregunta)} (campos {@code null} tratados como cadena vacía) y aplica las 4
     * exclusiones. {@code contains} es substring literal; {@code toLowerCase} con
     * {@link Locale#ROOT}.
     *
     * @param q       pregunta.
     * @param profile perfil de aplicabilidad.
     * @return {@code true} si la pregunta aplica.
     */
    boolean aplica(CalculoPregunta q, Aplicabilidad profile) {
        String s = String.join(" ",
                nz(q.getNorma()), nz(q.getArticulo()), nz(q.getRequisito()), nz(q.getPregunta()))
                .toLowerCase(Locale.ROOT);

        if (!profile.isPermAmbiental() && (s.contains("631") || s.contains("vertimiento"))) {
            return false;
        }
        if (!profile.isSaglaft()
                && (s.contains("saglaft") || s.contains("sarlaft") || s.contains("supersociedades")
                || s.contains("la/ft") || s.contains("lavado"))) {
            return false;
        }
        if (!profile.isPtee() && s.contains("ptee")) {
            return false;
        }
        if (!profile.isRnbd() && s.contains("rnbd")) {
            return false;
        }
        return true;
    }

    /**
     * Mapea un porcentaje de cumplimiento a nivel de madurez. Port 1:1 de
     * {@code scoring.ts::levelFor}. Umbrales estrictos {@code < 21/41/61/81}.
     *
     * @param pct porcentaje crudo o {@code null}.
     * @return el nivel, o {@code null} si {@code pct == null}.
     */
    Nivel levelFor(Double pct) {
        if (pct == null) {
            return null;
        }
        if (pct < 21) {
            return Nivel.builder().n(1).nombre("Inicial").build();
        }
        if (pct < 41) {
            return Nivel.builder().n(2).nombre("Básico").build();
        }
        if (pct < 61) {
            return Nivel.builder().n(3).nombre("En desarrollo").build();
        }
        if (pct < 81) {
            return Nivel.builder().n(4).nombre("Optimizado").build();
        }
        return Nivel.builder().n(5).nombre("Excelencia").build();
    }

    /**
     * Redondeo a 2 decimales. Port 1:1 de {@code scoring.ts::round2} =
     * {@code Math.round(x*100)/100}. <b>No</b> usa {@code BigDecimal.setScale}:
     * replica el mismo camino {@code double} del TS (clave para la paridad en
     * fronteras {@code .xx5}).
     *
     * @param x valor a redondear.
     * @return {@code x} redondeado a 2 decimales por la vía {@code double}.
     */
    double round2(double x) {
        return Math.round(x * 100.0) / 100.0;
    }

    /**
     * {@code true} si la norma es de tipo legal. Port 1:1 de
     * {@code motores.ts::esLegal}. Incluye a propósito las variantes
     * {@code "resolución"} (con tilde) y {@code "resolucion"} (sin tilde).
     *
     * @param norma norma (puede ser {@code null} → {@code ""}).
     * @return {@code true} si contiene una palabra clave legal.
     */
    boolean esLegal(String norma) {
        String n = nz(norma).toLowerCase(Locale.ROOT);
        return n.contains("ley") || n.contains("decreto") || n.contains("resolución")
                || n.contains("resolucion") || n.contains("circular");
    }

    /**
     * Competencia de adopción para una pregunta con brecha. Port 1:1 de
     * {@code motores.ts::rutaFor}. Arranca con la primera competencia del módulo
     * (genérica) y rompe en el <b>primer</b> item con disparadores no vacíos cuyo
     * token esté contenido en {@code s = lower(requisito + " " + pregunta + " " +
     * norma)}.
     *
     * @param moduloId id del módulo.
     * @param q        pregunta.
     * @return la competencia, o {@code null} si el módulo no está en {@code ADOPT}.
     */
    String rutaFor(String moduloId, CalculoPregunta q) {
        List<CompetenciaAdopcion> cat = ConstantesCalculo.ADOPT.get(moduloId);
        if (cat == null) {
            return null;
        }
        String s = String.join(" ", nz(q.getRequisito()), nz(q.getPregunta()), nz(q.getNorma()))
                .toLowerCase(Locale.ROOT);
        String route = cat.get(0).getCompetencia();
        for (CompetenciaAdopcion ca : cat) {
            List<String> trig = ca.getDisparadores();
            if (!trig.isEmpty() && containsAny(s, trig)) {
                route = ca.getCompetencia();
                break;
            }
        }
        return route;
    }

    private static boolean containsAny(String s, List<String> tokens) {
        for (String t : tokens) {
            if (s.contains(t)) {
                return true;
            }
        }
        return false;
    }

    /** {@code null} → cadena vacía (replica el {@code join} del TS). */
    private static String nz(String s) {
        return s == null ? "" : s;
    }

    private static int prioridadOrden(String prioridad) {
        return "alta".equals(prioridad) ? 0 : 1;
    }

    /** Acumulador mutable del plan de aprendizaje (análogo al objeto del Map del TS). */
    private static final class RutaAcc {
        private final String competencia;
        private final String modulo;
        private int brechas;
        private int prioridad;

        private RutaAcc(String competencia, String modulo, int brechas, int prioridad) {
            this.competencia = competencia;
            this.modulo = modulo;
            this.brechas = brechas;
            this.prioridad = prioridad;
        }
    }
}
