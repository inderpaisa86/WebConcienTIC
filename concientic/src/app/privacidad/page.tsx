import type { Metadata } from "next";

import { LegalPage } from "@/components/legal/legal-page";
import { legal } from "@/content/site";

export const metadata: Metadata = {
  title: legal.privacy.title,
  description: legal.privacy.intro,
};

export default function PrivacidadPage() {
  return <LegalPage content={legal.privacy} />;
}
