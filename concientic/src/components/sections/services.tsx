import { services } from "@/content/site";
import { SectionHeader } from "@/components/ui/section-header";

export function Services() {
  return (
    <section id={services.id} className="ct-section">
      <div className="ct-container">
        <SectionHeader
          eyebrow={services.eyebrow}
          title={services.title}
          description={services.subtitle}
        />
        <div className="card-grid card-grid--five">
          {services.items.map((item) => (
            <article className="service-card" key={item.title}>
              <h3>{item.title}</h3>
              <p>{item.description}</p>
              <span className="service-card__tag">{item.tag}</span>
            </article>
          ))}
        </div>
      </div>
    </section>
  );
}
