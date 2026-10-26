import { ecosistema } from "@/content/site";
import { SectionHeader } from "@/components/ui/section-header";

export function Ecosistema() {
  return (
    <section id={ecosistema.id} className="ct-section">
      <div className="ct-container">
        <SectionHeader
          eyebrow={ecosistema.eyebrow}
          title={ecosistema.title}
          description={ecosistema.intro}
        />
        <div className="card-grid card-grid--three">
          {ecosistema.items.map((item) => (
            <article className="info-card" key={item.name}>
              <span className="info-card__number">{item.number}</span>
              <h3>{item.name}</h3>
              <p>{item.description}</p>
              <a className="info-card__cta" href={item.cta.href}>
                {item.cta.label}
              </a>
            </article>
          ))}
        </div>
        <p className="ecosistema-honesty">{ecosistema.honesty}</p>
      </div>
    </section>
  );
}
