# Revisión — Fidelidad VISUAL home v13 vs `concientic_v13_reference.html`

Veredicto: **APPROVED**. Sin hallazgos bloqueantes.

Método: se extrajo el `<style>` del HTML de referencia (líneas ~11-350, bloques `:root`, `.nav-cta/.btn`, override v9 de botones, `.guardian-*`/`.guardians-section`, `.experience`, `footer`, `.final-cta`, `.ink-section` y media queries 900/650/600/560) y se cotejó regla por regla contra `concientic/src/app/globals.css` y `guardianes.tsx`. No se re-ejecutó build/lint/typecheck; se leyó la evidencia del coder (§7 del reporte + commit `b5957de`).

## 1. Tokens `:root` — OK
Cotejados contra el `:root` del HTML (`globals.css` ~34-58):
- `--ct-dark #0d182a` = `--navy:#0D182A` ✓
- `--ct-pink #ff68b4` = `--pink:#FF68B4` ✓
- `--ct-soft #f5f7f3` = `--paper:#f5f7f3` ✓
- `--ct-muted #75847a` = `--muted:#75847a` ✓
- `--ct-text #17231e` = `--ink:#17231e` ✓
- `--ct-line #dce3dd` = `--line/--light-line:#dce3dd` ✓
- `--ct-primary #00c49a` (green), `--ct-cyan #00b4d8`, `--ct-blue #0077ff`, `--ct-secondary #6c63ff` (purple), `--ct-orange #ff7a00`, `--ct-yellow #ffc857` ✓
- `--ct-radius-lg: 24px` = `--radius:24px` ✓; `--ct-ease-out: cubic-bezier(.22,1,.36,1)` = `--ease-out` ✓
- `--green-dark #008f70`, `--ink-2 #24332c`, `--max 1160px`, `--shadow 0 22px 60px rgba(0,0,0,.15)`: el HTML los define; el front no los expone como token `--ct-*` pero no son requeridos como tokens (se usan como literales cuando aplica). No bloqueante.
- `@theme inline` (líneas 7-33) intacto: nombres y mapeo `--color-*`/`--radius-*` sin alterar; solo cambiaron valores de `--ct-*`. ✓

