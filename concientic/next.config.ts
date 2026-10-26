import type { NextConfig } from "next";

// URL pública de la app Siginex (Next.js desplegada en Vercel).
// Configurable por entorno; cae al despliegue por defecto si no se define.
const SIGINEX_URL = (process.env.SIGINEX_URL ?? "https://siginex-gules.vercel.app").replace(
  /\/$/,
  "",
);

const nextConfig: NextConfig = {
  async rewrites() {
    return [
      // La app Siginex se sirve bajo /siginex mediante proxy (rewrite).
      // Para que funcione, Siginex debe tener basePath/assetPrefix = "/siginex"
      // en su propio next.config.ts, de modo que sus rutas internas y assets
      // (/_next/...) no colisionen con los de concientic.
      {
        source: "/siginex",
        destination: `${SIGINEX_URL}/siginex`,
      },
      {
        source: "/siginex/:path*",
        destination: `${SIGINEX_URL}/siginex/:path*`,
      },
    ];
  },
};

export default nextConfig;
