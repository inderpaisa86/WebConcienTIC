/**
 * Errores tipados de la capa de autenticación de SIGINEX.
 *
 * El discriminador `kind` permite que la UI muestre mensajes claros sin filtrar
 * detalles sensibles del backend (p. ej. no se expone si el usuario existe).
 */

export type SiginexAuthErrorKind =
  | "invalid_credentials"
  | "network"
  | "unauthenticated"
  | "unknown";

/** Mensajes en español, genéricos y seguros para mostrar al usuario. */
const DEFAULT_MESSAGES: Record<SiginexAuthErrorKind, string> = {
  invalid_credentials: "Correo o contraseña incorrectos.",
  network: "No se pudo conectar con el servidor. Inténtalo de nuevo.",
  unauthenticated: "Tu sesión no es válida o ha caducado.",
  unknown: "Ha ocurrido un error inesperado. Inténtalo de nuevo.",
};

export class SiginexAuthError extends Error {
  readonly kind: SiginexAuthErrorKind;

  constructor(kind: SiginexAuthErrorKind, message?: string, options?: ErrorOptions) {
    super(message ?? DEFAULT_MESSAGES[kind], options);
    this.name = "SiginexAuthError";
    this.kind = kind;
    // Mantener la cadena de prototipos correcta al transpilar a ES5/ES2017.
    Object.setPrototypeOf(this, SiginexAuthError.prototype);
  }
}

/** Type guard para distinguir errores de auth del resto. */
export function isSiginexAuthError(error: unknown): error is SiginexAuthError {
  return error instanceof SiginexAuthError;
}

/* ------------------------------------------------------------------ *
 * Sub-fase 3b: errores tipados de la capa de datos (organizaciones y
 * diagnósticos). Mismo patrón que `SiginexAuthError` (constructor con
 * `kind`, mensajes genéricos en español, `Object.setPrototypeOf`, type
 * guard). Se usa una clase separada porque los `kind` de datos
 * (`validation`) no existen en auth y mezclar ambos discriminadores los
 * ensuciaría.
 * ------------------------------------------------------------------ */

export type SiginexDataErrorKind =
  | "network" // fallo de conexión navegador↔BFF o BFF↔Java (incl. 502)
  | "unauthenticated" // 401: sesión no válida/caducada
  | "validation" // 400/422: datos del formulario rechazados por el backend
  | "unknown"; // cualquier otro estado (403 CSRF, 404, 409, 500, parse fallido)

/** Mensajes en español, genéricos y seguros para mostrar al usuario. */
const DATA_DEFAULT_MESSAGES: Record<SiginexDataErrorKind, string> = {
  network: "No se pudo conectar con el servidor. Inténtalo de nuevo.",
  unauthenticated: "Tu sesión no es válida o ha caducado.",
  validation: "Revisa los datos del formulario e inténtalo de nuevo.",
  unknown: "Ha ocurrido un error inesperado. Inténtalo de nuevo.",
};

export class SiginexDataError extends Error {
  readonly kind: SiginexDataErrorKind;

  constructor(kind: SiginexDataErrorKind, message?: string, options?: ErrorOptions) {
    super(message ?? DATA_DEFAULT_MESSAGES[kind], options);
    this.name = "SiginexDataError";
    this.kind = kind;
    // Mantener la cadena de prototipos correcta al transpilar a ES5/ES2017.
    Object.setPrototypeOf(this, SiginexDataError.prototype);
  }
}

/** Type guard para distinguir errores de datos del resto. */
export function isSiginexDataError(error: unknown): error is SiginexDataError {
  return error instanceof SiginexDataError;
}
