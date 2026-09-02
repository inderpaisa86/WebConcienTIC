import { z } from "zod";

export const contactSchema = z.object({
  name: z
    .string()
    .trim()
    .min(2, "El nombre es obligatorio.")
    .max(100, "El nombre es demasiado largo."),
  email: z.string().trim().email("Introduce un correo válido."),
  company: z.string().trim().max(120, "El nombre de empresa es demasiado largo.").optional(),
  message: z
    .string()
    .trim()
    .min(10, "Cuéntanos un poco más (mínimo 10 caracteres).")
    .max(2000, "El mensaje es demasiado largo."),
  consent: z.literal(true, {
    message: "Debes aceptar la política de datos.",
  }),
  // Honeypot anti-spam: debe venir vacío. No se muestra al usuario.
  website: z.string().max(0).optional(),
});

export type ContactInput = z.infer<typeof contactSchema>;
