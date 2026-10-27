import { type NextRequest } from "next/server";

import { forwardToSiginex } from "@/lib/siginex/server/forward";

/**
 * BFF: `GET /siginex/api/auth/me` → reenvía a `GET /auth/me` de la API Java.
 * Propaga la cookie de sesión del navegador hacia Java y devuelve 200 con el
 * JSON `MeResponse` o 401 tal cual. GET seguro: no requiere CSRF. Es dinámico
 * por request (lee la cookie de sesión), por lo que nunca se prerenderiza.
 */
export async function GET(request: NextRequest): Promise<Response> {
  return forwardToSiginex(request, {
    path: "/auth/me",
    method: "GET",
    forwardCsrf: false,
  });
}
