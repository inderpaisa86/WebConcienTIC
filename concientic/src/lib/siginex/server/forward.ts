import { type NextRequest, NextResponse } from "next/server";

import { SIGINEX_API_URL } from "../config";

/**
 * Helper de reenvío servidor↔servidor del BFF de SIGINEX (modelo B).
 *
 * El navegador habla SIEMPRE con concientic (same-origin). Estos route handlers
 * reciben la petición del navegador y la REENVÍAN a la API Java desde el
 * servidor de Next. Como el servidor controla las cabeceras de forma explícita,
 * NO se usa `credentials` (eso es semántica de navegador): se construye la
 * cabecera `Cookie` saliente a mano y se copia el `Set-Cookie` de Java de vuelta
 * al navegador tal cual (la cookie de sesión es `SameSite=Lax`, que ahora SÍ
 * aplica porque el navegador la ve emitida por el mismo origen que concientic).
 *
 * Server-only: este módulo solo debe importarse desde los route handlers del
 * BFF. El guard de abajo falla de forma ruidosa si se intenta cargar en el
 * navegador (no se usa el paquete `server-only` porque no es dependencia del
 * proyecto; el guard cumple la misma intención protectora sin añadir deps).
 */
if (typeof window !== "undefined") {
  throw new Error(
    "lib/siginex/server/forward.ts es server-only y no debe importarse en el navegador.",
  );
}

export interface ForwardOptions {
  /** Ruta en la API Java, con barra inicial (p. ej. `/auth/login`). */
  path: string;
  /** Método HTTP a usar contra la API Java. */
  method: "GET" | "POST";
  /**
   * Si `true`, propaga el header CSRF `X-XSRF-TOKEN` entrante hacia Java.
   * Solo `logout` lo requiere; `login` y `me` no.
   */
  forwardCsrf?: boolean;
}

/** Cabeceras que NO deben copiarse desde la petición entrante hacia Java. */
function buildOutgoingHeaders(
  request: NextRequest,
  { forwardCsrf }: ForwardOptions,
): Headers {
  const headers = new Headers();

  // Reenvía la cookie de sesión (y XSRF-TOKEN) del navegador hacia Java.
  const cookie = request.headers.get("cookie");
  if (cookie) {
    headers.set("cookie", cookie);
  }

  const contentType = request.headers.get("content-type");
  if (contentType) {
    headers.set("content-type", contentType);
  }

  const accept = request.headers.get("accept");
  if (accept) {
    headers.set("accept", accept);
  }

  if (forwardCsrf) {
    const xsrf = request.headers.get("x-xsrf-token");
    if (xsrf) {
      headers.set("x-xsrf-token", xsrf);
    }
  }

  return headers;
}

/**
 * Reenvía la petición del navegador a la API Java y devuelve una respuesta
 * para el navegador copiando status, cuerpo y TODAS las cabeceras `Set-Cookie`.
 */
export async function forwardToSiginex(
  request: NextRequest,
  options: ForwardOptions,
): Promise<Response> {
  const url = `${SIGINEX_API_URL}${options.path}`;
  const headers = buildOutgoingHeaders(request, options);

  // GET/HEAD no llevan cuerpo; POST reenvía el cuerpo crudo del navegador.
  const body =
    options.method === "GET" ? undefined : await request.arrayBuffer();

  let upstream: Response;
  try {
    upstream = await fetch(url, {
      method: options.method,
      headers,
      body,
      cache: "no-store",
      redirect: "manual",
    });
  } catch {
    // La API Java no responde / error de red servidor↔servidor.
    return new NextResponse(null, { status: 502 });
  }

  // Copia el cuerpo tal cual (puede ser vacío en 204).
  const responseBody = await upstream.arrayBuffer();
  const response = new NextResponse(
    responseBody.byteLength > 0 ? responseBody : null,
    { status: upstream.status },
  );

  // Reenvía el Content-Type para que el cliente parsee el JSON de `me`/errores.
  const upstreamContentType = upstream.headers.get("content-type");
  if (upstreamContentType) {
    response.headers.set("content-type", upstreamContentType);
  }

  // Reenvía TODAS las cabeceras Set-Cookie intactas (sin reescribir atributos).
  // `getSetCookie()` devuelve un array, preservando múltiples cookies.
  for (const setCookie of upstream.headers.getSetCookie()) {
    response.headers.append("set-cookie", setCookie);
  }

  return response;
}
