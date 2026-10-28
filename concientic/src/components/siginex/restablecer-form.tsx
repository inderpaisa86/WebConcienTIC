"use client";

import { useState } from "react";
import Link from "next/link";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";

import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { confirmarReset, isSiginexDataError } from "@/lib/siginex";
import {
  siginexRestablecerSchema,
  type SiginexRestablecerInput,
} from "@/lib/validations/siginex-reset";

/**
 * Formulario "restablecer contraseña" (client). Recibe el `token` del enlace
 * (search param, leído por la página server) y pide la nueva contraseña +
 * confirmación. Al confirmar correctamente muestra éxito y ofrece ir al login
 * (el backend ya revocó las sesiones). Un token inválido/usado/expirado produce
 * un mensaje genérico (el backend responde 401 sin distinguir el motivo).
 *
 * Si no llega token en la URL, se muestra un aviso en lugar del formulario.
 */
export function RestablecerForm({ token }: { token: string | null }) {
  const [completado, setCompletado] = useState(false);
  const [serverError, setServerError] = useState<string | null>(null);

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<SiginexRestablecerInput>({
    resolver: zodResolver(siginexRestablecerSchema),
    defaultValues: { nuevaPassword: "", confirmar: "" },
  });

  async function onSubmit(values: SiginexRestablecerInput) {
    setServerError(null);
    if (!token) {
      setServerError("El enlace no es válido o está incompleto.");
      return;
    }
    try {
      await confirmarReset(token, values.nuevaPassword);
      setCompletado(true);
    } catch (err) {
      if (isSiginexDataError(err) && err.kind === "validation") {
        setServerError("La contraseña no cumple los requisitos.");
        return;
      }
      if (isSiginexDataError(err) && err.kind === "unauthenticated") {
        setServerError(
          "El enlace no es válido o ha caducado. Solicita uno nuevo.",
        );
        return;
      }
      setServerError(
        "No se pudo restablecer la contraseña. Inténtalo de nuevo más tarde.",
      );
    }
  }

  if (!token) {
    return (
      <div className="flex flex-col items-start gap-3">
        <p role="alert" aria-live="assertive" className="text-sm text-destructive">
          El enlace no es válido o está incompleto.
        </p>
        <Button variant="outline" size="sm" render={<Link href="/siginex/recuperar" />}>
          Solicitar un enlace nuevo
        </Button>
      </div>
    );
  }

  if (completado) {
    return (
      <div className="flex flex-col items-start gap-3">
        <p role="status" aria-live="polite" className="text-sm text-muted-foreground">
          Tu contraseña se ha restablecido. Por seguridad hemos cerrado tus
          sesiones anteriores; vuelve a iniciar sesión con la nueva contraseña.
        </p>
        <Button size="sm" render={<Link href="/siginex/login" />}>
          Ir al inicio de sesión
        </Button>
      </div>
    );
  }

  return (
    <form
      onSubmit={handleSubmit(onSubmit)}
      noValidate
      className="flex flex-col gap-4"
    >
      <div className="flex flex-col gap-1.5">
        <Label htmlFor="nuevaPassword">Nueva contraseña</Label>
        <Input
          id="nuevaPassword"
          type="password"
          autoComplete="new-password"
          autoFocus
          aria-invalid={!!errors.nuevaPassword}
          {...register("nuevaPassword")}
        />
        {errors.nuevaPassword?.message ? (
          <p className="text-sm text-destructive">
            {errors.nuevaPassword.message}
          </p>
        ) : null}
        <p className="text-xs text-muted-foreground">Mínimo 12 caracteres.</p>
      </div>

      <div className="flex flex-col gap-1.5">
        <Label htmlFor="confirmar">Confirmar contraseña</Label>
        <Input
          id="confirmar"
          type="password"
          autoComplete="new-password"
          aria-invalid={!!errors.confirmar}
          {...register("confirmar")}
        />
        {errors.confirmar?.message ? (
          <p className="text-sm text-destructive">{errors.confirmar.message}</p>
        ) : null}
      </div>

      {serverError ? (
        <p role="alert" aria-live="assertive" className="text-sm text-destructive">
          {serverError}
        </p>
      ) : null}

      <Button
        type="submit"
        size="lg"
        disabled={isSubmitting}
        className="mt-2 w-full"
      >
        {isSubmitting ? "Guardando…" : "Restablecer contraseña"}
      </Button>
    </form>
  );
}
