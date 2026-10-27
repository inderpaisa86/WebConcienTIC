"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";

import { Button } from "@/components/ui/button";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import {
  crearDiagnostico,
  crearOrganizacion,
  isSiginexDataError,
} from "@/lib/siginex";
import {
  siginexOrganizacionSchema,
  type SiginexOrganizacionInput,
} from "@/lib/validations/siginex-organizacion";

/** Clases del `<select>` nativo, alineadas con la primitiva `Input`. */
const SELECT_CLASSES =
  "flex h-10 w-full rounded-lg border border-border bg-background px-3 py-2 text-sm text-foreground transition-colors outline-none focus-visible:border-ring focus-visible:ring-3 focus-visible:ring-ring/50 disabled:cursor-not-allowed disabled:opacity-50";

/**
 * Formulario de crear organización + iniciar diagnóstico ("use client").
 * Mismo patrón que `login-form.tsx` (react-hook-form + zodResolver + mensaje
 * genérico en `role="alert" aria-live="assertive"`).
 *
 * Flujo: `crearOrganizacion(...)` y, con el id devuelto,
 * `crearDiagnostico({ organizacionId })`. Al terminar invoca `onCreado()` para
 * que el padre refresque la lista.
 *
 * Caso parcial: si la organización se crea pero `crearDiagnostico` falla, se
 * informa con un mensaje genérico; la organización ya existe pero NO se
 * reintenta el diagnóstico automáticamente (lo deja al usuario).
 */
export function CrearDiagnosticoForm({ onCreado }: { onCreado: () => void }) {
  const router = useRouter();
  const [serverError, setServerError] = useState<string | null>(null);

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors, isSubmitting },
  } = useForm<SiginexOrganizacionInput>({
    resolver: zodResolver(siginexOrganizacionSchema),
    defaultValues: { nombre: "", sector: "", tamano: undefined },
  });

  async function onSubmit(values: SiginexOrganizacionInput) {
    setServerError(null);
    try {
      // Omitir sector/tamano vacíos: el backend los trata como opcionales.
      const sector = values.sector?.trim() ? values.sector.trim() : undefined;
      const tamano = values.tamano ? values.tamano : undefined;

      const organizacion = await crearOrganizacion({
        nombre: values.nombre.trim(),
        sector,
        tamano,
      });
      await crearDiagnostico({ organizacionId: organizacion.id });

      reset({ nombre: "", sector: "", tamano: undefined });
      onCreado();
    } catch (err) {
      if (isSiginexDataError(err) && err.kind === "unauthenticated") {
        router.replace("/siginex/login");
        return;
      }
      // Mensaje genérico y seguro: no se filtra detalle del backend.
      setServerError(
        isSiginexDataError(err)
          ? err.message
          : "Ha ocurrido un error inesperado. Inténtalo de nuevo.",
      );
    }
  }

  return (
    <Card>
      <CardHeader>
        <CardTitle>Nuevo diagnóstico</CardTitle>
        <CardDescription>
          Registra la organización a diagnosticar e inicia su diagnóstico.
        </CardDescription>
      </CardHeader>
      <CardContent>
        <form
          onSubmit={handleSubmit(onSubmit)}
          noValidate
          className="grid gap-4 sm:grid-cols-2"
        >
          <div className="flex flex-col gap-1.5 sm:col-span-2">
            <Label htmlFor="nombre">Nombre de la empresa</Label>
            <Input
              id="nombre"
              autoComplete="organization"
              aria-invalid={!!errors.nombre}
              placeholder="Manufacturas del Valle S.A.S."
              {...register("nombre")}
            />
            {errors.nombre?.message ? (
              <p className="text-sm text-destructive">{errors.nombre.message}</p>
            ) : null}
          </div>

          <div className="flex flex-col gap-1.5">
            <Label htmlFor="sector">Sector (opcional)</Label>
            <Input
              id="sector"
              aria-invalid={!!errors.sector}
              placeholder="Manufactura / Industria"
              {...register("sector")}
            />
            {errors.sector?.message ? (
              <p className="text-sm text-destructive">{errors.sector.message}</p>
            ) : null}
          </div>

          <div className="flex flex-col gap-1.5">
            <Label htmlFor="tamano">Tamaño (opcional)</Label>
            <select
              id="tamano"
              aria-invalid={!!errors.tamano}
              className={SELECT_CLASSES}
              defaultValue=""
              {...register("tamano")}
            >
              <option value="">(sin especificar)</option>
              <option value="micro">Micro</option>
              <option value="pequena">Pequeña</option>
              <option value="mediana">Mediana</option>
              <option value="grande">Grande</option>
            </select>
            {errors.tamano?.message ? (
              <p className="text-sm text-destructive">{errors.tamano.message}</p>
            ) : null}
          </div>

          {serverError ? (
            <p
              role="alert"
              aria-live="assertive"
              className="text-sm text-destructive sm:col-span-2"
            >
              {serverError}
            </p>
          ) : null}

          <div className="sm:col-span-2">
            <Button type="submit" size="lg" disabled={isSubmitting}>
              {isSubmitting ? "Creando…" : "Crear e iniciar diagnóstico"}
            </Button>
          </div>
        </form>
      </CardContent>
    </Card>
  );
}
