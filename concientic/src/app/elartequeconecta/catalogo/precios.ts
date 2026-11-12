/**
 * Precios del catálogo en COP, tomados de Catalogo_ElArteQueConecta.pdf.
 * Es el ÚNICO lugar donde se editan: las tarjetas y la vista rápida los leen de aquí.
 *
 * - `normal`:  precio de lista.
 * - `feria`:   precio de feria (promoción de evento). Borra esta línea cuando termine.
 * - `desde`:   el precio es "desde" (varía según tamaño/diseño).
 * - `paquete`: oferta por varias unidades, en texto (ej. "4 x $120.000").
 *
 * Las piezas que no aparecen aquí siguen mostrando "Precio a consultar".
 */
export type Precio = {
  normal: number;
  feria?: number;
  desde?: boolean;
  paquete?: string;
};

export const PRECIOS: Record<string, Precio> = {
  // Bolsos
  santorini: { normal: 220000, feria: 180000 },
  paris: { normal: 200000, feria: 160000 },
  bahia: { normal: 180000, feria: 140000 },
  verona: { normal: 240000, feria: 200000 },
  // Accesorios
  strap: { normal: 35000 },
  corazon: { normal: 35000, feria: 25000 },
  infinito: { normal: 35000, feria: 25000 },
  // Hogar
  // Espejo: el PDF trae dos precios de feria distintos ($145.000 y $130.000).
  // Hasta confirmar cuál es, se muestra solo el precio "desde".
  espejo: { normal: 185000, desde: true },
  portamacetas: { normal: 80000 },
  prana: { normal: 680000 },
  hojas: { normal: 380000 },
  chakras: { normal: 110000 },
  embarrilado: { normal: 210000 },
  cojin: { normal: 200000, feria: 160000 },
  // Navidad
  arbolpared: { normal: 180000, feria: 140000 },
  arbolito: { normal: 110000 },
  corona: { normal: 120000 },
  adorno: { normal: 35000, paquete: "4 x $120.000" },
};

/** 180000 -> "$180.000" (formato manual: idéntico en servidor y navegador). */
export const cop = (n: number): string => "$" + String(n).replace(/\B(?=(\d{3})+(?!\d))/g, ".");

/** HTML del precio dentro de la tarjeta de la grilla. */
export function precioTarjetaHtml(p: Precio): string {
  if (p.feria) {
    return (
      `<span class="pc-price pc-feria"><s>${cop(p.normal)}</s>` +
      `<b>${cop(p.feria)}</b><em>Precio de feria</em></span>`
    );
  }
  if (p.paquete) {
    return (
      `<span class="pc-price pc-feria"><b>${cop(p.normal)}</b>` +
      `<em>${p.paquete} · precio de feria</em></span>`
    );
  }
  if (p.desde) {
    return `<span class="pc-price pc-feria"><em>Desde</em><b>${cop(p.normal)}</b></span>`;
  }
  return `<span class="pc-price">${cop(p.normal)}</span>`;
}

/** Texto plano del precio para la vista rápida (modal). */
export function precioModalTexto(p: Precio): string {
  if (p.feria) return `${cop(p.feria)} · Precio de feria (antes ${cop(p.normal)})`;
  if (p.paquete) return `${cop(p.normal)} · ${p.paquete} precio de feria`;
  if (p.desde) return `Desde ${cop(p.normal)}`;
  return cop(p.normal);
}
