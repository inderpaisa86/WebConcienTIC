import Script from "next/script";

/**
 * Google Analytics 4 con Consent Mode v2.
 *
 * Por defecto, el consentimiento para analytics/ads arranca en "denied" (cumple con
 * regulaciones de privacidad). Cuando el usuario acepte en el banner de cookies, llamar a
 * `grantAnalyticsConsent()` (ver events.ts) para actualizar a "granted".
 *
 * Variables de entorno:
 *   - NEXT_PUBLIC_GA_ID: ID de medición GA4 (ej. "G-XXXXXXXXXX").
 *     Usar IDs distintos para preview y producción.
 *
 * Si NEXT_PUBLIC_GA_ID no está definido (dev/build), no se inyecta nada.
 */
export function GoogleAnalytics() {
  const gaId = process.env.NEXT_PUBLIC_GA_ID;
  if (!gaId) return null;

  return (
    <>
      <Script
        id="ga-consent-default"
        strategy="afterInteractive"
        dangerouslySetInnerHTML={{
          __html: `
            window.dataLayer = window.dataLayer || [];
            function gtag(){dataLayer.push(arguments);}
            window.gtag = gtag;
            gtag('consent', 'default', {
              ad_storage: 'denied',
              ad_user_data: 'denied',
              ad_personalization: 'denied',
              analytics_storage: 'denied'
            });
          `,
        }}
      />
      <Script
        src={`https://www.googletagmanager.com/gtag/js?id=${gaId}`}
        strategy="afterInteractive"
      />
      <Script
        id="ga-init"
        strategy="afterInteractive"
        dangerouslySetInnerHTML={{
          __html: `
            gtag('js', new Date());
            gtag('config', '${gaId}');
          `,
        }}
      />
    </>
  );
}
