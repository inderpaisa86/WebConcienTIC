import { methodology } from "@/content/site";
import { SectionHeader } from "@/components/ui/section-header";

const stepColors = ["#00C49A", "#0077FF", "#6C63FF", "#FF7A00", "#00B4D8"];

export function Methodology() {
  return (
    <section id={methodology.id} className="ct-section ct-section--soft">
      <div className="ct-container">
        <div className="methodology-grid">
          <div>
            <SectionHeader
              eyebrow={methodology.eyebrow}
              title={methodology.title}
              description={methodology.subtitle}
            />
            <div
              className="methodology-wheel"
              aria-label="Ciclo DQUILIBRIO: detener, observar, discernir, elegir y equilibrar"
              role="img"
            >
              {methodology.steps.map((step, index) => (
                <span
                  className="methodology-wheel__step"
                  key={step.step}
                  style={{ "--step-color": stepColors[index] } as React.CSSProperties}
                >
                  {step.title}
                </span>
              ))}
              <span className="methodology-wheel__center">
                DQUILIBRIO
                <small>volver a elegir</small>
              </span>
            </div>
          </div>
          <ol className="methodology-list">
            {methodology.steps.map((step, index) => (
              <li
                className="methodology-item"
                key={step.step}
                style={{ "--item-color": stepColors[index] } as React.CSSProperties}
              >
                <h3>
                  {step.step} · {step.title}
                </h3>
                <p>{step.description}</p>
              </li>
            ))}
          </ol>
        </div>
      </div>
    </section>
  );
}
