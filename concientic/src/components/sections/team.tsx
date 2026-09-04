import { guardians, guardiansQuote } from "@/content/guardians";
import { team } from "@/content/site";
import { GuardianCard } from "@/components/ui/guardian-card";
import { SectionHeader } from "@/components/ui/section-header";

export function Team() {
  return (
    <section id={team.id} className="ct-section ct-section--dark guardians-section">
      <div className="ct-container">
        <SectionHeader eyebrow={team.eyebrow} title={team.title} description={team.subtitle} dark />
        <div className="guardian-grid">
          {guardians.map((guardian) => (
            <GuardianCard key={guardian.name} guardian={guardian} />
          ))}
          <article className="guardian-quote">{guardiansQuote}</article>
        </div>
        <p className="team-principle">
          {team.principle} <strong className="team-principle__consciente">consciente</strong>,{" "}
          <strong className="team-principle__critica">crítica</strong>,{" "}
          <strong className="team-principle__creativa">creativa</strong>,{" "}
          <strong className="team-principle__colaborativa">colaborativa</strong> y{" "}
          <strong className="team-principle__transformadora">transformadora</strong>.
        </p>
      </div>
    </section>
  );
}
