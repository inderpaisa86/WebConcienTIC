import { evidence } from "@/content/site";
import { EvidenceCard } from "@/components/ui/evidence-card";
import { SectionHeader } from "@/components/ui/section-header";

export function Evidence() {
  return (
    <section id={evidence.id} className="ct-section evidence-section">
      <div className="ct-container">
        <SectionHeader
          eyebrow={evidence.eyebrow}
          title={evidence.title}
          description={evidence.subtitle}
        />
        <div className="card-grid card-grid--three">
          {evidence.items.map((item) => (
            <EvidenceCard key={item.title} {...item} />
          ))}
        </div>
        <p className="evidence-caveat">{evidence.caveat}</p>
      </div>
    </section>
  );
}
