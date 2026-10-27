/**
 * Cliente API de organizaciones de SIGINEX (modelo B / BFF same-origin).
 *
 * Mismo estilo que `client.ts`: el navegador llama SIEMPRE a rutas same-origin
 * de concientic (`/siginex/api/organizaciones`), que son route handlers (BFF)
 * que reenvían servidor↔servidor a la API Java. Con `credentials: "same-origin"`
 * la cookie de sesión viaja sola; los POST añaden el header `X-XSRF-TOKEN`
 * leído de la cookie `XSRF-TOKEN` (igual que `logout()`).
 */

import { readCookie, XSRF_COOKIE_NAME } from "./csrf";
import { SiginexDataError } from "./errors";
import type { CrearOrganizacionRequest, Organizacion } from "./types";

/** Ruta BFF same-origin de organizaciones. */
const ORGANIZACIONES_PATH = "/siginex/api/organizaciones";

/**
 * Lista las organizaciones del tenant en sesión.
 * @throws SiginexDataError `network` si falla la conexión, `unauthenticated`
 *   en 401, `unknown` en otros estados o si el JSON es ilegible.
 */
export async function listarOrganizaciones(): Promise<Organizacion[]> {
  let response: Response;
  try {
    response = await fetch(ORGANIZACIONES_PATH, {
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
    return (await response.json()) as Organizacion[];
  } catch {
    throw new SiginexDataError("unknown");
  }
}

/**
 * Crea una organización. POST no exento de CSRF: envía `X-XSRF-TOKEN` leído de
 * la cookie si existe (si no existe, el backend responderá 403 → `unknown`).
 * @throws SiginexDataError `network` si falla la conexión, `unauthenticated`
 *   en 401, `validation` en 400/422, `unknown` en otros estados o si el JSON
 *   es ilegible.
 */
export async function crearOrganizacion(
  req: CrearOrganizacionRequest,
): Promise<Organizacion> {
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
    response = await fetch(ORGANIZACIONES_PATH, {
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
    return (await response.json()) as Organizacion;
  } catch {
    throw new SiginexDataError("unknown");
  }
}
