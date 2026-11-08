import type { Metadata } from "next";

import { PaginaCurso } from "../_paginas/pagina-curso";
import { navidadHtmlB64 } from "../_paginas/navidad.html";

export const metadata: Metadata = {
  title: "Navidad Mágica · Curso de Navidad en macramé · 50%",
  description:
    "Navidad Mágica: 7 proyectos navideños en macramé, incluido el Árbol de Navidad 3D. Decora, regala o vende esta temporada. 50% de descuento hasta el 30 de noviembre.",
  alternates: { canonical: "/elartequeconecta/navidad" },
  openGraph: {
    type: "website",
    locale: "es_ES",
    url: "/elartequeconecta/navidad",
    title: "Navidad Mágica · Curso de Navidad en macramé · 50%",
    description:
      "7 proyectos navideños en macramé, incluido el Árbol de Navidad 3D. 50% de descuento hasta el 30 de noviembre.",
    images: ["/elartequeconecta/assets/fotos/v3_3.webp"],
  },
};

export default function NavidadPage() {
  return <PaginaCurso htmlB64={navidadHtmlB64} />;
}
