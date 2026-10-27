import { type NextRequest } from "next/server";

import { forwardToSiginex } from "@/lib/siginex/server/forward";

/**
 * BFF del recálculo de un diagnóstico. Delega en `forwardToSiginex` sin lógica
 * de negocio.
 * `POST /siginex/api/diagnosticos/{id}/calcular`
 *   → `POST /api/v1/diagnosticos/{id}/calcular` (muta: propaga `X-XSRF-TOKEN`).
 *
 * Next.js 16: `ctx.params` es una promesa y `RouteContext<'/ruta'>` es un helper
 * GLOBAL (no se importa). El `id` se interpola tras `encodeURIComponent`; el
 * backend valida que sea un UUID real y resuelve el tenant server-side.
 */
export async function POST(
  request: NextRequest,
  ctx: RouteContext<"/siginex/api/diagnosticos/[id]/calcular">,
): Promise<Response> {
  const { id } = await ctx.params;
  return forwardToSiginex(request, {
    path: `/api/v1/diagnosticos/${encodeURIComponent(id)}/calcular`,
    method: "POST",
    forwardCsrf: true,
  });
}
