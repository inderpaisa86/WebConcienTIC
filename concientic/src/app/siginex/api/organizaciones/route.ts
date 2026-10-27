import { type NextRequest } from "next/server";

import { forwardToSiginex } from "@/lib/siginex/server/forward";

/**
 * BFF de organizaciones. Delega en `forwardToSiginex` sin lógica de negocio.
 * - `GET  /siginex/api/organizaciones` → `GET  /api/v1/organizaciones` (seguro, sin CSRF).
 * - `POST /siginex/api/organizaciones` → `POST /api/v1/organizaciones` (muta: propaga `X-XSRF-TOKEN`).
 */
export async function GET(request: NextRequest): Promise<Response> {
  return forwardToSiginex(request, {
    path: "/api/v1/organizaciones",
    method: "GET",
    forwardCsrf: false,
  });
}

export async function POST(request: NextRequest): Promise<Response> {
  return forwardToSiginex(request, {
    path: "/api/v1/organizaciones",
    method: "POST",
    forwardCsrf: true,
  });
}
