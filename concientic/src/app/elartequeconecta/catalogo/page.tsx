import type { Metadata } from "next";
import { notFound } from "next/navigation";

import { CATALOGO_LISTO } from "./config";
import { catalogoLdB64 } from "./_data/jsonld";
import { PaginaCatalogo } from "./pagina-catalogo";

const TITLE = "Catálogo de macramé hecho a mano | El Arte que Conecta";
const DESCRIPTION =
  "Bolsos, accesorios, decoración y piezas navideñas en macramé, tejidas a mano. Elige tu pieza y pídela por WhatsApp.";

export const metadata: Metadata = {
  title: TITLE,
  description: DESCRIPTION,
  alternates: { canonical: "/elartequeconecta/catalogo" },
  // Mientras el número de WhatsApp sea el placeholder, no se indexa.
  robots: CATALOGO_LISTO ? undefined : { index: false, follow: false },
  openGraph: {
    type: "website",
    locale: "es_ES",
    url: "/elartequeconecta/catalogo",
    title: TITLE,
    description: DESCRIPTION,
    images: ["/elartequeconecta/assets/catalogo/bol-santorini.webp"],
  },
};

export default function CatalogoPage() {
  // Sin WhatsApp real, la página no se muestra en producción (404). En
  // desarrollo sigue visible para poder revisarla.
  if (!CATALOGO_LISTO && process.env.NODE_ENV === "production") {
    notFound();
  }

  const jsonLd = Buffer.from(catalogoLdB64, "base64").toString("utf-8");

  return (
    <>
      <script type="application/ld+json" dangerouslySetInnerHTML={{ __html: jsonLd }} />
      <PaginaCatalogo />
    </>
  );
}
