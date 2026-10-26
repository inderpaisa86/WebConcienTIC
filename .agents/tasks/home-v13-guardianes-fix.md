# Reporte — Fix sección GUARDIANES (home ConcienTIC, v13)

Objetivo: rehacer la sección Guardianes de la home para que su **markup y estilo**
coincidan con `entregables_v7/docs/concientic_v13_reference.html` (`#guardianes`). No era
solo color: la estructura del componente (SectionHeader + `.guardianes-band` +
`.guardianes-group` + `<ul>.guardianes-competencias`) difería de la del HTML.

Iteración: **primera** (no existía `guardianes-review.json`). Implementado según
`.agents/tasks/guardianes-plan.md` y verificado leyendo el HTML de referencia como UTF-8.

## Estructura final emitida (`src/components/sections/guardianes.tsx`)

`<section id="guardianes" class="ct-section guardians-feature">`
→ `<div class="ct-container guardians-layout">`
  1. `div.guardians-kickers` con DOS `span.ct-eyebrow.guardians-kicker` (color `#008f70`):
     "Los Guardianes" y "Inteligencia con propósito".
  2. `h2.guardians-title`: "La IA tiene un papel. La persona conserva el protagonismo."
  3. `p.guardians-intro`: "Los Guardianes se integran donde pueden aportar orientación,
     protección, comprensión o creatividad. Cada intervención debe tener una razón clara y
     un límite definido."
  4. `div.guardian-image-wrap` con `role="img"` + alt oficial; 7 PNG decorativos (`alt=""`)
     compuestos como tira/mosaico (ver "Imagen de grupo").
  5. `div.guardian-capabilities` con `aria-label="Capacidades representadas por los Guardianes"`:
     fila de 7 `div.guardian-capability`, cada uno `style="--cap-color:…"` + `span[aria-hidden]`
     con el ícono + `strong` con el nombre.
  6. `div.guardians-closing`: `<span aria-hidden="true">✦</span>` + espacio +
     "La mejor tecnología no decide por ti: te ayuda a decidir mejor."

Se eliminó el uso de `SectionHeader`, `.guardianes-band`, `.guardianes-group` y
`.guardianes-competencias`. El `id="guardianes"` y la posición en `page.tsx` no cambian.

### Nota de clase raíz (`.guardians-feature` en vez de `.guardians-section`)

El HTML usa `.guardians-section`, pero esa clase ya está **viva** en el front: la usa
`src/components/sections/team.tsx` con tratamiento navy (`background: var(--ct-dark)`).
Para no romper ese componente (no montado en la home pero sí compilado) ni reintroducir el
fondo oscuro que el usuario rechazó, la sección Guardianes usa una clase raíz propia
`.guardians-feature` con el MISMO padding y fondo claro (`#f4f7f2`) que la
`.guardians-section` del HTML. Resultado: la sección Guardianes de la home NO tiene fondo
navy; el único `background: var(--ct-dark)` sobre `.guardians-section` que queda pertenece a
Team y es correcto.

## Datos tipados (`src/content/site.ts` → objeto `guardianes`)

Reformado (`as const`, UTF-8) a: `id`, `eyebrows[2]`, `title`, `intro`, `groupAlt`,
`images[7]`, `capabilities[7]`, `closingIcon`, `closing`. Eliminadas las claves viejas
`eyebrow`/`lead`/`body`/`competencias`. Único consumidor: `guardianes.tsx`.

### Los 7 textos / colores / íconos (verbatim del HTML, UTF-8), en orden

| # | --cap-color | ícono | nombre |
|---|-------------|-------|--------|
| 1 | `#FF2F7D`   | `♥`   | Empatía Digital |
| 2 | `#0077FF`   | `▣`   | Seguridad Digital |
| 3 | `#FFB000`   | `⚖`   | Derechos Digitales |
| 4 | `#22A600`   | `▤`   | Alfabetización Digital |
| 5 | `#6C2BFF`   | `✥`   | Pensamiento Crítico |
| 6 | `#00A9B7`   | `♧`   | Colaboración Digital |
| 7 | `#FF5B00`   | `➤`   | Innovación Digital |

