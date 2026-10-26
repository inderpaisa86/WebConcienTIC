import Image from "next/image";

import { guardianes } from "@/content/site";
import { SectionHeader } from "@/components/ui/section-header";

export function Guardianes() {
  return (
    <section id={guardianes.id} className="ct-section ct-section--dark guardianes-band">
      <div className="ct-container">
        <SectionHeader eyebrow={guardianes.eyebrow} title={guardianes.title} dark />
        <p className="guardianes-lead">{guardianes.lead}</p>
        <p className="guardianes-body">{guardianes.body}</p>

        {/*
          No existe un asset único de grupo en el paquete v7 (public/guardians/
          solo trae los 7 PNG individuales). Según §14.5 se compone la imagen de
          grupo con los 7 PNG; el contenedor lleva el alt-text oficial como
          role="img" y cada PNG individual es decorativo (alt="").
        */}
        <div className="guardianes-group" role="img" aria-label={guardianes.groupAlt}>
          {guardianes.images.map((src) => (
            <span className="guardianes-group__item" key={src}>
              <Image
                src={src}
                alt=""
                width={160}
                height={160}
                sizes="(max-width: 700px) 30vw, 160px"
              />
            </span>
          ))}
        </div>

        <ul className="guardianes-competencias">
          {guardianes.competencias.map((competencia) => (
            <li className="guardianes-competencias__item" key={competencia}>
              {competencia}
            </li>
          ))}
        </ul>

        <p className="guardianes-closing">{guardianes.closing}</p>
      </div>
    </section>
  );
}
