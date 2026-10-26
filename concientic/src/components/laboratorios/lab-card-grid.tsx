"use client";

import { useRef, useState, type ReactNode } from "react";

import { laboratorios } from "@/content/laboratorios";

// URLs reales de los artefactos. Igual que el mock: todas vacías. NO inventar.
const ARTIFACT_URLS: Record<string, string> = {
  SIGINEX: "",
  EmprendeCreativo: "",
  "DQ-Nexus": "",
  TutorIA: "",
};

// SVG inline EXACTOS del mock, por marca (en los logo-slot placeholder).
const LAB_ICONS: Record<string, ReactNode> = {
  SIGINEX: (
    <svg viewBox="0 0 40 40" fill="none" aria-hidden="true">
      <path d="M20 4 33 11.5V27L20 35 7 27V11.5L20 4Z" stroke="currentColor" strokeWidth="1.8" />
      <path
        d="M20 12v16M12 16l8 4 8-4M12 24l8-4 8 4"
        stroke="currentColor"
        strokeWidth="1.8"
        strokeLinecap="round"
      />
    </svg>
  ),
  EmprendeCreativo: (
    <svg viewBox="0 0 40 40" fill="none" aria-hidden="true">
      <path
        d="M20 5v8M20 27v8M5 20h8M27 20h8M9.4 9.4l5.7 5.7M24.9 24.9l5.7 5.7M30.6 9.4l-5.7 5.7M15.1 24.9l-5.7 5.7"
        stroke="currentColor"
        strokeWidth="1.8"
        strokeLinecap="round"
      />
      <circle cx="20" cy="20" r="7" stroke="currentColor" strokeWidth="1.8" />
      <circle cx="20" cy="20" r="2" fill="currentColor" />
    </svg>
  ),
  "DQ-Nexus": (
    <svg viewBox="0 0 40 40" fill="none" aria-hidden="true">
      <path d="M20 4 32.5 11v14L20 32 7.5 25V11L20 4Z" stroke="currentColor" strokeWidth="1.8" />
      <circle cx="20" cy="18" r="4" stroke="currentColor" strokeWidth="1.8" />
      <circle cx="11" cy="13" r="2" fill="currentColor" />
      <circle cx="29" cy="13" r="2" fill="currentColor" />
      <circle cx="20" cy="29" r="2" fill="currentColor" />
      <path d="m13 14.5 4 2M27 14.5l-4 2M20 22v5" stroke="currentColor" strokeWidth="1.6" />
    </svg>
  ),
  TutorIA: (
    <svg viewBox="0 0 40 40" fill="none" aria-hidden="true">
      <path
        d="M7 9.5c5-2.5 9-1.5 13 1v23c-4-2.5-8-3.5-13-1v-23ZM33 9.5c-5-2.5-9-1.5-13 1v23c4-2.5 8-3.5 13-1v-23Z"
        stroke="currentColor"
        strokeWidth="1.8"
        strokeLinejoin="round"
      />
      <path
        d="M13 15h3M13 20h3M24 15h3M24 20h3M20 4v3"
        stroke="currentColor"
        strokeWidth="1.8"
        strokeLinecap="round"
      />
    </svg>
  ),
};

export function LabCardGrid() {
  const { cards } = laboratorios.labs;
  const [notice, setNotice] = useState<string | null>(null);
  const timeoutRef = useRef<ReturnType<typeof setTimeout> | null>(null);

  function handleArtifactClick(
    event: React.MouseEvent<HTMLAnchorElement>,
    name: string,
  ) {
    // Si el enlace apunta a una ruta interna del sitio (p. ej. /laboratorios),
    // dejar que navegue con normalidad.
    const href = event.currentTarget.getAttribute("href") ?? "";
    if (href.startsWith("/")) {
      return;
    }
    // Si en el futuro se configura una URL real, dejar que el enlace navegue.
    if (ARTIFACT_URLS[name]) {
      return;
    }
    event.preventDefault();
    setNotice(`El enlace del artefacto ${name} está pendiente de configurar.`);
    if (timeoutRef.current) {
      clearTimeout(timeoutRef.current);
    }
    timeoutRef.current = setTimeout(() => setNotice(null), 4500);
  }

  return (
    <>
      <div className="lab-grid">
        {cards.map((card) => (
          <article
            className={`lab-card${card.variant ? ` ${card.variant}` : ""}`}
            key={card.key}
          >
            <div className="card-top">
              <div className="logo-slot" aria-label={card.logoLabel}>
                {LAB_ICONS[card.key]}
              </div>
              <span className="logo-caption">
                Logo oficial
                <br />
                por incorporar
              </span>
            </div>
            <div className="tag">{card.tag}</div>
            <h3>{card.name}</h3>
            <p>{card.description}</p>
            <div className="card-bottom">
              <span className="card-number">{card.cardNumber}</span>
              <a
                className="card-link artifact-link"
                data-lab={card.dataLab}
                href={card.href}
                target={"newTab" in card && card.newTab ? "_blank" : undefined}
                rel={"newTab" in card && card.newTab ? "noopener noreferrer" : undefined}
                onClick={(event) => handleArtifactClick(event, card.dataLab)}
              >
                {card.linkLabel} <span aria-hidden="true">↗</span>
              </a>
            </div>
          </article>
        ))}
      </div>
      <div className="lab-notice" role="status" data-visible={notice ? "true" : "false"}>
        {notice}
      </div>
    </>
  );
}
