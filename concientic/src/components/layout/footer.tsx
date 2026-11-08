"use client";

import { usePathname } from "next/navigation";

import { footer, site } from "@/content/site";

/** Rutas que se renderizan de forma autónoma, sin el pie del sitio. */
const STANDALONE_ROUTES = ["/elartequeconecta"];

export function Footer() {
  const year = new Date().getFullYear();
  const pathname = usePathname();

  if (STANDALONE_ROUTES.some((route) => pathname === route || pathname.startsWith(`${route}/`))) {
    return null;
  }

  return (
    <footer className="site-footer">
      <div className="ct-container footer-grid">
        <a className="footer-brand brand" href="/" aria-label={`${site.name}, inicio`}>
          <span className="brand-wordmark">
            concienTIC
            <small>INTELIGENCIA DIGITAL CONSCIENTE</small>
          </span>
        </a>
        <nav className="footer-links" aria-label="Pie de página">
          {footer.links.map((link) => (
            <a key={link.href} href={link.href}>
              {link.label}
            </a>
          ))}
        </nav>
        <p className="footer-copyright">© {year} ConcienTIC</p>
      </div>
    </footer>
  );
}
