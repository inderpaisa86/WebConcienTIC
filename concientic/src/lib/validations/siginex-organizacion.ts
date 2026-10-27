import { z } from "zod";

/**
 * Validación del formulario de crear organización de SIGINEX (cliente).
 * El enum de `tamano` coincide con el `@Pattern` del backend
 * (`micro|pequena|mediana|grande`). `sector`/`tamano` son opcionales; el
 * formulario omite los vacíos antes de enviar.
 */
export const siginexOrganizacionSchema = z.object({
  nombre: z.string().trim().min(1, "El nombre es obligatorio."),
  sector: z.string().trim().optional(),
  // El `<select>` nativo emite "" para la opción "(sin especificar)". Se acepta
  // "" como alias de "sin tamaño"; el formulario lo normaliza a `undefined`
  // antes de enviar. El resto coincide con el `@Pattern` del backend.
  tamano: z
    .enum(["micro", "pequena", "mediana", "grande"])
    .or(z.literal(""))
    .optional(),
});

export type SiginexOrganizacionInput = z.infer<typeof siginexOrganizacionSchema>;
