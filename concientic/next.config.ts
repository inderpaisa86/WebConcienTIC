import type { NextConfig } from "next";

// El frontend de SIGINEX ahora vive como rutas NATIVAS de concientic
// (`src/app/siginex/*`) y consume la API Java a través del BFF same-origin
// (`src/app/siginex/api/*`). Por eso se RETIRÓ (sub-fase 3e) el rewrite/proxy
// que antes reenviaba `/siginex` a la app Siginex separada en Vercel; ya no se
// usa esa app como front destino. No se apaga su infraestructura (eso es Fase 6).
const nextConfig: NextConfig = {};

export default nextConfig;
