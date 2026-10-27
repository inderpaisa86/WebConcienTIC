/**
 * Cliente API centralizado de SIGINEX (modelo B / BFF same-origin).
 *
 * Única puerta de acceso a la autenticación: no debe haber `fetch` disperso en
 * componentes. El navegador llama SIEMPRE a rutas SAME-ORIGIN de concientic
 * (`/siginex/api/auth/...`), que son route handlers (BFF) que reenvían
 * servidor↔servidor a la API Java. Al ser same-origin basta
 * `credentials: 'same-origin'`: la cookie de sesión `__Host-siginex_session`
 * (`SameSite=Lax`) viaja sin problema.
 *
 * CSRF: el backend protege con `CookieCsrfTokenRepository` (cookie `XSRF-TOKEN`
 * legible por JS) y exige el header `X-XSRF-TOKEN` en peticiones que mutan
 * estado, SALVO `POST /auth/login` (exento). Por tanto:
 *   - `login()` → exento, no envía CSRF.
 *   - `me()`    → GET (seguro), no requiere CSRF.
 *   - `logout()`→ POST NO exento, SÍ envía `X-XSRF-TOKEN` leído de la cookie.
 *     (El BFF propaga ese header hacia Java.)
 */

import { SiginexAuthError } from "./errors";
import type { Me, MeResponsePayload } from "./types";

/** Rutas BFF same-origin de concientic (ganan al rewrite `/siginex`). */
const LOGIN_PATH = "/siginex/api/auth/login";
const LOGOUT_PATH = "/siginex/api/auth/logout";
const ME_PATH = "/siginex/api/auth/me";

const XSRF_COOKIE_NAME = "XSRF-TOKEN";

/** Lee una cookie no-HttpOnly del documento (solo en navegador). */
function readCookie(name: string): string | null {
  if (typeof document === "undefined") {
    return null;
  }
  const match = document.cookie
    .split("; ")
    .find((row) => row.startsWith(`${name}=`));
  return match ? decodeURIComponent(match.slice(name.length + 1)) : null;
}

/**
 * Inicia sesión. Éxito = 204 (sin cuerpo).
 * @throws SiginexAuthError `invalid_credentials` en 400/401/422,
 *   `network` si falla la conexión, `unknown` en otros estados.
 */
export async function login(email: string, password: string): Promise<void> {
  let response: Response;
  try {
    response = await fetch(LOGIN_PATH, {
      method: "POST",
      credentials: "same-origin",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ email, password }),
    });
  } catch {
    // Fallo de red: no exponer detalle al usuario.
    throw new SiginexAuthError("network");
  }

  if (response.status === 204) {
    return;
  }
  if (
    response.status === 400 ||
    response.status === 401 ||
    response.status === 403 ||
    response.status === 422
  ) {
    // Mensaje genérico: no revelar si el correo existe ni el detalle del backend.
    throw new SiginexAuthError("invalid_credentials");
  }
  throw new SiginexAuthError("unknown");
}

/**
 * Devuelve el usuario autenticado.
 * @throws SiginexAuthError `unauthenticated` en 401, `network` si falla la
 *   conexión, `unknown` en otros estados.
 */
export async function me(): Promise<Me> {
  let response: Response;
  try {
    response = await fetch(ME_PATH, {
      method: "GET",
      credentials: "same-origin",
      headers: { Accept: "application/json" },
    });
  } catch {
    throw new SiginexAuthError("network");
  }

  if (response.status === 401) {
    throw new SiginexAuthError("unauthenticated");
  }
  if (!response.ok) {
    throw new SiginexAuthError("unknown");
  }

  let payload: MeResponsePayload;
  try {
    payload = (await response.json()) as MeResponsePayload;
  } catch {
    throw new SiginexAuthError("unknown");
  }

  return {
    usuarioId: payload.usuarioId,
    tenantId: payload.tenantId,
    email: payload.email,
    rol: payload.rol,
  };
}

/**
 * Cierra la sesión. Best-effort: no lanza si el backend ya invalidó la sesión.
 * Envía `X-XSRF-TOKEN` porque `POST /auth/logout` NO está exento de CSRF; el
 * BFF lo reenvía a Java.
 */
export async function logout(): Promise<void> {
  const xsrfToken = readCookie(XSRF_COOKIE_NAME);
  const headers: HeadersInit = {};
  if (xsrfToken) {
    headers["X-XSRF-TOKEN"] = xsrfToken;
  }

  try {
    await fetch(LOGOUT_PATH, {
      method: "POST",
      credentials: "same-origin",
      headers,
    });
  } catch {
    // Logout best-effort: ignorar fallos de red; la UI redirige igualmente.
  }
}
