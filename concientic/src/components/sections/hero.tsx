import { hero } from "@/content/site";

export function Hero() {
  return (
    <section id="inicio" className="hero-section">
      <div className="ct-container hero-grid">
        <div className="hero-copy">
          <span className="pill">
            <span className="dot" aria-hidden="true" /> {hero.eyebrow}
          </span>
          <h1>
            {hero.titleLead} <em>{hero.titleEmphasis}</em>
          </h1>
          <p className="hero-lead">{hero.subtitle}</p>
          <div className="hero-actions">
            <a className="ct-button ct-button--primary" href={hero.primaryCta.href}>
              {hero.primaryCta.label}
            </a>
            <a className="ct-button ct-button--secondary" href={hero.secondaryCta.href}>
              {hero.secondaryCta.label}
            </a>
          </div>
          <p className="hero-note">{hero.closing}</p>
        </div>
      </div>
    </section>
  );
}
