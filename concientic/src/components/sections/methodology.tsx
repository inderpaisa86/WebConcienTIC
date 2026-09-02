import { SectionContainer } from "@/components/ui/section-container";
import { methodology } from "@/content/site";

export function Methodology() {
  return (
    <SectionContainer id={methodology.id} background="muted">
      <div className="flex flex-col gap-10">
        <div className="flex flex-col gap-3">
          <h2 className="font-heading text-3xl font-semibold tracking-tight">
            {methodology.title}
          </h2>
          <p className="max-w-2xl text-muted-foreground">{methodology.subtitle}</p>
        </div>
        <ol className="grid gap-6 sm:grid-cols-2 lg:grid-cols-4">
          {methodology.steps.map((step) => (
            <li key={step.step} className="flex flex-col gap-3">
              <span className="font-heading text-4xl font-bold text-primary/40">
                {step.step}
              </span>
              <h3 className="font-heading text-lg font-semibold">{step.title}</h3>
              <p className="text-sm text-muted-foreground">{step.description}</p>
            </li>
          ))}
        </ol>
      </div>
    </SectionContainer>
  );
}
