import { z } from "zod";

/** Planes que pueden solicitar contacto desde la landing de SIGINEX. */
export const LEAD_PLANES = ["Profesional", "Empresa"] as const;

/**
 * Celular colombiano: 10 dígitos que empiezan por 3. Acepta el prefijo +57 o 57
 * y separadores (espacios, guiones), que se retiran antes de validar.
 */
const celularSchema = z
  .string()
  .transform((valor) => valor.replace(/\D/g, "").replace(/^57(?=3\d{9}$)/, ""))
  .pipe(z.string().regex(/^3\d{9}$/, "El celular debe tener 10 dígitos y empezar por 3."));

export const leadSchema = z.object({
  plan: z.enum(LEAD_PLANES, { message: "Plan no válido." }),
  empresa: z
    .string()
    .trim()
    .min(1, "Escribe el nombre de la empresa.")
    .max(120, "El nombre de la empresa es demasiado largo."),
  email: z
    .string()
    .trim()
    .toLowerCase()
    .email("Revisa el correo electrónico.")
    .max(160, "El correo es demasiado largo."),
  celular: celularSchema,
  autoriza_datos: z.literal(true, {
    message: "Necesitamos tu autorización de tratamiento de datos.",
  }),
  politica_version: z.string().trim().min(1).max(20),
  origen: z.string().trim().max(60).optional(),
  // Honeypot: el campo está oculto para personas. Si llega con texto, es un bot.
  sitio_web: z.string().max(0, "Revisa los datos e inténtalo de nuevo.").optional(),
  ms_en_formulario: z.number().int().nonnegative().optional(),
});

export type LeadInput = z.infer<typeof leadSchema>;
