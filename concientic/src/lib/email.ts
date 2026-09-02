import { Resend } from "resend";

import { site } from "@/content/site";
import type { ContactInput } from "@/lib/validations/contact";

/**
 * Envío de correo del formulario de contacto con Resend.
 *
 * Variables de entorno requeridas en producción:
 *   - RESEND_API_KEY: API key de Resend.
 *   - CONTACT_FROM_EMAIL: remitente verificado en Resend (ej. "web@concientic.com").
 *   - CONTACT_TO_EMAIL (opcional): destino; por defecto site.contactEmail.
 */

const apiKey = process.env.RESEND_API_KEY;
const fromEmail = process.env.CONTACT_FROM_EMAIL;
const toEmail = process.env.CONTACT_TO_EMAIL ?? site.contactEmail;

const resend = apiKey ? new Resend(apiKey) : null;

export type SendResult = { ok: true } | { ok: false; error: string };

function escapeHtml(value: string): string {
  return value
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;")
    .replace(/'/g, "&#39;");
}

export async function sendContactEmail(data: ContactInput): Promise<SendResult> {
  if (!resend || !fromEmail) {
    return {
      ok: false,
      error: "El servicio de correo no está configurado.",
    };
  }

  const name = escapeHtml(data.name);
  const email = escapeHtml(data.email);
  const company = data.company ? escapeHtml(data.company) : "—";
  const message = escapeHtml(data.message).replace(/\n/g, "<br />");

  try {
    const { error } = await resend.emails.send({
      from: `${site.name} <${fromEmail}>`,
      to: [toEmail],
      replyTo: data.email,
      subject: `Nuevo contacto desde la web — ${name}`,
      html: `
        <h2>Nuevo mensaje de contacto</h2>
        <p><strong>Nombre:</strong> ${name}</p>
        <p><strong>Correo:</strong> ${email}</p>
        <p><strong>Empresa:</strong> ${company}</p>
        <p><strong>Mensaje:</strong></p>
        <p>${message}</p>
      `,
    });

    if (error) {
      return { ok: false, error: error.message };
    }
    return { ok: true };
  } catch (err) {
    const msg = err instanceof Error ? err.message : "Error desconocido al enviar el correo.";
    return { ok: false, error: msg };
  }
}
