import type { Metadata } from "next";

import { SessionGuard } from "@/components/siginex/session-guard";
import { PanelResumen } from "@/components/siginex/panel-resumen";

export const metadata: Metadata = {
  title: "Panel SIGINEX",
  description: "Panel de SIGINEX.",
  robots: { index: false, follow: false },
};

/**
 * `/siginex/panel`: panel real de SIGINEX (sub-fase 3b). Server component
 * mínimo protegido por `SessionGuard`; el cuerpo (`PanelResumen`) es client y
 * carga/deriva los KPIs vía `lib/siginex`. El `LogoutButton` vive dentro de
 * `PanelResumen`.
 */
export default function SiginexPanelPage() {
  return (
    <SessionGuard>
      <PanelResumen />
    </SessionGuard>
  );
}
