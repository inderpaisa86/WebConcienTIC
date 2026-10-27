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
  return (
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
  );
}
