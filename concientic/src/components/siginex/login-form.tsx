"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";

import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { login, isSiginexAuthError } from "@/lib/siginex";
import {
  siginexLoginSchema,
  type SiginexLoginInput,
} from "@/lib/validations/siginex-login";

/** Ruta a la que se redirige tras un login correcto: la home de SIGINEX. */
const PANEL_ROUTE = "/siginex";

export function LoginForm() {
  const router = useRouter();
  const [serverError, setServerError] = useState<string | null>(null);

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<SiginexLoginInput>({
    resolver: zodResolver(siginexLoginSchema),
    defaultValues: { email: "", password: "" },
  });

  async function onSubmit(values: SiginexLoginInput) {
    setServerError(null);
    try {
      await login(values.email, values.password);
      router.push(PANEL_ROUTE);
    } catch (err) {
      // Mensaje genérico y seguro: no se filtra detalle del backend.
      setServerError(
        isSiginexAuthError(err)
          ? err.message
          : "Ha ocurrido un error inesperado. Inténtalo de nuevo.",
      );
    }
  }

  return (
    <form onSubmit={handleSubmit(onSubmit)} noValidate className="flex flex-col gap-4">
      <div className="flex flex-col gap-1.5">
        <Label htmlFor="email">Correo</Label>
        <Input
          id="email"
          type="email"
          autoComplete="email"
          autoFocus
          aria-invalid={!!errors.email}
          {...register("email")}
        />
        {errors.email?.message ? (
          <p className="text-sm text-destructive">{errors.email.message}</p>
        ) : null}
      </div>

      <div className="flex flex-col gap-1.5">
        <Label htmlFor="password">Contraseña</Label>
        <Input
          id="password"
          type="password"
          autoComplete="current-password"
          aria-invalid={!!errors.password}
          {...register("password")}
        />
        {errors.password?.message ? (
          <p className="text-sm text-destructive">{errors.password.message}</p>
        ) : null}
        <Link
          href="/siginex/recuperar"
          className="self-end text-sm text-muted-foreground underline-offset-4 hover:underline"
        >
          ¿Olvidaste tu contraseña?
        </Link>
      </div>

      {serverError ? (
        <p role="alert" aria-live="assertive" className="text-sm text-destructive">
          {serverError}
        </p>
      ) : null}

      <Button type="submit" size="lg" disabled={isSubmitting} className="mt-2 w-full">
        {isSubmitting ? "Entrando..." : "Entrar"}
      </Button>
    </form>
  );
}
