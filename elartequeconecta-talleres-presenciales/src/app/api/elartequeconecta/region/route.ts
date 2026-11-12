// Devuelve la ubicación aproximada del visitante a partir de las cabeceras de geolocalización de Vercel.
// No guarda IP ni datos personales. La respuesta no se cachea porque cambia por visitante.

export const dynamic = 'force-dynamic';

export function GET(request: Request) {
  const h = request.headers;
  const ciudadRaw = h.get('x-vercel-ip-city') ?? '';
  let ciudad = ciudadRaw;
  try {
    ciudad = decodeURIComponent(ciudadRaw); // Vercel codifica caracteres no ASCII (Ibagu%C3%A9)
  } catch {}

  return Response.json(
    {
      pais: h.get('x-vercel-ip-country'), // CO
      region: h.get('x-vercel-ip-country-region'), // TOL = Tolima
      ciudad, // Ibagué
    },
    { headers: { 'Cache-Control': 'private, no-store' } },
  );
}
