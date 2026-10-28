import { type NextRequest } from "next/server";

import { forwardToSiginex } from "@/lib/siginex/server/forward";

/**
 * BFF: `POST /siginex/api/usuarios/{id}/desactivar` →
 * `POST /api/v1/usuarios/{id}/desactivar`. Muta: propaga `X-XSRF-TOKEN`. El
 * backend exige rol admin y resuelve el tenant server-side (RLS).
 */
export async function POST(
  request: NextRequest,
  ctx: RouteContext<"/siginex/api/usuarios/[id]/desactivar">,
): Promise<Response> {
  const { id } = await ctx.params;
  return forwardToSiginex(request, {
    path: `/api/v1/usuarios/${encodeURIComponent(id)}/desactivar`,
    method: "POST",
    forwardCsrf: true,
  });
}
