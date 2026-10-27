/**
 * Tipos del contrato de autenticación de SIGINEX.
 *
 * Reflejan los DTO del backend Java (`backend/siginex-api`):
 * - `POST /auth/login`  → body `LoginRequest { email, password }`, respuesta 204.
 * - `POST /auth/logout` → 204, expira la cookie de sesión.
 * - `GET  /auth/me`     → 200 `MeResponse { usuarioId, tenantId, email, rol }`,
 *                          401 si no hay sesión válida.
 *
 * Los UUID del backend se representan como `string` en el cliente.
 */

/** Body de `POST /auth/login`. */
export interface LoginRequest {
  email: string;
  password: string;
}

/**
 * Rol del usuario. El backend devuelve un `String` libre en `MeResponse.rol`;
 * aquí se tipa de forma abierta para no romper ante valores no previstos.
 */
export type Rol = string;

/**
 * Respuesta de `GET /auth/me` ya normalizada para el cliente.
 * Coincide con `MeResponse` del backend (UUID → string).
 */
export interface Me {
  usuarioId: string;
  tenantId: string;
  email: string;
  rol: Rol;
}

/** Forma cruda del JSON de `MeResponse` tal como lo emite el backend. */
export interface MeResponsePayload {
  usuarioId: string;
  tenantId: string;
  email: string;
  rol: string;
}
