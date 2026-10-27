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

/* ------------------------------------------------------------------ *
 * Sub-fase 3b: tipos del contrato de datos (organizaciones y
 * diagnósticos). Reflejan los DTO del backend Java:
 * - `OrganizacionResponse`         { UUID id, UUID tenantId, String nombre,
 *                                     String sector, String tamano,
 *                                     Instant creadoEn }
 * - `CrearOrganizacionRequest`     { @NotBlank nombre, sector,
 *                                     @Pattern(micro|pequena|mediana|grande) tamano }
 * - `DiagnosticoResumenResponse`   { id, organizacionId, organizacionNombre,
 *                                     estado, String scoreSgi, Integer nivelSgi,
 *                                     Instant creadoEn }
 * - `DiagnosticoDetalleResponse`   { id, organizacionId, organizacionNombre, estado }
 * - `CrearDiagnosticoRequest`      { @NotNull UUID organizacionId }
 * Los UUID se representan como `string`; los `Instant` como ISO `string`.
 * ------------------------------------------------------------------ */

/**
 * Tamaño de empresa aceptado por el backend en la ENTRADA
 * (`CrearOrganizacionRequest`), validado con `@Pattern`. Unión cerrada.
 */
export type OrganizacionTamano = "micro" | "pequena" | "mediana" | "grande";

/**
 * Organización tal como la devuelve el backend (`OrganizacionResponse`).
 * `tamano` se tipa `string | null` (abierto) porque el backend emite un
 * `String` libre en la salida; la unión cerrada solo aplica en la entrada
 * (mismo criterio abierto que `Rol`).
 */
export interface Organizacion {
  id: string;
  tenantId: string;
  nombre: string;
  sector: string | null;
  tamano: string | null;
  creadoEn: string;
}

/** Body de `POST /api/v1/organizaciones`. */
export interface CrearOrganizacionRequest {
  nombre: string;
  sector?: string;
  tamano?: OrganizacionTamano;
}

/**
 * Resumen de diagnóstico (`DiagnosticoResumenResponse`).
 * `scoreSgi` es STRING en el backend (no se castea a number); `nivelSgi` es
 * entero nullable; `estado` es string libre (p. ej. `"borrador"`/`"calculado"`).
 */
export interface DiagnosticoResumen {
  id: string;
  organizacionId: string;
  organizacionNombre: string;
  estado: string;
  scoreSgi: string | null;
  nivelSgi: number | null;
  creadoEn: string;
}

/** Detalle de diagnóstico (`DiagnosticoDetalleResponse`). */
export interface DiagnosticoDetalle {
  id: string;
  organizacionId: string;
  organizacionNombre: string;
  estado: string;
}

/** Body de `POST /api/v1/diagnosticos`. */
export interface CrearDiagnosticoRequest {
  organizacionId: string;
}
