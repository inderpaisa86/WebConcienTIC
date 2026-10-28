import { type NextRequest } from "next/server";

import { forwardToSiginex } from "@/lib/siginex/server/forward";

/**
 * BFF: `POST /siginex/api/auth/reset/solicitar` → `POST /auth/reset/solicitar`
 * de la API Java. Igual que login, está EXENTO de CSRF en el backend (no hay
 * sesión), así que no se propaga CSRF. Responde 204 exista o no el email
 * (no-enumeración).
 */
export async function POST(request: NextRequest): Promise<Response> {
  return forwardToSiginex(request, {
    path: "/auth/reset/solicitar",
    method: "POST",
    forwardCsrf: false,
  });
}
