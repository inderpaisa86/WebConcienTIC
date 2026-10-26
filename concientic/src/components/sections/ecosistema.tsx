import Link from "next/link";

import { ecosistema } from "@/content/site";

export function Ecosistema() {
  return (
    <section id={ecosistema.id} className="ct-section ecosistema-section">
      <div className="ct-container">
        <div className="section-head">
          <span className="eyebrow eyebrow--green-dark">{ecosistema.eyebrow}</span>
          <h2>
            Tres territorios.
            <br />
            Un propósito compartido.
          </h2>
          <p>{ecosistema.intro}</p>
        </div>
        <div className="feature-grid">
          {ecosistema.items.map((item) => (
            <article
              className={`feature-card${item.variant ? ` ${item.variant}` : ""}`}
              key={item.name}
            >
              <span className="tag">{item.number}</span>
              <h3>{item.name}</h3>
              <p>{item.description}</p>
              {item.cta.href.startsWith("/") ? (
                <Link href={item.cta.href}>{item.cta.label}</Link>
              ) : (
                <a href={item.cta.href}>{item.cta.label}</a>
              )}
            </article>
          ))}
        </div>
        <p className="small-note">{ecosistema.honesty}</p>
      </div>
    </section>
  );
}
