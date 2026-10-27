"use client";

import { useCallback, useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";

import { Button } from "@/components/ui/button";
import { BenchmarkMercado } from "@/components/siginex/benchmark-mercado";
import { PlanMejora } from "@/components/siginex/plan-mejora";
import {
  calcularDiagnostico,
  isSiginexDataError,
  listarModulos,
  obtenerBenchmark,
  obtenerDiagnostico,
  obtenerPlanMejora,
  obtenerResultado,
  type Benchmark,
  type Resultado,
  type TareaPlan,
} from "@/lib/siginex";

/**
 * Resultados del diagnóstico SIGINEX (sub-fase 3d), orquestador "use client".
 * Portado de `Siginex/.../resultados/page.tsx` + `plan-mejora/page.tsx`,
 * unificados en una sola vista por diagnóstico (el backend Java expone el plan
 * por diagnóstico, no de forma global como el original).
 *
 * Flujo:
 * 1. Al montar, lee el diagnóstico (`obtenerDiagnostico`) para conocer su
 *    `estado`. Si NO está `calculado`, muestra el botón "Calcular diagnóstico".
 * 2. Si está `calculado`, carga en paralelo resultado + plan + benchmark.
 * 3. "Calcular" dispara `POST .../calcular`; al volver con el resultado, carga
 *    plan y benchmark.
 *
 * Patrón client coherente con 3b/3c (SessionGuard client → carga vía
 * `lib/siginex`). `unauthenticated` (401) redirige a `/siginex/login`.
 * Estilo con tokens de concientic (sin navy+dorado literal del original).
 */

type Estado = "cargando" | "sin-calcular" | "listo" | "error";

const NIVEL_NOMBRE: Record<number, string> = {
  1: "Inicial",
  2: "Básico",
  3: "En desarrollo",
  4: "Optimizado",
  5: "Excelencia",
};

/** Clase de color de la barra de cumplimiento según el porcentaje. */
function claseBarra(pct: number | null): string {
  if (pct == null) return "bg-muted-foreground/40";
  if (pct < 41) return "bg-destructive";
  if (pct < 61) return "bg-amber-500";
  if (pct < 81) return "bg-yellow-500";
  return "bg-emerald-600";
}

export function ResultadoDiagnostico({
  diagnosticoId,
}: {
  diagnosticoId: string;
}) {
  const router = useRouter();

  const [estado, setEstado] = useState<Estado>("cargando");
  const [mensajeError, setMensajeError] = useState<string | null>(null);

  const [resultado, setResultado] = useState<Resultado | null>(null);
  const [plan, setPlan] = useState<TareaPlan[]>([]);
  const [benchmark, setBenchmark] = useState<Benchmark | null>(null);
  // Mapa skillId → nombre legible del módulo (del KB de la API Java). Fallback
  // al skillId si un módulo del resultado no está en el KB.
  const [nombresModulo, setNombresModulo] = useState<Record<string, string>>(
    {},
  );

  const [calculando, setCalculando] = useState(false);

  /** Normaliza un error a mensaje seguro; redirige en 401. */
  const resolverError = useCallback(
    (err: unknown): string => {
      if (isSiginexDataError(err) && err.kind === "unauthenticated") {
        router.replace("/siginex/login");
        return "Tu sesión no es válida o ha caducado.";
      }
      return isSiginexDataError(err)
        ? err.message
        : "Ha ocurrido un error inesperado. Inténtalo de nuevo.";
    },
    [router],
  );

  /** Carga resultado + plan + benchmark + nombres de módulo (ya calculado). */
  const cargarCalculado = useCallback(async () => {
    const [res, tareas, bench, modulos] = await Promise.all([
      obtenerResultado(diagnosticoId),
      obtenerPlanMejora(diagnosticoId),
      obtenerBenchmark(diagnosticoId),
      listarModulos(),
    ]);
    setResultado(res);
    setPlan(tareas);
    setBenchmark(bench);
    setNombresModulo(
      Object.fromEntries(modulos.map((m) => [m.id, m.nombre])),
    );
  }, [diagnosticoId]);

  /** Carga inicial: decide estado por el `estado` del diagnóstico. */
  const cargar = useCallback(async () => {
    setEstado("cargando");
    setMensajeError(null);
    try {
      const diag = await obtenerDiagnostico(diagnosticoId);
      if (diag.estado !== "calculado") {
        setEstado("sin-calcular");
        return;
      }
      await cargarCalculado();
      setEstado("listo");
    } catch (err) {
      setMensajeError(resolverError(err));
      setEstado("error");
    }
  }, [diagnosticoId, cargarCalculado, resolverError]);

  useEffect(() => {
    let activo = true;
    (async () => {
      try {
        const diag = await obtenerDiagnostico(diagnosticoId);
        if (!activo) return;
        if (diag.estado !== "calculado") {
          setEstado("sin-calcular");
          return;
        }
        await cargarCalculado();
        if (!activo) return;
        setEstado("listo");
      } catch (err) {
        if (!activo) return;
        setMensajeError(resolverError(err));
        setEstado("error");
      }
    })();
    return () => {
      activo = false;
    };
  }, [diagnosticoId, cargarCalculado, resolverError]);

  /** Dispara el cálculo y recarga resultado + plan + benchmark + módulos. */
  async function calcular() {
    setCalculando(true);
    setMensajeError(null);
    try {
      // El POST devuelve el resultado, pero recargamos todo con
      // `cargarCalculado` para traer también plan, benchmark y los nombres de
      // módulo de forma consistente (una sola fuente de carga).
      await calcularDiagnostico(diagnosticoId);
      await cargarCalculado();
      setEstado("listo");
    } catch (err) {
      setMensajeError(resolverError(err));
      // Permanece en "sin-calcular" para reintentar.
      setEstado("sin-calcular");
    } finally {
      setCalculando(false);
    }
  }

  if (estado === "cargando") {
    return (
      <p role="status" aria-live="polite" className="text-sm text-muted-foreground">
        Cargando resultados…
      </p>
    );
  }

  if (estado === "error") {
    return (
      <div
        role="status"
        aria-live="polite"
        className="flex flex-col items-start gap-3 rounded-xl border border-border bg-card p-6"
      >
        <p className="text-sm text-muted-foreground">
          {mensajeError ?? "No se pudieron cargar los resultados."}
        </p>
        <Button variant="outline" size="sm" onClick={() => void cargar()}>
          Reintentar
        </Button>
      </div>
    );
  }

  if (estado === "sin-calcular") {
    return (
      <div className="flex flex-col items-start gap-4 rounded-xl border border-border bg-card p-6">
        <div>
          <h3 className="font-heading text-lg font-semibold text-foreground">
            Diagnóstico sin calcular
          </h3>
          <p className="text-sm text-muted-foreground">
            Calcula el diagnóstico para ver el score SGI, el cumplimiento por
            módulo, las brechas y el plan de mejora.
          </p>
        </div>
        {mensajeError ? (
          <p role="alert" aria-live="assertive" className="text-sm text-destructive">
            {mensajeError}
          </p>
        ) : null}
        <div className="flex flex-wrap gap-3">
          <Button onClick={() => void calcular()} disabled={calculando}>
            {calculando ? "Calculando…" : "Calcular diagnóstico"}
          </Button>
          <Button
            variant="outline"
            render={
              <Link href={`/siginex/diagnosticos/${diagnosticoId}/cuestionario`} />
            }
          >
            Volver al cuestionario
          </Button>
        </div>
      </div>
    );
  }

  // estado === "listo"
  const score = resultado?.score != null ? Number(resultado.score) : null;

  return (
    <div className="space-y-5">
      {/* Hero con score global */}
      <section className="grid items-center gap-6 rounded-2xl border border-border bg-card p-7 shadow-sm sm:grid-cols-[auto_1fr]">
        <div className="grid size-[132px] place-content-center justify-items-center rounded-full border-4 border-primary/30 text-center">
          <span className="font-heading text-[40px] leading-none font-bold text-foreground">
            {score != null ? score.toFixed(0) : "—"}
          </span>
          <span className="mt-1 text-[11px] text-muted-foreground">/ 100</span>
        </div>
        <div>
          <p className="text-[13px] tracking-[1.4px] text-muted-foreground uppercase">
            Score SGI
          </p>
          <p className="font-heading text-[27px] font-bold text-foreground">
            {resultado?.nivelSgi != null
              ? (NIVEL_NOMBRE[resultado.nivelSgi] ?? "—")
              : "—"}
          </p>
          <p className="max-w-[56ch] text-[13.5px] text-muted-foreground">
            Cumplimiento global ponderado de los módulos evaluados. Resultado
            orientativo.
          </p>
        </div>
      </section>

      {/* Cumplimiento por módulo */}
      <section className="rounded-xl border border-border bg-card p-6 shadow-sm">
        <h3 className="mb-4 font-heading text-lg font-semibold text-foreground">
          Cumplimiento por módulo
        </h3>
        {resultado && resultado.pilares.length > 0 ? (
          <div className="space-y-3">
            {resultado.pilares.map((p) => {
              const pct =
                p.cumplimientoPct != null ? Number(p.cumplimientoPct) : null;
              return (
                <div key={p.skillId}>
                  <div className="mb-1.5 flex items-baseline justify-between gap-3">
                    <span className="text-[12.5px] font-semibold text-foreground">
                      {nombresModulo[p.skillId] ?? p.skillId}
                    </span>
                    <span className="text-[12.5px] font-semibold text-muted-foreground">
                      {pct != null ? `${pct.toFixed(0)}%` : "N/A"}
                    </span>
                  </div>
                  <div className="h-[9px] overflow-hidden rounded-full bg-muted">
                    <div
                      className={`h-full rounded-full transition-all ${claseBarra(pct)}`}
                      style={{ width: `${pct ?? 0}%` }}
                    />
                  </div>
                </div>
              );
            })}
          </div>
        ) : (
          <p className="text-sm text-muted-foreground">
            Sin datos de cumplimiento por módulo.
          </p>
        )}
      </section>

      {/* Benchmark de mercado */}
      <BenchmarkMercado diagnosticoId={diagnosticoId} inicial={benchmark} />

      {/* Brechas prioritarias */}
      <section className="rounded-xl border border-border bg-card p-6 shadow-sm">
        <h3 className="mb-1 font-heading text-lg font-semibold text-foreground">
          Brechas de alta prioridad
        </h3>
        <p className="mb-4 text-xs text-muted-foreground">
          Requisitos legales sin cumplir o con respuesta &ldquo;no
          cumple&rdquo;.
        </p>
        {!resultado || resultado.recomendaciones.length === 0 ? (
          <p className="text-sm text-muted-foreground">
            No se detectaron brechas de alta prioridad.
          </p>
        ) : (
          <div className="divide-y divide-border">
            {resultado.recomendaciones.slice(0, 20).map((rec, i) => (
              <div key={`${rec.skillId}-${i}`} className="flex gap-3 py-3">
                <span className="mt-1.5 size-2 flex-none rounded-full bg-destructive" />
                <p className="min-w-0 flex-1 text-[13px] leading-snug text-foreground">
                  {rec.texto}
                </p>
                <span className="h-fit rounded-md border border-border bg-secondary px-2 py-0.5 text-[10.5px] text-secondary-foreground">
                  {rec.skillId}
                </span>
              </div>
            ))}
          </div>
        )}
      </section>

      {/* Plan de mejora */}
      <section className="space-y-3">
        <div>
          <h3 className="font-heading text-lg font-semibold text-foreground">
            Plan de mejora
          </h3>
          <p className="text-sm text-muted-foreground">
            Tareas priorizadas generadas desde las brechas del diagnóstico.
          </p>
        </div>
        <PlanMejora tareas={plan} />
      </section>

      <div className="flex flex-wrap gap-3">
        <Button
          variant="outline"
          onClick={() => void calcular()}
          disabled={calculando}
        >
          {calculando ? "Recalculando…" : "Recalcular"}
        </Button>
        <Button
          variant="outline"
          render={<Link href="/siginex/diagnosticos" />}
        >
          Volver a diagnósticos
        </Button>
      </div>

      <p className="pt-2 text-center text-[11px] leading-relaxed text-muted-foreground">
        Resultado orientativo. No sustituye la autoevaluación oficial de
        estándares mínimos, una auditoría formal ni un concepto legal.
      </p>
    </div>
  );
}
