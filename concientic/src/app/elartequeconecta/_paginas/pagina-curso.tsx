"use client";

import { useEffect, useRef } from "react";

import { paginaCssB64 } from "./estilos";

function decode(b64: string): string {
  if (typeof window === "undefined") {
    return Buffer.from(b64, "base64").toString("utf-8");
  }
  // atob da binario; reinterpretar como UTF-8 para conservar acentos/emojis.
  const binary = atob(b64);
  const bytes = Uint8Array.from(binary, (c) => c.charCodeAt(0));
  return new TextDecoder("utf-8").decode(bytes);
}

type PaginaCursoProps = {
  /** HTML del <body> del diseño original, codificado en base64. */
  htmlB64: string;
  /** CSS adicional específico de la página (overrides), opcional. */
  extraCss?: string;
};

/**
 * Renderiza fielmente una de las landings de curso (escuela/bolsos/navidad)
 * inyectando su HTML y su CSS original, y reactivando sus comportamientos
 * (cuenta regresiva, carrusel de niveles, copiar cupón) de forma scopeada
 * al contenedor, sin tocar el documento global.
 */
export function PaginaCurso({ htmlB64, extraCss = "" }: PaginaCursoProps) {
  const rootRef = useRef<HTMLDivElement>(null);
  // El CSS original pide las familias por nombre literal ("Fraunces", "DM Sans").
  // El layout de /elartequeconecta ya carga esas fuentes con next/font y las
  // expone como variables; las enlazamos vía @font-face-like alias para que el
  // CSS original las use sin un <link> externo a Google Fonts.
  // El CSS original define --serif y --sans con Fraunces/DM Sans por nombre.
  // Redefinimos esas variables para que resuelvan a las fuentes next/font ya
  // cargadas por el layout (así respeta incluso las reglas con !important).
  const fontAlias = `
    .eac-curso{
      --serif: var(--eac-font-serif), "Fraunces", Georgia, serif;
      --sans: var(--eac-font-sans), "DM Sans", Arial, sans-serif;
      font-family: var(--sans);
    }
  `;
  const css = decode(paginaCssB64);
  const html = decode(htmlB64);

  useEffect(() => {
    const root = rootRef.current;
    if (!root) return;

    // --- Carrusel de niveles (botones con data-scroll / data-dir) ---
    const scrollButtons = Array.from(root.querySelectorAll<HTMLElement>("[data-scroll]"));
    const onScrollClick = (btn: HTMLElement) => () => {
      const target = root.querySelector<HTMLElement>(`#${CSS.escape(btn.dataset.scroll ?? "")}`);
      if (!target) return;
      target.scrollBy({
        left: target.clientWidth * 0.8 * Number(btn.dataset.dir ?? 1),
        behavior: "smooth",
      });
    };
    const scrollHandlers = scrollButtons.map((btn) => {
      const handler = onScrollClick(btn);
      btn.addEventListener("click", handler);
      return { btn, handler };
    });

    // --- Cuenta regresiva (elementos con data-cd) ---
    const end = new Date("2026-11-30T23:59:59-05:00").getTime();
    const cdEls = Array.from(root.querySelectorAll<HTMLElement>("[data-cd]"));
    let timer: ReturnType<typeof setTimeout> | undefined;
    const tick = () => {
      const diff = end - Date.now();
      if (diff <= 0) {
        root.classList.add("is-ended");
        return;
      }
      const s = Math.floor(diff / 1000);
      const v: Record<string, number> = {
        d: Math.floor(s / 86400),
        h: Math.floor((s % 86400) / 3600),
        m: Math.floor((s % 3600) / 60),
        s: s % 60,
      };
      for (const el of cdEls) {
        const n = v[el.dataset.cd ?? ""];
        if (n != null) el.textContent = (n < 10 ? "0" : "") + n;
      }
      timer = setTimeout(tick, 1000);
    };
    tick();

    // --- Copiar cupón (botones con data-copy) ---
    const copyButtons = Array.from(root.querySelectorAll<HTMLElement>("[data-copy]"));
    const copyHandlers = copyButtons.map((btn) => {
      const handler = () => {
        const text = btn.dataset.copy ?? "";
        const original = btn.textContent;
        const done = () => {
          btn.textContent = "¡Copiado!";
          setTimeout(() => {
            btn.textContent = original ?? "Copiar cupón";
          }, 2000);
        };
        if (navigator.clipboard) {
          navigator.clipboard.writeText(text).then(done, done);
        } else {
          done();
        }
      };
      btn.addEventListener("click", handler);
      return { btn, handler };
    });

    return () => {
      if (timer) clearTimeout(timer);
      for (const { btn, handler } of scrollHandlers) btn.removeEventListener("click", handler);
      for (const { btn, handler } of copyHandlers) btn.removeEventListener("click", handler);
    };
  }, []);

  return (
    <>
      <style dangerouslySetInnerHTML={{ __html: css + fontAlias + extraCss }} />
      <div ref={rootRef} className="eac-curso" dangerouslySetInnerHTML={{ __html: html }} />
    </>
  );
}
