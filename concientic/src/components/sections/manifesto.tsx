import { manifesto } from "@/content/site";
import { SectionHeader } from "@/components/ui/section-header";

export function Manifesto() {
  return (
    <section id={manifesto.id} className="ct-section ct-section--dark">
      <div className="ct-container manifesto-grid">
        <div className="manifesto-copy">
          <SectionHeader eyebrow={manifesto.eyebrow} title={manifesto.title} dark />
          <p>{manifesto.description}</p>
        </div>
        <div className="principles-grid">
          {manifesto.principles.map((principle) => (
            <article className="principle-card" key={principle.title}>
              <strong>{principle.title}</strong>
              <span>{principle.description}</span>
            </article>
          ))}
        </div>
      </div>
    </section>
  );
}
