import type { CSSProperties } from "react";

import { dquilibrio } from "@/content/site";

export function Dquilibrio() {
  return (
    <section id={dquilibrio.id} className="philosophy-section">
      <div className="ct-container philosophy-wrap">
        <div className="philosophy-panel">
          <div className="section-head">
            <span className="eyebrow eyebrow--green-dark">{dquilibrio.eyebrow}</span>
            <h2>{dquilibrio.title}</h2>
            <p>{dquilibrio.description}</p>
          </div>
          <div className="philosophy-cards">
            {dquilibrio.items.map((item) => (
              <article
                className="philosophy-card"
                key={item.number}
                style={{ "--accent": item.accent } as CSSProperties}
              >
                <span className="num">{item.number}</span>
                <h3>{item.title}</h3>
                <p>{item.description}</p>
                <a className="card-cta" href={item.href}>
                  {item.cta}
                </a>
              </article>
            ))}
          </div>
        </div>
      </div>
    </section>
  );
}
