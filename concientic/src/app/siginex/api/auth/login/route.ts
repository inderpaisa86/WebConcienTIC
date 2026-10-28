import { NextRequest, NextResponse } from "next/server";

import { forwardToSiginex } from "@/lib/siginex/server/forward";
import { limitAction } from "@/lib/rate-limit";

/**
 * BFF: `POST /siginex/api/auth/login` → `POST /auth/login` de la API Java.
 * Login está EXENTO de CSRF en el backend, así que no se propaga CSRF.
 *
 * Fase 6 (endurecimiento): rate-limiting por IP + cuenta ANTES de reenviar a
 * Java, para frenar fuerza bruta en el borde (Vercel) y ahorrar cold-starts de
 * la API. El backoff del backend (`fallos_login`/`bloqueado_hasta`) sigue como
 * segunda capa. Degrada seguro si Upstash no está configurado (no bloquea).
 * El email solo se usa para componer la clave de rate-limit; NUNCA se loguea.
 */
export const dynamic = "force-dynamic";

function getClientIp(request: NextRequest): string {
  const forwarded = request.headers.get("x-forwarded-for");
  if (forwarded) {
    return forwarded.split(",")[0]!.trim();
  }
  return request.headers.get("x-real-ip") ?? "127.0.0.1";
}

/** Extrae el email del cuerpo JSON sin romper si es ilegible. No se loguea. */
function extraerEmail(bodyText: string): string | null {
  try {
    const parsed = JSON.parse(bodyText) as { email?: unknown };
    return typeof parsed.email === "string" ? parsed.email.toLowerCase() : null;
  } catch {
    return null;
  }
}

export async function POST(request: NextRequest): Promise<Response> {
  const ip = getClientIp(request);

  // Leer el cuerpo UNA vez (el stream se consume) y reconstruir la petición
  // para el forward con el mismo cuerpo y cabeceras.
  const bodyText = await request.text();
  const email = extraerEmail(bodyText);
  const identifier = email ? `ip:${ip}|email:${email}` : `ip:${ip}`;

  const rate = await limitAction("siginex-login", identifier);
  if (!rate.success) {
    return NextResponse.json(
      { error: "Demasiados intentos. Inténtalo de nuevo en un minuto." },
      { status: 429 },
    );
  }

  const forwardRequest = new NextRequest(request.url, {
    method: "POST",
    headers: request.headers,
    body: bodyText,
  });

  return forwardToSiginex(forwardRequest, {
    path: "/auth/login",
    method: "POST",
    forwardCsrf: false,
  });
}
