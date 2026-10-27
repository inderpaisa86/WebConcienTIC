import { z } from "zod";

/** Validación del formulario de login de SIGINEX (cliente). */
export const siginexLoginSchema = z.object({
  email: z.string().trim().email("Introduce un correo válido."),
  password: z.string().min(1, "La contraseña es obligatoria."),
});

export type SiginexLoginInput = z.infer<typeof siginexLoginSchema>;
