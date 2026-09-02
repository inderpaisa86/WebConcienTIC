import { Hero } from "@/components/sections/hero";
import { ValueProps } from "@/components/sections/value-props";
import { Services } from "@/components/sections/services";
import { Methodology } from "@/components/sections/methodology";
import { Cases } from "@/components/sections/cases";
import { Team } from "@/components/sections/team";
import { Contact } from "@/components/sections/contact";

export default function Home() {
  return (
    <>
      <Hero />
      <ValueProps />
      <Services />
      <Methodology />
      <Cases />
      <Team />
      <Contact />
    </>
  );
}
