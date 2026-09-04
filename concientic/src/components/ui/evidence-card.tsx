type EvidenceCardProps = {
  type: string;
  title: string;
  description: string;
  href?: string;
  source?: string;
};

export function EvidenceCard({ type, title, description, href, source }: EvidenceCardProps) {
  return (
    <article className="evidence-card">
      <span className="evidence-card__type">{type}</span>
      <h3>{title}</h3>
      <p>{description}</p>
      {href && source ? (
        <a href={href} target="_blank" rel="noopener noreferrer">
          {source}
        </a>
      ) : null}
    </article>
  );
}
