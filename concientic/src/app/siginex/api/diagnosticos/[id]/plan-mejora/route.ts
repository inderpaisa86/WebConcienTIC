import { type NextRequest } from "next/server";

import { forwardToSiginex } from "@/lib/siginex/server/forward";

/**
 * BFF del plan de mejora de un diagnóstico. Delega en `forwardToSiginex` sin
 * lógica de negocio.
 * `GET /siginex/api/diagnosticos/{id}/plan-mejora`
 *   → `GET /api/v1/diagnosticos/{id}/plan-mejora` (seguro, sin CSRF).
 */
export async function GET(
  request: NextRequest,
  ctx: RouteContext<"/siginex/api/diagnosticos/[id]/plan-mejora">,
): Promise<Response> {
  const { id } = await ctx.params;
  return forwardToSiginex(request, {
    path: `/api/v1/diagnosticos/${encodeURIComponent(id)}/plan-mejora`,
    method: "GET",
    forwardCsrf: false,
  });
}
