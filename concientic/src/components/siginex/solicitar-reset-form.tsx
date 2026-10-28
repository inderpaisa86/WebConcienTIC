"use client";

import { useState } from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";

import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { solicitarReset, isSiginexDataError } from "@/lib/siginex";
import {
  siginexSolicitarResetSchema,
  type SiginexSolicitarResetInput,
} from "@/lib/validations/siginex-reset";

/**
 * Formulario "¿Olvidaste tu contraseña?" (client). Pide un email y llama a
 * `solicitarReset`. No-enumeración: ante ÉXITO muestra SIEMPRE el mismo mensaje
 * genérico ("si existe una cuenta, enviaremos un enlace"), sin revelar si el
 * email estaba registrado. Solo un fallo de red/validación de formato produce
 * un mensaje de error.
 */
export function SolicitarResetForm() {
  const [enviado, setEnviado] = useState(false);
  const [serverError, setServerError] = useState<string | null>(null);

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<SiginexSolicitarResetInput>({
    resolver: zodResolver(siginexSolicitarResetSchema),
    defaultValues: { email: "" },
  });

  async function onSubmit(values: SiginexSolicitarResetInput) {
    setServerError(null);
    try {
      await solicitarReset(values.email);
      // Éxito uniforme: no revelamos si el email existe.
      setEnviado(true);
    } catch (err) {
      if (isSiginexDataError(err) && err.kind === "validation") {
        setServerError("Introduce un correo válido.");
        return;
      }
      setServerError(
        "No se pudo procesar la solicitud. Inténtalo de nuevo más tarde.",
      );
    }
  }

  if (enviado) {
    return (
      <p
        role="status"
        aria-live="polite"
        className="text-sm text-muted-foreground"
      >
        Si existe una cuenta con ese correo, te enviaremos un enlace para
        restablecer tu contraseña. Revisa tu bandeja de entrada.
      </p>
    );
  }

  return (
    <form
      onSubmit={handleSubmit(onSubmit)}
      noValidate
      className="flex flex-col gap-4"
    >
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
        {isSubmitting ? "Enviando…" : "Enviar enlace de recuperación"}
      </Button>
    </form>
  );
}
