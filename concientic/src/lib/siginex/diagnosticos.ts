/**
 * Cliente API de diagnósticos de SIGINEX (modelo B / BFF same-origin).
 *
 * Mismo estilo que `client.ts`/`organizaciones.ts`: el navegador llama SIEMPRE
 * a rutas same-origin de concientic (`/siginex/api/diagnosticos`), que son
 * route handlers (BFF) que reenvían servidor↔servidor a la API Java. Con
 * `credentials: "same-origin"` la cookie de sesión viaja sola; el POST añade el
 * header `X-XSRF-TOKEN` leído de la cookie `XSRF-TOKEN`.
 */

import { readCookie, XSRF_COOKIE_NAME } from "./csrf";
import { SiginexDataError } from "./errors";
import type {
  CrearDiagnosticoRequest,
  DiagnosticoDetalle,
  DiagnosticoResumen,
} from "./types";

/** Ruta BFF same-origin de diagnósticos. */
const DIAGNOSTICOS_PATH = "/siginex/api/diagnosticos";

/**
 * Lista los diagnósticos del tenant en sesión.
 * @throws SiginexDataError `network` si falla la conexión, `unauthenticated`
 *   en 401, `unknown` en otros estados o si el JSON es ilegible.
 */
export async function listarDiagnosticos(): Promise<DiagnosticoResumen[]> {
  let response: Response;
  try {
    response = await fetch(DIAGNOSTICOS_PATH, {
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
  // 502 del BFF = la API Java no respondió = fallo de red recuperable (RF4).
  if (response.status === 502) {
    throw new SiginexDataError("network");
  }
  if (!response.ok) {
    throw new SiginexDataError("unknown");
  }

  try {
    return (await response.json()) as DiagnosticoResumen[];
  } catch {
    throw new SiginexDataError("unknown");
  }
}

/**
 * Crea un diagnóstico para la organización indicada. POST no exento de CSRF:
 * envía `X-XSRF-TOKEN` leído de la cookie si existe.
 * @throws SiginexDataError `network` si falla la conexión, `unauthenticated`
 *   en 401, `validation` en 400/422, `unknown` en otros estados o si el JSON
 *   es ilegible.
 */
export async function crearDiagnostico(
  req: CrearDiagnosticoRequest,
): Promise<DiagnosticoDetalle> {
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
    response = await fetch(DIAGNOSTICOS_PATH, {
      method: "POST",
      credentials: "same-origin",
      headers,
      body: JSON.stringify(req),
    });
  } catch {
    throw new SiginexDataError("network");
  }

  if (response.status === 401) {
    throw new SiginexDataError("unauthenticated");
  }
  if (response.status === 400 || response.status === 422) {
    throw new SiginexDataError("validation");
  }
  // 502 del BFF = la API Java no respondió = fallo de red recuperable (RF4).
  if (response.status === 502) {
    throw new SiginexDataError("network");
  }
  if (!response.ok) {
    throw new SiginexDataError("unknown");
  }

  try {
    return (await response.json()) as DiagnosticoDetalle;
  } catch {
    throw new SiginexDataError("unknown");
  }
}

/**
 * Obtiene el detalle de un diagnóstico por id. El `id` se codifica en la ruta
 * same-origin; el backend valida que sea un UUID real.
 * @throws SiginexDataError `network` si falla la conexión, `unauthenticated`
 *   en 401, `unknown` en otros estados (incl. 404) o si el JSON es ilegible.
 */
export async function obtenerDiagnostico(
  id: string,
): Promise<DiagnosticoDetalle> {
  let response: Response;
  try {
    response = await fetch(
      `${DIAGNOSTICOS_PATH}/${encodeURIComponent(id)}`,
      {
        method: "GET",
        credentials: "same-origin",
        headers: { Accept: "application/json" },
      },
    );
  } catch {
    throw new SiginexDataError("network");
  }

  if (response.status === 401) {
    throw new SiginexDataError("unauthenticated");
  }
  // 502 del BFF = la API Java no respondió = fallo de red recuperable (RF4).
  if (response.status === 502) {
    throw new SiginexDataError("network");
  }
  if (!response.ok) {
    throw new SiginexDataError("unknown");
  }

  try {
    return (await response.json()) as DiagnosticoDetalle;
  } catch {
    throw new SiginexDataError("unknown");
  }
}
