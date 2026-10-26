"use client";

import { useState } from "react";

import { experiencia } from "@/content/site";

export function Experiencia() {
  const [selected, setSelected] = useState<string | null>(null);
  const [shown, setShown] = useState(false);

  // Función pura (selected, shown) → recomendación. Sin fetch, sin persistencia,
  // sin envío: el estado vive solo en memoria mientras la página está abierta.
  const result =
    shown && selected
      ? (experiencia.options.find((option) => option.id === selected)?.recommendation ?? null)
      : null;
  const emptyWarning = shown && !selected;

  return (
    <section id={experiencia.id} className="experience">
      <div className="ct-container experience-grid">
        <div className="experience-copy">
          <span className="eyebrow eyebrow--green-dark">{experiencia.eyebrow}</span>
          <h2>{experiencia.title}</h2>
          {experiencia.paragraphs.map((paragraph) => (
            <p key={paragraph}>{paragraph}</p>
          ))}
        </div>

        <div className="demo">
          <p className="demo-label">{experiencia.demoLabel}</p>
          <h3>{experiencia.question}</h3>

          {experiencia.options.map((option) => (
            <label className="choice" key={option.id}>
              <input
                type="radio"
                name="need"
                value={option.id}
                checked={selected === option.id}
                onChange={() => setSelected(option.id)}
              />
              <span>{option.label}</span>
            </label>
          ))}

          <button
            type="button"
            className="ct-button ct-button--primary"
            onClick={() => setShown(true)}
          >
            {experiencia.submitLabel}
          </button>

          <div role="status" aria-live="polite" className="demo-result demo-result--live">
            {emptyWarning ? <p>{experiencia.emptyMessage}</p> : null}
            {result ? (
              <>
                <strong>{result.title}</strong>
                <p>{result.body}</p>
              </>
            ) : null}
          </div>
        </div>
      </div>
    </section>
  );
}
