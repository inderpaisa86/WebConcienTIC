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

/**
 * TEMPORAL: muestra el catálogo en producción aunque el número de WhatsApp
 * siga siendo el placeholder, solo para revisar el comportamiento real.
 * Con esto activo los botones "Pedir" NO funcionan (el número es falso).
 * La página sigue sin indexarse (noindex) y fuera del sitemap.
 * Quitar (poner en false o borrar) al cargar el número real.
 */
export const MOSTRAR_SIN_NUMERO = true;
