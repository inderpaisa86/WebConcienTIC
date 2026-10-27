import type { Metadata } from "next";

import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { SessionGuard } from "@/components/siginex/session-guard";
import { LogoutButton } from "@/components/siginex/logout-button";

export const metadata: Metadata = {
  title: "Panel SIGINEX",
  description: "Panel de SIGINEX.",
  robots: { index: false, follow: false },
};

/**
 * Placeholder de `/siginex/panel`. El panel real se implementa en la sub-fase
 * 3b. Aquí sirve para validar el flujo login → panel → logout; va protegido
 * por `SessionGuard`.
 */
export default function SiginexPanelPage() {
  return (
    <SessionGuard>
      <Card className="w-full max-w-md shadow-md">
        <CardHeader className="gap-2 text-center">
          <CardTitle className="text-2xl">Panel</CardTitle>
          <CardDescription>
            Panel provisional de SIGINEX. El panel real llega en la sub-fase 3b.
          </CardDescription>
        </CardHeader>
        <CardContent className="flex justify-center">
          <LogoutButton />
        </CardContent>
      </Card>
    </SessionGuard>
  );
}
