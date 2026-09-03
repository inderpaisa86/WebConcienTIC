import { SectionContainer } from "@/components/ui/section-container";

type LegalSection = {
  readonly heading: string;
  readonly body: string;
};

type LegalContent = {
  readonly title: string;
  readonly lastUpdated: string;
  readonly intro: string;
  readonly sections: readonly LegalSection[];
};

export function LegalPage({ content }: { content: LegalContent }) {
  const formatted = new Date(content.lastUpdated).toLocaleDateString("es-ES", {
    year: "numeric",
    month: "long",
    day: "numeric",
  });

  return (
    <SectionContainer spacing="default" contentClassName="max-w-3xl">
      <article className="flex flex-col gap-8">
        <header className="flex flex-col gap-2">
          <h1 className="font-heading text-3xl font-bold tracking-tight md:text-4xl">
            {content.title}
          </h1>
          <p className="text-sm text-muted-foreground">Última actualización: {formatted}</p>
          <p className="text-muted-foreground">{content.intro}</p>
        </header>

        <div className="flex flex-col gap-6">
          {content.sections.map((section) => (
            <section key={section.heading} className="flex flex-col gap-2">
              <h2 className="font-heading text-xl font-semibold">{section.heading}</h2>
              <p className="text-muted-foreground">{section.body}</p>
            </section>
          ))}
        </div>
      </article>
    </SectionContainer>
  );
}
