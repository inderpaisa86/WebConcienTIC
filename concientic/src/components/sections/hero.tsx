import Image from "next/image";

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

        <div className="hero-visual">
          <Image
            src="/diagrams/equilibrio-concientic-mejorado.png"
            alt="DQUILIBRIO: atención, IA y tecnología, criterio, autonomía y humanidad en equilibrio"
            width={600}
            height={600}
            priority
            sizes="(max-width: 700px) 100vw, (max-width: 1100px) 80vw, 600px"
          />
        </div>
      </div>
    </section>
  );
}
