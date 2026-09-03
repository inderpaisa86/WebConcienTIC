import Image from "next/image";

import { footer, navLinks, site } from "@/content/site";

export function Footer() {
  const year = new Date().getFullYear();

  return (
    <footer className="site-footer">
      <div className="ct-container">
        <div className="footer-grid">
          <div className="footer-brand">
            <Image src="/brand/logo-concientic.png" alt="ConcienTIC" width={280} height={107} />
            <p>{site.tagline}</p>
          </div>
          <nav className="footer-links" aria-label="Pie de página">
            {navLinks.map((link) => (
              <a key={link.href} href={link.href}>
                {link.label}
              </a>
            ))}
            {footer.links.map((link) => (
              <a key={link.href} href={link.href}>
                {link.label}
              </a>
            ))}
            {site.social.linkedin ? (
              <a href={site.social.linkedin} target="_blank" rel="noopener noreferrer">
                LinkedIn
              </a>
            ) : null}
          </nav>
        </div>
        <p className="footer-bottom">
          © {year} {footer.legalName}. {footer.rights}
        </p>
      </div>
    </footer>
  );
}
