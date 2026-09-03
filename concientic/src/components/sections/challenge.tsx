import { challenge } from "@/content/site";
import { SectionHeader } from "@/components/ui/section-header";

export function Challenge() {
  return (
    <section id={challenge.id} className="ct-section ct-section--soft">
      <div className="ct-container">
        <SectionHeader
          eyebrow={challenge.eyebrow}
          title={challenge.title}
          description={challenge.description}
        />
        <div className="card-grid card-grid--four">
          {challenge.items.map((item) => (
            <article className="info-card" key={item.number}>
              <span className="info-card__number">{item.number}</span>
              <h3>{item.title}</h3>
              <p>{item.description}</p>
            </article>
          ))}
        </div>
      </div>
    </section>
  );
}
