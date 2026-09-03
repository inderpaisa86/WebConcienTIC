import Image from "next/image";

import type { guardians } from "@/content/guardians";

type Guardian = (typeof guardians)[number];

type GuardianCardProps = {
  guardian: Guardian;
};

export function GuardianCard({ guardian }: GuardianCardProps) {
  return (
    <article
      className="guardian-card"
      style={{ "--guardian-color": guardian.color } as React.CSSProperties}
    >
      <div className="guardian-card__art">
        <Image
          src={guardian.image}
          alt={`${guardian.name}, ${guardian.role}`}
          width={420}
          height={420}
          sizes="(max-width: 700px) 100vw, (max-width: 1100px) 33vw, 25vw"
        />
      </div>
      <div className="guardian-card__body">
        <div className="guardian-card__name">{guardian.name}</div>
        <div className="guardian-card__role">{guardian.role}</div>
        <div className="guardian-card__competence">{guardian.competence}</div>
        <p>{guardian.description}</p>
        <span className="guardian-card__tag">Guardián ConcienTIC</span>
      </div>
    </article>
  );
}
