import type { Metadata } from "next";

import { PaginaCurso } from "../_paginas/pagina-curso";
import { bolsosHtmlB64 } from "../_paginas/bolsos.html";

export const metadata: Metadata = {
  title: "Curso de Bolsos en macramé · 50% en Noviembre Black",
  description:
    "8 proyectos de bolsos y accesorios en macramé: Maxibolso, Tula Verona, Maxitula, Bolso París, Bahía, Santorini, Accesorio Corazón e Infinito. 50% con el cupón BLACKNOV50.",
  alternates: { canonical: "/elartequeconecta/bolsos" },
  openGraph: {
    type: "website",
    locale: "es_ES",
    url: "/elartequeconecta/bolsos",
    title: "Curso de Bolsos en macramé · 50% en Noviembre Black",
    description:
      "8 proyectos de bolsos y accesorios en macramé. 50% de descuento hasta el 30 de noviembre.",
    images: ["/elartequeconecta/assets/fotos/v3_2.webp"],
  },
};

export default function BolsosPage() {
  return <PaginaCurso htmlB64={bolsosHtmlB64} />;
}
