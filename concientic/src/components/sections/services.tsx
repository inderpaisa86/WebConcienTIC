import { Card, CardHeader, CardTitle, CardDescription } from "@/components/ui/card";
import { SectionContainer } from "@/components/ui/section-container";
import { services } from "@/content/site";

export function Services() {
  return (
    <SectionContainer id={services.id}>
      <div className="flex flex-col gap-10">
        <div className="flex flex-col gap-3">
          <h2 className="font-heading text-3xl font-semibold tracking-tight">
            {services.title}
          </h2>
          <p className="max-w-2xl text-muted-foreground">{services.subtitle}</p>
        </div>
        <div className="grid gap-6 md:grid-cols-3">
          {services.items.map((item) => (
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
