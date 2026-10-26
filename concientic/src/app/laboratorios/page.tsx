import type { Metadata } from "next";
import Link from "next/link";

import { LabCardGrid } from "@/components/laboratorios/lab-card-grid";
import { laboratorios } from "@/content/laboratorios";

const { hero, labs, enfoque, finalCta } = laboratorios;

export const metadata: Metadata = {
  title: laboratorios.meta.title,
  description: laboratorios.meta.description,
};

export default function LaboratoriosPage() {
  return (
    <div className="labs-page">
      <section className="hero" id={hero.id}>
        <div className="ct-container">
          <div className="hero-content">
            <span className="pill">
              <span className="dot" /> {hero.pill}
            </span>
            <h1>
              {hero.titleLead} <em>{hero.titleEmphasis}</em>
            </h1>
            <p className="hero-copy">{hero.copy}</p>
            <div className="hero-actions">
              <a className="ct-button ct-button--primary" href={hero.primaryCta.href}>
                {hero.primaryCta.label} <span aria-hidden="true">{hero.primaryCta.arrow}</span>
              </a>
              <Link className="ct-button ct-button--secondary" href={hero.secondaryCta.href}>
                {hero.secondaryCta.label}
              </Link>
            </div>
            <p className="hero-note">{hero.note}</p>
          </div>
        </div>
      </section>

      <section className="section-tint" id={labs.id}>
        <div className="ct-container">
          <div className="section-head">
            <span className="eyebrow section-kicker">{labs.eyebrow}</span>
            <h2>
              {labs.titleLead}
              <br />
              {labs.titleTail}
            </h2>
            <p>{labs.intro}</p>
          </div>
          <LabCardGrid />
          <div className="intro-band">
            {labs.introBand.map((panel) => (
              <div
                className={`intro-panel${panel.variant ? ` ${panel.variant}` : ""}`}
                key={panel.title}
              >
                <div className="mini-label">{panel.miniLabel}</div>
                <h3>{panel.title}</h3>
                <p>{panel.description}</p>
              </div>
            ))}
          </div>
        </div>
      </section>

      <section id={enfoque.id}>
        <div className="ct-container principle">
          <div>
            <span className="eyebrow section-kicker">{enfoque.eyebrow}</span>
            <h2>{enfoque.title}</h2>
            <p>{enfoque.paragraph}</p>
            <Link className="ct-button ct-button--secondary" href={enfoque.cta.href}>
              {enfoque.cta.label}
            </Link>
          </div>
          <div className="principle-cards">
            {enfoque.cards.map((card) => (
              <div className="principle-card" key={card.title}>
                <strong>{card.title}</strong>
                <span>{card.description}</span>
              </div>
            ))}
          </div>
        </div>
      </section>

      <section className="final-cta">
        <div className="ct-container final-inner">
          <div>
            <span className="eyebrow" style={{ color: "#008f70" }}>
              {finalCta.eyebrow}
            </span>
            <h2>{finalCta.title}</h2>
            <p>{finalCta.paragraph}</p>
          </div>
          <a className="ct-button ct-button--primary" href={finalCta.cta.href}>
            {finalCta.cta.label}
          </a>
        </div>
      </section>
    </div>
  );
}
