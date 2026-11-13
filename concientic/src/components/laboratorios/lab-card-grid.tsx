"use client";

import { useRef, useState } from "react";

import { laboratorios } from "@/content/laboratorios";

// URLs reales de los artefactos. Igual que el mock: todas vacías. NO inventar.
const ARTIFACT_URLS: Record<string, string> = {
  SIGINEX: "/laboratorios/siginex",
  EmprendeCreativo: "",
  "DQ-Nexus": "",
  TutorIA: "",
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
