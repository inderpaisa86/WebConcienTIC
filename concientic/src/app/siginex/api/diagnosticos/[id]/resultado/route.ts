import { type NextRequest } from "next/server";

import { forwardToSiginex } from "@/lib/siginex/server/forward";

/**
 * BFF del resultado consolidado de un diagnóstico. Delega en `forwardToSiginex`
 * sin lógica de negocio.
 * `GET /siginex/api/diagnosticos/{id}/resultado`
 *   → `GET /api/v1/diagnosticos/{id}/resultado` (seguro, sin CSRF).
 */
export async function GET(
  request: NextRequest,
  ctx: RouteContext<"/siginex/api/diagnosticos/[id]/resultado">,
): Promise<Response> {
  const { id } = await ctx.params;
  return forwardToSiginex(request, {
    path: `/api/v1/diagnosticos/${encodeURIComponent(id)}/resultado`,
    method: "GET",
    forwardCsrf: false,
  });
}
