import { hero } from "@/content/site";

export function Hero() {
  return (
    <section id="inicio" className="hero-section">
      <div className="ct-container hero-grid">
        <div className="hero-copy">
          <p className="ct-eyebrow">{hero.eyebrow}</p>
          <h1>
            Aprender tecnología. <span>Sin perder humanidad.</span>
          </h1>
          <p>{hero.subtitle}</p>
          <div className="hero-actions">
            <a className="ct-button ct-button--primary" href={hero.primaryCta.href}>
              {hero.primaryCta.label}
            </a>
            <a className="ct-button ct-button--secondary" href={hero.secondaryCta.href}>
              {hero.secondaryCta.label}
            </a>
          </div>
        </div>

        <div
          className="hero-visual"
          aria-label="Visual de equilibrio entre atención, tecnología, criterio y autonomía"
          role="img"
        >
          <span className="hero-node hero-node--one">ATENCIÓN</span>
          <span className="hero-node hero-node--two">IA + TECH</span>
          <span className="hero-node hero-node--three">CRITERIO</span>
          <span className="hero-node hero-node--four">AUTONOMÍA</span>
          <span className="hero-node hero-node--five">HUMANIDAD</span>
          <div className="hero-core">
            <span>
              <strong>DQUI</strong>
              LIBRIO
            </span>
          </div>
        </div>
      </div>
    </section>
  );
}
