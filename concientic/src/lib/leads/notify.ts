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
export const LEADS_SUBJECT = "Contactame Siginex";

export type NotifyResult = { ok: true } | { ok: false; error: string };

// Paleta de la marca ConcienTIC (src/app/globals.css).
const C = {
  primary: "#00c49a",
  greenDark: "#008f70",
  dark: "#0d182a",
  soft: "#f5f7f3",
  muted: "#75847a",
  text: "#17231e",
  line: "#dce3dd",
  white: "#ffffff",
  chipBg: "#e3f8f2",
};

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

function filaDato(etiqueta: string, valorHtml: string): string {
  return `
          <tr>
            <td style="padding:14px 0;border-bottom:1px solid ${C.line};width:38%;vertical-align:top;font-family:Arial,Helvetica,sans-serif;font-size:12px;letter-spacing:0.08em;text-transform:uppercase;color:${C.muted};">${etiqueta}</td>
            <td style="padding:14px 0;border-bottom:1px solid ${C.line};vertical-align:top;font-family:Arial,Helvetica,sans-serif;font-size:16px;color:${C.text};word-break:break-word;">${valorHtml}</td>
          </tr>`;
}

export function renderLeadHtml(lead: LeadInput): string {
  const empresa = escapeHtml(lead.empresa);
  const email = escapeHtml(lead.email);
  const celular = escapeHtml(lead.celular);
  const plan = escapeHtml(lead.plan);
  const politica = escapeHtml(lead.politica_version);
  const origen = escapeHtml(lead.origen ?? "landing-siginex");
  const telHref = `tel:+57${escapeHtml(lead.celular)}`;
  const mailHref = `mailto:${escapeHtml(lead.email)}?subject=${encodeURIComponent("Re: Contactame Siginex")}`;

  return `<!doctype html>
<html lang="es">
  <head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1" />
    <title>${LEADS_SUBJECT}</title>
  </head>
  <body style="margin:0;padding:0;background:${C.soft};">
    <table role="presentation" width="100%" cellpadding="0" cellspacing="0" style="background:${C.soft};padding:32px 12px;">
      <tr>
        <td align="center">
          <table role="presentation" width="600" cellpadding="0" cellspacing="0" style="width:100%;max-width:600px;background:${C.white};border-radius:18px;overflow:hidden;border:1px solid ${C.line};">

            <!-- Encabezado -->
            <tr>
              <td style="background:${C.dark};padding:28px 32px;">
                <p style="margin:0 0 6px 0;font-family:Arial,Helvetica,sans-serif;font-size:12px;font-weight:bold;letter-spacing:0.14em;text-transform:uppercase;color:${C.primary};">ConcienTIC · SIGINEX</p>
                <h1 style="margin:0;font-family:Arial,Helvetica,sans-serif;font-size:24px;line-height:1.3;font-weight:bold;color:${C.white};">Nuevo contacto desde la landing</h1>
              </td>
            </tr>
            <tr>
              <td style="height:4px;background:${C.primary};line-height:4px;font-size:0;">&nbsp;</td>
            </tr>

            <!-- Cuerpo -->
            <tr>
              <td style="padding:28px 32px 8px 32px;">
                <p style="margin:0 0 16px 0;font-family:Arial,Helvetica,sans-serif;font-size:16px;line-height:1.55;color:${C.text};">
                  Una persona pidió que la contactes para conocer SIGINEX. Estos son sus datos:
                </p>
                <span style="display:inline-block;margin:0 0 20px 0;padding:6px 14px;border-radius:999px;background:${C.chipBg};font-family:Arial,Helvetica,sans-serif;font-size:13px;font-weight:bold;color:${C.greenDark};">Plan de interés: ${plan}</span>

                <table role="presentation" width="100%" cellpadding="0" cellspacing="0" style="border-top:1px solid ${C.line};">${filaDato("Empresa", empresa)}${filaDato("Correo", `<a href="mailto:${email}" style="color:${C.greenDark};text-decoration:none;">${email}</a>`)}${filaDato("Celular", `<a href="${telHref}" style="color:${C.greenDark};text-decoration:none;">${celular}</a>`)}
                </table>
              </td>
            </tr>

            <!-- Acción -->
            <tr>
              <td align="left" style="padding:24px 32px 32px 32px;">
                <a href="${mailHref}" style="display:inline-block;padding:14px 24px;border-radius:12px;background:${C.greenDark};font-family:Arial,Helvetica,sans-serif;font-size:16px;font-weight:bold;color:${C.white};text-decoration:none;">Responder a ${empresa}</a>
              </td>
            </tr>

            <!-- Pie -->
            <tr>
              <td style="background:${C.soft};padding:20px 32px;border-top:1px solid ${C.line};">
                <p style="margin:0 0 8px 0;font-family:Arial,Helvetica,sans-serif;font-size:12px;line-height:1.5;color:${C.muted};">
                  Autorización de tratamiento de datos personales conforme a la Ley 1581 de 2012. Versión de la política aceptada: ${politica}.
                </p>
                <p style="margin:0;font-family:Arial,Helvetica,sans-serif;font-size:12px;line-height:1.5;color:${C.muted};">
                  Origen: ${origen}. Mensaje enviado automáticamente desde concientic.com.
                </p>
              </td>
            </tr>
          </table>
        </td>
      </tr>
    </table>
  </body>
</html>`;
}

export function renderLeadText(lead: LeadInput): string {
  return [
    "Nuevo contacto desde la landing de SIGINEX",
    "",
    `Plan de interés: ${lead.plan}`,
    `Empresa: ${lead.empresa}`,
    `Correo: ${lead.email}`,
    `Celular: ${lead.celular}`,
    "",
    `Autorización de datos (Ley 1581 de 2012). Versión de la política: ${lead.politica_version}.`,
    `Origen: ${lead.origen ?? "landing-siginex"}.`,
  ].join("\n");
}

export async function notifyLead(lead: LeadInput): Promise<NotifyResult> {
  const apiKey = process.env.RESEND_API_KEY;
  const fromEmail = process.env.CONTACT_FROM_EMAIL;
  if (!apiKey || !fromEmail) {
    return { ok: false, error: "El servicio de correo no está configurado." };
  }

  const toEmail = process.env.LEADS_NOTIFY_TO || LEADS_NOTIFY_TO_DEFAULT;
  const resend = new Resend(apiKey);

  try {
    const { error } = await resend.emails.send({
      from: `ConcienTIC SIGINEX <${fromEmail}>`,
      to: [toEmail],
      replyTo: lead.email,
      subject: LEADS_SUBJECT,
      html: renderLeadHtml(lead),
      text: renderLeadText(lead),
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
