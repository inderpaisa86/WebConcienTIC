import { type NextRequest } from "next/server";

import { forwardToSiginex } from "@/lib/siginex/server/forward";

/**
 * BFF: `POST /siginex/api/usuarios/{id}/activar` →
 * `POST /api/v1/usuarios/{id}/activar`. Muta: propaga `X-XSRF-TOKEN`. El
 * backend exige rol admin y resuelve el tenant server-side (RLS).
 */
export async function POST(
  request: NextRequest,
  ctx: RouteContext<"/siginex/api/usuarios/[id]/activar">,
): Promise<Response> {
  const { id } = await ctx.params;
  return forwardToSiginex(request, {
    path: `/api/v1/usuarios/${encodeURIComponent(id)}/activar`,
    method: "POST",
    forwardCsrf: true,
  });
}
