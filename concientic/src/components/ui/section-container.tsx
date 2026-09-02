import { cva, type VariantProps } from "class-variance-authority";

import { cn } from "@/lib/utils";

const sectionVariants = cva("w-full", {
  variants: {
    background: {
      default: "bg-background text-foreground",
      muted: "bg-muted text-foreground",
      primary: "bg-primary text-primary-foreground",
    },
    spacing: {
      default: "py-16 md:py-24",
      compact: "py-10 md:py-14",
      none: "",
    },
  },
  defaultVariants: {
    background: "default",
    spacing: "default",
  },
});

type SectionContainerProps = React.ComponentProps<"section"> &
  VariantProps<typeof sectionVariants> & {
    /** Ancho máximo del contenido interno. */
    contentClassName?: string;
  };

/**
 * Contenedor de sección reutilizable.
 * - Aplica fondo y espaciado vertical vía variantes.
 * - Centra el contenido con ancho máximo y padding horizontal responsivo.
 * - Acepta `id` para anclas de navegación (scroll suave).
 */
function SectionContainer({
  className,
  contentClassName,
  background,
  spacing,
  children,
  ...props
}: SectionContainerProps) {
  return (
    <section
      data-slot="section-container"
      className={cn(sectionVariants({ background, spacing }), className)}
      {...props}
    >
      <div className={cn("mx-auto w-full max-w-6xl px-6 md:px-8", contentClassName)}>
        {children}
      </div>
    </section>
  );
}

export { SectionContainer, sectionVariants };