## 2. Botones — OK (patrón cian del HTML, sin relleno verde v12)
`globals.css` 191-265 vs HTML (`.nav-cta,.btn` base + override v9):
- `.ct-button--primary` y `.ct-button--dark` (CTA nav): `background: var(--ct-cyan)` + `color: var(--ct-dark)` (#0D182A). ✓
- `::before` brillo diagonal: `linear-gradient(110deg,transparent 20%,rgba(255,255,255,.38) 48%,transparent 72%)`, `translateX(-130%)`→`130%` en 0.6s. ✓ Idéntico al HTML.
- Hover: `translateY(-2px)` + `box-shadow: 0 9px 24px rgba(0,180,216,.2)`. ✓
- `.ct-button--secondary`: `background: transparent; color: inherit; border: 1px solid currentColor`; hover `rgba(0,196,154,.07)` + `0 7px 20px rgba(23,35,30,.06)`. ✓
- Base: `border-radius:999px`, `font-weight:800`, `padding:13px 18px`, `gap:9px`. ✓ (se conserva `min-height:48px` por accesibilidad; no contradice el HTML).
- El relleno oscuro/verde deslizante v12 (`::before` con `scaleX`) fue eliminado; no quedan `:hover{color:#fff}`. ✓
- `prefers-reduced-motion` (bloque global en 1217, posterior a los botones) anula `animation`/`transition` con `!important` en `*`, `*::before`, `*::after` → brillo y translate desactivados. ✓

## 3. Guardianes — OK (franja CLARA, ya no navy)
Componente `guardianes.tsx`: `className="ct-section guardianes-band"` (sin `ct-section--dark`) y `SectionHeader` sin prop `dark`. Textos, ids, `role="img"`/`aria-label`, estructura y los 7 PNG intactos (contenido desde `@/content/site`). ✓
`globals.css` 1851-1917 vs HTML:
- `.guardianes-band { background:#f4f7f2; color:var(--ct-text) }` = `.guardians-section{background:#f4f7f2}`. ✓
- `.guardianes-body { color:#5c6960 }` = `.guardian-copy p`. ✓
- `.guardianes-competencias__item { border:1px solid #d7e1d9; background:#fff; color:#34443a }` = `.guardian-pills span`. ✓
- `.guardianes-closing { border-left:4px solid var(--ct-primary); border-radius:0 12px 12px 0; padding:17px 20px; background:#e8f8f2; color:#23372d; font-weight:800; line-height:1.5 }` = `.guardians-closing` (bloque editorial final aprobado del HTML). ✓
- No existe regla que fuerce texto blanco en `.guardianes-band`; `.ct-section--dark` sigue definido pero ya no se aplica a esta sección (lo usan otras secciones; fuera de alcance). ✓

Nota (no bloqueante): el task prompt citó `.quote border-left 3px solid var(--green)` del bloque antiguo `.guardian-copy`; el coder trasladó el bloque editorial FINAL del HTML (`.guardians-closing`, 4px + `#e8f8f2`), que es la regla vigente en la cascada. Coincide con la referencia autoritativa.

## 4. Otros fondos — OK
- Hero `.hero-section`: `#f4f7f2`, `padding:82px 0 88px`, color `--ct-text`. ✓
- Experiencia `.experiencia-section`: `#eaf7f8`, `border-top:3px solid var(--ct-cyan)`, `border-bottom:1px solid #d8e8ea`. ✓
- Footer `.site-footer`: `#e7eee8`, `color:#4e5d53`, `border-top:1px solid #d8e1d9`. ✓
- Ecosistema/Recursos (feature/offer): tarjetas blancas con borde `--ct-line` (#dce3dd) = `--light-line` del HTML; alineadas tras el cambio de token. ✓
- Metodología/DQUILIBRIO y Contacto: mantenidos en blanco por decisión documentada (§6 del reporte). El HTML los pinta sobre `#f5f7f3` (`.ink-section`) / gradiente `.final-cta`; es una divergencia de fondo de sección anotada, no un color que contradiga la paleta ni una regresión de correctitud. No bloqueante: el encargo central (paleta, botones, Guardianes clara, hero/experiencia/footer) está cumplido y el coder dejó la asunción explícita.

## 5. Estructura / lógica — OK
- `page.tsx`: orden de las 8 secciones intacto (Hero, Ecosistema, Guardianes, Recursos, Methodology, Experiencia, Dquilibrio, Contact). ✓
- Diff del commit `b5957de` toca solo `globals.css` y `guardianes.tsx` (137 + 4 líneas). Sin cambios de textos §14, logo wordmark, form, skip link ni focus-visible. ✓
- `prefers-reduced-motion`, `:focus-visible` (outline ring) y `@layer base` intactos. ✓

## 6. Verificación del coder — OK
Evidencia leída en `home-v13-visual-alignment.md` §7: `npm run build` exit 0 ("Compiled successfully", 10/10 páginas), `npm run lint` exit 0, `npm run typecheck` exit 0. Trabajo commiteado en `b5957de`; árbol de trabajo sin archivos de producción modificados pendientes. No se re-ejecutaron suites (según instrucción). Las ediciones CSS leídas son sintácticamente correctas y el cambio `.tsx` es una remoción de className/prop que compila; no surgió duda que justifique spot-check adicional.

## 7. Responsive — OK (estructural)
HTML usa breakpoints 900/650/600/560. El front conserva 1050/768/700 (validados en v13). Guardianes refluye (grupo 7→4 col a ≤768px; competencias 4→2) y los grids/nav/hero colapsan a 1 col; los botones del hero apilan vía el breakpoint global. El DOM del front NO es el `.guardian-row` de 2 columnas del HTML, por lo que no se migran los selectores 900/650; es una decisión de layout documentada, fuera del alcance visual (paleta/botones/fondos). Guardianes y botones responden como corresponde. No bloqueante.

## Conclusión
Todas las reglas bloqueantes (tokens, botones cian + brillo + reduced-motion, Guardianes clara, hero/experiencia/footer, estructura/lógica, evidencia de build) coinciden con el `<style>` del HTML de referencia. Las divergencias restantes (fondos de Metodología/DQUILIBRIO/Contacto en blanco y breakpoints 1050/768/700) están documentadas como decisiones conscientes dentro del alcance "igualar colores/botones/Guardianes" y no constituyen fallos de correctitud, seguridad ni datos. Veredicto APPROVED.
