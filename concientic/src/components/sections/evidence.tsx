import { evidence } from "@/content/site";
import { EvidenceCard } from "@/components/ui/evidence-card";
import { SectionHeader } from "@/components/ui/section-header";

export function Evidence() {
  const [caveatLabel, ...caveatRest] = evidence.caveat.split(":");

  return (
    <section id={evidence.id} className="ct-section evidence-section">
      <div className="ct-container">
        <SectionHeader
          eyebrow={evidence.eyebrow}
          title={evidence.title}
          description={evidence.subtitle}
        />
        <div className="card-grid card-grid--four">
          {evidence.items.map((item) => (
            <EvidenceCard key={item.title} {...item} />
          ))}
        </div>
        <p className="evidence-caveat">
          <strong>{caveatLabel}:</strong> {caveatRest.join(":").trim()}
        </p>
      </div>
    </section>
  );
}
