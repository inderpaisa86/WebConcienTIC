import { Badge } from "@/components/ui/badge";
import type { DiagnosticoResumen } from "@/lib/siginex";

/**
 * Tabla de diagnósticos (presentacional, server component). Usa `<table>`
 * nativo con `<th scope="col">` (RNF5) y solo tokens/componentes de concientic.
 * El detalle NO navega a una ruta funcional en 3b: se muestra como texto
 * deshabilitado "Detalle (próximamente)" para no producir un 404.
 */

/** Mapeo de nivel SGI → nombre, portado de la referencia (Siginex). */
const NIVEL_NOMBRE: Record<number, string> = {
  1: "Inicial",
  2: "Básico",
  3: "En desarrollo",
  4: "Optimizado",
  5: "Excelencia",
};

/**
 * Formatea el score: "—" si `scoreSgi` es null; `${scoreSgi} · ${nivel}` si el
 * nivel existe y está en 1–5; solo el score si `nivelSgi` es null o cae fuera
 * del rango conocido (se omite el separador colgante).
 */
function formatearScore(scoreSgi: string | null, nivelSgi: number | null): string {
  if (scoreSgi == null) {
    return "—";
  }
  const nombre = nivelSgi != null ? NIVEL_NOMBRE[nivelSgi] : undefined;
  return nombre ? `${scoreSgi} · ${nombre}` : scoreSgi;
}

export function DiagnosticosTabla({
  diagnosticos,
}: {
  diagnosticos: DiagnosticoResumen[];
}) {
  if (diagnosticos.length === 0) {
    return (
      <p className="rounded-xl border border-dashed border-border bg-card p-6 text-center text-sm text-muted-foreground">
        Aún no hay diagnósticos. Crea el primero arriba.
      </p>
    );
  }

  return (
    <div className="overflow-hidden rounded-xl border border-border bg-card shadow-sm">
      <table className="w-full text-sm">
        <thead>
          <tr className="border-b border-border text-left">
            <th
              scope="col"
              className="px-4 py-3 text-xs font-semibold tracking-wide text-muted-foreground uppercase"
            >
              Empresa
            </th>
            <th
              scope="col"
              className="px-4 py-3 text-xs font-semibold tracking-wide text-muted-foreground uppercase"
            >
              Estado
            </th>
            <th
              scope="col"
              className="px-4 py-3 text-xs font-semibold tracking-wide text-muted-foreground uppercase"
            >
              Score
            </th>
            <th
              scope="col"
              className="px-4 py-3 text-xs font-semibold tracking-wide text-muted-foreground uppercase"
            >
              <span className="sr-only">Acciones</span>
            </th>
          </tr>
        </thead>
        <tbody>
          {diagnosticos.map((d) => (
            <tr key={d.id} className="border-b border-border last:border-0">
              <td className="px-4 py-3 font-medium text-foreground">
                {d.organizacionNombre}
              </td>
              <td className="px-4 py-3">
                <Badge variant={d.estado === "calculado" ? "accent" : "muted"}>
                  {d.estado}
                </Badge>
              </td>
              <td className="px-4 py-3 text-foreground">
                {formatearScore(d.scoreSgi, d.nivelSgi)}
              </td>
              <td className="px-4 py-3 text-right">
                <span
                  aria-disabled="true"
                  className="text-sm text-muted-foreground"
                >
                  Detalle (próximamente)
                </span>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
