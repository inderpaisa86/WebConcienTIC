import { ContactForm } from "@/components/sections/contact-form";
import { SectionHeader } from "@/components/ui/section-header";
import { contact } from "@/content/site";

export function Contact() {
  return (
    <section id={contact.id} className="ct-section">
      <div className="ct-container contact-grid">
        <div className="contact-copy">
          <SectionHeader eyebrow={contact.eyebrow} title={contact.title} />
          <p>{contact.subtitle}</p>
        </div>
        <div className="contact-form-shell">
          <ContactForm />
        </div>
      </div>
    </section>
  );
}
