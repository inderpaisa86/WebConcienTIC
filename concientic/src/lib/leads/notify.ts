import { Resend } from "resend";

import type { LeadInput } from "@/lib/validations/lead";

/**
 * Aviso por correo de cada lead de la landing de SIGINEX (Resend).
 *
 * Variables de entorno:
 *   - RESEND_API_KEY: API key de Resend (obligatoria para enviar).
 *   - CONTACT_FROM_EMAIL: remitente verificado en Resend (obligatoria para enviar).
 *   - LEADS_NOTIFY_TO (opcional): destino del aviso. Por defecto concientic@gmail.com.
 *
 * Sin RESEND_API_KEY o CONTACT_FROM_EMAIL, el envío NO se simula: se devuelve error.
 */

export const LEADS_NOTIFY_TO_DEFAULT = "concientic@gmail.com";

export type NotifyResult = { ok: true } | { ok: false; error: string };

function escapeHtml(valor: string): string {
  return valor
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;")
    .replace(/'/g, "&#39;");
}

export function isLeadNotifyConfigured(): boolean {
  return !!process.env.RESEND_API_KEY && !!process.env.CONTACT_FROM_EMAIL;
}

export async function notifyLead(lead: LeadInput): Promise<NotifyResult> {
  const apiKey = process.env.RESEND_API_KEY;
  const fromEmail = process.env.CONTACT_FROM_EMAIL;
  if (!apiKey || !fromEmail) {
    return { ok: false, error: "El servicio de correo no está configurado." };
  }

  const toEmail = process.env.LEADS_NOTIFY_TO || LEADS_NOTIFY_TO_DEFAULT;
  const resend = new Resend(apiKey);

  const empresa = escapeHtml(lead.empresa);
  const email = escapeHtml(lead.email);
  const celular = escapeHtml(lead.celular);
  const plan = escapeHtml(lead.plan);
  const origen = escapeHtml(lead.origen ?? "landing-siginex");

  try {
    const { error } = await resend.emails.send({
      from: `SIGINEX Landing <${fromEmail}>`,
      to: [toEmail],
      replyTo: lead.email,
      subject: `Nuevo lead SIGINEX (${lead.plan}) — ${lead.empresa}`,
      html: `
        <h2>Nuevo lead desde la landing de SIGINEX</h2>
        <p><strong>Plan de interés:</strong> ${plan}</p>
        <p><strong>Empresa:</strong> ${empresa}</p>
        <p><strong>Correo:</strong> ${email}</p>
        <p><strong>Celular:</strong> ${celular}</p>
        <p><strong>Autorización de datos (Ley 1581 de 2012):</strong> sí</p>
        <p><strong>Versión de la política aceptada:</strong> ${escapeHtml(lead.politica_version)}</p>
        <p><strong>Origen:</strong> ${origen}</p>
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
