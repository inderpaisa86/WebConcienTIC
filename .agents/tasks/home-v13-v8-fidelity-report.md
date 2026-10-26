# Informe de fidelidad — Home v13 vs. referencia v8

Referencia visual (fuente de verdad): `entregables_v8/docs/concientic_v13_reference.html`.
App auditada: `concientic` (Next 16.3.4 + Turbopack), rama de trabajo (sin commits).

Esta pasada fue la **primera iteración** (no existía `home-v13-review.json`): auditoría
completa sección por sección + corrección, siguiendo
`.agents/tasks/home-v13-audit-plan.md`.

## Restricción Next.js
Antes de tocar código Next se leyeron las guías obligatorias bajo
`concientic/node_modules/next/dist/docs/01-app/01-getting-started/`: `11-css.md`,
`12-images.md`, `05-server-and-client-components.md`. El bloque autogenerado
`nextjs-agent-rules` de `concientic/AGENTS.md` quedó intacto.

## Método de extracción de la referencia
El HTML (~1.5 MB con imágenes base64) NO se leyó entero. Se extrajo el `<style>` y el
`<body>` tras reemplazar `data:image/...` por `DATAURI` a archivos temporales de
trabajo, para comparar reglas CSS y markup exactos. La referencia tiene varias capas de
overrides tardíos (`!important`); se resolvió la cascada final y se usaron esos valores.

---

## Tokens globales (`globals.css`)
- Añadido `--ct-green-dark:#008f70` (eyebrows verde-oscuro y CTAs de tarjeta).
- `html`/`body` background cambiado de `#fff` a `--ct-soft #f5f7f3` (= `--paper`).
- Paleta ya coincidía con la referencia; no se renombraron tokens.

## HEADER
- Reemplazado el `brand-mark` con `<Image>` del wordmark PNG por el **wordmark de
  texto** `concienTIC` + `small` `INTELIGENCIA DIGITAL CONSCIENTE` (la referencia oculta
  el ícono con `.brand-mark{display:none}`). El PNG de marca sigue en `public/` pero la
  home lo renderiza como texto, igual que la referencia.
- Header: fondo `rgba(247,249,245,.94)`, borde `#dfe6df`, altura nav 76px.
- Nav links color `#405047`, hover `--green-dark`, subrayado cian animado (`::after`).
- CTA nav cian `#00B4D8` texto `#0D182A`, label "Encuentra tu ruta ↗" (ya correcto).

## FOOTER
- Reemplazado el `<Image>` del wordmark por el **wordmark de texto** (igual al header).
- `© <año dinámico> ConcienTIC` (antes mostraba solo `© ConcienTIC`); verificado render
  `© 2026 ConcienTIC`.
- Fondo `#e7eee8`, padding 28px, `font-size:.82rem`, hover enlaces `--green-dark`.

## 1. INICIO (hero)
- De 2 columnas con imagen a **1 columna sin imagen** (`grid-template-columns:minmax(0,1fr)`),
  copy `max-width:920px`. Eliminado el `<Image>` `equilibrio-...` del hero (asset no borrado).
- Eyebrow plano → **pill verde con punto** (`.pill` + `.dot`).
- `<h1>` con `<em>` en **cian** (no itálica): "La inteligencia digital empieza *contigo.*".
- Padding `clamp(76px,9vw,124px) 0 clamp(78px,9vw,116px)`, fondo `#f4f7f2`, aura radial
  `::before` + rejilla `::after`.
- `hero-lead` `#536158` / `hero-note` `.82rem #6f7d73`. Botón primario cian, secundario
  transparente con borde.

## 2. ECOSISTEMA
- Fondo de sección `#edf2ee`, padding 88px.
- Tarjetas migradas de `.info-card` (número + "Abrir →") a **`.feature-card`** con
  `.tag` + `h3` + `p` + `<a>` CTA al pie; variantes `.purple` (card 2, `#5b51df`) y
  `.lab-card` (card 3, gradiente `#fff→#fff4e9`, CTA `#b65a17`).
- CTA de Laboratorios → **"Explorar laboratorios ↗"** destino `#contacto`; se eliminó el
  aviso "pending" (la referencia no lo muestra).
