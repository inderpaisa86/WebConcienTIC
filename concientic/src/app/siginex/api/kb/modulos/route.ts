import { type NextRequest } from "next/server";

import { forwardToSiginex } from "@/lib/siginex/server/forward";

/**
 * BFF del banco KB (módulos). Delega en `forwardToSiginex` sin lógica de
 * negocio. `GET /siginex/api/kb/modulos` → `GET /api/v1/kb/modulos` (seguro,
 * sin CSRF). El KB es global pero el endpoint exige sesión válida.
 */
export async function GET(request: NextRequest): Promise<Response> {
  return forwardToSiginex(request, {
    path: "/api/v1/kb/modulos",
    method: "GET",
    forwardCsrf: false,
  });
}
