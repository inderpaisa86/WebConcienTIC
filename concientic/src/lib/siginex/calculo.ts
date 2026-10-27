/**
 * Cliente API del cálculo de diagnósticos de SIGINEX (resultado, plan de mejora
 * y benchmark) — modelo B / BFF same-origin. Mismo estilo que `diagnosticos.ts`:
 * el navegador llama SIEMPRE a rutas same-origin de concientic
 * (`/siginex/api/diagnosticos/{id}/...`), que son route handlers (BFF) que
 * reenvían a la API Java (`/api/v1/diagnosticos/{id}/...`). Con
 * `credentials: "same-origin"` la cookie de sesión viaja sola; los POST (mutan
 * estado) añaden `X-XSRF-TOKEN` leído de la cookie `XSRF-TOKEN`.
 */

import { readCookie, XSRF_COOKIE_NAME } from "./csrf";
import { SiginexDataError } from "./errors";
import type {
  Benchmark,
  RegistrarBenchmarkRequest,
  Resultado,
  TareaPlan,
} from "./types";

/** Base de la ruta BFF de un diagnóstico. */
function diagnosticoBase(diagnosticoId: string): string {
  return `/siginex/api/diagnosticos/${encodeURIComponent(diagnosticoId)}`;
}

/** Cabeceras con `X-XSRF-TOKEN` para los POST que mutan estado. */
function headersMutacion(): Record<string, string> {
  const xsrfToken = readCookie(XSRF_COOKIE_NAME);
  const headers: Record<string, string> = {
    "Content-Type": "application/json",
    Accept: "application/json",
  };
  if (xsrfToken) {
    headers["X-XSRF-TOKEN"] = xsrfToken;
  }
  return headers;
}

/**
 * Dispara el (re)cálculo del diagnóstico y devuelve el resultado consolidado.
 * POST no exento de CSRF.
 * @throws SiginexDataError `network` si falla la conexión (incl. 502 del BFF),
 *   `unauthenticated` en 401, `validation` en 400/422 (p. ej. sin respuestas
 *   suficientes), `unknown` en otros estados o si el JSON es ilegible.
 */
export async function calcularDiagnostico(
  diagnosticoId: string,
): Promise<Resultado> {
  let response: Response;
  try {
    response = await fetch(`${diagnosticoBase(diagnosticoId)}/calcular`, {
      method: "POST",
      credentials: "same-origin",
      headers: headersMutacion(),
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
  // 502 del BFF = la API Java no respondió = fallo de red recuperable.
  if (response.status === 502) {
    throw new SiginexDataError("network");
  }
  if (!response.ok) {
    throw new SiginexDataError("unknown");
  }

  try {
    return (await response.json()) as Resultado;
  } catch {
    throw new SiginexDataError("unknown");
  }
}

/**
 * Lee el resultado consolidado ya calculado de un diagnóstico.
 * @throws SiginexDataError `network` si falla la conexión (incl. 502 del BFF),
 *   `unauthenticated` en 401, `unknown` en otros estados (incl. 404 si el
 *   diagnóstico no es del tenant o no existe) o si el JSON es ilegible.
 */
export async function obtenerResultado(
  diagnosticoId: string,
): Promise<Resultado> {
  let response: Response;
  try {
    response = await fetch(`${diagnosticoBase(diagnosticoId)}/resultado`, {
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
  if (response.status === 502) {
    throw new SiginexDataError("network");
  }
  if (!response.ok) {
    throw new SiginexDataError("unknown");
  }

  try {
    return (await response.json()) as Resultado;
  } catch {
    throw new SiginexDataError("unknown");
  }
}

/**
 * Lee el plan de mejora (lista de tareas priorizadas) de un diagnóstico.
 * @throws SiginexDataError `network` si falla la conexión (incl. 502 del BFF),
 *   `unauthenticated` en 401, `unknown` en otros estados o si el JSON es ilegible.
 */
export async function obtenerPlanMejora(
  diagnosticoId: string,
): Promise<TareaPlan[]> {
  let response: Response;
  try {
    response = await fetch(`${diagnosticoBase(diagnosticoId)}/plan-mejora`, {
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
  if (response.status === 502) {
    throw new SiginexDataError("network");
  }
  if (!response.ok) {
    throw new SiginexDataError("unknown");
  }

  try {
    return (await response.json()) as TareaPlan[];
  } catch {
    throw new SiginexDataError("unknown");
  }
}

/**
 * Lee el benchmark de mercado de un diagnóstico, o `null` si aún no se ha
 * registrado (el backend responde **204 No Content** en ese caso).
 * @throws SiginexDataError `network` si falla la conexión (incl. 502 del BFF),
 *   `unauthenticated` en 401, `unknown` en otros estados o si el JSON es ilegible.
 */
export async function obtenerBenchmark(
  diagnosticoId: string,
): Promise<Benchmark | null> {
  let response: Response;
  try {
    response = await fetch(`${diagnosticoBase(diagnosticoId)}/benchmark`, {
      method: "GET",
      credentials: "same-origin",
      headers: { Accept: "application/json" },
    });
  } catch {
    throw new SiginexDataError("network");
  }

  // 204 = aún no hay benchmark registrado: no es un error, es "sin dato".
  if (response.status === 204) {
    return null;
  }
  if (response.status === 401) {
    throw new SiginexDataError("unauthenticated");
  }
  if (response.status === 502) {
    throw new SiginexDataError("network");
  }
  if (!response.ok) {
    throw new SiginexDataError("unknown");
  }

  try {
    return (await response.json()) as Benchmark;
  } catch {
    throw new SiginexDataError("unknown");
  }
}

/**
 * Registra el nivel tecnológico declarado (1–5) y devuelve el benchmark
 * recalculado. POST no exento de CSRF.
 * @throws SiginexDataError `network` si falla la conexión (incl. 502 del BFF),
 *   `unauthenticated` en 401, `validation` en 400/422 (nivel fuera de 1–5),
 *   `unknown` en otros estados o si el JSON es ilegible.
 */
export async function registrarBenchmark(
  diagnosticoId: string,
  req: RegistrarBenchmarkRequest,
): Promise<Benchmark> {
  let response: Response;
  try {
    response = await fetch(`${diagnosticoBase(diagnosticoId)}/benchmark`, {
      method: "POST",
      credentials: "same-origin",
      headers: headersMutacion(),
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
  if (response.status === 502) {
    throw new SiginexDataError("network");
  }
  if (!response.ok) {
    throw new SiginexDataError("unknown");
  }

  try {
    return (await response.json()) as Benchmark;
  } catch {
    throw new SiginexDataError("unknown");
  }
}
