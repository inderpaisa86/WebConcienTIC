/**
 * Superficie pública de la capa de acceso a datos de SIGINEX.
 * Importa desde `@/lib/siginex` en lugar de los módulos internos.
 *
 * Nota (modelo B / BFF): `SIGINEX_API_URL` (de `./config`) es server-only y NO
 * se reexporta aquí: solo la usa el helper de reenvío de los route handlers
 * (`./server/forward`). El cliente del navegador llama a rutas same-origin.
 */

export { login, logout, me } from "./client";
export { listarOrganizaciones, crearOrganizacion } from "./organizaciones";
export {
  listarDiagnosticos,
  crearDiagnostico,
  obtenerDiagnostico,
} from "./diagnosticos";
export { SiginexAuthError, isSiginexAuthError } from "./errors";
export { SiginexDataError, isSiginexDataError } from "./errors";
export type { SiginexAuthErrorKind, SiginexDataErrorKind } from "./errors";
export type { LoginRequest, Me, MeResponsePayload, Rol } from "./types";
export type {
  Organizacion,
  OrganizacionTamano,
  CrearOrganizacionRequest,
  DiagnosticoResumen,
  DiagnosticoDetalle,
  CrearDiagnosticoRequest,
} from "./types";
