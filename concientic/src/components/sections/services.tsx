import { services } from "@/content/site";
import { SectionHeader } from "@/components/ui/section-header";

export function Services() {
  return (
    <section id={services.id} className="ct-section ct-section--soft">
      <div className="ct-container">
        <SectionHeader
          eyebrow={services.eyebrow}
          title={services.title}
          description={services.subtitle}
        />
        <div className="card-grid card-grid--five">
          {services.items.map((item) => {
            const className = `service-card service-card--${item.state}`;
            const content = (
              <>
                {item.state === "disabled" ? (
                  <span className="service-card__soon">En construcción</span>
                ) : null}
                <h3>{item.title}</h3>
                <p>{item.description}</p>
                <span className="service-card__tag">{item.tag}</span>
              </>
            );

            if (item.href) {
              return (
                <a
                  className={className}
                  href={item.href}
                  key={item.title}
                  aria-label={item.ariaLabel ?? undefined}
                >
                  {content}
                </a>
              );
            }

            return (
              <article
                aria-disabled={item.state === "disabled" ? true : undefined}
                className={className}
                key={item.title}
              >
                {content}
              </article>
            );
          })}
        </div>
      </div>
    </section>
  );
}
