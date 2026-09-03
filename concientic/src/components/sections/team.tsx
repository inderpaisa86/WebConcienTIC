import { guardians } from "@/content/guardians";
import { team } from "@/content/site";
import { GuardianCard } from "@/components/ui/guardian-card";
import { SectionHeader } from "@/components/ui/section-header";

export function Team() {
  return (
    <section id={team.id} className="ct-section guardians-section">
      <div className="ct-container">
        <SectionHeader eyebrow={team.eyebrow} title={team.title} description={team.subtitle} dark />
        <div className="guardian-grid">
          {guardians.map((guardian) => (
            <GuardianCard key={guardian.name} guardian={guardian} />
          ))}
        </div>
        <p className="team-principle">
          <strong>Principio de equipo: </strong>
          {team.principle}
        </p>
      </div>
    </section>
  );
}
