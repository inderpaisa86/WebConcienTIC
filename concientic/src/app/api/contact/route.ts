import { NextResponse } from "next/server";

import { sendContactEmail } from "@/lib/email";
import { limitByIp } from "@/lib/rate-limit";
import { contactSchema } from "@/lib/validations/contact";

// Este handler siempre se ejecuta en request-time (usa datos de la petición).
export const dynamic = "force-dynamic";

function getClientIp(request: Request): string {
  const forwarded = request.headers.get("x-forwarded-for");
  if (forwarded) {
    return forwarded.split(",")[0]!.trim();
  }
  return request.headers.get("x-real-ip") ?? "127.0.0.1";
}

export async function POST(request: Request) {
  // 1) Rate limiting por IP (degrada seguro si no hay Upstash configurado).
  const ip = getClientIp(request);
  const rate = await limitByIp(ip);
  if (!rate.success) {
    return NextResponse.json(
      { error: "Demasiadas solicitudes. Inténtalo de nuevo en un minuto." },
      { status: 429 },
    );
  }

  // 2) Parseo del cuerpo.
  let body: unknown;
  try {
    body = await request.json();
  } catch {
    return NextResponse.json({ error: "Cuerpo de la solicitud inválido." }, { status: 400 });
  }

  // 3) Validación con zod.
  const parsed = contactSchema.safeParse(body);
  if (!parsed.success) {
    return NextResponse.json(
      { error: "Datos inválidos.", issues: parsed.error.flatten().fieldErrors },
      { status: 422 },
    );
  }

  // 4) Honeypot: si viene relleno, fingimos éxito sin enviar nada.
  if (parsed.data.website) {
    return NextResponse.json({ ok: true }, { status: 200 });
  }

  // 5) Envío del correo.
  const result = await sendContactEmail(parsed.data);
  if (!result.ok) {
    return NextResponse.json(
      { error: "No se pudo enviar el mensaje. Inténtalo más tarde." },
      { status: 502 },
    );
  }

  return NextResponse.json({ ok: true }, { status: 200 });
}
