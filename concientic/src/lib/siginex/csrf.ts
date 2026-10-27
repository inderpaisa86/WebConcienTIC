/**
 * Helper CSRF compartido de la capa de cliente de SIGINEX.
 *
 * El backend protege con `CookieCsrfTokenRepository` (cookie `XSRF-TOKEN`
 * legible por JS) y exige el header `X-XSRF-TOKEN` en los POST que mutan
 * estado. Tanto `client.ts` (`logout()`) como los módulos de datos de la 3b
 * (`organizaciones.ts`, `diagnosticos.ts`) necesitan leer esa cookie, así que
 * la lectura vive aquí para no duplicarla.
 *
 * Server-safe: `readCookie` devuelve `null` fuera del navegador (sin
 * `document`), de modo que importar este módulo desde código que pudiera
 * ejecutarse en el servidor no rompe.
 */

/** Nombre de la cookie CSRF que emite el backend (legible por JS). */
export const XSRF_COOKIE_NAME = "XSRF-TOKEN";

/** Lee una cookie no-HttpOnly del documento (solo en navegador). */
export function readCookie(name: string): string | null {
  if (typeof document === "undefined") {
    return null;
  }
  const match = document.cookie
    .split("; ")
    .find((row) => row.startsWith(`${name}=`));
  return match ? decodeURIComponent(match.slice(name.length + 1)) : null;
}
