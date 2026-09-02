import { ContactForm } from "@/components/sections/contact-form";
import { SectionContainer } from "@/components/ui/section-container";
import { contact } from "@/content/site";

export function Contact() {
  return (
    <SectionContainer id={contact.id}>
      <div className="grid gap-10 md:grid-cols-2">
        <div className="flex flex-col gap-3">
          <h2 className="font-heading text-3xl font-semibold tracking-tight">
            {contact.title}
          </h2>
          <p className="max-w-md text-muted-foreground">{contact.subtitle}</p>
        </div>
        <div>
          <ContactForm />
        </div>
      </div>
    </SectionContainer>
  );
}
