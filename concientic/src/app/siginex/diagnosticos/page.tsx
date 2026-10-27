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
 */
export default function SiginexDiagnosticosPage() {
  return (
    <SessionGuard>
      <GestionDiagnosticos />
    </SessionGuard>
  );
}
