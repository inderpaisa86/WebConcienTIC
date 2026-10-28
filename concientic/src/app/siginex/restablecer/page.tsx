import type { Metadata } from "next";

import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { RestablecerForm } from "@/components/siginex/restablecer-form";

export const metadata: Metadata = {
  title: "Restablecer contraseña SIGINEX",
  description: "Define una nueva contraseña con el enlace de recuperación.",
  robots: { index: false, follow: false },
};

/**
 * `/siginex/restablecer?token=<token>` (Fase 5): confirmar el reset con el token
 * del enlace del correo. Pública (sin `SessionGuard`). Next.js 16: `searchParams`
 * es una promesa; si llega repetido se toma el primero. El token viaja al
 * componente client, que lo envía al BFF; nunca se persiste en el front.
 */
export default async function SiginexRestablecerPage({
  searchParams,
}: {
  searchParams: Promise<{ token?: string | string[] }>;
}) {
  const { token } = await searchParams;
  const tokenValor = Array.isArray(token) ? (token[0] ?? null) : (token ?? null);

  return (
    <div className="flex min-h-[70vh] items-center justify-center bg-muted px-4 py-16">
      <Card className="w-full max-w-md shadow-md">
        <CardHeader className="gap-2 text-center">
          <CardTitle className="text-2xl">Restablecer contraseña</CardTitle>
          <CardDescription>
            Define tu nueva contraseña para acceder a SIGINEX.
          </CardDescription>
        </CardHeader>
        <CardContent>
          <RestablecerForm token={tokenValor} />
        </CardContent>
      </Card>
    </div>
  );
}
