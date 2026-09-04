import Image from "next/image";

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
            <div className="methodology-wheel">
              <Image
                src="/diagrams/ciclo-dquilibrio-mejorado.png"
                alt="Ciclo DQUILIBRIO: detener, observar, discernir, elegir y equilibrar"
                width={610}
                height={610}
                sizes="(max-width: 700px) 100vw, (max-width: 1100px) 80vw, 610px"
              />
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
