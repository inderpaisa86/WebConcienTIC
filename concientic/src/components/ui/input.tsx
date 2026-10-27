import { cn } from "@/lib/utils";

/**
 * Primitiva de input estilada con los tokens de concientic.
 *
 * Es un `<input>` nativo (no la primitiva controlada de base-ui) para que
 * integre de forma directa con `register` de react-hook-form, igual que el
 * patrón de formularios ya existente en el proyecto. Soporta `aria-invalid`
 * para marcar errores de validación.
 */
function Input({ className, type, ...props }: React.ComponentProps<"input">) {
  return (
    <input
      type={type}
      data-slot="input"
      className={cn(
        "flex h-10 w-full rounded-lg border border-border bg-background px-3 py-2 text-sm text-foreground transition-colors outline-none",
        "placeholder:text-muted-foreground",
        "focus-visible:border-ring focus-visible:ring-3 focus-visible:ring-ring/50",
        "disabled:cursor-not-allowed disabled:opacity-50",
        "aria-invalid:border-destructive aria-invalid:ring-3 aria-invalid:ring-destructive/20",
        className,
      )}
      {...props}
    />
  );
}

export { Input };
