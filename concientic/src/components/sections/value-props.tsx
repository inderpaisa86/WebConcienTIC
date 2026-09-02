import { Card, CardHeader, CardTitle, CardDescription } from "@/components/ui/card";
import { SectionContainer } from "@/components/ui/section-container";
import { valueProps } from "@/content/site";

export function ValueProps() {
  return (
    <SectionContainer background="muted">
      <div className="flex flex-col gap-10">
        <h2 className="font-heading text-3xl font-semibold tracking-tight">
          {valueProps.title}
        </h2>
        <div className="grid gap-6 md:grid-cols-3">
          {valueProps.items.map((item) => (
            <Card key={item.title}>
              <CardHeader>
                <CardTitle>{item.title}</CardTitle>
                <CardDescription>{item.description}</CardDescription>
              </CardHeader>
            </Card>
          ))}
        </div>
      </div>
    </SectionContainer>
  );
}
