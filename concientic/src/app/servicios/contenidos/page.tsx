import type { Metadata } from "next";

import { LibraryExplorer } from "@/components/library/library-explorer";

export const metadata: Metadata = {
  title: "Biblioteca abierta",
  description:
    "Recursos gratuitos y verificables para desarrollar competencias digitales con criterio.",
};

export default function ContenidosPage() {
  return <LibraryExplorer />;
}
