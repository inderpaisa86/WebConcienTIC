import { type NextRequest } from "next/server";

import { forwardToSiginex } from "@/lib/siginex/server/forward";

/**
 * BFF: `GET /siginex/api/usuarios` → `GET /api/v1/usuarios` (lista los usuarios
 * del tenant del admin). GET seguro: no propaga CSRF.
 */
export async function GET(request: NextRequest): Promise<Response> {
  return forwardToSiginex(request, {
    path: "/api/v1/usuarios",
    method: "GET",
  });
}

/**
 * BFF: `POST /siginex/api/usuarios` → `POST /api/v1/usuarios` (crea un usuario
 * en el tenant del admin). POST no exento: propaga `X-XSRF-TOKEN`.
 */
export async function POST(request: NextRequest): Promise<Response> {
  return forwardToSiginex(request, {
    path: "/api/v1/usuarios",
    method: "POST",
    forwardCsrf: true,
  });
}
