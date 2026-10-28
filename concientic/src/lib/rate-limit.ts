import { Ratelimit } from "@upstash/ratelimit";
import { Redis } from "@upstash/redis";

/**
 * Rate limiter distribuido para route handlers (serverless-safe).
 *
 * Usa Upstash Redis (compatible con Vercel KV). En entornos donde no estén configuradas
 * las variables de entorno (ej. build o desarrollo local sin credenciales), degrada de
 * forma segura: NO bloquea (devuelve success=true). En producción, configurar:
 *   - UPSTASH_REDIS_REST_URL
 *   - UPSTASH_REDIS_REST_TOKEN
 */

const hasUpstash = !!process.env.UPSTASH_REDIS_REST_URL && !!process.env.UPSTASH_REDIS_REST_TOKEN;

const ratelimit = hasUpstash
  ? new Ratelimit({
      redis: Redis.fromEnv(),
      // 5 solicitudes por minuto por identificador (IP).
      limiter: Ratelimit.slidingWindow(5, "60 s"),
      prefix: "ratelimit:contact",
      analytics: true,
    })
  : null;

export type RateLimitResult = {
  success: boolean;
  configured: boolean;
};

export async function limitByIp(ip: string): Promise<RateLimitResult> {
  if (!ratelimit) {
    // Sin credenciales: no bloquear (útil en dev/build). Registrar en logs de servidor.
    return { success: true, configured: false };
  }
  const { success } = await ratelimit.limit(ip);
  return { success, configured: true };
}

/* ------------------------------------------------------------------ *
 * Fase 6 (endurecimiento): rate-limiting por ACCIÓN para el BFF de
 * SIGINEX (login y solicitar-reset). Reutiliza Upstash con la misma
 * degradación segura: sin credenciales NO bloquea. Cada acción tiene su
 * propio limiter (prefijo distinto) para no compartir cuota con /contact.
 * ------------------------------------------------------------------ */

/** Acciones sensibles a fuerza bruta protegidas en el BFF. */
export type RateLimitAction = "siginex-login" | "siginex-reset";

/** Ventana/política por acción. Login y reset: 5 intentos por minuto. */
const ACTION_LIMITERS: Record<RateLimitAction, Ratelimit | null> = {
  "siginex-login": hasUpstash
    ? new Ratelimit({
        redis: Redis.fromEnv(),
        limiter: Ratelimit.slidingWindow(5, "60 s"),
        prefix: "ratelimit:siginex-login",
        analytics: true,
      })
    : null,
  "siginex-reset": hasUpstash
    ? new Ratelimit({
        redis: Redis.fromEnv(),
        limiter: Ratelimit.slidingWindow(5, "60 s"),
        prefix: "ratelimit:siginex-reset",
        analytics: true,
      })
    : null,
};

/**
 * Limita una acción por un identificador arbitrario (p. ej. `ip:1.2.3.4` o
 * `ip:1.2.3.4|email:user@x.com`). Degrada seguro si Upstash no está
 * configurado (no bloquea). El identificador NO debe contener secretos.
 */
export async function limitAction(
  action: RateLimitAction,
  identifier: string,
): Promise<RateLimitResult> {
  const limiter = ACTION_LIMITERS[action];
  if (!limiter) {
    return { success: true, configured: false };
  }
  const { success } = await limiter.limit(identifier);
  return { success, configured: true };
}
