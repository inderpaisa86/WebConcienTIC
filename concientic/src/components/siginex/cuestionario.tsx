"use client";

import { useCallback, useEffect, useMemo, useState } from "react";
import { useRouter } from "next/navigation";

import { Button } from "@/components/ui/button";
import {
  guardarRespuestas,
  isSiginexDataError,
  listarModulos,
  listarPreguntas,
  obtenerRespuestas,
  type KbModulo,
  type KbPregunta,
  type RespuestaItem,
  type ValorRespuesta,
} from "@/lib/siginex";

/**
 * Cuestionario del diagnóstico SIGINEX (sub-fase 3c), portado desde
 * `Siginex/src/app/diagnostico/[id]/Cuestionario.tsx`.
 *
 * Cambios respecto al original:
 * - Los datos vienen de la API Java vía `lib/siginex` (modelo B/BFF), no del
 *   banco KB local ni de server actions: `listarModulos()`, y por cada módulo
 *   `listarPreguntas(moduloId)` (perezoso, con caché en memoria). Las respuestas
 *   iniciales se precargan con `obtenerRespuestas(diagnosticoId)`.
 * - Estilo con tokens de concientic (sin navy+dorado literal), coherente con el
 *   resto de componentes `siginex/` de la 3a/3b.
 * - Valores expuestos: `0 | 1 | 2 | na` (subconjunto del `@Pattern` del backend).
 * - `unauthenticated` redirige a `/siginex/login`, igual que `gestion-diagnosticos`.
 *
 * Es un componente CLIENT con estado; se monta dentro de `SessionGuard`.
 */

const OPCIONES: Array<{ valor: ValorRespuesta; label: string }> = [
  { valor: "0", label: "No cumple" },
  { valor: "1", label: "Parcial" },
  { valor: "2", label: "Cumple" },
  { valor: "na", label: "N/A" },
];

/** Clases del botón de opción según esté o no seleccionado. */
function clasesOpcion(seleccionada: boolean): string {
  return [
    "rounded-lg border px-3.5 py-2 text-sm font-semibold transition-colors",
    "focus-visible:outline-none focus-visible:ring-3 focus-visible:ring-ring/50",
    seleccionada
      ? "border-primary bg-primary text-primary-foreground"
      : "border-border bg-background text-muted-foreground hover:bg-muted hover:text-foreground",
  ].join(" ");
}

type EstadoCarga = "cargando" | "listo" | "error";

