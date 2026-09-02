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

const hasUpstash =
  !!process.env.UPSTASH_REDIS_REST_URL && !!process.env.UPSTASH_REDIS_REST_TOKEN;

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
