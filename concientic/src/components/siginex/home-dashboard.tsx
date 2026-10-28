import Link from "next/link";

import { Button } from "@/components/ui/button";
import {
  MODULOS_SGI,
  TOTAL_PREGUNTAS,
  KB_VERSION,
} from "@/lib/siginex/modulos-sgi";

/**
 * Contenido de la home `/siginex`: clon visual fiel de la home del repo de
 * referencia (Siginex). Usa colores HEX literales (navy + dorado) en lugar de
 * los tokens de concientic, por decisión explícita del usuario para esta
 * pantalla. Todas las tarjetas/CTA enlazan a rutas reales de concientic.
 */
const KPIS = [
  { etiqueta: "Módulos del SGI", valor: String(MODULOS_SGI.length) },
  { etiqueta: "Preguntas del banco", valor: String(TOTAL_PREGUNTAS) },
  { etiqueta: "Versión del banco", valor: KB_VERSION },
  { etiqueta: "Ítems SST oficiales", valor: "60" },
];

const SHADOW_CARD =
  "0 1px 2px rgba(20, 32, 46, 0.04), 0 6px 22px rgba(20, 32, 46, 0.06)";

export function HomeDashboard() {
  return (
    <div className="mx-auto max-w-[1080px] space-y-6">
      {/* Hero */}
      <section
        className="rounded-2xl px-7 py-7 text-white"
        style={{ background: "#1E3050" }}
      >
        <p className="mb-2.5 font-mono text-[11px] uppercase tracking-[2px] text-[#C08A2E]">
          Autodiagnóstico del SGI
        </p>
        <h2 className="max-w-[24ch] text-[27px] leading-[1.15] font-bold">
          Mide, prioriza y actúa sobre tu Sistema de Gestión Integral
        </h2>
        <p className="mt-3 max-w-[56ch] text-[13.5px] text-[#CBD5E1]">
          SIGINEX evalúa el cumplimiento normativo y la madurez de tu
          organización en {MODULOS_SGI.length} módulos, y convierte cada brecha
          en un plan de acción y en rutas de aprendizaje.
        </p>
        <div className="mt-5 flex flex-wrap gap-3">
          <Button
            render={<Link href="/siginex/diagnosticos" />}
            className="bg-[#C08A2E] text-white hover:bg-[#a9781f]"
          >
            Iniciar diagnóstico
          </Button>
          <Button
            render={<Link href="/siginex/diagnosticos" />}
            className="border border-white/25 bg-transparent text-white hover:bg-white/10"
          >
            Ver reportes
          </Button>
        </div>
      </section>

      {/* KPIs */}
      <section className="grid grid-cols-2 gap-4 md:grid-cols-4">
        {KPIS.map((kpi) => (
          <div
            key={kpi.etiqueta}
            className="rounded-xl border border-[#E1E7F0] bg-[#ffffff] p-[18px]"
            style={{ boxShadow: SHADOW_CARD }}
          >
            <p className="text-[11px] font-semibold uppercase tracking-[0.5px] text-[#64748B]">
              {kpi.etiqueta}
            </p>
            <p className="mt-2 text-[30px] leading-[1.1] font-bold text-[#14202E]">
              {kpi.valor}
            </p>
          </div>
        ))}
      </section>

      {/* Módulos */}
      <section className="space-y-4">
        <div>
          <h3 className="text-lg font-semibold text-[#14202E]">
            Módulos del SGI
          </h3>
          <p className="text-sm text-[#64748B]">
            Cada módulo se evalúa por separado y aporta su peso al score global.
          </p>
        </div>

        <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {MODULOS_SGI.map((m) => (
            <Link
              key={m.id}
              href={`/siginex/diagnosticos?modulo=${encodeURIComponent(m.id)}`}
              className="group rounded-[14px] border border-[#E1E7F0] bg-[#ffffff] p-5 transition-colors hover:border-[#C08A2E]/50"
              style={{ boxShadow: SHADOW_CARD }}
            >
              <div className="flex items-start justify-between gap-2">
                <h4 className="text-[15px] font-semibold text-[#14202E]">
                  {m.nombre}
                </h4>
                <span className="rounded-md border border-[#E1E7F0] bg-[#edf1f6] px-2 py-0.5 font-mono text-[10.5px] text-[#2A4571]">
                  {m.corto}
                </span>
              </div>
              <p className="mt-2 line-clamp-2 text-[12.5px] leading-relaxed text-[#64748B]">
                {m.descripcion}
              </p>
              <div className="mt-4 flex items-center justify-between border-t border-[#E1E7F0] pt-3 text-[11.5px] text-[#64748B]">
                <span className="font-mono">{m.preguntas} preguntas</span>
                <span className="font-mono">
                  Peso {(m.peso * 100).toFixed(0)}%
                </span>
              </div>
            </Link>
          ))}
        </div>
      </section>

      {/* Descargo */}
      <section>
        <div className="rounded-[11px] border border-[#F3E7CC] bg-[#FFF7E6] px-4 py-3 text-[13px] leading-relaxed text-[#7a5b18]">
          <strong className="font-semibold">Resultado orientativo.</strong> El
          diagnóstico no sustituye la autoevaluación oficial de estándares
          mínimos, una auditoría formal ni un concepto legal. Los 60 ítems de
          SG-SST son oficiales (Res. 0312/2019); el resto se generó desde la
          estructura normativa de cada dominio.
        </div>
      </section>
    </div>
  );
}
