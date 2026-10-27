import { cn } from "@/lib/utils";

/**
 * Primitiva de label estilada con los tokens de concientic.
 * `<label>` nativo para asociarse a los controles por `htmlFor`.
 */
function Label({ className, ...props }: React.ComponentProps<"label">) {
  return (
    <label
      data-slot="label"
      className={cn(
        "text-sm font-medium text-foreground select-none",
        "peer-disabled:cursor-not-allowed peer-disabled:opacity-50",
        className,
      )}
      {...props}
    />
  );
}

export { Label };
