import { footer, site } from "@/content/site";

export function Footer() {
  const year = new Date().getFullYear();

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
