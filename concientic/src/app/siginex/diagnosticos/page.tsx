import type { Metadata } from "next";

import { SessionGuard } from "@/components/siginex/session-guard";
import { GestionDiagnosticos } from "@/components/siginex/gestion-diagnosticos";

export const metadata: Metadata = {
  title: "Diagnósticos SIGINEX",
  description: "Gestión de organizaciones y diagnósticos de SIGINEX.",
  robots: { index: false, follow: false },
};

/**
 * `/siginex/diagnosticos`: gestión de organizaciones/diagnósticos (sub-fase
 * 3b). Server component mínimo protegido por `SessionGuard`; el cuerpo
 * (`GestionDiagnosticos`) es client y carga/muta vía `lib/siginex`.
 *
 * Fase 4 (navegación por módulos): acepta `?modulo=<id>`. Cuando el usuario
 * pulsa un módulo en la home/sidebar llega aquí con ese id; la pantalla elige
 * sobre QUÉ diagnóstico abrir ese módulo (modelo multi-empresa) y propaga el
 * `?modulo=<id>` al enlace del cuestionario. Next.js 16: `searchParams` es
 * promesa.
 */
export default async function SiginexDiagnosticosPage({
  searchParams,
}: {
  searchParams: Promise<{ modulo?: string | string[] }>;
}) {
  const { modulo } = await searchParams;
  const moduloSeleccionado = Array.isArray(modulo) ? modulo[0] : modulo;

  return (
    <SessionGuard>
      <GestionDiagnosticos moduloSeleccionado={moduloSeleccionado} />
    </SessionGuard>
  );
}
