import Image from "next/image";

import { footer } from "@/content/site";

export function Footer() {
  return (
    <footer className="site-footer">
      <div className="ct-container footer-grid">
        <div className="footer-brand">
          <span className="footer-brand-mark">
            <Image
              src="/brand/concientic-nexus-wordmark-transparente-fondo-claro.png"
              alt="ConcienTIC Nexus — Conciencia Digital para un mundo mejor"
              fill
              sizes="250px"
            />
          </span>
        </div>
        <nav className="footer-links" aria-label="Pie de página">
          {footer.links.map((link) => (
            <a key={link.href} href={link.href}>
              {link.label}
            </a>
          ))}
        </nav>
      </div>
    </footer>
  );
}
