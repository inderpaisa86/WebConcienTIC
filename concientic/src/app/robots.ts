import type { MetadataRoute } from "next";

import { site } from "@/content/site";

export default function robots(): MetadataRoute.Robots {
  return {
    rules: {
      userAgent: "*",
      allow: "/",
      // La página de validación del design system no debe indexarse.
      disallow: ["/design-system"],
    },
    sitemap: `${site.domain}/sitemap.xml`,
    host: site.domain,
  };
}
