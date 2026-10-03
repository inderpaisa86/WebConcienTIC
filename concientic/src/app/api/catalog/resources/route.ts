const BACKEND_URL = process.env.CATALOG_API_URL ?? "https://webconcientic.onrender.com";
const ALLOWED_PARAMETERS = ["q", "competency", "provider", "language", "level", "format", "page", "size"];

export const dynamic = "force-dynamic";

export async function GET(request: Request) {
  const requestUrl = new URL(request.url);
  const backendUrl = new URL("/api/v1/resources", BACKEND_URL);

  for (const parameter of ALLOWED_PARAMETERS) {
    const value = requestUrl.searchParams.get(parameter);
    if (value) backendUrl.searchParams.set(parameter, value);
  }

  try {
    const response = await fetch(backendUrl, {
      cache: "no-store",
      headers: { Accept: "application/json" },
    });
    const body = await response.text();

    return new Response(body, {
      status: response.status,
      headers: { "Content-Type": response.headers.get("Content-Type") ?? "application/json" },
    });
  } catch {
    return Response.json(
      { message: "El catálogo no está disponible temporalmente." },
      { status: 502 },
    );
  }
}
