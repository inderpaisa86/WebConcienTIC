import { type NextRequest } from "next/server";

import { forwardToSiginex } from "@/lib/siginex/server/forward";

/**
 * BFF del benchmark de mercado de un diagnóstico. Delega en `forwardToSiginex`
 * sin lógica de negocio.
 * - `GET  /siginex/api/diagnosticos/{id}/benchmark`
 *     → `GET  /api/v1/diagnosticos/{id}/benchmark` (seguro, sin CSRF). El
 *       backend responde 204 si aún no hay benchmark; `forwardToSiginex` copia
 *       ese 204 con cuerpo vacío.
 * - `POST /siginex/api/diagnosticos/{id}/benchmark`
 *     → `POST /api/v1/diagnosticos/{id}/benchmark` (muta: propaga `X-XSRF-TOKEN`).
 */
export async function GET(
  request: NextRequest,
  ctx: RouteContext<"/siginex/api/diagnosticos/[id]/benchmark">,
): Promise<Response> {
  const { id } = await ctx.params;
  return forwardToSiginex(request, {
    path: `/api/v1/diagnosticos/${encodeURIComponent(id)}/benchmark`,
    method: "GET",
    forwardCsrf: false,
  });
}

export async function POST(
  request: NextRequest,
  ctx: RouteContext<"/siginex/api/diagnosticos/[id]/benchmark">,
): Promise<Response> {
  const { id } = await ctx.params;
  return forwardToSiginex(request, {
    path: `/api/v1/diagnosticos/${encodeURIComponent(id)}/benchmark`,
    method: "POST",
    forwardCsrf: true,
  });
}
