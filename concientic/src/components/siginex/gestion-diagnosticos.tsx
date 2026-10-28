"use client";

import { useCallback, useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";

import { Button } from "@/components/ui/button";
import { CrearDiagnosticoForm } from "@/components/siginex/crear-diagnostico-form";
import { DiagnosticosTabla } from "@/components/siginex/diagnosticos-tabla";
import { MODULOS_SGI } from "@/lib/siginex/modulos-sgi";
import {
  listarDiagnosticos,
  isSiginexDataError,
  type DiagnosticoResumen,
} from "@/lib/siginex";

type EstadoCarga = "cargando" | "listo" | "error";

/**
 * Orquestador de la gestión de diagnósticos ("use client"). Carga la lista al
 * montar, renderiza el formulario de crear + la tabla, y refresca la lista tras
 * una creación exitosa (patrón client coherente con la 3a; sin server actions).
 *
 * Fase 4 (navegación por módulos): si `moduloSeleccionado` llega (el usuario
 * pulsó un módulo en home/sidebar), se muestra un aviso con el nombre legible
 * del módulo y la tabla propaga ese `?modulo=<id>` al enlace "Responder" de cada
 * diagnóstico, de modo que el cuestionario abre directamente esa sección. El
 * usuario elige sobre QUÉ diagnóstico abrirlo (modelo multi-empresa).
 */
export function GestionDiagnosticos({
  moduloSeleccionado,
}: {
  moduloSeleccionado?: string;
}) {
  const router = useRouter();
  const [diagnosticos, setDiagnosticos] = useState<DiagnosticoResumen[]>([]);
  const [estado, setEstado] = useState<EstadoCarga>("cargando");
  const [mensajeError, setMensajeError] = useState<string | null>(null);

  // Nombre legible del módulo seleccionado (si el id es válido). Si no coincide
  // con ningún módulo conocido, no se muestra el aviso ni se propaga el filtro.
  const moduloInfo = moduloSeleccionado
    ? MODULOS_SGI.find((m) => m.id === moduloSeleccionado)
    : undefined;

  // Recarga invocada desde eventos de usuario (crear/reintentar): reinicia el
  // estado a "cargando" para dar feedback inmediato y vuelve a pedir la lista.
  const cargar = useCallback(async () => {
    setEstado("cargando");
    setMensajeError(null);
    try {
      const datos = await listarDiagnosticos();
      setDiagnosticos(datos);
      setEstado("listo");
    } catch (err) {
      if (isSiginexDataError(err) && err.kind === "unauthenticated") {
        router.replace("/siginex/login");
        return;
      }
      setMensajeError(
        isSiginexDataError(err)
          ? err.message
          : "Ha ocurrido un error inesperado. Inténtalo de nuevo.",
      );
      setEstado("error");
    }
  }, [router]);

  // Carga inicial al montar. Se usa el patrón `.then/.catch` (como
  // `session-guard.tsx`): los `setState` ocurren en callbacks de la promesa, no
  // de forma síncrona dentro del efecto, evitando renders en cascada.
  useEffect(() => {
    let activo = true;

    listarDiagnosticos()
      .then((datos) => {
        if (!activo) return;
        setDiagnosticos(datos);
        setEstado("listo");
      })
      .catch((err: unknown) => {
        if (!activo) return;
        if (isSiginexDataError(err) && err.kind === "unauthenticated") {
          router.replace("/siginex/login");
          return;
        }
        setMensajeError(
          isSiginexDataError(err)
            ? err.message
            : "Ha ocurrido un error inesperado. Inténtalo de nuevo.",
        );
        setEstado("error");
      });

    return () => {
      activo = false;
    };
  }, [router]);

  return (
    <div className="mx-auto w-full max-w-5xl space-y-6">
      <header className="flex flex-wrap items-center justify-between gap-4">
        <div>
          <h1 className="font-heading text-2xl font-semibold text-foreground">
            Gestión de diagnósticos
          </h1>
          <p className="text-sm text-muted-foreground">
            Crea organizaciones e inicia sus diagnósticos del SGI.
          </p>
        </div>
        <Button variant="outline" size="lg" render={<Link href="/siginex" />}>
          Volver al panel
        </Button>
      </header>

      {moduloInfo ? (
        <div
          role="status"
          aria-live="polite"
          className="flex flex-wrap items-center justify-between gap-3 rounded-xl border border-border bg-muted/40 px-4 py-3"
        >
          <p className="text-sm text-foreground">
            Vas a responder el módulo{" "}
            <strong className="font-semibold">{moduloInfo.nombre}</strong>.
            Elige el diagnóstico sobre el que abrirlo pulsando{" "}
            <strong className="font-semibold">Responder</strong>, o crea uno
            nuevo abajo.
          </p>
          <Button
            variant="ghost"
            size="sm"
            render={<Link href="/siginex/diagnosticos" />}
          >
            Quitar filtro de módulo
          </Button>
        </div>
      ) : null}

      <CrearDiagnosticoForm onCreado={() => void cargar()} />

      <section className="space-y-3">
        <h2 className="font-heading text-lg font-semibold text-foreground">
          Diagnósticos
        </h2>

        {estado === "cargando" ? (
          <p
            role="status"
            aria-live="polite"
            className="text-sm text-muted-foreground"
          >
            Cargando diagnósticos…
          </p>
        ) : null}

        {estado === "error" ? (
          <div
            role="status"
            aria-live="polite"
            className="flex flex-col items-start gap-3 rounded-xl border border-border bg-card p-6"
          >
            <p className="text-sm text-muted-foreground">
              {mensajeError ?? "No se pudieron cargar los diagnósticos."}
            </p>
            <Button variant="outline" size="sm" onClick={() => void cargar()}>
              Reintentar
            </Button>
          </div>
        ) : null}

        {estado === "listo" ? (
          <DiagnosticosTabla
            diagnosticos={diagnosticos}
            moduloSeleccionado={moduloInfo?.id}
          />
        ) : null}
      </section>
    </div>
  );
}
