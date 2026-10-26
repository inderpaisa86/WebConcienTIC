# Implementation Plan — Página `/laboratorios` (LaboratoriosIA)

Reproduce "tal cual" el CONTENIDO y el estilo de secciones del mock
`c:\Jose\RepoSiginex\WebConcienTIC\concientic_laboratoriosIA_v2.html` como una nueva
ruta nativa de Next.js en `/laboratorios`, heredando header/footer globales del layout.
Además, relinkear la tarjeta `lab-card` del Ecosistema del home de `#contacto` a `/laboratorios`.

## Contexto verificado durante la exploración (no re-explorar)

- **App root:** `c:\Jose\RepoSiginex\WebConcienTIC\concientic`. Next **16.3.4 + Turbopack** (ver `AGENTS.md` → bloque `nextjs-agent-rules`, NO borrar).
- **Layout raíz** `src/app/layout.tsx` ya monta `<Header />` + `<main id="main">{children}</main>` + `<Footer />`. **La nueva page NO debe renderizar header/footer propios**: los hereda automáticamente. Ignorar por completo `<header>`/`<footer>`/`<nav>` del HTML de referencia y su JS de menú/scroll de header.
- **Patrón de page/route confirmado** en `src/app/privacidad/page.tsx`: server component que exporta `const metadata: Metadata` + `export default function` y renderiza un componente con datos desde `@/content/site`. Replicar ese patrón.
- **Metadata/generateMetadata solo en Server Components** (doc `01-getting-started/14-metadata-and-og-images.md`). Por eso la page será server component y la interactividad (toast "pendiente de configurar") se aísla en un componente `"use client"`.
- **Navegación interna** (doc `04-linking-and-navigating.md`): usar `next/link` `<Link href="/laboratorios">` para transición en la MISMA pestaña con prefetch. NADA de `target="_blank"` para el link interno del home.
- **Tokens/clases ya existentes en `src/app/globals.css`** que COINCIDEN con la referencia y hay que REUTILIZAR (no duplicar):
  - Colores: `--ct-primary` #00c49a (green), `--ct-cyan` #00b4d8, `--ct-secondary` #6c63ff (purple), `--ct-green-dark` #008f70, `--ct-orange` #ff7a00, `--ct-dark` #0d182a, `--ct-line` #dce3dd. (Equivalen a `--green/--cyan/--purple/--green-dark/--orange/--ink/--line` del mock.)
  - Botones: `.ct-button` + `.ct-button--primary` (cyan, == `btn-primary` del mock) y `.ct-button--secondary` (transparent + borde, == `btn-secondary`). Reutilizar para los botones del hero, enfoque y final-cta.
  - `.ct-container` (width min(1160px, 100% - 44px)) == `.wrap` del mock. Reutilizar como contenedor.
- **COLISIONES de clases confirmadas** en `globals.css` (el home ya define estas con otra pinta): `.hero-copy`, `.hero-actions`, `.hero-note`, `.pill`, `.pill .dot`, `.section-head`, `.eyebrow`, `.eyebrow--green-dark`, `.feature-grid`, `.feature-card` (+ `.feature-card.lab-card` — ¡ya existe y es del HOME!), `.final-cta`, `.final-inner`, `.principle-card`, `.principle-band`. 
  → **Estrategia de namespacing:** TODO el CSS nuevo de la página se escribe bajo una raíz `.labs-page` (la page envuelve su contenido en `<div className="labs-page">`). Las clases que chocan se escriben SIEMPRE como descendientes de `.labs-page` (p.ej. `.labs-page .hero`, `.labs-page .lab-card`, `.labs-page .final-cta`, `.labs-page .principle`, `.labs-page .section-tint`, `.labs-page .intro-band`, `.labs-page .logo-slot`, `.labs-page .card-link`, etc.). Así la especificidad del selector de 2 niveles no afecta al home y el home no afecta a la página. Clases propias sin equivalente en el home (`.lab-card`, `.lab-grid`, `.intro-band`, `.intro-panel`, `.logo-slot`, `.logo-caption`, `.card-top`, `.card-bottom`, `.card-number`, `.card-link`, `.tag`, `.section-tint`, `.section-kicker`, `.principle`, `.principle-cards`) igualmente bajo `.labs-page` para coherencia y para no introducir nuevas globales que puedan chocar a futuro.
  - NOTA sobre `.tag` y `.eyebrow`: existen en el home; bajo `.labs-page` se redefine su variante del mock.
