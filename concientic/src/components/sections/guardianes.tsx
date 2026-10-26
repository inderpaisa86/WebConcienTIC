import type { CSSProperties } from "react";
import Image from "next/image";

import { guardianes } from "@/content/site";

export function Guardianes() {
  return (
    <section id={guardianes.id} className="ct-section guardians-feature">
      <div className="ct-container guardians-layout">
        <div className="guardians-kickers">
          {guardianes.eyebrows.map((eyebrow) => (
            <span className="ct-eyebrow guardians-kicker" key={eyebrow}>
              {eyebrow}
            </span>
          ))}
        </div>

        <h2 className="guardians-title">{guardianes.title}</h2>
        <p className="guardians-intro">{guardianes.intro}</p>

        {/*
          No existe un asset único de grupo en el paquete v7 (public/guardians/
          solo trae los 7 PNG individuales) y §14.5 prohíbe incrustar base64 o
          sustituir assets de marca por aproximaciones. Por eso la imagen de
          grupo se compone con los 7 PNG individuales, cada uno rotulado con su
          nombre en una píldora del color oficial del Guardián (Manual de Marca).
          El contenedor lleva el alt-text oficial como role="group" y cada PNG es
          decorativo (alt="").
        */}
        <div className="guardian-image-wrap" role="group" aria-label={guardianes.groupAlt}>
          {guardianes.images.map((guardian) => (
            <figure
              className="guardian-image__item"
              key={guardian.src}
              style={{ "--guardian-color": guardian.color } as CSSProperties}
            >
              <span className="guardian-image__art">
                <Image
                  src={guardian.src}
                  alt=""
                  width={160}
                  height={160}
                  sizes="(max-width: 700px) 30vw, 160px"
                />
              </span>
              <figcaption className="guardian-image__name">{guardian.name}</figcaption>
            </figure>
          ))}
        </div>

        <div
          className="guardian-capabilities"
          aria-label="Capacidades representadas por los Guardianes"
        >
          {guardianes.capabilities.map((cap) => (
            <div
              className="guardian-capability"
              key={cap.name}
              style={{ "--cap-color": cap.color } as CSSProperties}
            >
              <span aria-hidden="true">{cap.icon}</span>
              <strong>{cap.name}</strong>
            </div>
          ))}
        </div>

        <div className="guardians-closing">
          <span aria-hidden="true">{guardianes.closingIcon}</span>{" "}
          {guardianes.closing}
        </div>
      </div>
    </section>
  );
}
