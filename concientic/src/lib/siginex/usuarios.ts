/**
 * Cliente API de gestión de usuarios por el admin (modelo B / BFF same-origin).
 * El navegador llama a rutas same-origin `/siginex/api/usuarios`, que reenvían a
 * `/api/v1/usuarios` de la API Java con la cookie de sesión. Los POST que mutan
 * estado envían `X-XSRF-TOKEN` (no exentos de CSRF).
 *
 * Autorización: el backend exige rol `admin` y opera siempre sobre el tenant
 * del admin en sesión (RLS). El cliente nunca envía `tenant_id`.
 */

import { readCookie, XSRF_COOKIE_NAME } from "./csrf";
import { SiginexDataError } from "./errors";
import type { CrearUsuarioRequest, UsuarioResumen } from "./types";

const USUARIOS_PATH = "/siginex/api/usuarios";

/** Cabeceras con CSRF para los POST (leyendo la cookie XSRF-TOKEN). */
function postHeaders(): Record<string, string> {
  const headers: Record<string, string> = {
    "Content-Type": "application/json",
    Accept: "application/json",
  };
  const xsrf = readCookie(XSRF_COOKIE_NAME);
  if (xsrf) {
    headers["X-XSRF-TOKEN"] = xsrf;
  }
  return headers;
}

/** Normaliza la respuesta a un error tipado según el status. */
function errorPorStatus(status: number): SiginexDataError {
  if (status === 401) return new SiginexDataError("unauthenticated");
  if (status === 403) return new SiginexDataError("validation"); // no admin
  if (status === 400 || status === 409 || status === 422) {
    return new SiginexDataError("validation");
  }
  if (status === 502) return new SiginexDataError("network");
  return new SiginexDataError("unknown");
}

/** Lista los usuarios del tenant del admin. */
export async function listarUsuarios(): Promise<UsuarioResumen[]> {
  let response: Response;
  try {
    response = await fetch(USUARIOS_PATH, {
      method: "GET",
      credentials: "same-origin",
      headers: { Accept: "application/json" },
    });
  } catch {
    throw new SiginexDataError("network");
  }
  if (!response.ok) {
    throw errorPorStatus(response.status);
  }
  try {
    return (await response.json()) as UsuarioResumen[];
  } catch {
    throw new SiginexDataError("unknown");
  }
}

/** Crea un usuario en el tenant del admin. */
export async function crearUsuario(
  req: CrearUsuarioRequest,
): Promise<UsuarioResumen> {
  let response: Response;
  try {
    response = await fetch(USUARIOS_PATH, {
      method: "POST",
      credentials: "same-origin",
      headers: postHeaders(),
      body: JSON.stringify(req),
    });
  } catch {
    throw new SiginexDataError("network");
  }
  if (!response.ok) {
    throw errorPorStatus(response.status);
  }
  try {
    return (await response.json()) as UsuarioResumen;
  } catch {
    throw new SiginexDataError("unknown");
  }
}

/** Activa o desactiva un usuario del tenant del admin. */
export async function cambiarActivoUsuario(
  id: string,
  activo: boolean,
): Promise<UsuarioResumen> {
  const accion = activo ? "activar" : "desactivar";
  let response: Response;
  try {
    response = await fetch(
      `${USUARIOS_PATH}/${encodeURIComponent(id)}/${accion}`,
      {
        method: "POST",
        credentials: "same-origin",
        headers: postHeaders(),
      },
    );
  } catch {
    throw new SiginexDataError("network");
  }
  if (!response.ok) {
    throw errorPorStatus(response.status);
  }
  try {
    return (await response.json()) as UsuarioResumen;
  } catch {
    throw new SiginexDataError("unknown");
  }
}