- **Datos de contenido:** patrón `as const` en `src/content/site.ts`. La tarjeta a relinkear es el export `ecosistema`, item con `variant: "lab-card"`, cuyo `cta.href` hoy es `"#contacto"` (líneas ~61-71), renderizado en `src/components/sections/ecosistema.tsx` con `<a href={item.cta.href}>`.

## Decisiones de diseño (una línea cada una)

1. **Datos del nuevo contenido en módulo nuevo** `src/content/laboratorios.ts` (export `laboratorios`, `as const`), en lugar de engordar `site.ts`: la data es extensa (4 cards + 4 principios + 2 paneles + textos) y exclusiva de esta ruta; mantiene `site.ts` enfocado y sigue el estilo tipado existente. Los SVG inline de los logo-slots viven en el componente de render (JSX), no en el data module.
2. **Page server component + un único componente cliente** `LabArtifactLinks`/toast: la metadata exige server component; solo el aviso "pendiente" necesita estado/eventos, así que se aísla el mínimo en `"use client"`. El resto del markup es estático server-rendered.
3. **Namespacing por raíz `.labs-page`** (ver arriba) en vez de renombrar clases con prefijo `labs-`: conserva los nombres del mock (fidelidad/lectura) y aísla por especificidad sin tocar el home.
4. **Reutilizar `.ct-button--primary/--secondary` y `.ct-container`** en lugar de recrear `.btn`/`.wrap`: ya son idénticos visualmente a la referencia.
5. **Decoraciones opcionales** (scroll-progress bar, scroll-reveal) NO se reproducen (son nice-to-have y el brief las marca no requeridas); si se añadieran luego, respetar `prefers-reduced-motion`. La aura radial y la rejilla del hero SÍ se reproducen en CSS puro (sin JS) respetando `prefers-reduced-motion` (bloque `@media (prefers-reduced-motion: reduce)` que anula animaciones), igual que ya hace el home en `.hero-section`.
6. **No tocar el header global ni los menús** (home, subpáginas DQUILIBRIO, Contenidos). El link `#configurar-*` de las cards no necesita destino real; los 4 artifact-links quedan con URL vacía y comportamiento "pendiente".

## Mapeo sección-por-sección (del `<main>` del mock → nueva page)

