import Image from "next/image";

import { methodology } from "@/content/site";

export function Methodology() {
  return (
    <section id={methodology.id} className="ct-section">
      <div className="ct-container">
        <div className="methodology-grid">
          <div className="methodology-copy">
            <p className="ct-eyebrow">{methodology.eyebrow}</p>
            <h2>{methodology.title}</h2>
            <p className="methodology-lead">{methodology.subtitle}</p>
            <ol className="methodology-list">
              {methodology.steps.map((step) => (
                <li className="methodology-item" key={step.step}>
                  <b>
                    {step.step} · {step.title}
                  </b>
                  <p>{step.description}</p>
                </li>
              ))}
            </ol>
          </div>
          <div className="cycle">
            <Image
              src="/diagrams/ciclo-dquilibrio-mejorado.png"
              alt="Ciclo DQUILIBRIO: Detener, Observar, Discernir, Elegir, Equilibrar y vuelta a elegir"
              width={610}
              height={610}
              sizes="(max-width: 700px) 100vw, (max-width: 1100px) 80vw, 610px"
            />
          </div>
        </div>
      </div>
    </section>
  );
}
