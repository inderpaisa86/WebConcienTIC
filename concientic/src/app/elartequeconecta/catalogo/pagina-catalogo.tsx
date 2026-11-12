"use client";

import { useEffect, useRef } from "react";

import { WHATSAPP_NUMBER } from "./config";
import { catalogoCssB64 } from "./_data/estilos";
import { catalogoHtmlB64 } from "./_data/cuerpo";
import { catalogoPiezasB64 } from "./_data/piezas";
import { PRECIOS, precioModalTexto, precioTarjetaHtml } from "./precios";

const WA_PLACEHOLDER = "57XXXXXXXXXX";

type Pieza = {
  n: string; // nombre
  c: string; // categoría
  i: string; // imagen
  d: string; // descripción
  l: string[]; // detalles
  p: string; // precio
  w: string; // enlace de WhatsApp
};

function decode(b64: string): string {
  if (typeof window === "undefined") {
    return Buffer.from(b64, "base64").toString("utf-8");
  }
  const binary = atob(b64);
  const bytes = Uint8Array.from(binary, (c) => c.charCodeAt(0));
  return new TextDecoder("utf-8").decode(bytes);
}

// El número real se inyecta aquí, una sola vez, para todo el HTML y los datos.
const withWa = (s: string) => s.split(WA_PLACEHOLDER).join(WHATSAPP_NUMBER);

// Sustituye "Precio a consultar" por el precio de cada pieza (ver ./precios).
const PLACEHOLDER_PRECIO = '<span class="pc-price ask">Precio a consultar</span>';
function conPrecios(html: string): string {
  let out = html;
  for (const [id, precio] of Object.entries(PRECIOS)) {
    const inicio = out.indexOf(`data-id="${id}"`);
    if (inicio === -1) continue;
    const pos = out.indexOf(PLACEHOLDER_PRECIO, inicio);
    if (pos === -1) continue;
    out =
      out.slice(0, pos) + precioTarjetaHtml(precio) + out.slice(pos + PLACEHOLDER_PRECIO.length);
  }
  return out;
}

// Estilos del precio con oferta (paleta del catálogo). Se añaden al CSS original.
const preciosCss = `
  .eac-catalogo .pc-price.pc-feria{display:flex;flex-direction:column;align-items:flex-start;gap:1px;line-height:1.1}
  .eac-catalogo .pc-price.pc-feria s{font-family:var(--sans);font-size:.78rem;color:var(--taupe)}
  .eac-catalogo .pc-price.pc-feria b{font-weight:600;font-size:1.15rem;color:var(--title)}
  .eac-catalogo .pc-price.pc-feria em{font-family:var(--sans);font-style:normal;font-size:.72rem;font-weight:700;color:var(--terra)}
`;

// El CSS original define --serif/--sans/--hand con nombres literales. Los
// redefinimos con las fuentes next/font que carga el layout de /elartequeconecta.
const fontAlias = `
  .eac-catalogo{
    --serif: var(--eac-font-serif), "Fraunces", Georgia, serif;
    --sans: var(--eac-font-sans), "DM Sans", Arial, sans-serif;
    --hand: var(--eac-font-hand), "Caveat", cursive;
    font-family: var(--sans);
  }
`;

/**
 * Catálogo de piezas de El Arte que Conecta. Renderiza fielmente el HTML/CSS
 * original y reimplementa su JS (filtros, contador, categorías y vista rápida)
 * scopeado al contenedor, sin tocar el documento global.
 */
export function PaginaCatalogo() {
  const rootRef = useRef<HTMLDivElement>(null);
  const css = decode(catalogoCssB64);
  const html = conPrecios(withWa(decode(catalogoHtmlB64)));

  useEffect(() => {
    const root = rootRef.current;
    if (!root) return;

    const piezas = JSON.parse(withWa(decode(catalogoPiezasB64))) as Record<string, Pieza>;
    for (const [id, precio] of Object.entries(PRECIOS)) {
      if (piezas[id]) piezas[id].p = precioModalTexto(precio);
    }
    const q = <T extends HTMLElement>(sel: string) => root.querySelector<T>(sel);

    const modal = q<HTMLElement>("#modal");
    const count = q<HTMLElement>("#count");

    const filtrar = (f: string) => {
      let n = 0;
      root.querySelectorAll<HTMLElement>(".filters button").forEach((b) => {
        b.classList.toggle("on", b.dataset.f === f);
      });
      root.querySelectorAll<HTMLElement>("#grid .pc").forEach((card) => {
        const visible = f === "all" || card.dataset.cat === f;
        card.style.display = visible ? "" : "none";
        if (visible) n++;
      });
      if (count) count.textContent = `${n} piezas`;
    };

    const abrir = (clave: string) => {
      const d = piezas[clave];
      if (!d || !modal) return;
      const img = q<HTMLImageElement>("#mimg");
      const lista = q<HTMLElement>("#mlist");
      if (img) {
        img.src = d.i;
        img.alt = d.n;
      }
      const set = (sel: string, text: string) => {
        const el = q<HTMLElement>(sel);
        if (el) el.textContent = text;
      };
      set("#mcat", d.c);
      set("#mname", d.n);
      set("#mdesc", d.d);
      set("#mprice", d.p);
      const wa = q<HTMLAnchorElement>("#mwa");
      if (wa) wa.href = d.w;
      if (lista) {
        lista.replaceChildren(
          ...d.l.map((texto) => {
            const li = document.createElement("li");
            li.textContent = texto;
            return li;
          }),
        );
      }
      modal.classList.add("on");
    };

    const cerrar = () => modal?.classList.remove("on");

    // Delegación de eventos: un solo listener para filtros, categorías y modal.
    const onClick = (e: MouseEvent) => {
      const t = e.target as HTMLElement;

      const filtro = t.closest<HTMLElement>(".filters button");
      if (filtro) {
        filtrar(filtro.dataset.f ?? "all");
        return;
      }
      const cat = t.closest<HTMLElement>(".cat");
      if (cat) {
        filtrar(cat.dataset.go ?? "all");
        q("#piezas")?.scrollIntoView({ behavior: "smooth" });
        return;
      }
      const abre = t.closest<HTMLElement>("[data-open]");
      if (abre) {
        abrir(abre.dataset.open ?? "");
        return;
      }
      if (t === modal || t.classList.contains("mclose")) cerrar();
    };

    const onKey = (e: KeyboardEvent) => {
      if (e.key === "Escape") cerrar();
    };

    root.addEventListener("click", onClick);
    document.addEventListener("keydown", onKey);
    return () => {
      root.removeEventListener("click", onClick);
      document.removeEventListener("keydown", onKey);
    };
  }, []);

  return (
    <>
      <style dangerouslySetInnerHTML={{ __html: css + fontAlias + preciosCss }} />
      <div ref={rootRef} className="eac-catalogo" dangerouslySetInnerHTML={{ __html: html }} />
    </>
  );
}
