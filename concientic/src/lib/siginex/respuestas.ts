/**
 * Cliente API de respuestas del cuestionario de SIGINEX — modelo B / BFF
 * same-origin. Mismo estilo que `diagnosticos.ts`: el navegador llama SIEMPRE a
 * rutas same-origin de concientic (`/siginex/api/diagnosticos/{id}/respuestas`),
 * que son route handlers (BFF) que reenvían a la API Java
 * (`/api/v1/diagnosticos/{id}/respuestas`). Con `credentials: "same-origin"` la
 * cookie de sesión viaja sola; el PUT (muta estado) añade `X-XSRF-TOKEN` leído
 * de la cookie `XSRF-TOKEN`.
 */

import { readCookie, XSRF_COOKIE_NAME } from "./csrf";
import { SiginexDataError } from "./errors";
import type {
  GuardarRespuestasRequest,
  RespuestasGuardadas,
} from "./types";

/** Construye la ruta BFF same-origin de respuestas para un diagnóstico. */
function respuestasPath(diagnosticoId: string): string {
  return `/siginex/api/diagnosticos/${encodeURIComponent(diagnosticoId)}/respuestas`;
}

/**
 * Obtiene las respuestas ya guardadas de un diagnóstico (mapa
 * `preguntaId → valor`), para precargar el cuestionario.
 * @throws SiginexDataError `network` si falla la conexión (incl. 502 del BFF),
 *   `unauthenticated` en 401, `unknown` en otros estados (incl. 404) o si el
 *   JSON es ilegible.
 */
export async function obtenerRespuestas(
  diagnosticoId: string,
): Promise<RespuestasGuardadas> {
  let response: Response;
  try {
    response = await fetch(respuestasPath(diagnosticoId), {
      method: "GET",
      credentials: "same-origin",
      headers: { Accept: "application/json" },
    });
  } catch {
    throw new SiginexDataError("network");
  }

  if (response.status === 401) {
    throw new SiginexDataError("unauthenticated");
  }
  // 502 del BFF = la API Java no respondió = fallo de red recuperable.
  if (response.status === 502) {
    throw new SiginexDataError("network");
  }
  if (!response.ok) {
    throw new SiginexDataError("unknown");
  }

  try {
    return (await response.json()) as RespuestasGuardadas;
  } catch {
    throw new SiginexDataError("unknown");
  }
}

/**
 * Guarda un lote de respuestas de un diagnóstico. Éxito = 204 (sin cuerpo). PUT
 * no exento de CSRF: envía `X-XSRF-TOKEN` leído de la cookie si existe (si no
 * existe, el backend responderá 403 → `unknown`).
 * @throws SiginexDataError `network` si falla la conexión (incl. 502 del BFF),
 *   `unauthenticated` en 401, `validation` en 400/422 (lote vacío o valor fuera
 *   del patrón del backend), `unknown` en otros estados.
 */
export async function guardarRespuestas(
  diagnosticoId: string,
  req: GuardarRespuestasRequest,
): Promise<void> {
  const xsrfToken = readCookie(XSRF_COOKIE_NAME);
  const headers: Record<string, string> = {
    "Content-Type": "application/json",
    Accept: "application/json",
  };
  if (xsrfToken) {
    headers["X-XSRF-TOKEN"] = xsrfToken;
  }

  let response: Response;
  try {
    response = await fetch(respuestasPath(diagnosticoId), {
      method: "PUT",
      credentials: "same-origin",
      headers,
      body: JSON.stringify(req),
    });
  } catch {
    throw new SiginexDataError("network");
  }

  if (response.status === 204) {
    return;
  }
  if (response.status === 401) {
    throw new SiginexDataError("unauthenticated");
  }
  if (response.status === 400 || response.status === 422) {
    throw new SiginexDataError("validation");
  }
  // 502 del BFF = la API Java no respondió = fallo de red recuperable.
  if (response.status === 502) {
    throw new SiginexDataError("network");
  }
  throw new SiginexDataError("unknown");
}
