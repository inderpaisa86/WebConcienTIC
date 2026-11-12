"use client";

import { useEffect, useRef } from "react";

import { WHATSAPP_NUMBER } from "./config";
import { catalogoCssB64 } from "./_data/estilos";
import { catalogoHtmlB64 } from "./_data/cuerpo";
import { catalogoPiezasB64 } from "./_data/piezas";

const WA_PLACEHOLDER = "57XXXXXXXXXX";

type Opcion = {
  n: string; // nombre de la opción
  i: string; // imagen
  w: string; // enlace de WhatsApp
};

type Pieza = {
  n: string; // nombre
  c: string; // categoría
  i: string; // imagen
  d: string; // descripción
  l: string[]; // detalles
  p: string; // precio (HTML)
  w: string; // enlace de WhatsApp
  o?: Opcion[]; // opciones de color (solo la caja navideña)
};

// Fin de la promoción Noviembre Black: 30/11/2026 23:59 (UTC-5). Ver generator/catalogo_v3.py.
const PROMO_FIN = new Date("2026-11-30T23:59:59-05:00");

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
 * Catálogo de piezas de El Arte que Conecta (V3). Renderiza el HTML y el CSS
 * generados por generator/catalogo_v3.py y reproduce su JavaScript (filtros,
 * promoción Noviembre Black, vista rápida con opciones) scopeado al contenedor.
 */
export function PaginaCatalogo() {
  const rootRef = useRef<HTMLDivElement>(null);
  const css = decode(catalogoCssB64);
  const html = withWa(decode(catalogoHtmlB64));

  useEffect(() => {
    const root = rootRef.current;
    if (!root) return;

    const piezas = JSON.parse(withWa(decode(catalogoPiezasB64))) as Record<string, Pieza>;
    const q = <T extends HTMLElement>(sel: string) => root.querySelector<T>(sel);

    const modal = q<HTMLElement>("#modal");
    const count = q<HTMLElement>("#count");

    // Promoción terminada: se ocultan descuentos y se muestra el precio normal,
    // sin volver a publicar. Mismo criterio que la referencia.
    if (new Date() > PROMO_FIN) {
      root.classList.add("promo-off");
      root.querySelectorAll<HTMLElement>(".pc-price.promo").forEach((e) => {
        e.outerHTML = `<span class="pc-price"><b class="pr-now">${e.dataset.normal ?? ""}</b></span>`;
      });
      for (const pieza of Object.values(piezas)) {
        const m = pieza.p.match(/data-normal="([^"]+)"/);
        if (pieza.p.includes("promo") && m) {
          pieza.p = `<span class="pc-price"><b class="pr-now">${m[1]}</b></span>`;
        }
      }
      root.querySelectorAll<HTMLElement>('.pc[data-promo="1"]').forEach((c) => {
        c.dataset.promo = "0";
      });
    }

    const filtrar = (f: string) => {
      let n = 0;
      root.querySelectorAll<HTMLElement>(".filters button").forEach((b) => {
        b.classList.toggle("on", b.dataset.f === f);
      });
      root.querySelectorAll<HTMLElement>("#grid .pc").forEach((card) => {
        const visible =
          f === "all" || card.dataset.cat === f || (f === "promo" && card.dataset.promo === "1");
        card.style.display = visible ? "" : "none";
        if (visible) n++;
      });
      if (count) count.textContent = `${n} piezas`;
    };

    const abrir = (clave: string) => {
      const d = piezas[clave];
      if (!d || !modal) return;
      const img = q<HTMLImageElement>("#mimg");
      const wa = q<HTMLAnchorElement>("#mwa");
      const lista = q<HTMLElement>("#mlist");
      const opciones = q<HTMLElement>("#mopts");

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
      const precio = q<HTMLElement>("#mprice");
      if (precio) precio.innerHTML = d.p;
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

      // Opciones de color (caja navideña): cambian la foto y el enlace de WhatsApp.
      if (opciones) {
        opciones.replaceChildren();
        (d.o ?? []).forEach((opcion, idx) => {
          const boton = document.createElement("button");
          boton.type = "button";
          boton.className = `mopt${idx === 0 ? " on" : ""}`;
          const miniatura = document.createElement("img");
          miniatura.alt = "";
          miniatura.src = opcion.i;
          const etiqueta = document.createElement("span");
          etiqueta.textContent = opcion.n;
          boton.append(miniatura, etiqueta);
          boton.onclick = () => {
            if (img) img.src = opcion.i;
            if (wa) wa.href = opcion.w;
            opciones.querySelectorAll(".mopt").forEach((x) => x.classList.remove("on"));
            boton.classList.add("on");
          };
          opciones.appendChild(boton);
        });
        const primera = d.o?.[0];
        if (primera) {
          if (img) img.src = primera.i;
          if (wa) wa.href = primera.w;
        }
      }
      modal.classList.add("on");
    };

    const cerrar = () => modal?.classList.remove("on");

    // Delegación de eventos: un solo listener para filtros, categorías, enlace de
    // promoción, vista rápida y cierre del modal.
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
      if (t.closest("[data-promo-link]")) {
        e.preventDefault();
        filtrar("promo");
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
      <style dangerouslySetInnerHTML={{ __html: css + fontAlias }} />
      <div ref={rootRef} className="eac-catalogo" dangerouslySetInnerHTML={{ __html: html }} />
    </>
  );
}
