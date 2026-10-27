"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";

import { me } from "@/lib/siginex";

type GuardState = "checking" | "authenticated";

/** Ruta de login a la que se redirige cuando no hay sesión válida. */
const LOGIN_ROUTE = "/siginex/login";

/**
 * Guard de sesión client-side para las rutas nativas protegidas de SIGINEX.
 *
 * Coherente con la Opción A (la sesión vive en el navegador): al montar llama
 * a `me()`; si la sesión no es válida (`unauthenticated`), o hay cualquier
 * fallo, redirige a `/siginex/login`. Solo renderiza `children` cuando hay
 * sesión confirmada. No se usa en la propia página de login.
 */
export function SessionGuard({ children }: { children: React.ReactNode }) {
  const router = useRouter();
  const [state, setState] = useState<GuardState>("checking");

  useEffect(() => {
    let active = true;

    me()
      .then(() => {
        if (active) {
          setState("authenticated");
        }
      })
      .catch(() => {
        if (active) {
          router.replace(LOGIN_ROUTE);
        }
      });

    return () => {
      active = false;
    };
  }, [router]);

  if (state === "checking") {
    return (
      <p role="status" aria-live="polite" className="text-sm text-muted-foreground">
        Comprobando sesión…
      </p>
    );
  }

  return <>{children}</>;
}
