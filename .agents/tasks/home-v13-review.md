# Revisión de fidelidad visual — Home v13 vs. referencia

La home de Next (`concientic`) fue reescrita sección por sección para replicar `entregables_v8/docs/concientic_v13_reference.html`. Esta pasada audita la fidelidad visual (no solo que compile): tokens, colores de botón, fondos de sección, estructura de Guardianes, reglas de assets y textos en español, contrastando los componentes/CSS contra las reglas CSS y el markup extraídos de la referencia. El coder entregó evidencia de verificación en ejecución (build exit 0, dev server, inspección de DOM y CSS compilado, capturas headless a 1440px comparadas lado a lado) y corrigió en la iteración 2 el único hallazgo previo (ancho de contenedor 1160px).

Watch for: la sección Guardianes se aparta deliberadamente de la referencia (imagen única base64 → 7 PNG reales compuestos), pero es una desviación **exigida por el encargo y las reglas de assets**, no un defecto de fidelidad (confirmed). No quedan hallazgos visuales bloqueantes.

**Verdict**: APPROVED

## Vista de alto nivel

Los tokens `:root` coinciden con la referencia: `--ct-primary:#00c49a`, `--ct-cyan:#00b4d8`, `--ct-dark:#0d182a`, radio grande 24px y `.ct-container` a `min(1160px, calc(100% - 44px))`. El ancho de 1160px fue el hallazgo corregido en la iteración 2; Guardianes conserva su ancho propio de 1240px mediante regla explícita, tal como la referencia define para `.guardians-layout`.

Los botones primarios y el CTA de nav usan fondo cian `#00B4D8` con texto `#0D182A` (`.ct-button--primary`/`--dark`), y `.ct-button--secondary` es transparente con borde `currentColor`, exactamente como pide el encargo.

Los fondos de sección replican los tintes de la referencia: hero `#f4f7f2`, ecosistema `#edf2ee`, recursos/metodología `#f5f7f3`, experiencia `#eaf7f8` con borde superior cian, y la banda final con gradiente `#e5faf3→#f2efff→#fff0f7`.

Guardianes es CLARA (`#f4f7f2`, raíz `.guardians-feature` que no colisiona con el navy `.guardians-section` del componente Team, que sigue presente), una columna centrada: dos eyebrows verde-oscuro, el título "La IA tiene un papel. La persona conserva el protagonismo." verbatim, intro, la tarjeta contenedora con los 7 PNG reales sobre halo circular al ~24% y pill de nombre, la fila de 7 capability-cards en el orden/colores/iconos exactos del encargo, y el cierre "✦ La mejor tecnología no decide por ti: te ayuda a decidir mejor." verbatim.

La única diferencia con la referencia es la composición de Guardianes: el mock usa una imagen única base64 con los nombres rotulados; la home compone los 7 PNG individuales reales. Es lo que el encargo pide explícitamente y lo que las reglas de assets obligan (nada de base64 en producción, nada de sustituir/generar assets de marca, y no existe un archivo de grupo en `public/guardians/`).

<details>
<summary>Issues (0)</summary>

Ninguna acción pendiente. Las desviaciones respecto al mock son intencionales y exigidas por el encargo/reglas de assets.

</details>

<details>
<summary>Details</summary>

## Tokens y ancho de contenedor

`globals.css` declara en `:root` la paleta de la referencia: `--ct-primary:#00c49a`, `--ct-cyan:#00b4d8`, `--ct-dark:#0d182a`, más `--ct-green-dark:#008f70` para eyebrows y CTAs de tarjeta. El radio grande es `--ct-radius-lg:24px`. `.ct-container` quedó en `min(1160px, calc(100% - 44px))` (y `calc(100% - 30px)` en móvil), que es el `.wrap` genérico de la referencia (`--max:1160px`). Verificado contra `Select-String` sobre el HTML en la iteración 2.

El matiz importante es `.guardians-layout`: la referencia le da su **propio** ancho de `min(1240px, calc(100% - 48px))`, distinto del `.wrap` genérico. El coder añadió esa regla explícita para que, al bajar `.ct-container` a 1160px, Guardianes no quedara 80px más estrecha que el mock. `.philosophy-wrap` conserva 1440px. Las tres anchuras coinciden con la cascada final de la referencia.

## Botones y CTA

