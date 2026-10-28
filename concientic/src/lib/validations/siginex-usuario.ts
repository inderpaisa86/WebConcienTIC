import { z } from "zod";

/**
 * Validación del formulario de crear usuario (cliente). Coincide con el
 * `CrearUsuarioRequest` del backend: email válido, nombre no vacío, contraseña
 * mínimo 12 (igual que `@Size(min=12)`), rol admin|miembro.
 */
export const siginexUsuarioSchema = z.object({
  email: z.string().trim().email("Introduce un correo válido."),
  nombre: z.string().trim().min(1, "El nombre es obligatorio."),
  password: z
    .string()
    .min(12, "La contraseña debe tener al menos 12 caracteres."),
  rol: z.enum(["admin", "miembro"]),
});

export type SiginexUsuarioInput = z.infer<typeof siginexUsuarioSchema>;
