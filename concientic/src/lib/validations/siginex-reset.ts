import { z } from "zod";

/** Validación del formulario "solicitar recuperación" (cliente). */
export const siginexSolicitarResetSchema = z.object({
  email: z.string().trim().email("Introduce un correo válido."),
});

export type SiginexSolicitarResetInput = z.infer<
  typeof siginexSolicitarResetSchema
>;

/**
 * Validación del formulario "restablecer contraseña" (cliente). La política de
 * contraseña (mínimo 12) coincide con el `@Size(min=12)` del backend y se
 * valida también allí (Delegate), defensa en profundidad. `confirmar` debe
 * coincidir con `nuevaPassword`.
 */
export const siginexRestablecerSchema = z
  .object({
    nuevaPassword: z
      .string()
      .min(12, "La contraseña debe tener al menos 12 caracteres."),
    confirmar: z.string().min(1, "Confirma la contraseña."),
  })
  .refine((data) => data.nuevaPassword === data.confirmar, {
    message: "Las contraseñas no coinciden.",
    path: ["confirmar"],
  });

export type SiginexRestablecerInput = z.infer<typeof siginexRestablecerSchema>;
