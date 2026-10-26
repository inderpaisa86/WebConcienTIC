import { Contact } from "@/components/sections/contact";
import { Dquilibrio } from "@/components/sections/dquilibrio";
import { Ecosistema } from "@/components/sections/ecosistema";
import { Experiencia } from "@/components/sections/experiencia";
import { Guardianes } from "@/components/sections/guardianes";
import { Hero } from "@/components/sections/hero";
import { Methodology } from "@/components/sections/methodology";
import { Recursos } from "@/components/sections/recursos";

export default function Home() {
  return (
    <>
      <Hero />
      <Ecosistema />
      <Guardianes />
      <Recursos />
      <Methodology />
      <Experiencia />
      <Dquilibrio />
      <Contact />
    </>
  );
}
