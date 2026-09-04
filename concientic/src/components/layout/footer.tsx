import { footer } from "@/content/site";

export function Footer() {
  return (
    <footer className="site-footer">
      <div className="ct-container footer-grid">
        <div>
          <strong>ConcienTIC</strong> · {footer.tagline}
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
