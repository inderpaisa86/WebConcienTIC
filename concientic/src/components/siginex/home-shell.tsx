import type { ReactNode } from "react";

import { HomeSidebar } from "@/components/siginex/home-sidebar";
import { HomeTopbar } from "@/components/siginex/home-topbar";

/**
 * Shell navy de la home `/siginex`, clon del `DashboardShell` del repo de
 * referencia (Siginex): Sidebar navy + Topbar + área de contenido. El fondo
 * usa el canvas HEX literal del original (`#EDF1F7`) para no depender de los
 * tokens de concientic. Esta pantalla NO usa el header global del sitio.
 */
export function HomeShell({
  titulo,
  children,
}: {
  titulo: string;
  children: ReactNode;
}) {
  return (
    <div className="flex min-h-screen" style={{ background: "#EDF1F7" }}>
      <HomeSidebar />
      <div className="flex min-w-0 flex-1 flex-col">
        <HomeTopbar titulo={titulo} />
        <main className="flex-1 overflow-y-auto p-6">{children}</main>
      </div>
    </div>
  );
}
