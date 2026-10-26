# Review — Sección GUARDIANES (home ConcienTIC, v13)

Iteración: primera (no existía review previo). Veredicto: **APPROVED** — sin hallazgos bloqueantes.

## Alcance verificado

Objetivo aprobado: markup + estilo de la sección `#guardianes` deben quedar EXACTAMENTE como en
`entregables_v7/docs/concientic_v13_reference.html` (no solo color). Cotejé el HTML de referencia
(markup líneas 451-470, CSS líneas 306-321 + reglas de hover/transición 358-360, tokens `:root`
líneas 11-13) leído como UTF-8 contra el componente, los datos y el CSS del front. No reejecuté
build/lint/typecheck; leí la evidencia registrada por el coder.

## Cotejo punto por punto

1. **Una sola columna centrada** — `guardianes.tsx` emite `<div className="ct-container
   guardians-layout">` como único contenedor; `.ct-container` = `width:min(1240px,calc(100% - 48px));
   margin-inline:auto`, equivalente al `.guardians-layout` del HTML (`width:min(1240px,calc(100% -
   48px));margin-inline:auto`). No hay grid de 2 columnas. ✓

2. **guardians-kickers** — dos `<span className="ct-eyebrow guardians-kicker">` con los textos
   'Los Guardianes' e 'Inteligencia con propósito' (site.ts `eyebrows`). `.guardians-kicker` tiene
   `color:#008f70` (globals.css 1868-1876). El HTML aplica el color vía
   `style="color:var(--green-dark)"` con `--green-dark:#008f70`; el front lo fija en la regla de
   clase. Mismo resultado de color. ✓

3. **h2.guardians-title** verbatim: "La IA tiene un papel. La persona conserva el protagonismo."
   (site.ts línea 70 = HTML línea 457). ✓

4. **p.guardians-intro** verbatim: coincide carácter a carácter con HTML línea 459. ✓

5. **guardian-image-wrap** — contenedor con `role="img"` y `aria-label={guardianes.groupAlt}`; el
   alt oficial es idéntico al del HTML. Composición de los 7 PNG individuales
   (`/guardians/{emi,locky,lex,byte,detective-dq,nexo,nova}.png`), cada `<Image alt="">` decorativo.
   NO hay base64 ni imagen de grupo generada. Esto es correcto por la restricción de assets
   (`concientic/AGENTS.md` / `entregables_v7/AGENTS.md`: prohíbe base64 en producción y sustituir
   assets de marca) y el brief indica no marcarlo. ✓

6. **guardian-capabilities** (`aria-label="Capacidades representadas por los Guardianes"`) — los 7
   `guardian-capability` coinciden en color, ícono y nombre Y en orden con el HTML (líneas 460-467),
   verificados leyendo ambos como UTF-8:
   | # | --cap-color | ícono | nombre | HTML | front |
   |---|---|---|---|---|---|
   | 1 | #FF2F7D | ♥ | Empatía Digital | ✓ | ✓ |
   | 2 | #0077FF | ▣ | Seguridad Digital | ✓ | ✓ |
   | 3 | #FFB000 | ⚖ | Derechos Digitales | ✓ | ✓ |
   | 4 | #22A600 | ▤ | Alfabetización Digital | ✓ | ✓ |
   | 5 | #6C2BFF | ✥ | Pensamiento Crítico | ✓ | ✓ |
   | 6 | #00A9B7 | ♧ | Colaboración Digital | ✓ | ✓ |
   | 7 | #FF5B00 | ➤ | Innovación Digital | ✓ | ✓ |
   Los `--cap-color` van literales vía `style={{ "--cap-color": cap.color }}` (sin mapear a tokens). ✓

7. **guardians-closing** — `<span aria-hidden="true">✦</span>` + espacio + "La mejor tecnología no
   decide por ti: te ayuda a decidir mejor." (component + site.ts `closingIcon`/`closing` = HTML
   línea 469, incluyendo el espacio tras el ✦ vía `{" "}`). ✓

8. **CSS tratamiento claro, sin navy, sin CSS muerto** —
   - `.guardians-feature { background:#f4f7f2 }` (globals.css 1854-1858); mismo padding e idéntico
     fondo claro que `.guardians-section` del HTML (línea 306). NO navy.
   - `grep` de `guardians-section` con `background: var(--ct-dark)`: solo el bloque de Team
     (globals.css 847-851), usado por `team.tsx`. La sección Guardianes NO usa esa clase (usa
     `.guardians-feature`), así que no hereda navy. Renombre justificado para no romper Team.
   - `grep` de `guardianes-band|guardianes-lead|guardianes-body|guardianes-group|
     guardianes-competencias` sobre `concientic/src/**`: 0 coincidencias (CSS/markup muerto
     eliminado).
   - Reglas `.guardians-*`/`.guardian-*` portadas coinciden con el HTML (incl. breakpoints 900/600px).
     Mapeo de tokens fiel: `--green #00C49A`→`var(--ct-primary) #00c49a`, `--green-dark`→literal
     `#008f70`, `--ink #17231e`→`var(--ct-text) #17231e` (valores idénticos). Grises/literales
     (`#5c6b61`, `#e8f8f2`, `#23372d`, `#e1e9e2`, `#24332b`) conservados. `--ease-out` inlineado como
     `cubic-bezier(.22,1,.36,1)` en `.guardian-capability` (el front no tiene ese token). El HTML usa
     breakpoints 900/600px; el front los replica en 900/600px (no 650). El plan menciona "650px" pero
     el HTML y el CSS portado usan 600px: el CSS del front coincide con el HTML, que es la fuente
     autoritativa. Sin discrepancia material.
   - Adaptación documentada: `.guardian-image-wrap` pasa de una `<img>` única a grid de 7 PNG; no se
     replica el zoom `:hover .guardian-image{scale(1.012)}` porque no hay img de grupo. Justificado
     por la restricción de assets; es la única vía conforme.

9. **Otras secciones y tokens globales intactos** — `page.tsx` monta Hero, Ecosistema, Guardianes,
   Recursos, Methodology, etc.; `Team` NO está montado (su navy no afecta la home). El `@media
   (max-width:768px)` solo conserva `.experiencia-demo` (reglas huérfanas de guardianes eliminadas).
   No se tocaron tokens `:root`. ✓

## Evidencia de verificación (registrada por el coder, no reejecutada)

`.agents/tasks/home-v13-guardianes-fix.md` registra, cwd `concientic`:
- `npm run typecheck` (`tsc --noEmit`) → exit 0
- `npm run lint` (`eslint`) → exit 0
- `npm run build` (`next build`, Next.js 16.3.4) → exit 0 (10 páginas estáticas)

Los tres comandos requeridos constan con exit 0. Spot-checks propios (grep de clases, lectura del
componente/CSS/datos) confirman el estado descrito.

## Conclusión

Markup, datos y CSS de la sección Guardianes coinciden con el HTML de referencia dentro de los
mapeos de token aprobados y la restricción de assets (7 PNG individuales en lugar del base64 de
grupo). No quedan rastros navy ni CSS muerto sobre la sección. Sin hallazgos bloqueantes.
