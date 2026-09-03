import { references } from "@/content/site";
import { SectionHeader } from "@/components/ui/section-header";

export function References() {
  return (
    <section id={references.id} className="ct-section">
      <div className="ct-container">
        <SectionHeader
          eyebrow={references.eyebrow}
          title={references.title}
          description={references.subtitle}
        />
        <div className="card-grid card-grid--three">
          {references.items.map((item) => (
            <article className="reference-card" key={item.title}>
              <span className="reference-card__type">{item.type}</span>
              <h3>{item.title}</h3>
              <p>{item.description}</p>
              <a href={item.href} target="_blank" rel="noopener noreferrer">
                Explorar referente <span aria-hidden="true">↗</span>
              </a>
            </article>
          ))}
        </div>
      </div>
    </section>
  );
}
