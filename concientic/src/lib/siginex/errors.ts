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