`.ct-button--primary` y `.ct-button--dark` usan `background: var(--ct-cyan)` (#00b4d8) con `color: var(--ct-dark)` (#0d182a), que es el contrato de botón del encargo. `.ct-button--secondary` es `background: transparent` con `border: 1px solid currentColor`. El CTA de nav es cian con la etiqueta "Encuentra tu ruta ↗".

## Fondos de sección

Confirmados contra la referencia: `.hero-section` `#f4f7f2` con aura radial `::before` y rejilla `::after`; `.ecosistema-section` `#edf2ee`; `.recursos-section` y `.ink-section` `#f5f7f3`; `.experience` `#eaf7f8` con `border-top: 3px solid var(--ct-cyan)`; `.philosophy-section` `#f5f7f3`; `.final-cta` con `linear-gradient(110deg,#e5faf3,#f2efff 62%,#fff0f7)` y glow radial. Coinciden con los tintes verdosos/tintados del mock.

## Guardianes

Raíz `.guardians-feature` con fondo claro `#f4f7f2` y padding `clamp(54px,7vw,92px)`; no colisiona con `.guardians-section{background:var(--ct-dark)}`, que **sigue presente** en `globals.css` para el componente Team no montado (confirmed: la regla navy existe sin cambios).

Estructura confirmada contra el markup de la referencia:
- Dos eyebrows apilados ("Los Guardianes", "Inteligencia con propósito") en verde-oscuro `#008f70`.
- Título verbatim: "La IA tiene un papel. La persona conserva el protagonismo.".
- Intro verbatim.
- Tarjeta contenedora `.guardian-image-wrap` con grid de 7, cada PNG real de `public/guardians/` sobre halo circular `color-mix(... 24% ...)` y pill de nombre en el color oficial del Guardián.
- Fila de 7 `.guardian-capability` en el orden y con los valores exactos del encargo y de la referencia:

```
#FF2F7D ♥ Empatía Digital
#0077FF ▣ Seguridad Digital
#FFB000 ⚖ Derechos Digitales
#22A600 ▤ Alfabetización Digital
#6C2BFF ✥ Pensamiento Crítico
#00A9B7 ♧ Colaboración Digital
#FF5B00 ➤ Innovación Digital
```

- Cierre verbatim: "✦ La mejor tecnología no decide por ti: te ayuda a decidir mejor." con barra verde (`border-left:4px var(--ct-primary)`, fondo `#e8f8f2`).

La única diferencia con el mock es el medio: la referencia incrusta una sola imagen base64 con los nombres rotulados dentro; la home compone los 7 PNG individuales. El encargo pide explícitamente "los 7 PNG individuales reales", y las reglas de assets de ambos `AGENTS.md` prohíben base64 en producción y sustituir/generar assets de marca; no existe archivo de grupo en `public/guardians/`. La desviación es correcta y no bloqueante.

## Reglas de assets

Sin base64 en producción; Guardianes usa los 7 PNG reales vía `next/image` (`/guardians/emi.png` … `/guardians/nova.png`). El wordmark de marca se renderiza como texto (la referencia oculta el ícono con `.brand-mark{display:none}`), sin sustituir ni generar assets.

## Textos en español

Spot-check verbatim de la referencia: título e intro de Guardianes, las 7 capacidades, el cierre, y el eyebrow/título/párrafo de Recursos coinciden carácter por carácter con el contenido de `src/content/site.ts`.

</details>

<details>
<summary>Mapa de archivos</summary>

- `src/app/globals.css` — tokens `:root`, `.ct-container` 1160px, botones cian, fondos de sección, bloque completo de Guardianes (`.guardians-feature`/`.guardian-image-wrap`/`.guardian-capability`/`.guardians-closing`) y navy `.guardians-section` conservado.
- `src/app/page.tsx` — orden de secciones: Hero, Ecosistema, Guardianes, Recursos, Methodology, Experiencia, Dquilibrio, Contact.
- `src/components/sections/guardianes.tsx` — 1 columna, 2 eyebrows, título/intro, 7 PNG con halo+pill, 7 capability-cards, cierre.
- `src/content/site.ts` — textos en español y datos de Guardianes (images, capabilities, closing).

Diff completo: `git diff` contra la rama base en `concientic`. Informe del coder: `.agents/tasks/home-v13-v8-fidelity-report.md`.

</details>
