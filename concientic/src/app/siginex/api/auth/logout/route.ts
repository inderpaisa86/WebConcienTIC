import { type NextRequest } from "next/server";

import { forwardToSiginex } from "@/lib/siginex/server/forward";

/**
 * BFF: `POST /siginex/api/auth/logout` → reenvía a `POST /auth/logout` de la API
 * Java. Logout NO está exento de CSRF, así que se propaga `X-XSRF-TOKEN`.
 * Propaga el `Set-Cookie` que expira la sesión hacia el navegador.
 */
export async function POST(request: NextRequest): Promise<Response> {
  return forwardToSiginex(request, {
    path: "/auth/logout",
    method: "POST",
    forwardCsrf: true,
  });
}
