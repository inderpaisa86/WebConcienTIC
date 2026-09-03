import { desinfoxication } from "@/content/site";
import { SectionHeader } from "@/components/ui/section-header";

export function Desinfoxication() {
  return (
    <section id={desinfoxication.id} className="ct-section desinfoxication-section">
      <div className="ct-container">
        <SectionHeader
          eyebrow={desinfoxication.eyebrow}
          title={desinfoxication.title}
          description={desinfoxication.description}
          dark
        />
        <div className="desinfoxication-grid">
          {desinfoxication.steps.map((step, index) => (
            <article className="desinfoxication-card" key={step.title}>
              <span className="info-card__number">0{index + 1}</span>
              <h3>{step.title}</h3>
              <p>{step.description}</p>
            </article>
          ))}
        </div>
      </div>
    </section>
  );
}
