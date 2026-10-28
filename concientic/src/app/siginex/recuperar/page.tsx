import type { Metadata } from "next";
import Link from "next/link";

import {
  Card,
  CardContent,
  CardDescription,
  CardFooter,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { SolicitarResetForm } from "@/components/siginex/solicitar-reset-form";

export const metadata: Metadata = {
  title: "Recuperar contraseña SIGINEX",
  description: "Solicita un enlace para restablecer tu contraseña.",
  robots: { index: false, follow: false },
};

/**
 * `/siginex/recuperar` (Fase 5): solicitar el correo de recuperación. Pública
 * (sin `SessionGuard`): no hay sesión en este punto. Mismo layout centrado que
 * el login.
 */
export default function SiginexRecuperarPage() {
  return (
    <div className="flex min-h-[70vh] items-center justify-center bg-muted px-4 py-16">
      <Card className="w-full max-w-md shadow-md">
        <CardHeader className="gap-2 text-center">
          <CardTitle className="text-2xl">Recuperar contraseña</CardTitle>
          <CardDescription>
            Introduce tu correo y te enviaremos un enlace para restablecerla.
          </CardDescription>
        </CardHeader>
        <CardContent>
          <SolicitarResetForm />
        </CardContent>
        <CardFooter className="justify-center">
          <Link
            href="/siginex/login"
            className="text-sm text-muted-foreground underline-offset-4 hover:underline"
          >
            Volver al inicio de sesión
          </Link>
        </CardFooter>
      </Card>
    </div>
  );
}
