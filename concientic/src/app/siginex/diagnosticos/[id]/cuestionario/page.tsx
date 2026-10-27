import type { Metadata } from "next";
import { notFound } from "next/navigation";
import Link from "next/link";

import { Button } from "@/components/ui/button";
import { SessionGuard } from "@/components/siginex/session-guard";
import { Cuestionario } from "@/components/siginex/cuestionario";

export const metadata: Metadata = {
  title: "Cuestionario SIGINEX",
  description: "Responder el diagnóstico del SGI por módulo.",
  robots: { index: false, follow: false },
};

// El [id] de esta ruta es el UUID de un diagnóstico. Se valida el formato antes
// de montar el cuestionario: un valor no-UUID (p. ej. un id de módulo) haría
// fallar la consulta del backend. Mismo criterio que la página original.
const UUID_RE =
  /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i;

/**
 * `/siginex/diagnosticos/[id]/cuestionario` (sub-fase 3c): responder el
 * diagnóstico por módulo. Server component mínimo que valida el UUID y, tras el
 * `SessionGuard`, monta el `Cuestionario` (client) — que carga módulos,
 * preguntas y respuestas vía `lib/siginex` y guarda con la PUT de respuestas.
 *
 * Next.js 16: `params` es una promesa.
 */
export default async function SiginexCuestionarioPage({
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
              Cuestionario del diagnóstico
            </h1>
            <p className="text-sm text-muted-foreground">
              Responde cada módulo del SGI y guarda tus respuestas.
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

        <Cuestionario diagnosticoId={id} />
      </div>
    </SessionGuard>
  );
}
