import { recursos } from "@/content/site";
import { SectionHeader } from "@/components/ui/section-header";

export function Recursos() {
  return (
    <section id={recursos.id} className="ct-section ct-section--soft">
      <div className="ct-container">
        <SectionHeader
          eyebrow={recursos.eyebrow}
          title={recursos.title}
          description={recursos.intro}
        />
        <div className="card-grid card-grid--three">
          {recursos.items.map((item) => (
            <article className="info-card" key={item.title}>
              <span className="info-card__number">{item.number}</span>
              <h3>{item.title}</h3>
              <p>{item.description}</p>
              {item.cta ? (
                <>
                  <a
                    className="info-card__cta"
                    href={item.cta.href}
                    target={item.cta.external ? "_blank" : undefined}
                    rel={item.cta.external ? "noopener noreferrer" : undefined}
                  >
                    {item.cta.label}
                  </a>
                  {item.cta.pending ? (
                    <span className="info-card__pending">
                      URL pendiente de confirmación
                    </span>
                  ) : null}
                </>
              ) : null}
            </article>
          ))}
        </div>
      </div>
    </section>
  );
}
