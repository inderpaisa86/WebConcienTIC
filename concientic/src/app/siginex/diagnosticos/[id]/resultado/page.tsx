import type { Metadata } from "next";
import { notFound } from "next/navigation";
import Link from "next/link";

import { Button } from "@/components/ui/button";
import { SessionGuard } from "@/components/siginex/session-guard";
import { ResultadoDiagnostico } from "@/components/siginex/resultado-diagnostico";

export const metadata: Metadata = {
  title: "Resultados SIGINEX",
  description: "Resultado, plan de mejora y benchmark del diagnóstico del SGI.",
  robots: { index: false, follow: false },
};

// El [id] de esta ruta es el UUID de un diagnóstico. Se valida el formato antes
// de montar la vista; mismo criterio que la página del cuestionario (3c).
const UUID_RE =
  /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i;

/**
 * `/siginex/diagnosticos/[id]/resultado` (sub-fase 3d): resultado consolidado,
 * plan de mejora y benchmark de un diagnóstico. Server component mínimo que
 * valida el UUID y, tras el `SessionGuard`, monta `ResultadoDiagnostico`
 * (client) — que dispara/lee el cálculo y consume la API Java vía `lib/siginex`.
 *
 * Next.js 16: `params` es una promesa.
 */
export default async function SiginexResultadoPage({
  params,
}: {
  params: Promise<{ id: string }>;
}) {
  const { id } = await params;
  if (!UUID_RE.test(id)) {
    notFound();
  }

  return (
    <SessionGuard>
      <div className="mx-auto w-full max-w-4xl space-y-6">
        <header className="flex flex-wrap items-center justify-between gap-4">
          <div>
            <h1 className="font-heading text-2xl font-semibold text-foreground">
              Resultados del diagnóstico
            </h1>
            <p className="text-sm text-muted-foreground">
              Score SGI, cumplimiento por módulo, brechas, plan de mejora y
              benchmark.
            </p>
          </div>
          <Button
            variant="outline"
            size="lg"
            render={<Link href="/siginex/diagnosticos" />}
          >
            Volver a diagnósticos
          </Button>
        </header>

        <ResultadoDiagnostico diagnosticoId={id} />
      </div>
    </SessionGuard>
  );
}
