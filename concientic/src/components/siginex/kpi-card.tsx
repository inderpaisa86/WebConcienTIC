import { Card, CardContent } from "@/components/ui/card";

/**
 * Tarjeta de KPI del panel (presentacional, server component). Sin datos
 * propios: recibe una etiqueta y un valor ya calculados. Usa solo tokens de
 * concientic (`bg-card`, `text-foreground`, `text-muted-foreground`).
 */
export function KpiCard({
  etiqueta,
  valor,
}: {
  etiqueta: string;
  valor: number | string;
}) {
  return (
    <Card className="gap-2 py-5">
      <CardContent className="flex flex-col gap-1">
        <span className="text-sm text-muted-foreground">{etiqueta}</span>
        <span className="font-heading text-3xl font-semibold text-foreground">
          {valor}
        </span>
      </CardContent>
    </Card>
  );
}
