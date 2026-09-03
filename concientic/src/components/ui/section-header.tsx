type SectionHeaderProps = {
  eyebrow: string;
  title: string;
  description?: string;
  dark?: boolean;
};

export function SectionHeader({ eyebrow, title, description, dark = false }: SectionHeaderProps) {
  return (
    <div className={`ct-section-header${dark ? " ct-section-header--dark" : ""}`}>
      <p className="ct-eyebrow">{eyebrow}</p>
      <h2>{title}</h2>
      {description ? <p>{description}</p> : null}
    </div>
  );
}
