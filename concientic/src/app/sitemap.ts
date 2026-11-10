import type { MetadataRoute } from "next";

import { CATALOGO_LISTO } from "@/app/elartequeconecta/catalogo/config";
import { site } from "@/content/site";

export default function sitemap(): MetadataRoute.Sitemap {
  const now = new Date();
  return [
    {
      url: site.domain,
      lastModified: now,
      changeFrequency: "monthly",
      priority: 1,
    },
    {
      url: `${site.domain}/elartequeconecta`,
      lastModified: now,
      changeFrequency: "monthly",
      priority: 0.8,
    },
    {
      url: `${site.domain}/elartequeconecta/escuela`,
      lastModified: now,
      changeFrequency: "monthly",
      priority: 0.7,
    },
    {
      url: `${site.domain}/elartequeconecta/bolsos`,
      lastModified: now,
      changeFrequency: "monthly",
      priority: 0.7,
    },
    {
      url: `${site.domain}/elartequeconecta/navidad`,
      lastModified: now,
      changeFrequency: "monthly",
      priority: 0.7,
    },
    // El catálogo solo se anuncia a buscadores cuando el WhatsApp real está cargado.
    ...(CATALOGO_LISTO
      ? [
          {
            url: `${site.domain}/elartequeconecta/catalogo`,
            lastModified: now,
            changeFrequency: "monthly" as const,
            priority: 0.8,
          },
        ]
      : []),
    {
      url: `${site.domain}/privacidad`,
      lastModified: now,
      changeFrequency: "yearly",
      priority: 0.3,
    },
    {
      url: `${site.domain}/terminos`,
      lastModified: now,
      changeFrequency: "yearly",
      priority: 0.3,
    },
  ];
}
