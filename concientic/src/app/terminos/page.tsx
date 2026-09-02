import type { Metadata } from "next";

import { LegalPage } from "@/components/legal/legal-page";
import { legal } from "@/content/site";

export const metadata: Metadata = {
  title: legal.terms.title,
  description: legal.terms.intro,
};

export default function TerminosPage() {
  return <LegalPage content={legal.terms} />;
}
