import { type NextRequest } from "next/server";

import { forwardToSiginex } from "@/lib/siginex/server/forward";

/**
 * BFF de preguntas de un módulo del banco KB. Delega en `forwardToSiginex` sin
 * lógica de negocio.
 * `GET /siginex/api/kb/modulos/{moduloId}/preguntas`
 *   → `GET /api/v1/kb/modulos/{moduloId}/preguntas` (seguro, sin CSRF).
 *
 * Next.js 16: `ctx.params` es una promesa y `RouteContext<'/ruta'>` es un helper
 * GLOBAL (no se importa). El `moduloId` se interpola tras `encodeURIComponent`;
 * el backend responde 404 si el módulo no existe.
 */
export async function GET(
  request: NextRequest,
  ctx: RouteContext<"/siginex/api/kb/modulos/[moduloId]/preguntas">,
): Promise<Response> {
  const { moduloId } = await ctx.params;
  return forwardToSiginex(request, {
    path: `/api/v1/kb/modulos/${encodeURIComponent(moduloId)}/preguntas`,
    method: "GET",
    forwardCsrf: false,
  });
}
