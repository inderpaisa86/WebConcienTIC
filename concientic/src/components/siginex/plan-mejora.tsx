import { Badge } from "@/components/ui/badge";
import type { TareaPlan } from "@/lib/siginex";

/**
 * Lista de tareas del plan de mejora (presentacional, server component).
 * Portado de `Siginex/src/components/plan/PlanMejora.tsx`, adaptado a tokens de
 * concientic (sin la paleta navy+dorado literal del original).
 *
 * El original mapeaba `skillId → nombre` con `MODULOS_SGI` (banco local). Aquí
 * no se dispone de ese banco en el frontend (el KB vive en la API Java), así que
 * se muestra el `skillId` tal cual como etiqueta del módulo.
 */

/** Variante de Badge por prioridad (`alta`/`media`/`baja` o desconocida). */
function variantePrioridad(
  prioridad: string | null,
): "default" | "secondary" | "muted" {
  switch (prioridad) {
    case "alta":
      return "default";
    case "media":
      return "secondary";
    default:
      return "muted";
  }
}

export function PlanMejora({ tareas }: { tareas: TareaPlan[] }) {
  if (tareas.length === 0) {
    return (
      <p className="rounded-xl border border-dashed border-border bg-card p-6 text-center text-sm text-muted-foreground">
        No hay tareas de plan de mejora. Se generan al calcular un diagnóstico
        con brechas.
      </p>
    );
  }

  return (
    <div className="space-y-3">
      {tareas.map((t, i) => (
        <div
          key={`${t.skillId}-${t.preguntaId ?? i}`}
          className="rounded-xl border border-border bg-card p-4 shadow-sm"
        >
          <div className="mb-2 flex items-start gap-3">
            <h4 className="flex-1 font-heading text-[14.5px] leading-snug font-semibold text-foreground">
              {t.brecha ?? t.accion ?? "Tarea de mejora"}
            </h4>
            {t.prioridad ? (
              <Badge variant={variantePrioridad(t.prioridad)}>
                {t.prioridad}
              </Badge>
            ) : null}
          </div>

          {t.accion ? (
            <p className="mb-3 text-[13px] leading-relaxed text-foreground/80">
              {t.accion}
            </p>
          ) : null}

          <div className="flex flex-wrap gap-x-5 gap-y-1.5 text-[11px] text-muted-foreground">
            <Badge variant="outline">{t.skillId}</Badge>
            {t.responsable ? <span>Responsable: {t.responsable}</span> : null}
            {t.plazoDias != null ? <span>Plazo: {t.plazoDias} días</span> : null}
            {t.fase != null ? <span>Fase {t.fase}</span> : null}
            {t.normaRef ? <span>{t.normaRef}</span> : null}
            {t.criterioCierre ? (
              <span className="basis-full text-foreground/70">
                Criterio de cierre: {t.criterioCierre}
              </span>
            ) : null}
          </div>
        </div>
      ))}
    </div>
  );
}
