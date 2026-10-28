import type { Metadata } from "next";

import { SessionGuard } from "@/components/siginex/session-guard";
import { GestionUsuarios } from "@/components/siginex/gestion-usuarios";

export const metadata: Metadata = {
  title: "Usuarios SIGINEX",
  description: "Gestión de usuarios de la empresa.",
  robots: { index: false, follow: false },
};

/**
 * `/siginex/usuarios` (Fase 5): gestión de usuarios por el admin. Server
 * component mínimo protegido por `SessionGuard`; el cuerpo
 * (`GestionUsuarios`) es client y carga/muta vía `lib/siginex`. El backend
 * exige rol admin: un miembro recibe 403 y la UI lo muestra como error.
 */
export default function SiginexUsuariosPage() {
  return (
    <SessionGuard>
      <GestionUsuarios />
    </SessionGuard>
  );
}
