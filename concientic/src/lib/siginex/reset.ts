/**
 * Cliente API de recuperación de contraseña de SIGINEX (modelo B / BFF
 * same-origin). El navegador llama SIEMPRE a rutas same-origin de concientic
 * (`/siginex/api/auth/reset/...`), que son route handlers (BFF) que reenvían a
 * la API Java (`/auth/reset/...`).
 *
 * CSRF: igual que `POST /auth/login`, los dos endpoints de reset están EXENTOS
 * de CSRF en el backend (no hay sesión todavía), así que NO se envía
 * `X-XSRF-TOKEN`. La respuesta de éxito es 204 (sin cuerpo).
 *
 * No-enumeración: `solicitarReset` resuelve SIEMPRE igual (no revela si el
 * email existe). `confirmarReset` lanza un error genérico si el token es
 * inválido/usado/expirado.
 */

import { SiginexDataError } from "./errors";

const SOLICITAR_PATH = "/siginex/api/auth/reset/solicitar";
const CONFIRMAR_PATH = "/siginex/api/auth/reset/confirmar";

/**
 * Solicita un correo de recuperación para `email`. Termina sin error en
 * cualquier caso salvo fallo de red/servidor; NO revela si la cuenta existe.
 * @throws SiginexDataError `network` si falla la conexión (incl. 502 del BFF),
 *   `unknown` en estados inesperados.
 */
export async function solicitarReset(email: string): Promise<void> {
  let response: Response;
  try {
    response = await fetch(SOLICITAR_PATH, {
      method: "POST",
      credentials: "same-origin",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ email }),
    });
  } catch {
    throw new SiginexDataError("network");
  }

  if (response.status === 204) {
    return;
  }
  // 400 de validación de formato (email mal formado) se trata como validación.
  if (response.status === 400 || response.status === 422) {
    throw new SiginexDataError("validation");
  }
  if (response.status === 502) {
    throw new SiginexDataError("network");
  }
  throw new SiginexDataError("unknown");
}

/**
 * Confirma el reset con el `token` del enlace y la `nuevaPassword`. Éxito = 204.
 * @throws SiginexDataError `validation` en 400/422 (p. ej. contraseña corta),
 *   `unauthenticated` en 401 (token inválido/usado/expirado — mensaje genérico),
 *   `network` si falla la conexión (incl. 502), `unknown` en otros estados.
 */
export async function confirmarReset(
  token: string,
  nuevaPassword: string,
): Promise<void> {
  let response: Response;
  try {
    response = await fetch(CONFIRMAR_PATH, {
      method: "POST",
      credentials: "same-origin",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ token, nuevaPassword }),
    });
  } catch {
    throw new SiginexDataError("network");
  }

  if (response.status === 204) {
    return;
  }
  if (response.status === 400 || response.status === 422) {
    throw new SiginexDataError("validation");
  }
  // El backend devuelve 401 genérico para token inválido/usado/expirado.
  if (response.status === 401) {
    throw new SiginexDataError("unauthenticated");
  }
  if (response.status === 502) {
    throw new SiginexDataError("network");
  }
  throw new SiginexDataError("unknown");
}