- **hero** `id="inicio"`, fondo #f4f7f2: pill (punto verde) "Experimenta con intención. Aplica con criterio." · h1 "La inteligencia artificial empieza cuando la llevas <em>a la práctica.</em>" (el `<em>` en cyan, NO itálica) · párrafo hero-copy · 2 botones: primario cyan "Explorar laboratorios ↘" (ancla `#laboratorios` dentro de la página) y secundario con borde "Conoce ConcienTIC ↗" (a `/` del sitio) · hero-note "Menos ruido. Más criterio. Tecnología al servicio de las personas." · aura + grid decorativos (CSS).
- **laboratorios** `id="laboratorios"` clase `section-tint` (bg #edf2ee): section-head con eyebrow "Ecosistema de innovación aplicada", h2 "Cuatro laboratorios.<br>Un propósito compartido.", intro; `lab-grid` 2×2 de 4 `lab-card`; luego `intro-band` de 2 paneles. Copys EXACTOS del mock. Cards:
  - 01 **SIGINEX** — accent `var(--ct-primary)` (green), tag "01 · Gestión inteligente", h3 "Sistema de Gestión SIGINEX", link "Explorar SIGINEX ↗" (`data-lab="SIGINEX"`, href `#configurar-siginex`).
  - 02 **EmprendeCreativo** — clase `violet`, accent `var(--ct-secondary)` (purple), tag "02 · Creatividad y emprendimiento", h3 "EmprendeCreativo", link "Entrar al laboratorio ↗" (`data-lab="EmprendeCreativo"`, href `#configurar-emprendecreativo`).
  - 03 **DQ-Nexus** — clase `blue`, accent #0785a0, tag "03 · Inteligencia digital", h3 "Autodiagnóstico de Inteligencia Digital DQ-Nexus", link "Iniciar autodiagnóstico ↗" (`data-lab="DQ-Nexus"`, href `#configurar-dqnexus`).
  - 04 **TutorIA** — clase `orange`, accent #bd5b10, tag "04 · Aprendizaje inteligente", h3 "Aprende IA con TutorIA", link "Aprender con TutorIA ↗" (`data-lab="TutorIA"`, href `#configurar-tutoria`).
  - Cada card: `card-top` con `logo-slot` (caja dashed con el SVG inline EXACTO del mock por marca) + caption "Logo oficial / por incorporar"; `tag`; `h3`; `p` (copy exacta); `card-bottom` con `card-number` "LABORATORIO 0N" + `card-link artifact-link`.
  - intro-band: panel 1 (mini-label "Cómo funciona", h3 "Una introducción antes de la experiencia", p) y panel 2 `accent` (mini-label "Una experiencia coherente", h3 "Identidades propias, visión compartida", p). Copys exactas.
- **enfoque** `id="enfoque"` clase `principle` (grid .8fr/1.2fr): izq eyebrow "Tecnología con intención", h2 "La IA tiene un papel. Las personas conservan el protagonismo.", p "En ConcienTIC, la inteligencia artificial es un medio para comprender, crear y decidir mejor. Cada experiencia debe responder a una necesidad concreta y dejar espacio para el criterio humano.", botón secundario "Conoce nuestra visión ↗" (a `/`); der `principle-cards` 2×2 con 4 `principle-card` (Propósito antes que ruido / Criterio y responsabilidad / Aprender haciendo / Equilibrio digital — strings exactos del mock).
- **final-cta** (gradiente): eyebrow "Tu siguiente paso empieza aquí", h2 "Encuentra el laboratorio que conecta con tu reto.", p, botón primario "Elegir un laboratorio ↗" (ancla `#laboratorios`).

## Comportamiento a preservar (del JS del mock)

`ARTIFACT_URLS = {SIGINEX:"", EmprendeCreativo:"", "DQ-Nexus":"", TutorIA:""}` — todos vacíos. Al hacer click en un `artifact-link` cuya URL está vacía: `preventDefault` y mostrar un aviso transitorio accesible `role="status"`: "El enlace del artefacto `<name>` está pendiente de configurar." que se oculta tras ~4.5 s. NO inventar URLs. Las anclas `#configurar-*` no necesitan destino real. Esta lógica vive en el componente cliente (ver ítem 2). NO añadir scroll-behavior de header que compita con el header global.

## Verificación global

El proyecto usa Next + Turbopack. Comandos reales (leer `concientic/package.json` para confirmar nombres exactos de scripts antes de ejecutar; típicamente `lint`, `build`, `dev`). Verificación mínima por cambio: `npm run lint` y `npm run build` desde `c:\Jose\RepoSiginex\WebConcienTIC\concientic`, más comprobación visual con `npm run dev` en `/` (home intacto) y `/laboratorios`.

---

- [ ] 1. Leer `concientic/package.json` y confirmar los scripts reales de lint/build/dev y gestor de paquetes (npm/pnpm/yarn). Usar esos comandos exactos en las verificaciones siguientes.
      Files: (solo lectura) `c:\Jose\RepoSiginex\WebConcienTIC\concientic\package.json`
      Verify: `npm run build` (o el script equivalente) corre sin errores sobre el árbol actual (baseline verde antes de empezar).

- [ ] 2. Crear el módulo de contenido tipado `laboratorios` con hero, sección de laboratorios (4 cards con `id` de ancla, `accentVar`/`variant`, `tag`, `name`, `description`, `cardNumber`, `linkLabel`, `dataLab`, `href`), los 2 paneles del `intro-band`, el bloque `enfoque` (eyebrow, h2, párrafo, cta) con sus 4 `principle-card`, y el bloque `final-cta`. Copys en español EXACTOS del mock. No incluir SVG aquí (van en el componente de render).
      Files: crear `c:\Jose\RepoSiginex\WebConcienTIC\concientic\src\content\laboratorios.ts`
      Verify: `npm run lint` pasa sin errores de tipos/TS en el archivo nuevo.

- [ ] 3. Crear el componente cliente del aviso "pendiente de configurar": `"use client"`, mantiene estado del mensaje visible, expone un handler que recibe el `name` del lab, hace `preventDefault`, muestra un contenedor `role="status"` con el texto "El enlace del artefacto {name} está pendiente de configurar." y lo oculta tras ~4500 ms (limpiando el timeout anterior). Debe renderizar los `card-link artifact-link` (o envolver su `onClick`) con `data-lab` y `href` `#configurar-*`. Diseño del toast equivalente al del mock (fixed, abajo-centro) pero con clases bajo `.labs-page`.
      Files: crear `c:\Jose\RepoSiginex\WebConcienTIC\concientic\src\components\laboratorios\lab-artifact-link.tsx` (o `lab-card-grid.tsx` que agrupe las 4 cards + el toast). Decidir una de las dos formas y mantenerla.
      Verify: `npm run lint` pasa; el componente no usa APIs de servidor.

- [ ] 4. Crear la page server component en la ruta `/laboratorios`: exporta `const metadata: Metadata` con `title: "LaboratoriosIA — ConcienTIC"` y `description` = meta del mock ("Explora los laboratorios de inteligencia artificial de ConcienTIC: SIGINEX, EmprendeCreativo, DQ-Nexus y TutorIA."), y `export default function` que renderiza `<div className="labs-page">` con las 4 secciones (hero, laboratorios+intro-band, enfoque, final-cta) leyendo de `@/content/laboratorios`, usando `.ct-container`, `.ct-button--primary/--secondary`, e insertando los SVG inline EXACTOS por marca en cada `logo-slot`. Las 4 cards + toast se renderizan vía el componente cliente del ítem 3. El botón secundario "Conoce ConcienTIC ↗" y "Conoce nuestra visión ↗" apuntan a `/` (home); los botones "Explorar laboratorios"/"Elegir un laboratorio" a ancla `#laboratorios`. NO renderizar header/footer.
      Files: crear `c:\Jose\RepoSiginex\WebConcienTIC\concientic\src\app\laboratorios\page.tsx`
      Verify: `npm run build` compila la ruta `/laboratorios`; `npm run dev` y abrir `/laboratorios` muestra las 4 secciones con header/footer globales.

- [ ] 5. Añadir el CSS de la página en `globals.css`, TODO bajo la raíz `.labs-page` (hero + aura/grid, `.section-tint`, `.lab-grid`, `.lab-card` y variantes `.violet/.blue/.orange` con `--accent`, `.logo-slot`/`.logo-caption`, `.card-top/.tag/.card-bottom/.card-number/.card-link`, `.intro-band/.intro-panel(.accent)/.mini-label`, `.principle/.principle-cards/.principle-card`, `.final-cta/.final-inner`, `.pill/.dot`, `.section-head/.eyebrow/.section-kicker`, responsive a 900px/650px, y bloque `@media (prefers-reduced-motion: reduce)`). Reutilizar tokens `--ct-*` existentes; para acentos sin token usar literales #0785a0 y #bd5b10 como el mock. No modificar reglas existentes del home.
      Files: editar (append) `c:\Jose\RepoSiginex\WebConcienTIC\concientic\src\app\globals.css`
      Verify: `npm run dev` → `/laboratorios` fiel al mock; abrir `/` y confirmar que hero, ecosistema y la banda final del HOME NO cambiaron (sin regresión por colisión de clases).

- [ ] 6. Relinkear la tarjeta del Ecosistema en el home: en `src/content/site.ts`, en el export `ecosistema`, item con `variant: "lab-card"`, cambiar `cta.href` de `"#contacto"` a `"/laboratorios"` (dejar el `label` "Explorar laboratorios ↗" igual).
      Files: editar `c:\Jose\RepoSiginex\WebConcienTIC\concientic\src\content\site.ts`
      Verify: `npm run lint` pasa; el tipo `as const` sigue válido.

- [ ] 7. Hacer que `ecosistema.tsx` renderice ese CTA como enlace interno Next en la MISMA pestaña: importar `Link from "next/link"` y, para el item cuyo `href` empieza por `/` (ruta interna), renderizar `<Link href={item.cta.href}>` (sin `target`/`rel`); para los `#ancla` seguir con `<a href>`. Mantener el resto del componente igual. Confirmar que no rompe los otros dos items (que usan anclas `#experiencia`).
      Files: editar `c:\Jose\RepoSiginex\WebConcienTIC\concientic\src\components\sections\ecosistema.tsx`
      Verify: `npm run dev` en `/` → la tarjeta "03 · Experimentación y aplicación — Laboratorios IA" navega a `/laboratorios` en la misma pestaña; los otros CTAs del ecosistema siguen funcionando.

- [ ] 8. Verificación integral final: `npm run lint` y `npm run build` sin errores; revisar `/` (home sin regresiones, menú/header/footer intactos, link de la card → `/laboratorios`) y `/laboratorios` (4 secciones fieles, header/footer globales heredados, los 4 artifact-links muestran el aviso "pendiente de configurar" al click, respeta prefers-reduced-motion). Confirmar que el bloque `nextjs-agent-rules` de `AGENTS.md` sigue intacto. NO crear commits; dejar el árbol modificado.
      Files: (verificación) todo lo anterior
      Verify: `npm run build` verde + comprobación visual de ambas rutas descrita arriba.

## Verificación ejecutada (iteración 1 — implementación desde cero)

Ejecutado en `c:\Jose\RepoSiginex\WebConcienTIC\concientic` (Next 16.3.4 + Turbopack):

- `npm run typecheck` (`tsc --noEmit`) → **Exit Code 0**, sin errores.
- `npm run lint` (`eslint`) → **Exit Code 0**, sin warnings ni errores.
- `npm run build` (`next build`) → **Exit Code 0**. Compiló en 3.4s; TypeScript OK; 11/11 páginas estáticas generadas. La ruta `/laboratorios` aparece como **Static (prerendered)** junto a `/` (home) y el resto de rutas existentes. Home `/` sigue compilando sin regresión.

Bloque `nextjs-agent-rules` de `concientic/AGENTS.md`: **intacto** (verificado por grep tras los cambios).

No se crearon commits; el árbol de trabajo queda modificado (según lo pedido).

Archivos creados/modificados:
- CREADO `concientic/src/content/laboratorios.ts` (contenido tipado `as const`).
- CREADO `concientic/src/components/laboratorios/lab-card-grid.tsx` (`"use client"`; grid de 4 cards + toast `role="status"` con comportamiento "pendiente de configurar", URLs vacías, sin inventar enlaces; SVG inline exactos del mock).
- CREADO `concientic/src/app/laboratorios/page.tsx` (server component, `metadata`, `<div className="labs-page">` con hero/laboratorios+intro-band/enfoque/final-cta; hereda header/footer globales del layout; no renderiza header/footer propios).
- EDITADO `concientic/src/app/globals.css` (append de CSS namespaced bajo `.labs-page`, incl. `@media (prefers-reduced-motion: reduce)`; sin modificar reglas del home).
- EDITADO `concientic/src/content/site.ts` (`ecosistema` → item `lab-card`: `cta.href` de `#contacto` a `/laboratorios`).
- EDITADO `concientic/src/components/sections/ecosistema.tsx` (rutas internas que empiezan por `/` se renderizan con `next/link` `<Link>` en la misma pestaña, sin `target`/`rel`; las anclas `#` siguen con `<a>`).

## Supuestos / lagunas

- Nombres exactos de scripts (`lint`/`build`/`dev`) y gestor de paquetes se confirman en el ítem 1 leyendo `package.json`; el plan asume npm por defecto.
- Las anclas `#configurar-*` quedan como marcadores sin destino real (coherente con el mock y con el comportamiento "pendiente").
- Se opta por un módulo nuevo `src/content/laboratorios.ts` (no engordar `site.ts`); si el implementador prefiere un export `laboratorios` dentro de `site.ts` por consistencia estricta, es aceptable, pero el plan recomienda el módulo nuevo por tamaño/aislamiento.
