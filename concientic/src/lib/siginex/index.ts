/**
 * Superficie pública de la capa de acceso a datos de SIGINEX.
 * Importa desde `@/lib/siginex` en lugar de los módulos internos.
 *
 * Nota (modelo B / BFF): `SIGINEX_API_URL` (de `./config`) es server-only y NO
 * se reexporta aquí: solo la usa el helper de reenvío de los route handlers
 * (`./server/forward`). El cliente del navegador llama a rutas same-origin.
 */

export { login, logout, me } from "./client";
export { SiginexAuthError, isSiginexAuthError } from "./errors";
export type { SiginexAuthErrorKind } from "./errors";
export type { LoginRequest, Me, MeResponsePayload, Rol } from "./types";
