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

// Override solo para esta página: el video del Árbol de Navidad 3D ocupa todo
// el ancho de la sección y se ve sin recortes. El reproductor de Google Drive
// ajusta el video dentro del marco (con bandas si hace falta) en vez de
// recortarlo, así que quitamos el tope de ancho y el aspect-ratio 16/9 fijo
// que lo recortaba, dándole un alto amplio.
const navidadExtraCss = `
  #video .video {
    max-width: 100%;
    width: 100%;
    aspect-ratio: auto;
    height: min(85vh, 900px);
  }
  #video .video iframe {
    width: 100%;
    height: 100%;
  }
`;

export default function NavidadPage() {
  return <PaginaCurso htmlB64={navidadHtmlB64} extraCss={navidadExtraCss} />;
}
