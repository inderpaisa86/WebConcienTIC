import { contact } from "@/content/site";

export function Contact() {
  return (
    <section id={contact.id} className="final-cta">
      <div className="ct-container final-inner">
        <div className="final-copy">
          <span className="eyebrow eyebrow--green">{contact.eyebrow}</span>
          <h2>{contact.title}</h2>
          <p>{contact.subtitle}</p>
        </div>
        <a className="ct-button ct-button--primary" href={contact.ctaHref}>
          {contact.cta}
        </a>
      </div>
    </section>
  );
}
