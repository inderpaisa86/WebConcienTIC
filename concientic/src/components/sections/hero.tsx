import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { SectionContainer } from "@/components/ui/section-container";
import { hero } from "@/content/site";

export function Hero() {
  return (
    <SectionContainer id="inicio" spacing="default">
      <div className="grid items-center gap-10 md:grid-cols-2">
        <div className="flex flex-col gap-6">
          {hero.eyebrow ? (
            <Badge variant="outline" className="w-fit">
              {hero.eyebrow}
            </Badge>
          ) : null}
          <h1 className="font-heading text-4xl font-bold tracking-tight md:text-5xl">
            {hero.title}
          </h1>
          <p className="max-w-prose text-lg text-muted-foreground">{hero.subtitle}</p>
          <div className="flex flex-col gap-3 sm:flex-row">
            <Button size="lg" nativeButton={false} render={<a href={hero.primaryCta.href} />}>
              {hero.primaryCta.label}
            </Button>
            {hero.secondaryCta ? (
              <Button
                size="lg"
                variant="outline"
                nativeButton={false}
                render={<a href={hero.secondaryCta.href} />}
              >
                {hero.secondaryCta.label}
              </Button>
            ) : null}
          </div>
        </div>

        {/*
          Imagen del hero (Fase 0): colocar el asset en public/assets/hero.png y descomentar.
          Usar next/image con priority y sizes para optimizar LCP sin CLS.

          <div className="relative">
            <Image
              src="/assets/hero.png"
              alt="Descripción de la ilustración del hero"
              width={640}
              height={480}
              priority
              sizes="(max-width: 768px) 100vw, 50vw"
              className="h-auto w-full rounded-xl"
            />
          </div>
        */}
        <div
          aria-hidden
          className="hidden aspect-[4/3] w-full rounded-xl border border-border bg-muted md:block"
        />
      </div>
    </SectionContainer>
  );
}
