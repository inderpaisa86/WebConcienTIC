/**
 * Número de WhatsApp del catálogo, con indicativo y sin signos (ej. "573001234567").
 * Es el ÚNICO lugar donde hay que cambiarlo: se sustituye en todos los botones
 * "Pedir", en la vista rápida y en los CTA generales.
 *
 * Si algún día vuelve a ser un placeholder (con "X"), la página deja de mostrarse
 * en producción (404), no se indexa y no entra al sitemap.
 */
export const WHATSAPP_NUMBER = "573133072355";

export const CATALOGO_LISTO = !/x/i.test(WHATSAPP_NUMBER);
