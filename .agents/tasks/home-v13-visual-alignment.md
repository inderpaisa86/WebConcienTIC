# Reporte — Alineación VISUAL de la home v13 con `concientic_v13_reference.html`

Objetivo: corregir SOLO lo visual (paleta de tokens, botones, fondos de sección y la franja Guardianes) para que la home coincida con el HTML de referencia. No se tocó estructura, textos, orden de secciones ni lógica. Fuente de verdad visual: el `<style>` de `entregables_v7/docs/concientic_v13_reference.html` (incluidas sus desviaciones respecto al Manual de Marca).

Archivos editados:
- `concientic/src/app/globals.css` (tokens, botones, fondos de sección, Guardianes).
- `concientic/src/components/sections/guardianes.tsx` (quitar `ct-section--dark` y la prop `dark` del header para que la franja use tratamiento claro).

## 1. Tokens `:root` — viejo → nuevo (del `:root` del HTML)

| Token `--ct-*` | Valor viejo | Valor nuevo | Equivalente en HTML |
|---|---|---|---|
| `--ct-dark` | `#0d1b2a` | `#0d182a` | `--navy:#0D182A` |
| `--ct-pink` | `#ff6eb6` | `#ff68b4` | `--pink:#FF68B4` |
| `--ct-soft` | `#f5f7f9` | `#f5f7f3` | `--paper:#f5f7f3` |
| `--ct-muted` | `#64748b` | `#75847a` | `--muted:#75847a` |
| `--ct-text` | `#1e293b` | `#17231e` | `--ink:#17231e` |
| `--ct-line` | `rgba(13,27,42,.12)` | `#dce3dd` | `--line/--light-line:#dce3dd` |

Tokens que ya coincidían (sin cambio): `--ct-primary:#00c49a` (`--green`), `--ct-secondary:#6c63ff` (`--purple`), `--ct-blue:#0077ff` (`--blue`), `--ct-cyan:#00b4d8` (`--cyan`), `--ct-orange:#ff7a00` (`--orange`), `--ct-yellow:#ffc857` (`--yellow`), `--ct-white:#ffffff`.

Añadido: `--ct-ease-out: cubic-bezier(.22,1,.36,1)` (equivale a `--ease-out` del HTML) para las transiciones de botón. No se tocó el bloque `@theme inline` de Tailwind (solo se cambiaron valores de los tokens `--ct-*`, no nombres ni mapeo).

Nota: `--ct-line` pasó de `rgba(...)` translúcido a sólido `#dce3dd`. Afecta bordes de tarjetas/inputs/header en toda la home y subpáginas (biblioteca, legales). Es intencional y coincide con `--line`/`--light-line` del HTML.

## 2. Botones → CIAN (reemplaza el patrón v12 de relleno oscuro deslizante)

