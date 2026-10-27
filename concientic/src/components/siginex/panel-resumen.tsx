"use client";

import { useCallback, useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";

import { Button } from "@/components/ui/button";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { KpiCard } from "@/components/siginex/kpi-card";
import { LogoutButton } from "@/components/siginex/logout-button";
import {
  listarDiagnosticos,
  listarOrganizaciones,
  isSiginexDataError,
  type DiagnosticoResumen,
  type Organizacion,
} from "@/lib/siginex";

type EstadoCarga = "cargando" | "listo" | "error";

interface Kpis {
  organizaciones: number;
  diagnosticos: number;
  calculados: number;
  pendientes: number;
}

/**
 * Deriva los KPIs del panel de los datos disponibles en 3b (función pura,
 * testeable sin React). NO usa endpoints de KB/cálculo (eso es 3c/3d).
 * "Pendientes" = todo lo que no está `calculado`, de modo que
 * calculados + pendientes = total sin enumerar todos los estados.
 */
export function derivarKpis(
  orgs: Organizacion[],
  diags: DiagnosticoResumen[],
): Kpis {
  const calculados = diags.filter((d) => d.estado === "calculado").length;
  return {
    organizaciones: orgs.length,
    diagnosticos: diags.length,
    calculados,
    pendientes: diags.length - calculados,
  };
}

/**
 * Cuerpo del panel ("use client"). Carga organizaciones y diagnósticos en
 * paralelo al montar, calcula los KPIs derivados y muestra la rejilla de
 * tarjetas + accesos (gestión y cerrar sesión). Los KPIs se recalculan al
 * montar, así que volver al panel ya los refresca (sin estado compartido
 * entre rutas).
 */
export function PanelResumen() {
  const router = useRouter();
  const [kpis, setKpis] = useState<Kpis | null>(null);
  const [estado, setEstado] = useState<EstadoCarga>("cargando");
  const [mensajeError, setMensajeError] = useState<string | null>(null);

  // Recarga invocada desde eventos de usuario (reintentar): reinicia el estado
  // a "cargando" para feedback inmediato y vuelve a pedir los datos.
  const cargar = useCallback(async () => {
    setEstado("cargando");
    setMensajeError(null);
    try {
      const [orgs, diags] = await Promise.all([
        listarOrganizaciones(),
        listarDiagnosticos(),
      ]);
      setKpis(derivarKpis(orgs, diags));
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

  // Carga inicial al montar. Patrón `.then/.catch` (como `session-guard.tsx`):
  // los `setState` ocurren en callbacks de la promesa, no síncronamente en el
  // efecto, evitando renders en cascada. Al recalcularse al montar, volver al
  // panel refresca los KPIs sin estado compartido entre rutas.
  useEffect(() => {
    let activo = true;

    Promise.all([listarOrganizaciones(), listarDiagnosticos()])
      .then(([orgs, diags]) => {
        if (!activo) return;
        setKpis(derivarKpis(orgs, diags));
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
            Panel SIGINEX
          </h1>
          <p className="text-sm text-muted-foreground">
            Resumen de tus organizaciones y diagnósticos.
          </p>
        </div>
        <LogoutButton />
      </header>

      {estado === "cargando" ? (
        <p
          role="status"
          aria-live="polite"
          className="text-sm text-muted-foreground"
        >
          Cargando panel…
        </p>
      ) : null}

      {estado === "error" ? (
        <div
          role="status"
          aria-live="polite"
          className="flex flex-col items-start gap-3 rounded-xl border border-border bg-card p-6"
        >
          <p className="text-sm text-muted-foreground">
            {mensajeError ?? "No se pudo cargar el panel."}
          </p>
          <Button variant="outline" size="sm" onClick={() => void cargar()}>
            Reintentar
          </Button>
        </div>
      ) : null}

      {estado === "listo" && kpis ? (
        <>
          <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
            <KpiCard etiqueta="Organizaciones" valor={kpis.organizaciones} />
            <KpiCard etiqueta="Diagnósticos" valor={kpis.diagnosticos} />
            <KpiCard etiqueta="Calculados" valor={kpis.calculados} />
            <KpiCard etiqueta="Pendientes" valor={kpis.pendientes} />
          </div>

          <Card>
            <CardHeader>
              <CardTitle>Gestión</CardTitle>
              <CardDescription>
                Crea organizaciones, inicia diagnósticos y consulta su estado.
              </CardDescription>
            </CardHeader>
            <CardContent>
              <Button
                size="lg"
                render={<Link href="/siginex/diagnosticos" />}
              >
                Gestionar diagnósticos
              </Button>
            </CardContent>
          </Card>
        </>
      ) : null}
    </div>
  );
}
