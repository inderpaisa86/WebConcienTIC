import { Card, CardHeader, CardTitle, CardDescription, CardContent } from "@/components/ui/card";
import { SectionContainer } from "@/components/ui/section-container";
import { team } from "@/content/site";

export function Team() {
  return (
    <SectionContainer id={team.id} background="muted">
      <div className="flex flex-col gap-10">
        <div className="flex flex-col gap-3">
          <h2 className="font-heading text-3xl font-semibold tracking-tight">
            {team.title}
          </h2>
          <p className="max-w-2xl text-muted-foreground">{team.subtitle}</p>
        </div>
        <div className="grid gap-6 sm:grid-cols-2">
          {team.members.map((member) => (
            <Card key={member.name}>
              <CardHeader>
                <CardTitle>{member.name}</CardTitle>
                <CardDescription>{member.role}</CardDescription>
              </CardHeader>
              <CardContent>
                <p className="text-sm text-muted-foreground">{member.bio}</p>
              </CardContent>
            </Card>
          ))}
        </div>
      </div>
    </SectionContainer>
  );
}