Los `--cap-color` van literales (no mapeados a tokens). Ícono de cierre: `✦`.

## Imagen de grupo (cómo se resolvió)

El HTML embebe el grupo como un PNG en base64. `concientic/AGENTS.md` PROHÍBE base64 en
producción y PROHÍBE sustituir assets de marca por aproximaciones generadas. En
`public/guardians/` solo existen los 7 PNG individuales (byte, detective-dq, emi, lex, locky,
nexo, nova); **no hay** PNG de grupo. Por tanto: NO se incrustó base64 y NO se generó/inventó
una imagen. La `guardian-image` del HTML se sustituye por una composición de los 7 PNG dentro
de `.guardian-image-wrap` (grid de 7, tira/mosaico centrado): el contenedor lleva `role="img"`
y el alt-text oficial; cada `<Image>` individual es decorativo (`alt=""`).

## CSS (`src/app/globals.css`)

Reglas portadas del `<style>` del HTML (bloque autoritativo líneas ~304–320) con mapeo de
tokens: `.guardians-feature` (fondo claro `#f4f7f2`, no navy), `.guardians-layout` (hereda el
ancho/centrado de `.ct-container`), `.guardians-kickers`, `.guardians-kicker`,
`.guardians-title`, `.guardians-intro`, `.guardian-image-wrap` (adaptado a grid de 7 PNG),
`.guardian-image__item(+img)`, `.guardian-capabilities`, `.guardian-capability` (usa
`var(--cap-color)`, con el hover que colorea el borde y la transición `cubic-bezier` inlineada),
`.guardians-closing` (+ `span`), y los breakpoints `@media (max-width:900px)` (capabilities → 4
col) y `@media (max-width:600px)` (kickers en columna, image-wrap → 4 col, capabilities → 2 col).

Mapeo de tokens: `--green`→`var(--ct-primary)`, `--green-dark`→literal `#008f70`,
`--ink`→`var(--ct-text)`. Los grises/colores que el HTML usa literales (`#5c6b61`, `#e8f8f2`,
`#23372d`, `#e1e9e2`, `#24332b`, `#f4f7f2`) quedan literales. Los 7 `--cap-color` quedan literales.

### Reglas CSS retiradas (limpieza de CSS muerto)

- Bloque `/* ── Rediseño v13: franja de Guardianes ── */`: `.guardianes-band`,
  `.guardianes-lead`, `.guardianes-body`, `.guardianes-group`, `.guardianes-group__item`,
  `.guardianes-group__item img`, `.guardianes-competencias`, `.guardianes-competencias__item`,
  `.guardianes-closing` — eliminadas (reemplazadas por el bloque `.guardians-feature`).
- En `@media (max-width: 768px)`: eliminadas las reglas huérfanas `.guardianes-group` y
  `.guardianes-competencias` (se conserva `.experiencia-demo` del mismo media query).

### Lo que NO se tocó

- El bloque navy `.guardians-section` / `.ct-section-header *` / `.guardian-grid` /
  `.guardian-quote` / `.guardian-card*` se conserva: lo usa `team.tsx`. No es CSS muerto.
- Tokens `:root`, logo wordmark, formulario, skip link, focus-visible,
  prefers-reduced-motion, dominio www.concientic.com, bloque `BEGIN:nextjs-agent-rules`.

## Verificación ejecutada (cwd `concientic`)

| Comando | Resultado |
|---------|-----------|
| `npm run typecheck` (`tsc --noEmit`) | exit 0 |
| `npm run lint` (`eslint`) | exit 0 |
| `npm run build` (`next build`, Next.js 16.3.4) | exit 0 — compiló y generó 10 páginas estáticas |

Comprobaciones por grep:
- `background: var(--ct-dark)` sobre `.guardians-section`: solo queda el bloque de Team
  (correcto); la sección Guardianes de la home usa `.guardians-feature` (fondo claro).
- `guardianes-band|guardianes-lead|guardianes-body|guardianes-group|guardianes-competencias`
  en `concientic/src`: **0 coincidencias** (no queda CSS ni markup muerto). Las únicas
  coincidencias restantes están en documentos `.agents/tasks/*.md`.
