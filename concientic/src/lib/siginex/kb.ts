/**
 * Cliente API del banco de conocimiento (KB) de SIGINEX — modelo B / BFF
 * same-origin. Mismo estilo que `diagnosticos.ts`/`organizaciones.ts`: el
 * navegador llama SIEMPRE a rutas same-origin de concientic
 * (`/siginex/api/kb/...`), que son route handlers (BFF) que reenvían
 * servidor↔servidor a la API Java (`/api/v1/kb/...`). Con
 * `credentials: "same-origin"` la cookie de sesión viaja sola.
 *
 * El KB es GLOBAL (sin tenant/RLS) pero los endpoints exigen sesión válida, por
 * lo que un 401 se trata igual que en el resto de la capa de datos.
 * Ambas rutas son de solo lectura (GET), así que NO se envía CSRF.
 */

import { SiginexDataError } from "./errors";
import type { KbModulo, KbPregunta } from "./types";

/** Ruta BFF same-origin del banco KB. */
const KB_PATH = "/siginex/api/kb";

/**
 * Lista los módulos del banco KB.
 * @throws SiginexDataError `network` si falla la conexión (incl. 502 del BFF),
 *   `unauthenticated` en 401, `unknown` en otros estados o si el JSON es ilegible.
 */
export async function listarModulos(): Promise<KbModulo[]> {
  let response: Response;
  try {
    response = await fetch(`${KB_PATH}/modulos`, {
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
    return (await response.json()) as KbModulo[];
  } catch {
    throw new SiginexDataError("unknown");
  }
}

/**
 * Lista las preguntas de un módulo del banco KB. El `moduloId` se codifica en la
 * ruta; el backend responde 404 si el módulo no existe.
 * @throws SiginexDataError `network` si falla la conexión (incl. 502 del BFF),
 *   `unauthenticated` en 401, `unknown` en otros estados (incl. 404) o si el
 *   JSON es ilegible.
 */
export async function listarPreguntas(
  moduloId: string,
): Promise<KbPregunta[]> {
  let response: Response;
  try {
    response = await fetch(
      `${KB_PATH}/modulos/${encodeURIComponent(moduloId)}/preguntas`,
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
  // 502 del BFF = la API Java no respondió = fallo de red recuperable.
  if (response.status === 502) {
    throw new SiginexDataError("network");
  }
  if (!response.ok) {
    throw new SiginexDataError("unknown");
  }

  try {
    return (await response.json()) as KbPregunta[];
  } catch {
    throw new SiginexDataError("unknown");
  }
}
