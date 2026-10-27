import { type NextRequest } from "next/server";

import { forwardToSiginex } from "@/lib/siginex/server/forward";

/**
 * BFF de diagnósticos. Delega en `forwardToSiginex` sin lógica de negocio.
 * - `GET  /siginex/api/diagnosticos` → `GET  /api/v1/diagnosticos` (seguro, sin CSRF).
 * - `POST /siginex/api/diagnosticos` → `POST /api/v1/diagnosticos` (muta: propaga `X-XSRF-TOKEN`).
 */
export async function GET(request: NextRequest): Promise<Response> {
  return forwardToSiginex(request, {
    path: "/api/v1/diagnosticos",
    method: "GET",
    forwardCsrf: false,
  });
}

export async function POST(request: NextRequest): Promise<Response> {
  return forwardToSiginex(request, {
    path: "/api/v1/diagnosticos",
    method: "POST",
    forwardCsrf: true,
  });
}
