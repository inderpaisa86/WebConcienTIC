/**
 * Número de WhatsApp del catálogo, con indicativo y sin signos (ej. "573001234567").
 * Es el ÚNICO lugar donde hay que cambiarlo: se sustituye en todos los botones
 * "Pedir", en la vista rápida y en los CTA generales.
 *
 * Mientras siga el placeholder, la página NO se muestra en producción (404),
 * no se indexa y no entra al sitemap (la spec del catálogo prohíbe publicarla
 * con el número sin definir). En desarrollo sigue visible para revisarla.
 */
export const WHATSAPP_NUMBER = "57XXXXXXXXXX";

export const CATALOGO_LISTO = !/x/i.test(WHATSAPP_NUMBER);
