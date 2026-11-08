import type { Metadata } from "next";

import { PaginaCurso } from "../_paginas/pagina-curso";
import { escuelaHtmlB64 } from "../_paginas/escuela.html";

export const metadata: Metadata = {
  title: "Escuela de Macramé: aprende y emprende · 50% en Noviembre Black",
  description:
    "Aprende macramé desde cero con la Escuela de Macramé: proyectos paso a paso en video, ebooks, comunidad y acceso de por vida. 50% de descuento hasta el 30 de noviembre.",
  alternates: { canonical: "/elartequeconecta/escuela" },
  openGraph: {
    type: "website",
    locale: "es_ES",
    url: "/elartequeconecta/escuela",
    title: "Escuela de Macramé: aprende y emprende · 50% en Noviembre Black",
    description:
      "Aprende macramé desde cero con la Escuela de Macramé: proyectos paso a paso en video, ebooks, comunidad y acceso de por vida.",
    images: ["/elartequeconecta/assets/fotos/v3_1.webp"],
  },
};

export default function EscuelaPage() {
  return <PaginaCurso htmlB64={escuelaHtmlB64} />;
}
