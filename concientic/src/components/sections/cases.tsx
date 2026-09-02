import { Card, CardHeader, CardTitle, CardDescription, CardContent } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { SectionContainer } from "@/components/ui/section-container";
import { cases } from "@/content/site";

export function Cases() {
  return (
    <SectionContainer id={cases.id}>
      <div className="flex flex-col gap-10">
        <div className="flex flex-col gap-3">
          <h2 className="font-heading text-3xl font-semibold tracking-tight">
            {cases.title}
          </h2>
          <p className="max-w-2xl text-muted-foreground">{cases.subtitle}</p>
        </div>
        <div className="grid gap-6 md:grid-cols-3">
          {cases.items.map((item) => (
            <Card key={item.client}>
              <CardHeader>
                <Badge variant="accent" className="w-fit">
                  {item.result}
                </Badge>
                <CardTitle>{item.client}</CardTitle>
              </CardHeader>
              <CardContent>
                <CardDescription>{item.description}</CardDescription>
              </CardContent>
            </Card>
          ))}
        </div>
      </div>
    </SectionContainer>
  );
}
