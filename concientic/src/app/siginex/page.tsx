import type { Metadata } from "next";

import { SessionGuard } from "@/components/siginex/session-guard";
import { HomeShell } from "@/components/siginex/home-shell";
import { HomeDashboard } from "@/components/siginex/home-dashboard";

export const metadata: Metadata = {
  title: "SIGINEX",
  description: "Autodiagnóstico del Sistema de Gestión Integral.",
  robots: { index: false, follow: false },
};

/**
 * `/siginex`: home dashboard de SIGINEX (clon visual de la home del repo de
 * referencia). Es la pantalla a la que aterriza el usuario tras un login
 * correcto (la URL queda en `/siginex`, sin `/panel`).
 *
 * Server component mínimo protegido por `SessionGuard`: sin sesión válida el
 * guard redirige a `/siginex/login`; con sesión renderiza el shell navy
 * (Sidebar + Topbar) con el contenido del dashboard.
 */
export default function SiginexIndexPage() {
  return (
    <SessionGuard>
      <HomeShell titulo="Panel">
        <HomeDashboard />
      </HomeShell>
    </SessionGuard>
  );
}
