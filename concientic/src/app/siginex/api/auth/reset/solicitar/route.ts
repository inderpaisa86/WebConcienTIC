import { NextRequest, NextResponse } from "next/server";

import { forwardToSiginex } from "@/lib/siginex/server/forward";
import { limitAction } from "@/lib/rate-limit";

/**
 * BFF: `POST /siginex/api/auth/reset/solicitar` → `POST /auth/reset/solicitar`
 * de la API Java. EXENTO de CSRF (no hay sesión). Responde 204 exista o no el
 * email (no-enumeración).
 *
 * Fase 6 (endurecimiento): rate-limiting por IP + cuenta ANTES de reenviar,
 * para frenar abuso del envío de correos de recuperación. Degrada seguro sin
 * Upstash. El email solo compone la clave de rate-limit; NUNCA se loguea.
 *
 * Nota no-enumeración: el límite se compone con IP + email; un 429 no revela si
 * la cuenta existe (se dispara por volumen de solicitudes, no por existencia).
 */
export const dynamic = "force-dynamic";

function getClientIp(request: NextRequest): string {
  const forwarded = request.headers.get("x-forwarded-for");
  if (forwarded) {
    return forwarded.split(",")[0]!.trim();
  }
  return request.headers.get("x-real-ip") ?? "127.0.0.1";
}

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
  const bodyText = await request.text();
  const email = extraerEmail(bodyText);
  const identifier = email ? `ip:${ip}|email:${email}` : `ip:${ip}`;

  const rate = await limitAction("siginex-reset", identifier);
  if (!rate.success) {
    return NextResponse.json(
      { error: "Demasiadas solicitudes. Inténtalo de nuevo en un minuto." },
      { status: 429 },
    );
  }

  const forwardRequest = new NextRequest(request.url, {
    method: "POST",
    headers: request.headers,
    body: bodyText,
  });

  return forwardToSiginex(forwardRequest, {
    path: "/auth/reset/solicitar",
    method: "POST",
    forwardCsrf: false,
  });
}
