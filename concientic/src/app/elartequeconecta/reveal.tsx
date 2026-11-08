"use client";

import { useEffect, useRef, useState, type ElementType } from "react";

import styles from "./elartequeconecta.module.css";

type RevealProps = {
  children: React.ReactNode;
  /** Elemento HTML a renderizar (por defecto div). */
  as?: ElementType;
  /** Retraso escalonado en ms, para que las tarjetas aparezcan en cascada. */
  delay?: number;
  className?: string;
};

/**
 * Envoltorio que revela su contenido con un fade + desplazamiento suave
 * cuando entra en el viewport. Usa IntersectionObserver (nativo, sin librerías).
 * Respeta prefers-reduced-motion vía CSS: si el usuario pide menos movimiento,
 * el contenido aparece de inmediato, sin transición.
 */
export function Reveal({ children, as, delay = 0, className }: RevealProps) {
  const Tag = as ?? "div";
  const ref = useRef<HTMLElement>(null);
  // Si el navegador no soporta IntersectionObserver, mostramos el contenido
  // de inmediato (sin animar) desde el primer render.
  const [visible, setVisible] = useState(() => typeof IntersectionObserver === "undefined");

  useEffect(() => {
    const node = ref.current;
    if (!node || typeof IntersectionObserver === "undefined") return;

    const observer = new IntersectionObserver(
      (entries) => {
        for (const entry of entries) {
          if (entry.isIntersecting) {
            setVisible(true);
            observer.disconnect();
            break;
          }
        }
      },
      { threshold: 0.15, rootMargin: "0px 0px -10% 0px" },
    );

    observer.observe(node);
    return () => observer.disconnect();
  }, []);

  const classes = [styles.reveal, visible ? styles.revealVisible : "", className]
    .filter(Boolean)
    .join(" ");

  return (
    <Tag
      ref={ref}
      className={classes}
      style={delay ? { transitionDelay: `${delay}ms` } : undefined}
    >
      {children}
    </Tag>
  );
}
