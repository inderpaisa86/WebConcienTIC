import { footer, navLinks, site } from "@/content/site";

export function Footer() {
  const year = new Date().getFullYear();

  return (
    <footer className="border-t border-border bg-background">
      <div className="mx-auto flex w-full max-w-6xl flex-col gap-8 px-6 py-12 md:px-8">
        <div className="flex flex-col justify-between gap-8 md:flex-row">
          <div className="flex flex-col gap-2">
            <span className="font-heading text-lg font-bold">{site.name}</span>
            <p className="max-w-xs text-sm text-muted-foreground">{site.tagline}</p>
          </div>

          <nav aria-label="Pie de página" className="flex flex-col gap-2">
            {navLinks.map((link) => (
              <a
                key={link.href}
                href={link.href}
                className="text-sm text-muted-foreground transition-colors hover:text-foreground"
              >
                {link.label}
              </a>
            ))}
          </nav>

          <div className="flex flex-col gap-2">
            {footer.links.map((link) => (
              <a
                key={link.href}
                href={link.href}
                className="text-sm text-muted-foreground transition-colors hover:text-foreground"
              >
                {link.label}
              </a>
            ))}
            {site.social.linkedin ? (
              <a
                href={site.social.linkedin}
                target="_blank"
                rel="noopener noreferrer"
                className="text-sm text-muted-foreground transition-colors hover:text-foreground"
              >
                LinkedIn
              </a>
            ) : null}
          </div>
        </div>

        <p className="text-xs text-muted-foreground">
          © {year} {footer.legalName}. {footer.rights}
        </p>
      </div>
    </footer>
  );
}
