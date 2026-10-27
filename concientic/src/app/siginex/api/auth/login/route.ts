import { type NextRequest } from "next/server";

import { forwardToSiginex } from "@/lib/siginex/server/forward";

/**
 * BFF: `POST /siginex/api/auth/login` → reenvía a `POST /auth/login` de la API
 * Java. Login está EXENTO de CSRF en el backend, así que no se propaga CSRF.
 * Propaga el `Set-Cookie` de la sesión (`__Host-siginex_session`, `XSRF-TOKEN`)
 * hacia el navegador y devuelve el status de Java (204 éxito, 400/401 error).
 */
export async function POST(request: NextRequest): Promise<Response> {
  return forwardToSiginex(request, {
    path: "/auth/login",
    method: "POST",
    forwardCsrf: false,
  });
}
