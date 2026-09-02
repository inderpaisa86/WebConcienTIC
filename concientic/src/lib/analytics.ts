/**
 * Helpers de analítica GA4 (client-side).
 *
 * Seguros de llamar aunque GA no esté cargado (dev o sin NEXT_PUBLIC_GA_ID): si `gtag` no
 * existe, no hacen nada.
 */

type GtagFn = (...args: unknown[]) => void;

declare global {
  interface Window {
    gtag?: GtagFn;
    dataLayer?: unknown[];
  }
}

function gtag(...args: unknown[]): void {
  if (typeof window !== "undefined" && typeof window.gtag === "function") {
    window.gtag(...args);
  }
}

/** Llamar cuando el usuario acepte cookies en el banner de consentimiento. */
export function grantAnalyticsConsent(): void {
  gtag("consent", "update", {
    analytics_storage: "granted",
  });
}

/** Llamar si el usuario rechaza. */
export function denyAnalyticsConsent(): void {
  gtag("consent", "update", {
    analytics_storage: "denied",
  });
}

/** Evento de clic en un CTA. `location` describe dónde está el CTA (hero, header, ...). */
export function trackCtaClick(location: string): void {
  gtag("event", "cta_click", { location });
}

/** Evento de conversión: formulario de contacto enviado con éxito. */
export function trackLeadSubmitted(): void {
  gtag("event", "lead_submitted");
}

export {};
