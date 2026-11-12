import { NextResponse } from "next/server";

import { notifyLead, isLeadNotifyConfigured } from "@/lib/leads/notify";
import { limitAction } from "@/lib/rate-limit";
import { leadSchema } from "@/lib/validations/lead";

// Siempre en request-time: procesa datos de cada petición.
export const dynamic = "force-dynamic";

function getClientIp(request: Request): string {
  const forwarded = request.headers.get("x-forwarded-for");
  if (forwarded) {
    return forwarded.split(",")[0]!.trim();
  }
  return request.headers.get("x-real-ip") ?? "127.0.0.1";
}

/**
 * POST /api/leads — formulario «Hablemos» de la landing de SIGINEX.
 *   201: el correo de aviso se envió.
 *   400: datos inválidos (incluido el honeypot).
 *   429: demasiados envíos desde la misma IP.
 *   503: el servicio de correo no está disponible. Nunca responde 201 sin enviar.
 */
export async function POST(request: Request) {
  // 1) Límite por IP. Sin Upstash no bloquea (ver rate-limit.ts).
  const rate = await limitAction("siginex-lead", `ip:${getClientIp(request)}`);
  if (!rate.success) {
    return NextResponse.json(
      { mensaje: "Recibimos varios intentos seguidos. Espera un momento y vuelve a intentarlo." },
      { status: 429 },
    );
  }

  // 2) Cuerpo JSON.
  let body: unknown;
  try {
    body = await request.json();
  } catch {
    return NextResponse.json(
      { mensaje: "Revisa los datos e inténtalo de nuevo." },
      { status: 400 },
    );
  }

  // 3) Validación.
  const parsed = leadSchema.safeParse(body);
  if (!parsed.success) {
    const primero = parsed.error.issues[0]?.message ?? "Revisa los datos e inténtalo de nuevo.";
    return NextResponse.json({ mensaje: primero }, { status: 400 });
  }

  // 4) Servicio de correo configurado.
  if (!isLeadNotifyConfigured()) {
    return NextResponse.json(
      { mensaje: "No pudimos enviar tu solicitud en este momento. Inténtalo más tarde." },
      { status: 503 },
    );
  }

  // 5) Envío del aviso.
  const result = await notifyLead(parsed.data);
  if (!result.ok) {
    return NextResponse.json(
      { mensaje: "No pudimos enviar tu solicitud en este momento. Inténtalo más tarde." },
      { status: 503 },
    );
  }

  return NextResponse.json({ ok: true }, { status: 201 });
}
