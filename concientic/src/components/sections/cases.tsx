import { cases } from "@/content/site";
import { SectionHeader } from "@/components/ui/section-header";

export function Cases() {
  return (
    <section id={cases.id} className="ct-section cases-section">
      <div className="ct-container">
        <SectionHeader eyebrow={cases.eyebrow} title={cases.title} description={cases.subtitle} />
        <div className="card-grid card-grid--three">
          {cases.items.map((item) => (
            <article className="case-card" key={item.title}>
              <span className="case-card__status">{item.status}</span>
              <h3>{item.title}</h3>
              <p>{item.description}</p>
            </article>
          ))}
        </div>
      </div>
    </section>
  );
}
