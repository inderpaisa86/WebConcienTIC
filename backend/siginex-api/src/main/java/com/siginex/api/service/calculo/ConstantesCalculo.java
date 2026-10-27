package com.siginex.api.service.calculo;

import java.util.List;
import java.util.Map;

/**
 * Constantes de los motores del pipeline, portadas <b>literal</b> de
 * {@code orquestador/motores.ts} (RESP, ADOPT, BENCH_NIVELES, BENCH_RECO) y de
 * {@code run.ts} (CRITERIO_CIERRE). Clase de utilidad, no instanciable.
 *
 * <p><b>Paridad:</b> los disparadores de {@link #ADOPT} se portan exactamente,
 * incluyendo tokens con tilde y abreviados ({@code "emisi"}, {@code "transparen"},
 * {@code "explica"}, {@code "supervis"}, {@code "auditoría"}, {@code "satisfacción"}),
 * y el <b>orden</b> del catálogo se preserva (el motor rompe al primer match).
 */
public final class ConstantesCalculo {

    private ConstantesCalculo() {
    }

    /** Responsable sugerido por módulo (port literal de {@code RESP}). */
    public static final Map<String, String> RESP = Map.of(
            "sst", "Responsable del SG-SST",
            "ambiental", "Coordinador Ambiental / HSEQ",
            "calidad", "Líder de Calidad",
            "riesgos", "Oficial de Riesgos / Continuidad",
            "cumplimiento", "Oficial de Cumplimiento",
            "datos", "Oficial de Protección de Datos / Seguridad de la Información",
            "gobierno", "Secretaría General / Gobierno Corporativo",
            "esg", "Líder de Sostenibilidad / ESG",
            "gobernanza_ia", "Responsable de Gobernanza de IA");

    /**
     * Catálogo de competencias de adopción por módulo. Cada entrada mantiene el
     * orden del catálogo TS; la primera competencia de cada módulo tiene
     * disparadores vacíos (la genérica / default). Port literal de {@code ADOPT}.
     */
    public static final Map<String, List<CompetenciaAdopcion>> ADOPT = Map.ofEntries(
            Map.entry("sst", List.of(
                    new CompetenciaAdopcion("Fundamentos del SG-SST", List.of()),
                    new CompetenciaAdopcion(
                            "Identificación de peligros y valoración de riesgos (IPER)",
                            List.of("peligro", "iper", "riesgo")),
                    new CompetenciaAdopcion(
                            "Investigación de incidentes e indicadores de SST",
                            List.of("incidente", "accidente", "indicador")))),
            Map.entry("ambiental", List.of(
                    new CompetenciaAdopcion("Gestión ambiental ISO 14001", List.of()),
                    new CompetenciaAdopcion(
                            "Cumplimiento legal ambiental y permisos",
                            List.of("permiso", "licencia", "legal", "631", "vertimiento")),
                    new CompetenciaAdopcion(
                            "Gestión de residuos y uso eficiente de recursos",
                            List.of("residuo", "respel", "agua", "energía")))),
            Map.entry("calidad", List.of(
                    new CompetenciaAdopcion("Sistema de gestión de calidad ISO 9001", List.of()),
                    new CompetenciaAdopcion(
                            "Enfoque a procesos e indicadores",
                            List.of("proceso", "indicador", "satisfacción")),
                    new CompetenciaAdopcion(
                            "Auditoría interna y mejora continua",
                            List.of("auditoría", "no conformidad", "mejora", "revisión")))),
            Map.entry("riesgos", List.of(
                    new CompetenciaAdopcion("Gestión de riesgos ISO 31000", List.of()),
                    new CompetenciaAdopcion(
                            "Continuidad del negocio ISO 22301",
                            List.of("continuidad", "22301", "bia")),
                    new CompetenciaAdopcion(
                            "Prevención LA/FT (SAGRLAFT/SARLAFT)",
                            List.of("saglaft", "sarlaft", "la/ft", "lavado", "supersociedades")))),
            Map.entry("cumplimiento", List.of(
                    new CompetenciaAdopcion("Cumplimiento legal y matriz de requisitos", List.of()),
                    new CompetenciaAdopcion(
                            "Programa de Transparencia y Ética Empresarial (PTEE)",
                            List.of("ptee", "transparencia", "2195")),
                    new CompetenciaAdopcion(
                            "Anticorrupción y debida diligencia",
                            List.of("anticorrup", "1474", "debida diligencia")))),
            Map.entry("datos", List.of(
                    new CompetenciaAdopcion(
                            "Protección de datos personales (Ley 1581)",
                            List.of("1581", "dato", "rnbd", "titular", "autoriz")),
                    new CompetenciaAdopcion(
                            "Seguridad de la información ISO 27001",
                            List.of("27001", "seguridad", "control", "acceso")),
                    new CompetenciaAdopcion(
                            "Gestión de incidentes de seguridad",
                            List.of("incidente")))),
            Map.entry("gobierno", List.of(
                    new CompetenciaAdopcion("Gobierno corporativo y ética", List.of()),
                    new CompetenciaAdopcion(
                            "Antisoborno ISO 37001",
                            List.of("soborno", "37001", "regalo", "conflicto")),
                    new CompetenciaAdopcion(
                            "Control interno y rendición de cuentas",
                            List.of("control interno", "coso", "transparencia", "rendición", "1712")))),
            Map.entry("esg", List.of(
                    new CompetenciaAdopcion("Fundamentos de sostenibilidad y ESG", List.of()),
                    new CompetenciaAdopcion(
                            "Reporte de sostenibilidad (NIIF S1/S2, GRI)",
                            List.of("reporte", "divulga", "niif", "gri", "emisor", "031")),
                    new CompetenciaAdopcion(
                            "Gestión de riesgos ASG y clima",
                            List.of("riesgo", "clima", "emisi", "material", "escenario")))),
            Map.entry("gobernanza_ia", List.of(
                    new CompetenciaAdopcion("Fundamentos de gobernanza de IA (ISO 42001)", List.of()),
                    new CompetenciaAdopcion(
                            "Evaluación de impacto y riesgos de IA",
                            List.of("impacto", "aia", "riesgo", "ciclo")),
                    new CompetenciaAdopcion(
                            "Datos, sesgos y transparencia en IA",
                            List.of("dato", "sesgo", "transparen", "explica", "supervis", "seguridad")))));

    /** Niveles tecnológicos de las 9 herramientas del mercado (port literal). */
    public static final int[] BENCH_NIVELES = {3, 3, 2, 2, 4, 4, 5, 4, 5};

    /** Recomendación de benchmark por nivel tecnológico 1..5 (port literal). */
    public static final Map<Integer, String> BENCH_RECO = Map.of(
            1, "Digitalice el SGI: pase de papel/Excel a una plataforma integrada.",
            2, "Consolide en una sola plataforma integrada (nivel 3) y elimine silos.",
            3, "Automatice flujos, alertas e indicadores (nivel 4) e incorpore analítica.",
            4, "Incorpore inteligencia (nivel 5): IA para vigilancia, priorización y recomendaciones.",
            5, "Manténgase a la vanguardia: orqueste IA sobre los datos del SGI (predicción y benchmarking).");

    /** Criterio de cierre fijo de toda tarea del plan (port literal de {@code run.ts}). */
    public static final String CRITERIO_CIERRE =
            "Evidencia verificable del requisito, vigente y trazable.";
}
