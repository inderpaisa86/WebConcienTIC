import type { Metadata } from "next";
import { DM_Sans, Fraunces, Dancing_Script } from "next/font/google";

const dmSans = DM_Sans({
  variable: "--eac-font-sans",
  subsets: ["latin"],
  weight: ["400", "500", "700"],
  display: "swap",
});

const fraunces = Fraunces({
  variable: "--eac-font-serif",
  subsets: ["latin"],
  weight: ["400", "500"],
  style: ["normal", "italic"],
  display: "swap",
});

const dancingScript = Dancing_Script({
  variable: "--eac-font-script",
  subsets: ["latin"],
  weight: ["500", "600"],
  display: "swap",
});

export const metadata: Metadata = {
  title: "El Arte que Conecta | Escuela de Macramé online",
  description:
    "Aprende macramé a tu ritmo: programa principal, cursos de bolsos y Navidad, talleres individuales y una comunidad artesanal que te acompaña.",
  alternates: {
    canonical: "/elartequeconecta",
  },
  openGraph: {
    type: "website",
    locale: "es_ES",
    url: "/elartequeconecta",
    title: "El Arte que Conecta | Escuela de Macramé",
    description:
      "Teje momentos. Crea con el corazón. Aprende macramé y conecta con una comunidad artesanal.",
    images: [
      {
        url: "/elartequeconecta/logomacrame.jpg",
        width: 800,
        height: 800,
        alt: "El Arte que Conecta — escuela de macramé online",
      },
    ],
  },
  twitter: {
    card: "summary_large_image",
    title: "El Arte que Conecta | Escuela de Macramé",
    description:
      "Teje momentos. Crea con el corazón. Aprende macramé y conecta con una comunidad artesanal.",
    images: ["/elartequeconecta/logomacrame.jpg"],
  },
};

export default function ElArteQueConectaLayout({ children }: { children: React.ReactNode }) {
  return (
    <div className={`${dmSans.variable} ${fraunces.variable} ${dancingScript.variable}`}>
      {children}
    </div>
  );
}
