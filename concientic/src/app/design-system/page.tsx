import { notFound } from "next/navigation";

import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import {
  Card,
  CardContent,
  CardDescription,
  CardFooter,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { SectionContainer } from "@/components/ui/section-container";

export const metadata = {
  title: "Design System — CONCIENTIC (solo dev)",
  robots: { index: false, follow: false },
};

const colorTokens = [
  "background",
  "foreground",
  "primary",
  "primary-foreground",
  "secondary",
  "secondary-foreground",
  "muted",
  "muted-foreground",
  "accent",
  "accent-foreground",
  "border",
  "card",
  "card-foreground",
] as const;

const radii = ["sm", "md", "lg", "xl", "2xl"] as const;
const shadows = ["shadow-sm", "shadow", "shadow-md", "shadow-lg", "shadow-xl"] as const;

function Swatch({ token }: { token: string }) {
  return (
    <div className="flex flex-col gap-2">
      <div
        className="h-16 w-full rounded-lg border border-border"
        style={{ backgroundColor: `var(--${token})` }}
      />
      <code className="text-xs text-muted-foreground">--{token}</code>
    </div>
  );
}

function Block({ title, children }: { title: string; children: React.ReactNode }) {
  return (
    <div className="flex flex-col gap-4">
      <h2 className="font-heading text-xl font-semibold">{title}</h2>
      {children}
    </div>
  );
}

export default function DesignSystemPage() {
  // Página solo para desarrollo: no debe existir en producción.
  if (process.env.NODE_ENV === "production") {
    notFound();
  }

  return (
    <main>
      <SectionContainer spacing="compact">
        <div className="flex flex-col gap-2">
          <Badge variant="outline">solo dev</Badge>
          <h1 className="font-heading text-3xl font-bold tracking-tight">
            Design System — CONCIENTIC
          </h1>
          <p className="max-w-2xl text-muted-foreground">
            Validación visual de tokens y componentes base. Los valores de color y tipografía son
            provisionales hasta completar la Fase 0.
          </p>
        </div>
      </SectionContainer>

      <SectionContainer background="muted" spacing="compact">
        <div className="flex flex-col gap-10">
          <Block title="Colores">
            <div className="grid grid-cols-2 gap-4 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-6">
              {colorTokens.map((token) => (
                <Swatch key={token} token={token} />
              ))}
            </div>
          </Block>

          <Block title="Tipografía">
            <div className="flex flex-col gap-3">
              <h1 className="font-heading text-4xl font-bold">Título H1 — Aa Bb Cc</h1>
              <h2 className="font-heading text-3xl font-semibold">Título H2 — Aa Bb Cc</h2>
              <h3 className="font-heading text-2xl font-semibold">Título H3 — Aa Bb Cc</h3>
              <p className="text-base">
                Párrafo de cuerpo. El texto usa la fuente base del sistema. Sustituir por la
                tipografía real de la marca cuando esté definida.
              </p>
              <p className="text-sm text-muted-foreground">Texto pequeño / notas / labels.</p>
            </div>
          </Block>

          <Block title="Radios">
            <div className="flex flex-wrap gap-4">
              {radii.map((r) => (
                <div key={r} className="flex flex-col items-center gap-2">
                  <div
                    className="size-16 border border-border bg-card"
                    style={{ borderRadius: `var(--radius-${r})` }}
                  />
                  <code className="text-xs text-muted-foreground">radius-{r}</code>
                </div>
              ))}
            </div>
          </Block>

          <Block title="Sombras">
            <div className="flex flex-wrap gap-6">
              {shadows.map((s) => (
                <div key={s} className="flex flex-col items-center gap-2">
                  <div className={`size-16 rounded-lg bg-card ${s}`} />
                  <code className="text-xs text-muted-foreground">{s}</code>
                </div>
              ))}
            </div>
          </Block>
        </div>
      </SectionContainer>

      <SectionContainer spacing="compact">
        <div className="flex flex-col gap-10">
          <Block title="Botones">
            <div className="flex flex-wrap items-center gap-3">
              <Button>Default</Button>
              <Button variant="secondary">Secondary</Button>
              <Button variant="outline">Outline</Button>
              <Button variant="ghost">Ghost</Button>
              <Button variant="destructive">Destructive</Button>
              <Button variant="link">Link</Button>
            </div>
            <div className="flex flex-wrap items-center gap-3">
              <Button size="sm">Small</Button>
              <Button size="default">Default</Button>
              <Button size="lg">Large</Button>
              <Button disabled>Disabled</Button>
            </div>
          </Block>

          <Block title="Badges">
            <div className="flex flex-wrap items-center gap-3">
              <Badge>Default</Badge>
              <Badge variant="secondary">Secondary</Badge>
              <Badge variant="outline">Outline</Badge>
              <Badge variant="accent">Accent</Badge>
              <Badge variant="muted">Muted</Badge>
            </div>
          </Block>

          <Block title="Cards">
            <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
              <Card>
                <CardHeader>
                  <CardTitle>Título de la card</CardTitle>
                  <CardDescription>Descripción breve de apoyo.</CardDescription>
                </CardHeader>
                <CardContent>
                  <p className="text-sm text-muted-foreground">
                    Contenido de ejemplo dentro de la card para validar espaciados.
                  </p>
                </CardContent>
                <CardFooter>
                  <Button size="sm">Acción</Button>
                </CardFooter>
              </Card>
            </div>
          </Block>
        </div>
      </SectionContainer>
    </main>
  );
}
