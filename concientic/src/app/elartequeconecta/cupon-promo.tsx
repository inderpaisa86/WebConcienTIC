"use client";

import { useEffect, useState } from "react";

import styles from "./elartequeconecta.module.css";

// Corte de la promoción Noviembre Black: 1 de diciembre de 2026, 00:00 hora
// Colombia (UTC−5). A partir de ese instante el cupón deja de mostrarse solo.
const FIN_PROMO = new Date("2026-12-01T00:00:00-05:00").getTime();

/**
 * Cupón de la promoción Noviembre Black. La página es estática, así que la
 * vigencia se evalúa en el cliente (fecha real del visitante): durante
 * noviembre se muestra; desde el 1 de diciembre no se renderiza.
 */
export function CuponPromo() {
  // En el servidor (y primer render) no se dibuja, para que el HTML estático
  // no incluya un cupón vencido ni cause desajuste de hidratación. Tras montar
  // en el cliente, se evalúa la fecha real del visitante (fuera del render para
  // no leer el reloj durante el renderizado).
  const [vigente, setVigente] = useState(false);

  useEffect(() => {
    const id = setTimeout(() => setVigente(Date.now() < FIN_PROMO), 0);
    return () => clearTimeout(id);
  }, []);

  if (!vigente) return null;

  return (
    <p className={styles.coupon}>
      🎟️ <strong>BLACKNOV50</strong> <span className={styles.couponNote}>−50% · nov</span>
    </p>
  );
}
