"use client";

import { useCallback, useEffect, useSyncExternalStore } from "react";

import { Button } from "@/components/ui/button";
import { denyAnalyticsConsent, grantAnalyticsConsent } from "@/lib/analytics";

const STORAGE_KEY = "concientic:cookie-consent";
type Choice = "granted" | "denied";

const listeners = new Set<() => void>();

function subscribe(cb: () => void) {
  listeners.add(cb);
  return () => {
    listeners.delete(cb);
  };
}

function readStoredChoice(): Choice | null {
  const stored = window.localStorage.getItem(STORAGE_KEY);
  return stored === "granted" || stored === "denied" ? stored : null;
}

function getSnapshot(): Choice | null {
  return readStoredChoice();
}

// En el servidor no hay elección guardada; el banner no se renderiza en SSR.
function getServerSnapshot(): Choice | null {
  return "denied";
}

function persist(choice: Choice) {
  window.localStorage.setItem(STORAGE_KEY, choice);
  listeners.forEach((cb) => cb());
}

/**
 * Banner de consentimiento de cookies, conectado al Consent Mode v2 de GA4.
 *
 * - Estado leído con useSyncExternalStore (consistente SSR/cliente, sin setState en effect
 *   ni mismatch de hidratación).
 * - SSR devuelve "denied" para no renderizar el banner en el HTML del servidor.
 * - Se muestra solo si hay GA configurado (NEXT_PUBLIC_GA_ID) y no hay elección previa.
 */
export function CookieConsent() {
  const choice = useSyncExternalStore(subscribe, getSnapshot, getServerSnapshot);
  const hasGa = !!process.env.NEXT_PUBLIC_GA_ID;

  // Reflejar en GA la elección ya guardada (efecto externo puro, sin setState).
  useEffect(() => {
    if (!hasGa) return;
    if (choice === "granted") {
      grantAnalyticsConsent();
    } else if (choice === "denied") {
      denyAnalyticsConsent();
    }
  }, [choice, hasGa]);

  const decide = useCallback((next: Choice) => {
    if (next === "granted") {
      grantAnalyticsConsent();
    } else {
      denyAnalyticsConsent();
    }
    persist(next);
  }, []);

  if (!hasGa || choice !== null) return null;

  return (
    <div
      role="dialog"
      aria-live="polite"
      aria-label="Aviso de cookies"
      className="fixed inset-x-4 bottom-4 z-[60] mx-auto max-w-2xl rounded-xl border border-border bg-card p-5 text-card-foreground shadow-lg"
    >
      <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <p className="text-sm text-muted-foreground">
          Usamos cookies de analítica para entender cómo se usa el sitio y mejorarlo. Puedes
          aceptarlas o rechazarlas.
        </p>
        <div className="flex shrink-0 gap-2">
          <Button variant="outline" size="sm" onClick={() => decide("denied")}>
            Rechazar
          </Button>
          <Button size="sm" onClick={() => decide("granted")}>
            Aceptar
          </Button>
        </div>
      </div>
    </div>
  );
}
