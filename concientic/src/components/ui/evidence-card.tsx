type EvidenceCardProps = {
  type: string;
  title: string;
  description: string;
};

export function EvidenceCard({ type, title, description }: EvidenceCardProps) {
  return (
    <article className="evidence-card">
      <span className="evidence-card__type">{type}</span>
      <h3>{title}</h3>
      <p>{description}</p>
    </article>
  );
}