- `.ct-button--primary` y `.ct-button--dark` (CTA del nav "Encuentra tu ruta ↗"): `background: var(--ct-cyan)` (#00B4D8) con `color: var(--ct-dark)` (#0D182A). Antes el primario era verde (`--ct-primary`) y el `--dark` era fondo oscuro con texto blanco.
- Se eliminó el relleno oscuro deslizante (`::before` con `background:var(--ct-dark)` + `scaleX`) y los `:hover{color:#fff}`.
- Hover primario/CTA: `transform: translateY(-2px)` + brillo diagonal deslizante (`::before` gradiente translúcido `translateX(-130%)→130%` en 0.6s) + `box-shadow: 0 9px 24px rgba(0,180,216,.2)`.
- `.ct-button--secondary`: `background: transparent; color: inherit; border: 1px solid currentColor`; hover `background: rgba(0,196,154,.07)` + `box-shadow: 0 7px 20px rgba(23,35,30,.06)`.
- Base: `border-radius:999px`, `font-weight:800`, `padding:13px 18px`, `gap:9px` (cotejado del HTML `.nav-cta,.btn`). Se conservó `min-height:48px` para accesibilidad de área táctil.
- `prefers-reduced-motion`: se respeta el bloque global existente en `globals.css` (anula `transition`/`animation` con `!important`), posterior a estas reglas. No se añadió nada nuevo; el brillo y el translate quedan desactivados.

## 3. Sección Guardianes — CLARA (corrección clave; ya NO navy)

Confirmado: la franja Guardianes quedó CLARA, sin fondo navy ni texto blanco.

- Componente: se quitó `ct-section--dark` (→ `className="ct-section guardianes-band"`) y la prop `dark` del `SectionHeader`. Textos, ids, anclas, `role="img"`/`alt` del grupo y estructura intactos.
- `.guardianes-band { background:#f4f7f2; color:var(--ct-text) }` (del HTML `.guardians-section{background:#f4f7f2}`). El eyebrow/título usan sus colores de sección clara por defecto.
- `.guardianes-lead { color:var(--ct-text) }` (antes `--ct-white`).
- `.guardianes-body { color:#5c6960 }` (antes `rgba(255,255,255,.72)`; gris verdoso del HTML `.guardian-copy p`).
- `.guardianes-competencias__item { border:1px solid #d7e1d9; background:#fff; color:#34443a }` (antes bordes/relleno blancos translúcidos y texto blanco → pills claras estilo `.guardian-pills span`).
- `.guardianes-closing`: bloque de cita claro del HTML `.guardians-closing`: `border-left:4px solid var(--ct-primary)`, `border-radius:0 12px 12px 0`, `padding:17px 20px`, `background:#e8f8f2`, `color:#23372d`, `font-weight:800`, `line-height:1.5`.

## 4. Otros fondos de sección

| Sección (clase front) | Cambio | Referencia HTML |
|---|---|---|
| Hero (`.hero-section`) | `background:#f4f7f2; color:var(--ct-text)`, padding `82px 0 88px` (antes gradiente blanco→#f7fffc) | `.hero{background:#f4f7f2;color:var(--ink);padding:82px 0 88px}` |
| Experiencia (`.experiencia-section`) | nueva regla: `background:#eaf7f8; border-top:3px solid var(--ct-cyan); border-bottom:1px solid #d8e8ea` (gana al `--soft`) | `.experience{background:#eaf7f8;border-top:3px solid var(--cyan);border-bottom:1px solid #d8e8ea}` |
| Footer (`.site-footer`) | `background:#e7eee8; color:#4e5d53; border-top:1px solid #d8e1d9` (antes blanco/línea token/muted) | `footer{background:#e7eee8;color:#4e5d53;border-top:1px solid #d8e1d9}` |
| Ecosistema / Recursos (feature/offer) | sin cambio explícito: con `--ct-soft=#f5f7f3` y `--ct-line=#dce3dd` las tarjetas `.info-card` (blancas, borde `--ct-line`) ya quedan alineadas con `--light-line` del HTML | `.feature-card/.offer{border:1px solid var(--light-line);background:white}` |
| Metodología / DQUILIBRIO | sin cambio: se mantienen con fondo blanco (coherente con la alternancia del front). El HTML los pinta sobre `#f5f7f3` (`.ink-section`); ver §6 asunciones. | `.ink-section{background:#f5f7f3!important}` |
| Contacto (formulario) | sin cambio: fondo blanco. No se forzó el gradiente `.final-cta` del HTML (es un banner, no el formulario funcional). Ver §6. | `.final-cta{background:linear-gradient(110deg,#e5faf3,#f2efff 62%,#fff0f7)}` |

## 5. Breakpoints

- Guardianes: se conservan los breakpoints existentes del front (`max-width:768px` → grupo a 4 col, competencias a 2 col). El DOM del front (grid de 7 PNG + lista de competencias) NO es el `.guardian-row` de 2 columnas del HTML, por lo que no se migró a `900px/650px`. El HTML de referencia usa para su bloque editorial `@media(max-width:900px)` (capabilities a 4 col) y `@media(max-width:600px)` (a 2 col); esos selectores (`.guardian-capabilities`, `.guardian-capability`) no existen en el front.
- Botones/nav: responden con los breakpoints globales existentes del front (`1050px` colapsa nav, `700px` apila hero). No se introdujeron `900px/650px` nuevos (son estructura/layout, fuera del alcance visual). El HTML a `650px` hace `.hero-actions .btn{width:100%}`; se deja como ajuste opcional de layout no aplicado.

## 6. Reglas del HTML no cotejadas / asunciones

1. **CTA del nav** (`.ct-button--dark`): alineado a CIAN porque el HTML define `.nav-cta` cian (misma regla que `.btn-primary`). Única desviación discutible respecto al v12 (era oscuro).
2. **Contacto**: NO se aplicó el gradiente `.final-cta` del HTML; la sección de contacto del front es un formulario funcional (necesita contraste), no el banner final decorativo del HTML. Fuera del alcance "igualar colores" de forma inequívoca.
3. **Metodología/DQUILIBRIO**: se mantienen en blanco; el HTML los pinta en `#f5f7f3` dentro de `.ink-section`. No contradice la paleta; si la revisión visual exige `#f5f7f3`, aplicar `background:var(--ct-soft)` a esas secciones.
4. **PNG de Guardianes**: pensados para fondo oscuro; sobre `#f4f7f2` podrían mostrar halos. No se modifican assets (regla de `entregables_v7`). El HTML editorial final usa una imagen única con `mix-blend-mode`, que no aplica al grupo de 7 PNG del front.
5. Pseudo-fondos decorativos del Hero (`::before/::after`) y el `::after` de `.guardian-visual` del HTML son decorativos y pertenecen a un DOM distinto; no se trasladaron (no afectan la paleta/igualdad de color pedida).

## 7. Verificación ejecutada (desde `concientic/`)

| Comando | Resultado |
|---|---|
| `npm run build` (incluye TypeScript) | Exit 0 — "Compiled successfully", TypeScript "Finished", 10/10 páginas generadas |
| `npm run lint` (eslint) | Exit 0 — sin errores |
| `npm run typecheck` (`tsc --noEmit`) | Exit 0 — sin errores |

No se crearon archivos temporales. No se eliminó el bloque `BEGIN/END:nextjs-agent-rules` de `concientic/AGENTS.md`.
