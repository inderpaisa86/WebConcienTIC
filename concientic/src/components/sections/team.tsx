import { guardians } from "@/content/guardians";
import { team } from "@/content/site";
import { GuardianCard } from "@/components/ui/guardian-card";
import { SectionHeader } from "@/components/ui/section-header";

export function Team() {
  return (
    <section id={team.id} className="ct-section guardians-section">
      <div className="ct-container">
        <SectionHeader eyebrow={team.eyebrow} title={team.title} description={team.subtitle} dark />
        <div className="team-intro">
          {team.statements.map((statement) => (
            <div className="team-statement" key={statement.emphasis}>
              <strong>{statement.emphasis}</strong>
              <br />
              {statement.text}
            </div>
          ))}
        </div>
        <div className="guardians-head">
          <div>
            <p className="ct-eyebrow">{team.guardiansEyebrow}</p>
            <h2>{team.guardiansTitle}</h2>
          </div>
          <p>{team.guardiansDescription}</p>
        </div>
        <div className="guardian-grid">
          {guardians.map((guardian) => (
            <GuardianCard key={guardian.name} guardian={guardian} />
          ))}
        </div>
        <p className="team-principle">{team.principle}</p>
      </div>
    </section>
  );
}
