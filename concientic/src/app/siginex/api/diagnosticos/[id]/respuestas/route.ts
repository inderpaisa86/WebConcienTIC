import { type NextRequest } from "next/server";

import { forwardToSiginex } from "@/lib/siginex/server/forward";

/**
 * BFF de respuestas de un diagnóstico. Delega en `forwardToSiginex` sin lógica
 * de negocio.
 * - `GET /siginex/api/diagnosticos/{id}/respuestas`
 *     → `GET /api/v1/diagnosticos/{id}/respuestas` (seguro, sin CSRF).
 * - `PUT /siginex/api/diagnosticos/{id}/respuestas`
 *     → `PUT /api/v1/diagnosticos/{id}/respuestas` (muta: propaga `X-XSRF-TOKEN`).
 *
 * Next.js 16: `ctx.params` es una promesa y `RouteContext<'/ruta'>` es un helper
 * GLOBAL (no se importa). El `id` se interpola tras `encodeURIComponent`; el
 * backend valida que sea un UUID real y resuelve el tenant server-side (nunca
 * del cliente), respondiendo 404 si el diagnóstico no es de su tenant.
 */
export async function GET(
  request: NextRequest,
  ctx: RouteContext<"/siginex/api/diagnosticos/[id]/respuestas">,
): Promise<Response> {
  const { id } = await ctx.params;
  return forwardToSiginex(request, {
    path: `/api/v1/diagnosticos/${encodeURIComponent(id)}/respuestas`,
    method: "GET",
    forwardCsrf: false,
  });
}

export async function PUT(
  request: NextRequest,
  ctx: RouteContext<"/siginex/api/diagnosticos/[id]/respuestas">,
): Promise<Response> {
  const { id } = await ctx.params;
  return forwardToSiginex(request, {
    path: `/api/v1/diagnosticos/${encodeURIComponent(id)}/respuestas`,
    method: "PUT",
    forwardCsrf: true,
  });
}
