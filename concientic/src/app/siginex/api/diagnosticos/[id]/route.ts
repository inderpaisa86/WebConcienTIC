import { type NextRequest } from "next/server";

import { forwardToSiginex } from "@/lib/siginex/server/forward";

/**
 * BFF de detalle de diagnóstico. Delega en `forwardToSiginex` sin lógica de
 * negocio. `GET /siginex/api/diagnosticos/{id}` → `GET /api/v1/diagnosticos/{id}`.
 *
 * Next.js 16: `ctx.params` es una promesa y `RouteContext<'/ruta'>` es un
 * helper GLOBAL (no se importa; lo genera `next dev`/`next build`/`next
 * typegen`). El `id` se interpola tras `encodeURIComponent`; el backend valida
 * que sea un UUID real y responde 404/400 si no.
 */
export async function GET(
  request: NextRequest,
  ctx: RouteContext<"/siginex/api/diagnosticos/[id]">,
): Promise<Response> {
  const { id } = await ctx.params;
  return forwardToSiginex(request, {
    path: `/api/v1/diagnosticos/${encodeURIComponent(id)}`,
    method: "GET",
    forwardCsrf: false,
  });
}
