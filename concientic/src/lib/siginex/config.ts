/**
 * Configuración de entorno de la capa SIGINEX (modelo B / BFF).
 *
 * La URL base del backend Java se lee de `SIGINEX_API_URL`. Es una variable
 * SERVER-ONLY (sin prefijo `NEXT_PUBLIC_`): el `fetch` a la API Java ocurre en
 * los route handlers del BFF (servidor de Next), nunca en el navegador, así que
 * NO debe filtrarse al bundle de cliente. El navegador solo habla con rutas
 * same-origin de concientic (`/siginex/api/auth/...`).
 *
 * El acceso se hace de forma LITERAL (`process.env.SIGINEX_API_URL`) para que
 * Node lo resuelva en runtime del servidor.
 */

const DEFAULT_SIGINEX_API_URL = "http://localhost:8080";

/** Quita la barra final para poder concatenar rutas sin dobles barras. */
function normalizeBaseUrl(url: string): string {
  return url.trim().replace(/\/+$/, "");
}

/**
 * URL base del backend SIGINEX, sin barra final.
 * Default: `http://localhost:8080` (no hay host de producción todavía).
 * SOLO para uso en código de servidor (route handlers del BFF).
 */
export const SIGINEX_API_URL: string = normalizeBaseUrl(
  process.env.SIGINEX_API_URL ?? DEFAULT_SIGINEX_API_URL,
);
