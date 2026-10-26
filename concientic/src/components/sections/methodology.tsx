import type { CSSProperties } from "react";

import { methodology } from "@/content/site";

export function Methodology() {
  return (
    <section id={methodology.id} className="ink-section">
      <div className="ct-container">
        <div className="method-cycle">
          <div className="method-cycle-copy">
            <span className="eyebrow eyebrow--green-dark">{methodology.eyebrow}</span>
            <h3>{methodology.title}</h3>
            <p>{methodology.subtitle}</p>
            <div className="method-steps">
              {methodology.steps.map((step) => (
                <div
                  className="method-step"
                  key={step.step}
                  style={{ "--step-color": step.color } as CSSProperties}
                >
                  <b>
                    {step.step} · {step.title}
                  </b>
                  <span>{step.description}</span>
                </div>
              ))}
            </div>
          </div>
          <div
            className="dq-diagram"
            role="img"
            aria-label="Ciclo DQUILIBRIO con cinco etapas: detener, observar, discernir, elegir y equilibrar"
          >
            <div className="dq-orbit">
              <div className="dq-core">
                DQUILIBRIO
                <small>
                  tecnología
                  <br />
                  con conciencia
                </small>
              </div>
              {methodology.diagramNodes.map((node) => (
                <span
                  className={`dq-node ${node.position}`}
                  key={node.position}
                  style={{ "--node-color": node.color } as CSSProperties}
                >
                  {node.label}
                </span>
              ))}
            </div>
          </div>
        </div>
      </div>
    </section>
  );
}
