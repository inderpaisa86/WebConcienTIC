"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";

import { Button } from "@/components/ui/button";
import { registrarBenchmark, isSiginexDataError } from "@/lib/siginex";
import type { Benchmark } from "@/lib/siginex";

/**
 * Benchmark de mercado del diagnóstico ("use client"), portado de
 * `Siginex/.../resultados/Benchmark.tsx`.
 *
 * Diferencia clave con el original: el original recalculaba el benchmark en el
 * cliente para feedback inmediato (`calcularLocal`). Aquí NO se duplica la
 * lógica de negocio: `registrarBenchmark` ya devuelve el `Benchmark` calculado
 * por el backend Java, que es la única fuente de verdad. Se muestra ese valor.
 *
 * Estilo con tokens de concientic (sin navy+dorado literal). `unauthenticated`
 * (401) redirige a `/siginex/login`, como el resto de componentes de la 3b/3c.
 */

const NIVELES: Array<{ n: number; label: string }> = [
  { n: 1, label: "Manual / papel" },
  { n: 2, label: "Digital básico (Excel)" },
  { n: 3, label: "Plataforma integrada" },
  { n: 4, label: "Automatizado" },
  { n: 5, label: "Inteligencia / IA" },
];

/** Clases del `<select>` nativo, alineadas con la primitiva `Input`/`CrearDiagnosticoForm`. */
const SELECT_CLASSES =
  "flex h-10 w-full rounded-lg border border-border bg-background px-3 py-2 text-sm text-foreground transition-colors outline-none focus-visible:border-ring focus-visible:ring-3 focus-visible:ring-ring/50 disabled:cursor-not-allowed disabled:opacity-50";

export function BenchmarkMercado({
  diagnosticoId,
  inicial,
}: {
  diagnosticoId: string;
  inicial: Benchmark | null;
}) {
  const router = useRouter();
  const [data, setData] = useState<Benchmark | null>(inicial);
  const [nivel, setNivel] = useState<number>(inicial?.nivel_empresa ?? 3);
  const [guardando, setGuardando] = useState(false);
  const [error, setError] = useState<string | null>(null);

  async function comparar() {
    setGuardando(true);
    setError(null);
    try {
      const resultado = await registrarBenchmark(diagnosticoId, { nivel });
      setData(resultado);
    } catch (err) {
      if (isSiginexDataError(err) && err.kind === "unauthenticated") {
        router.replace("/siginex/login");
        return;
      }
      setError(
        isSiginexDataError(err)
          ? err.message
          : "Ha ocurrido un error inesperado. Inténtalo de nuevo.",
      );
    } finally {
      setGuardando(false);
    }
  }

  return (
    <section className="rounded-xl border border-border bg-card p-6 shadow-sm">
      <h3 className="mb-1 font-heading text-lg font-semibold text-foreground">
        Benchmark de mercado
      </h3>
      <p className="mb-4 text-xs text-muted-foreground">
        Compara tu nivel tecnológico con las herramientas del mercado.
      </p>

      <div className="flex flex-wrap items-end gap-3">
        <div className="min-w-56 flex-1">
          <label
            htmlFor="benchmark-nivel"
            className="mb-1.5 block text-[11.5px] font-semibold text-muted-foreground"
          >
            Nivel tecnológico de tu empresa
          </label>
          <select
            id="benchmark-nivel"
            value={nivel}
            onChange={(e) => setNivel(Number(e.target.value))}
            className={SELECT_CLASSES}
            disabled={guardando}
          >
            {NIVELES.map((n) => (
              <option key={n.n} value={n.n}>
                {n.n} · {n.label}
              </option>
            ))}
          </select>
        </div>
        <Button onClick={() => void comparar()} disabled={guardando}>
          {guardando ? "Calculando…" : "Comparar"}
        </Button>
      </div>

      {error ? (
        <p role="alert" aria-live="assertive" className="mt-4 text-sm text-destructive">
          {error}
        </p>
      ) : null}

      {data ? (
        <div className="mt-5 space-y-4">
          <div className="grid grid-cols-2 gap-3 sm:grid-cols-4">
            <Stat label="Tu nivel" valor={String(data.nivel_empresa)} />
            <Stat label="Promedio mercado" valor={String(data.promedio_mercado)} />
            <Stat
              label="Por encima de ti"
              valor={`${data.herramientas_por_encima} / 9`}
            />
            <Stat label="Brecha al líder" valor={String(data.brecha_al_lider)} />
          </div>
          {data.recomendacion ? (
            <div className="rounded-lg border border-border bg-muted px-4 py-3 text-[13px] leading-relaxed text-foreground">
              <strong className="font-semibold">Recomendación.</strong>{" "}
              {data.recomendacion}
            </div>
          ) : null}
        </div>
      ) : null}
    </section>
  );
}

function Stat({ label, valor }: { label: string; valor: string }) {
  return (
    <div className="rounded-lg border border-border bg-background p-3.5">
      <p className="text-[11px] font-semibold tracking-[0.4px] text-muted-foreground uppercase">
        {label}
      </p>
      <p className="mt-1 font-heading text-[22px] leading-none font-bold text-foreground">
        {valor}
      </p>
    </div>
  );
}
