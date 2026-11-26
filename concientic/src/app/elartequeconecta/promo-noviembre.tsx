"use client";

import { useEffect, useState } from "react";

import styles from "./elartequeconecta.module.css";

// Fin de la promoción Noviembre Black: 1 de diciembre de 2026, 00:00 hora
// Colombia (UTC−5). Desde ese instante la línea de descuento deja de mostrarse.
const FIN_PROMO = new Date("2026-12-01T00:00:00-05:00").getTime();

/**
 * Aviso "Descuento Noviembre Black" de la sección Catálogo. La página es
 * estática, así que la vigencia se evalúa en el navegador del visitante.
 */
export function PromoNoviembre() {
  // El primer render no dibuja nada: así el HTML estático no muestra un
  // descuento vencido y no hay desajuste de hidratación.
  const [vigente, setVigente] = useState(false);

  useEffect(() => {
    const id = setTimeout(() => setVigente(Date.now() < FIN_PROMO), 0);
    return () => clearTimeout(id);
  }, []);

  if (!vigente) return null;

  return (
    <p className={styles.cpPromo}>
      ⚡ <b>Descuento Noviembre Black:</b> precios especiales hasta el 30 de noviembre
    </p>
  );
}
