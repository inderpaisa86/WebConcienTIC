import { type NextRequest } from "next/server";

import { forwardToSiginex } from "@/lib/siginex/server/forward";

/**
 * BFF: `POST /siginex/api/auth/reset/confirmar` → `POST /auth/reset/confirmar`
 * de la API Java. EXENTO de CSRF en el backend (no hay sesión). Responde 204 al
 * completar; 401 genérico si el token es inválido/usado/expirado.
 */
export async function POST(request: NextRequest): Promise<Response> {
  return forwardToSiginex(request, {
    path: "/auth/reset/confirmar",
    method: "POST",
    forwardCsrf: false,
  });
}
