import type { Metadata } from "next";

import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { LoginForm } from "@/components/siginex/login-form";

export const metadata: Metadata = {
  title: "Acceso SIGINEX",
  description: "Inicia sesión para acceder a SIGINEX.",
  robots: { index: false, follow: false },
};

export default function SiginexLoginPage() {
  // El centrado vive aquí (no en el layout compartido) porque la home
  // dashboard de `/siginex` debe ocupar todo el ancho con su shell navy.
  return (
    <div className="flex min-h-[70vh] items-center justify-center bg-muted px-4 py-16">
      <Card className="w-full max-w-md shadow-md">
        <CardHeader className="gap-2 text-center">
          <CardTitle className="text-2xl">Acceso SIGINEX</CardTitle>
          <CardDescription>
            Inicia sesión con tu correo y contraseña para continuar.
          </CardDescription>
        </CardHeader>
        <CardContent>
          <LoginForm />
        </CardContent>
      </Card>
    </div>
  );
}
