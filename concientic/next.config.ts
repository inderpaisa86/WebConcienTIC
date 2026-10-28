import type { NextConfig } from "next";

// El frontend de SIGINEX vive como rutas NATIVAS de concientic
// (`src/app/siginex/*`) y consume la API Java a través del BFF same-origin
// (`src/app/siginex/api/*`). El rewrite/proxy a la app Siginex separada se
// retiró en la sub-fase 3e.
//
// Fase 6 (endurecimiento): se añaden cabeceras de seguridad a TODO el sitio.
// Las cabeceras "firmes" van en modo enforce (no rompen una app Next normal).
// La CSP, en cambio, va en `Content-Security-Policy-Report-Only`: concientic
// usa Next.js (que inyecta scripts), analytics y posibles estilos/ís inline, y
// una CSP estricta en enforce puede romper la página. En report-only el
// navegador NO bloquea nada: solo reporta violaciones, lo que permite observar
// qué habría que permitir antes de pasarla a enforce.
//
// Cómo endurecer (pasar CSP a enforce) más adelante:
//   1. Observar violaciones (consola del navegador o un endpoint report-to).
//   2. Ajustar `script-src`/`style-src`/`connect-src` para cubrir lo legítimo
//      (p. ej. el dominio de analytics, el origen de la API si deja de ser
//      same-origin). Evitar `unsafe-inline` en `script-src`; para estilos de
//      Next suele requerirse `'unsafe-inline'` en `style-src` o nonces.
//   3. Renombrar la cabecera de `Content-Security-Policy-Report-Only` a
//      `Content-Security-Policy` (cambiar `key` abajo) y volver a verificar.

/**
 * CSP en REPORT-ONLY. Permisiva en lo que Next necesita para no reportar ruido
 * imposible, pero sin `unsafe-eval`. `connect-src 'self'` cubre el BFF
 * same-origin (`/siginex/api/*`); si en el futuro el navegador llamara directo
 * a la API Java (no es el caso hoy), habría que añadir su origen aquí.
 */
const CSP_REPORT_ONLY = [
  "default-src 'self'",
  "base-uri 'self'",
  "object-src 'none'",
  "frame-ancestors 'none'",
  "form-action 'self'",
  // Next inyecta scripts; 'unsafe-inline' se tolera SOLO en report-only para no
  // generar ruido. Al endurecer, sustituir por nonces/hashes.
  "script-src 'self' 'unsafe-inline'",
  "style-src 'self' 'unsafe-inline'",
  "img-src 'self' data: blob:",
  "font-src 'self' data:",
  "connect-src 'self'",
  "frame-src 'self'",
  "upgrade-insecure-requests",
].join("; ");

/**
 * Permissions-Policy restrictiva: desactiva APIs potentes que el sitio no usa.
 */
const PERMISSIONS_POLICY = [
  "camera=()",
  "microphone=()",
  "geolocation=()",
  "payment=()",
  "usb=()",
  "magnetometer=()",
  "gyroscope=()",
  "accelerometer=()",
  "interest-cohort=()",
].join(", ");

const SECURITY_HEADERS = [
  // HSTS: fuerza HTTPS durante 2 años, incluye subdominios y preload.
  {
    key: "Strict-Transport-Security",
    value: "max-age=63072000; includeSubDomains; preload",
  },
  { key: "X-Content-Type-Options", value: "nosniff" },
  { key: "X-Frame-Options", value: "DENY" },
  { key: "Referrer-Policy", value: "strict-origin-when-cross-origin" },
  { key: "Permissions-Policy", value: PERMISSIONS_POLICY },
  { key: "X-DNS-Prefetch-Control", value: "off" },
  // CSP en report-only (ver nota de arriba): NO bloquea, solo reporta.
  { key: "Content-Security-Policy-Report-Only", value: CSP_REPORT_ONLY },
];

const nextConfig: NextConfig = {
  async headers() {
    return [
      {
        // Aplica a TODO el sitio (incluye las rutas /siginex/* y el BFF).
        source: "/:path*",
        headers: SECURITY_HEADERS,
      },
    ];
  },
};

export default nextConfig;