export function Cuestionario({ diagnosticoId }: { diagnosticoId: string }) {
  const router = useRouter();

  const [modulos, setModulos] = useState<KbModulo[]>([]);
  const [respuestas, setRespuestas] = useState<Record<string, string>>({});
  const [estado, setEstado] = useState<EstadoCarga>("cargando");
  const [mensajeError, setMensajeError] = useState<string | null>(null);

  const [moduloIdx, setModuloIdx] = useState(0);

  // Caché de preguntas por módulo (perezosa): se piden al navegar a un módulo y
  // se conservan. El error de preguntas se guarda junto al módulo al que
  // pertenece ({moduloId, mensaje}) para no mostrarlo en otro módulo; el estado
  // "cargando" se deriva (ver más abajo), no se guarda.
  const [preguntasPorModulo, setPreguntasPorModulo] = useState<
    Record<string, KbPregunta[]>
  >({});
  const [errorPreguntas, setErrorPreguntas] = useState<{
    moduloId: string;
    mensaje: string;
  } | null>(null);

  const [guardando, setGuardando] = useState(false);
  const [avisoGuardado, setAvisoGuardado] = useState<string | null>(null);

  const moduloActual = modulos[moduloIdx] ?? null;

  /** Normaliza un error de la capa de datos a un mensaje seguro; redirige en 401. */
  const resolverError = useCallback(
    (err: unknown): string => {
      if (isSiginexDataError(err) && err.kind === "unauthenticated") {
        router.replace("/siginex/login");
        return "Tu sesión no es válida o ha caducado.";
      }
      return isSiginexDataError(err)
        ? err.message
        : "Ha ocurrido un error inesperado. Inténtalo de nuevo.";
    },
    [router],
  );

  // Carga inicial: módulos + respuestas ya guardadas, en paralelo.
  const cargarBase = useCallback(async () => {
    setEstado("cargando");
    setMensajeError(null);
    try {
      const [mods, resp] = await Promise.all([
        listarModulos(),
        obtenerRespuestas(diagnosticoId),
      ]);
      setModulos(mods);
      setRespuestas(resp);
      setModuloIdx(0);
      setEstado("listo");
    } catch (err) {
      setMensajeError(resolverError(err));
      setEstado("error");
    }
  }, [diagnosticoId, resolverError]);

  useEffect(() => {
    let activo = true;
    Promise.all([listarModulos(), obtenerRespuestas(diagnosticoId)])
      .then(([mods, resp]) => {
        if (!activo) return;
        setModulos(mods);
        setRespuestas(resp);
        setEstado("listo");
      })
      .catch((err: unknown) => {
        if (!activo) return;
        setMensajeError(resolverError(err));
        setEstado("error");
      });
    return () => {
      activo = false;
    };
  }, [diagnosticoId, resolverError]);

  // Carga perezosa de las preguntas del módulo visible (si no están en caché).
  // Para no llamar setState de forma SÍNCRONA en el cuerpo del efecto
  // (react-hooks/set-state-in-effect), toda actualización de estado ocurre
  // dentro de los callbacks de la promesa, nunca antes de await/then. El estado
  // "cargando" se DERIVA (no hay clave en caché ni error para este módulo).
  useEffect(() => {
    if (!moduloActual) return;
    // Caché-hit: nada que hacer (no se toca estado).
    if (preguntasPorModulo[moduloActual.id]) return;

    let activo = true;
    const moduloId = moduloActual.id;
    void listarPreguntas(moduloId)
      .then((preguntas) => {
        if (!activo) return;
        setPreguntasPorModulo((prev) => ({ ...prev, [moduloId]: preguntas }));
      })
      .catch((err: unknown) => {
        if (!activo) return;
        setErrorPreguntas({ moduloId, mensaje: resolverError(err) });
      });

    return () => {
      activo = false;
    };
  }, [moduloActual, preguntasPorModulo, resolverError]);

  const preguntasActuales = moduloActual
    ? (preguntasPorModulo[moduloActual.id] ?? [])
    : [];

  // "Cargando" derivado: hay módulo visible, sin preguntas en caché y sin error
  // registrado para ESE módulo (evita un flash de "cargando" tras un fallo).
  const cargandoPreguntas =
    !!moduloActual &&
    !preguntasPorModulo[moduloActual.id] &&
    errorPreguntas?.moduloId !== moduloActual.id;

  // Error de preguntas solo si corresponde al módulo visible.
  const mensajeErrorPreguntas =
    moduloActual && errorPreguntas?.moduloId === moduloActual.id
      ? errorPreguntas.mensaje
      : null;

  // Progreso: nº de respuestas registradas sobre el total de preguntas YA
  // cargadas. El total crece a medida que se visitan módulos (no se conoce el
  // total global sin pedir todas las preguntas); se rotula como tal.
  const totalCargadas = useMemo(
    () =>
      Object.values(preguntasPorModulo).reduce((a, qs) => a + qs.length, 0),
    [preguntasPorModulo],
  );
  const respondidas = Object.keys(respuestas).length;

  function responder(preguntaId: string, valor: ValorRespuesta) {
    setAvisoGuardado(null);
    setRespuestas((prev) => ({ ...prev, [preguntaId]: valor }));
  }

  /** Lote del módulo actual: solo preguntas cargadas con respuesta válida. */
  function loteDelModulo(): RespuestaItem[] {
    return preguntasActuales
      .filter((q) => respuestas[q.id] !== undefined)
      .map((q) => ({
        preguntaId: q.id,
        valor: respuestas[q.id] as ValorRespuesta,
      }));
  }

  async function guardarModulo(): Promise<boolean> {
    const lote = loteDelModulo();
    if (lote.length === 0) {
      setAvisoGuardado("No hay respuestas nuevas que guardar en este módulo.");
      return true;
    }
    setGuardando(true);
    setMensajeError(null);
    try {
      await guardarRespuestas(diagnosticoId, { respuestas: lote });
      setAvisoGuardado("Respuestas del módulo guardadas.");
      return true;
    } catch (err) {
      setMensajeError(resolverError(err));
      return false;
    } finally {
      setGuardando(false);
    }
  }

  async function siguiente() {
    const ok = await guardarModulo();
    if (ok && moduloIdx < modulos.length - 1) {
      setModuloIdx((i) => i + 1);
      setAvisoGuardado(null);
    }
  }

  function anterior() {
    if (moduloIdx > 0) {
      setModuloIdx((i) => i - 1);
      setAvisoGuardado(null);
    }
  }

  function irAModulo(idx: number) {
    setModuloIdx(idx);
    setAvisoGuardado(null);
  }

  // ---- Estados de carga / error de la base (módulos + respuestas) ----
  if (estado === "cargando") {
    return (
      <p
        role="status"
        aria-live="polite"
        className="text-sm text-muted-foreground"
      >
        Cargando cuestionario…
      </p>
    );
  }

  if (estado === "error") {
    return (
      <div
        role="status"
        aria-live="polite"
        className="flex flex-col items-start gap-3 rounded-xl border border-border bg-card p-6"
      >
        <p className="text-sm text-muted-foreground">
          {mensajeError ?? "No se pudo cargar el cuestionario."}
        </p>
        <Button variant="outline" size="sm" onClick={() => void cargarBase()}>
          Reintentar
        </Button>
      </div>
    );
  }

  if (modulos.length === 0) {
    return (
      <p className="rounded-xl border border-dashed border-border bg-card p-6 text-center text-sm text-muted-foreground">
        El banco de conocimiento no tiene módulos disponibles.
      </p>
    );
  }

  const esUltimo = moduloIdx === modulos.length - 1;

  return (
    <div className="space-y-5">
      {/* Navegación de módulos + progreso */}
      <div className="flex flex-wrap items-center justify-between gap-4 rounded-xl border border-border bg-card p-4 shadow-sm">
        <div className="flex flex-wrap gap-1.5">
          {modulos.map((m, i) => (
            <button
              key={m.id}
              type="button"
              onClick={() => irAModulo(i)}
              aria-current={i === moduloIdx ? "step" : undefined}
              className={[
                "rounded-md px-2.5 py-1.5 text-xs font-semibold transition-colors",
                "focus-visible:outline-none focus-visible:ring-3 focus-visible:ring-ring/50",
                i === moduloIdx
                  ? "bg-primary text-primary-foreground"
                  : "bg-muted text-muted-foreground hover:text-foreground",
              ].join(" ")}
            >
              {m.short || m.nombre}
            </button>
          ))}
        </div>
        <div className="flex items-center gap-3">
          <span className="text-xs font-semibold text-muted-foreground">
            {respondidas}
            {totalCargadas > 0 ? `/${totalCargadas}` : ""} respondidas
          </span>
        </div>
      </div>

      {/* Preguntas del módulo */}
      <div className="rounded-xl border border-border bg-card p-6 shadow-sm">
        <h3 className="font-heading text-lg font-semibold text-foreground">
          {moduloActual?.nombre}
        </h3>
        <p className="mb-4 text-xs text-muted-foreground">
          Módulo {moduloIdx + 1} de {modulos.length}
          {preguntasActuales.length > 0
            ? ` · ${preguntasActuales.length} preguntas`
            : ""}
        </p>

        {cargandoPreguntas ? (
          <p
            role="status"
            aria-live="polite"
            className="text-sm text-muted-foreground"
          >
            Cargando preguntas…
          </p>
        ) : null}

        {errorPreguntas && mensajeErrorPreguntas ? (
          <div className="flex flex-col items-start gap-3 rounded-lg border border-border bg-background p-4">
            <p className="text-sm text-muted-foreground">
              {mensajeErrorPreguntas}
            </p>
            <Button
              variant="outline"
              size="sm"
              onClick={() => {
                if (!moduloActual) return;
                const moduloId = moduloActual.id;
                // Forzar recarga: limpiar el error de este módulo y quitarlo de
                // caché para re-disparar el efecto de carga perezosa.
                setErrorPreguntas((prev) =>
                  prev?.moduloId === moduloId ? null : prev,
                );
                setPreguntasPorModulo((prev) => {
                  const copia = { ...prev };
                  delete copia[moduloId];
                  return copia;
                });
              }}
            >
              Reintentar
            </Button>
          </div>
        ) : null}

        {!cargandoPreguntas && !mensajeErrorPreguntas ? (
          <div className="divide-y divide-border">
            {preguntasActuales.map((q, i) => (
              <div key={q.id} className="py-4">
                <div className="flex gap-3">
                  <span className="pt-0.5 text-xs font-semibold text-muted-foreground">
                    {String(i + 1).padStart(2, "0")}
                  </span>
                  <div className="flex-1">
                    <p className="text-[14.5px] leading-snug font-medium text-foreground">
                      {q.pregunta}
                    </p>
                    {q.norma ? (
                      <p className="mt-1 text-[11px] text-muted-foreground">
                        {q.norma}
                      </p>
                    ) : null}
                    <div className="mt-3 flex flex-wrap gap-2">
                      {OPCIONES.map((op) => {
                        const sel = respuestas[q.id] === op.valor;
                        return (
                          <button
                            key={op.valor}
                            type="button"
                            aria-pressed={sel}
                            onClick={() => responder(q.id, op.valor)}
                            className={clasesOpcion(sel)}
                          >
                            {op.label}
                          </button>
                        );
                      })}
                    </div>
                  </div>
                </div>
              </div>
            ))}
          </div>
        ) : null}

        {mensajeError ? (
          <p
            role="alert"
            aria-live="assertive"
            className="mt-4 text-sm text-destructive"
          >
            {mensajeError}
          </p>
        ) : null}

        {avisoGuardado ? (
          <p
            role="status"
            aria-live="polite"
            className="mt-4 text-sm text-muted-foreground"
          >
            {avisoGuardado}
          </p>
        ) : null}

        {/* Navegación */}
        <div className="mt-5 flex items-center justify-between gap-3">
          <Button
            variant="outline"
            onClick={anterior}
            disabled={moduloIdx === 0 || guardando}
          >
            Anterior
          </Button>
          <div className="flex gap-3">
            <Button
              variant="outline"
              onClick={() => void guardarModulo()}
              disabled={guardando}
            >
              {guardando ? "Guardando…" : "Guardar"}
            </Button>
            {!esUltimo ? (
              <Button onClick={() => void siguiente()} disabled={guardando}>
                {guardando ? "Guardando…" : "Siguiente módulo"}
              </Button>
            ) : (
              <Button onClick={() => void guardarModulo()} disabled={guardando}>
                {guardando ? "Guardando…" : "Guardar último módulo"}
              </Button>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
