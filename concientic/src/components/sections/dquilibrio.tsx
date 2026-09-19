import { dquilibrio } from "@/content/site";
import { SectionHeader } from "@/components/ui/section-header";

export function Dquilibrio() {
  return (
    <section id={dquilibrio.id} className="ct-section">
      <div className="ct-container">
        <SectionHeader
          eyebrow={dquilibrio.eyebrow}
          title={dquilibrio.title}
          description={dquilibrio.description}
        />
        <div className="card-grid card-grid--four">
          {dquilibrio.items.map((item) => (
            <a
              className="info-card"
              key={item.number}
              href={item.href}
              target="_blank"
              rel="noopener noreferrer"
              aria-label={`Abrir la experiencia ${item.title} en una pestaña nueva`}
            >
              <span className="info-card__number">{item.number}</span>
              <h3>{item.title}</h3>
              <p>{item.description}</p>
            </a>
          ))}
        </div>
      </div>
    </section>
  );
}
