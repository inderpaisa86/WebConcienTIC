import { Challenge } from "@/components/sections/challenge";
import { Cases } from "@/components/sections/cases";
import { Contact } from "@/components/sections/contact";
import { Desinfoxication } from "@/components/sections/desinfoxication";
import { Dquilibrio } from "@/components/sections/dquilibrio";
import { Evidence } from "@/components/sections/evidence";
import { Hero } from "@/components/sections/hero";
import { Manifesto } from "@/components/sections/manifesto";
import { Methodology } from "@/components/sections/methodology";
import { PrincipleBand } from "@/components/sections/principle-band";
import { References } from "@/components/sections/references";
import { Services } from "@/components/sections/services";
import { Team } from "@/components/sections/team";

export default function Home() {
  return (
    <>
      <Hero />
      <PrincipleBand />
      <Dquilibrio />
      <Challenge />
      <Desinfoxication />
      <Services />
      <Methodology />
      <Evidence />
      <Team />
      <Cases />
      <References />
      <Manifesto />
      <Contact />
    </>
  );
}