- `h2` con `<br/>`, eyebrow `#008f70`, nota `.small-note` (`.78rem #7c8c82`).

## 3. GUARDIANES
- Ya estaba alineada con la referencia y se mantuvo: sección CLARA `#f4f7f2` con raíz
  `.guardians-feature` (no colisiona con el navy `.guardians-section` del componente Team
  no montado). Una columna centrada, dos eyebrows apilados verde-oscuro, título, intro.
- Composición de los **7 PNG individuales** de `public/guardians/` (no hay archivo de
  grupo; base64 prohibido): cada personaje sobre **halo circular** del color de marca
  (~24% opacidad) con **píldora de nombre** en ese color. `role="group"` con alt oficial.
- Fila de 7 `.guardian-capability` (border `#e1e9e2`, radio 13px, icono por `--cap-color`)
  en el orden exacto; barra de cierre verde (`border-left:4px --green`, fondo `#e8f8f2`).
- Verificado contra la referencia: coincide (ver nota de diferencias abajo).

## 4. RECURSOS
- Fondo `--paper #f5f7f3` (via `.recursos-section`), padding 88px.
- Tarjetas migradas a **`.offer`** / `.offer.featured` (card 2, gradiente `#e7fbf5→#f3f0ff`,
  borde `#bcefe0`). `.num` con formato `NN / TEXTO`. `.offer-cta` verde `#008f70`; CTA
  Academia purple `#5b51df` con `target="_blank"`. Card 3 sin CTA.
- Eliminado el aviso "URL pendiente de confirmación" (la referencia no lo muestra).
- `h2` con `<br/>`, eyebrow `#008f70`.

## 5. METODOLOGÍA
- Reemplazado el **PNG** `ciclo-dquilibrio-mejorado.png` por el **diagrama CSS**
  (`.dq-diagram`/`.dq-orbit` con `conic-gradient(from -35deg,...)`, `.dq-core` "DQUILIBRIO"
  + `<small>`, 5 `.dq-node` posicionados n-att/n-hum/n-ai/n-auto/n-crit con sus colores).
