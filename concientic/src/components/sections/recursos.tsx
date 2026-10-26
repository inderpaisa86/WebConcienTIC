import { recursos } from "@/content/site";

export function Recursos() {
  return (
    <section id={recursos.id} className="recursos-section">
      <div className="ct-container">
        <div className="section-head">
          <span className="eyebrow eyebrow--green-dark">{recursos.eyebrow}</span>
          <h2>
            Contenido que orienta.
            <br />
            Experiencias que transforman.
          </h2>
          <p>{recursos.intro}</p>
        </div>
        <div className="offer-grid">
          {recursos.items.map((item) => (
            <article
              className={`offer${item.featured ? " featured" : ""}`}
              key={item.title}
            >
              <span className="num">{item.number}</span>
              <h3>{item.title}</h3>
              <p>{item.description}</p>
              {item.cta ? (
                <a
                  className={item.featured ? "offer-cta offer-cta--purple" : "offer-cta"}
                  href={item.cta.href}
                  aria-label={item.cta.ariaLabel ?? undefined}
                  target={item.cta.external ? "_blank" : undefined}
                  rel={item.cta.external ? "noopener" : undefined}
                >
                  {item.cta.label}
                </a>
              ) : null}
            </article>
          ))}
        </div>
      </div>
    </section>
  );
}
