"use client";

import { useState } from "react";

import { experiencia } from "@/content/site";
import { SectionHeader } from "@/components/ui/section-header";

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
    <section id={experiencia.id} className="ct-section ct-section--soft experiencia-section">
      <div className="ct-container">
        <SectionHeader eyebrow={experiencia.eyebrow} title={experiencia.title} />

        {/* Aviso SIEMPRE visible, sin condicionar al estado ni ocultarlo. */}
        <p className="experiencia-notice">{experiencia.notice}</p>

        <div className="experiencia-demo">
          <p className="experiencia-demo__label">{experiencia.demoLabel}</p>

          <fieldset className="experiencia-fieldset">
            <legend className="experiencia-question">{experiencia.question}</legend>
            {experiencia.options.map((option) => (
              <label className="experiencia-option" key={option.id}>
                <input
                  type="radio"
                  name="experiencia"
                  value={option.id}
                  checked={selected === option.id}
                  onChange={() => setSelected(option.id)}
                />
                <span>{option.label}</span>
              </label>
            ))}
          </fieldset>

          <button
            type="button"
            className="ct-button ct-button--primary"
            onClick={() => setShown(true)}
          >
            {experiencia.submitLabel}
          </button>

          <div role="status" aria-live="polite" className="experiencia-result">
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