- Layout `.method-cycle` grid `.9fr 1.1fr`; sección `.ink-section` fondo `#f5f7f3`.
- Encabezado con `<h3>` "Un ciclo, no una carrera." y eyebrow "Nuestra metodología".
- Párrafo actualizado al texto de la referencia ("...No es una escalera rígida ni un
  instrumento clínico: es una invitación a detenerse...").
- 5 `.method-step` con `--step-color` (green/cyan/purple/orange/pink), formato
  `NN · TÍTULO` + descripción; textos de la referencia (p. ej. 02 "Hacer visible el
  contexto y los hábitos.").
- Animación del orbe respeta `prefers-reduced-motion` (regla global en `globals.css`).

## 6. EXPERIENCIA
- Fondo `#eaf7f8`, borde superior cian 3px, borde inferior `#d8e8ea`; padding 88px.
- Reestructurado a `.experience-grid` (2 columnas: copy | demo). El `notice` único se
  dividió en los **dos párrafos** de la referencia (P1 incluye "Comienza identificando…"
  + "Este pequeño ejemplo…"; P2 "En la experiencia real…").
- Demo `.demo` con `.demo-label`, `<h3>`, 4 `.choice` (radio `name="need"`,
  `:has(input:checked)` → borde cian + fondo `#eafaff`, `accent-color:cyan`), botón ancho
  100% "Ver una orientación inicial ↗".
- Lógica en memoria conservada (sin fetch/persistencia/envío). Se mantuvo el
  `emptyMessage` accesible con `aria-live` (mejora admisible; no existe en la referencia).

## 7. DQUILIBRIO
- Reestructurado a `.philosophy-section` > `.wrap` (1440px) > `.philosophy-panel`
  (transparente, sin recuadro) > `.section-head` + `.philosophy-cards`.
- Tarjetas migradas de `.info-card` clicable completa a **`.philosophy-card`** (no es
  enlace completo) con `.num` + `h3` + `p` + `.card-cta` "Explorar X ↗" teñido por
  `--accent`. Borde `#e5e9e5`, radio 20px, `min-height:220`, padding clamp.
- Textos actualizados a la referencia: eyebrow "La filosofía que nos guía · DQUILIBRIO";
  numbers en MAYÚSCULA (`01 · ATENCIÓN`…`04 · AUTONOMÍA`); descripciones de cards 2/3/4;
  `--accent` por tarjeta (#00C49A/#0077FF/#6C63FF/#FF7A00).
- Fondo `#f5f7f3`. Enlaces same-tab (la referencia no usa `target` en `.card-cta`).

## 8. CONTACTO
- Reemplazado el formulario de contacto por la **banda `.final-cta`** horizontal
  (`.final-inner` flex, fondo `linear-gradient(110deg,#e5faf3,#f2efff 62%,#fff0f7)` +
  glow radial `::after`), texto a la izquierda y botón cian "Conversemos ↗" a la derecha.
  **Sin formulario** en la home (como la referencia).
- `contact-form.tsx` se **conserva en el repo sin montar** (AGENTS.md exige un formulario
  real en el sitio; la home v13 simplemente no lo muestra — queda para una futura ruta).
- `href` del CTA: se usó el ancla interna `#contacto` (la referencia apunta a
  `https://concientic.com/#contacto`). Decisión de navegación interna; documentada.

---

## Verificación ejecutada (evidencia)
- `npm run typecheck` → **exit 0** (sin errores de tsc).
- `npm run lint` → **exit 0** (ESLint sin errores).
- `npm run build` (Next 16.3.4 Turbopack) → **exit 0**; `/` prerenderizada como estática;
  resto de rutas intactas (`/design-system`, `/servicios/contenidos`, `/privacidad`,
  `/terminos`, API, sitemap, robots).
- Servidor de desarrollo (`next dev -p 3000`) levantado y verificado:
  - DOM renderizado (`/`, 200): se confirmaron las clases/markup de las 8 secciones
    (wordmark de texto, CTA cian, pill+dot, `<em>contigo.</em>`, ausencia de `hero-visual`,
    `feature-card lab-card`, `01 / EXPLORAR`, `ink-section`+`dq-orbit`, `01 · DETENER`,
    `class="experience"`+`choice`, `philosophy-section`+`01 · ATENCIÓN`+`card-cta`,
    `final-cta`+`Conversemos`, ausencia de `contact-form`, `© 2026 ConcienTIC`).
  - CSS compilado: se confirmaron los valores de referencia (`#f4f7f2`, `#edf2ee`,
    `#eaf7f8`, `#f5f7f3`, cian `#00b4d8`, `#008f70`, `conic-gradient(from -35deg`,
    gradientes `#e7fbf5/#f3f0ff`, `lab-card` `#fff→#fff4e9`, `final-cta` `#e5faf3`,
    `#eafaff`, `#e8f8f2`).
  - Capturas headless (Chrome 1440px) de la app y de la referencia, comparadas lado a
    lado: las 8 secciones coinciden en fondos, colores de botón (cian), tipografía,
    espaciado, estructura y textos.
- Archivos temporales de verificación eliminados. Sin commits de git.

## Diferencias restantes (con razón)
1. **Imagen de grupo de Guardianes**: la referencia usa una sola imagen base64 con los
   nombres rotulados dentro; la home compone los 7 PNG reales con halo circular + píldora
   de nombre. Es intencional: §assets de ambos AGENTS.md prohíben base64 en producción y
   sustituir/generar assets de marca, y no existe archivo de grupo en `public/guardians/`.
   Resultado visualmente equivalente.
2. **CTA de Contacto**: la referencia enlaza a `https://concientic.com/#contacto`; la home
   usa el ancla interna `#contacto` para navegación dentro del sitio. Diferencia menor de
   destino, no visual.
3. **`emptyMessage` de Experiencia**: no existe en la referencia; se conserva como mejora
   de accesibilidad (`aria-live`) sin alterar la apariencia por defecto.
4. **Animación de entrada del hero** (`fadeSlideUp`) y del orbe (`orbitBreathe`): presentes
   en la referencia y reproducidas; en capturas headless aparecen a mitad de animación
   (hero levemente desvanecido), comportamiento idéntico en ambos, no es un defecto.
5. **Ancho máximo**: CORREGIDO en la iteración 2 (ver más abajo). `.ct-container` ahora es
   `min(1160px, calc(100% - 44px))` = el `.wrap` genérico de la referencia. Las únicas
   excepciones son las que la propia referencia define: `.guardians-layout` (1240px/48px)
   y `.philosophy-wrap` (1440px/44px).

## Componentes v12 no montados
No se borró ningún componente v12 no montado (cases, challenge, desinfoxication, evidence,
references, services, manifesto, team, value-props, contact-form). La regla navy
`.guardians-section{background:var(--ct-dark)}` (usada por Team) se conservó.

---

## Iteración 2 — corrección del hallazgo de la revisión

Existía `home-v13-review.json` (verdict `CHANGES_REQUESTED`, 1 hallazgo confirmado,
`reviewDoc` = `.agents/tasks/home-v13-review.md`). Se leyó el razonamiento completo y se
corrigió el único hallazgo.

### Hallazgo: ancho de contenedor 1240px vs. referencia 1160px
- **Qué decía la referencia**: `--max:1160px` y
  `.wrap{width:min(var(--max),calc(100% - 44px))}` para todas las secciones; en móvil
  `.wrap{width:min(var(--max),calc(100% - 30px))}`. Confirmado con `Select-String` sobre
  `entregables_v8/docs/concientic_v13_reference.html`.
- **Qué había**: `.ct-container{width:min(1240px,calc(100% - 48px))}` (desktop) y
  `min(1240px,calc(100% - 30px))` (móvil), ~80px más ancho y 4px más de gutter que el mock
  en todas las secciones no-philosophy.
- **Corrección** (`globals.css`):
  - `.ct-container` desktop → `min(1160px, calc(100% - 44px))`.
  - `.ct-container` móvil (`@media max-width:700px`) → `min(1160px, calc(100% - 30px))`.
  - Se **añadió** una regla explícita `.guardians-layout{width:min(1240px,calc(100% - 48px))}`.
    Motivo: la referencia da a `.guardians-layout` su **propio** ancho de 1240px/48px
    (`.guardians-layout{width:min(1240px,calc(100% - 48px))}` verificado en el HTML), que
    NO es el `.wrap` genérico. Como en la app `.guardians-layout` heredaba de
    `.ct-container`, al bajar el contenedor a 1160px Guardianes habría quedado 80px más
    estrecho que la referencia; la regla explícita preserva la fidelidad de esa sección.
  - `.philosophy-wrap` (1440px/44px) se dejó intacto (ya coincidía).

### Verificación iteración 2 (evidencia)
- `npm run typecheck` → **exit 0**.
- `npm run lint` → **exit 0**.
- `npm run build` (Next 16.3.4 Turbopack) → **exit 0**; `/` prerenderizada como estática.
- `next dev` (en ejecución, puerto 3000) — inspección del CSS compilado servido y del DOM
  renderizado de `/`:
  - CSS servido (`/_next/static/chunks/...css`):
    `.ct-container{width:min(1160px,100% - 44px)}` y `{min(1160px,100% - 30px)}` (móvil);
    `.guardians-layout{width:min(1240px,100% - 48px)}` y `{calc(100% - 30px)}` (móvil);
    `.philosophy-wrap{width:min(1440px,...)}`. (Turbopack en dev reescribe `calc(min(...))`
    quitando el `calc` externo; los valores son los esperados.)
  - DOM `/` (200): 11 nodos `.ct-container` (secciones no-philosophy, ahora 1160px),
    Guardianes con `class="ct-container guardians-layout"` (1240px por la regla explícita),
    DQUILIBRIO con `.philosophy-wrap` (1440px).
- Archivos temporales eliminados (`.devlog.txt`, `.deverr.txt`). Sin commits de git.
- `concientic/AGENTS.md` intacto (el cambio fue solo CSS; el bloque `nextjs-agent-rules`
  no se tocó).

### Diferencias restantes tras iteración 2
Ninguna pendiente del hallazgo de la revisión. Se mantienen únicamente las diferencias
intencionales ya documentadas arriba (items 1–4: Guardianes 7 PNG vs base64, CTA de
Contacto a ancla interna, `emptyMessage` accesible de Experiencia, animaciones de entrada).
