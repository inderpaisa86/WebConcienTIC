# Plan de implementación — Alineación VISUAL de la home v13 con el HTML de referencia

Objetivo: corregir SOLO lo visual (paleta de tokens, botones, sección Guardianes clara, fondos de sección) para que la home del front coincida con `entregables_v7/docs/concientic_v13_reference.html`. NO se toca estructura, textos, ni lógica. La fuente de verdad VISUAL es el HTML de referencia, incluidas sus desviaciones respecto al Manual de Marca.

## 0. Contexto verificado durante la exploración

- Front: Next.js 16.3.4, App Router, TS estricto, Tailwind v4 con tokens CSS. CWD del workspace = `c:\Jose\RepoSiginex\WebConcienTIC` (NO `concientic`); usar rutas absolutas.
- El rediseño v13 (8 secciones, textos, estructura) YA está aplicado. Orden en `concientic/src/app/page.tsx`: Hero, Ecosistema, Guardianes, Recursos, Methodology, Experiencia, Dquilibrio, Contact.
- Todo el CSS vive en un único archivo global: `c:\Jose\RepoSiginex\WebConcienTIC\concientic\src\app\globals.css` (2016 líneas). Los `:root` tokens están al inicio (líneas ~34-58) con VALORES VIEJOS; el bloque `@theme inline` de Tailwind está en las líneas ~7-33 y NO debe romperse (solo se cambian los valores de los tokens `--ct-*`, no los nombres ni el mapeo `@theme`).
- La home reutiliza clases genéricas, no clases por sección: botones `.ct-button` / `.ct-button--primary` / `.ct-button--secondary` / `.ct-button--dark`; secciones `.hero-section`, `.ct-section`, `.ct-section--soft`, `.ct-section--dark`, `.experiencia-section`.
- NO hay framework de tests en `concientic/package.json`. Scripts disponibles: `dev`, `build`, `lint` (eslint), `typecheck` (`tsc --noEmit`), `format:check` (prettier). La verificación autoritativa de esta tarea (CSS puro) es `npm run build` sin errores + revisión visual con `npm run dev`.

### IMPORTANTE — divergencia de DOM en Guardianes
El HTML de referencia tiene dos tratamientos de Guardianes: uno antiguo (`.guardian-row`/`.guardian-visual`/`.guardian-pills`/`.guardian-copy`) y uno final "editorial" (`.guardians-section{background:#f4f7f2}` + `.guardian-image-wrap` + `.guardian-capabilities` + `.guardians-closing`). El DOM del front NO coincide con ninguno de los dos: usa `guardianes-band` (= `ct-section--dark`), `guardianes-lead`, `guardianes-body`, `guardianes-group` (7 PNG), `guardianes-competencias` y `guardianes-closing`. Por tanto NO se copian selectores del HTML tal cual; se traslada el TRATAMIENTO VISUAL (fondo claro, texto oscuro, pills/items claros, cierre con borde verde) a las clases reales del front. El fondo claro objetivo del HTML es `#f4f7f2` (bloque final `.guardians-section`).

## 1. Guía(s) de Next.js a leer ANTES de tocar código (requisito de `concientic/AGENTS.md`)

Esta tarea es CSS en un stylesheet global ya importado (`globals.css` se importa en `src/app/layout.tsx`) más, como mucho, el cambio de UNA className en un componente de sección. No se usan APIs nuevas de Next, ni `next/image`, ni límites Server/Client. Guías a leer:

- OBLIGATORIA: `c:\Jose\RepoSiginex\WebConcienTIC\concientic\node_modules\next\dist\docs\01-app\01-getting-started\11-css.md` (CSS global / importación de estilos).
- Solo si el paso 6 obliga a tocar el JSX del componente Guardianes: `c:\Jose\RepoSiginex\WebConcienTIC\concientic\node_modules\next\dist\docs\01-app\01-getting-started\12-images.md` (para confirmar que `next/image` existente no requiere cambios). No se espera tocar JSX.
- NO eliminar el bloque `BEGIN:nextjs-agent-rules`/`END:nextjs-agent-rules` de `concientic/AGENTS.md` (lo re-genera `next dev`; conservarlo en el diff deja el árbol limpio).

## 2. Archivos exactos a editar

- `c:\Jose\RepoSiginex\WebConcienTIC\concientic\src\app\globals.css` — ÚNICO archivo obligatorio. Contiene tokens, botones, secciones y Guardianes.
- Componentes de sección: NO requieren edición. `guardianes.tsx`, `hero.tsx`, etc. ya aplican las clases correctas; el cambio de aspecto se logra solo en CSS. (Opcional/cosmético: en `c:\Jose\RepoSiginex\WebConcienTIC\concientic\src\components\sections\guardianes.tsx` el `SectionHeader` recibe `dark`, que añade la clase sin regla `ct-section-header--dark`; como la banda pasa a clara, conviene quitar `dark` para que el eyebrow/título usen el color de sección clara. Ver paso 6.)

---

## 3. Tokens `:root` — tabla viejo→nuevo (editar valores en `globals.css`, líneas ~34-58; NO tocar `@theme inline`)

Mapeo real de nombres (verificado en `globals.css`) y cambios requeridos. Solo se listan los tokens cuyo valor DEBE cambiar para igualar el `:root` del HTML de referencia.

| Token `--ct-*` (nombre real front) | Valor VIEJO (front actual) | Valor NUEVO (HTML ref) | Equivalente en `:root` del HTML |
|---|---|---|---|
| `--ct-dark` | `#0d1b2a` | `#0d182a` | `--navy:#0D182A` |
| `--ct-pink` | `#ff6eb6` | `#ff68b4` | `--pink:#FF68B4` |
| `--ct-soft` | `#f5f7f9` | `#f5f7f3` | `--paper:#f5f7f3` |
| `--ct-muted` | `#64748b` | `#75847a` | `--muted:#75847a` |
| `--ct-text` | `#1e293b` | `#17231e` | `--ink:#17231e` |
| `--ct-line` | `rgba(13, 27, 42, 0.12)` | `#dce3dd` | `--line:#dce3dd` |

Tokens que NO cambian (ya coinciden con el HTML): `--ct-primary:#00c49a` (`--green`), `--ct-secondary:#6c63ff` (`--purple`), `--ct-blue:#0077ff` (`--blue`), `--ct-cyan:#00b4d8` (`--cyan`), `--ct-orange:#ff7a00` (`--orange`), `--ct-yellow:#ffc857` (`--yellow`), `--ct-white:#ffffff`.

Notas:
- El HTML tiene colores adicionales sin token `--ct-*` en el front: `--green-dark:#008f70`, `--ink-2:#24332c`, `--radius:24px` (ya existe `--ct-radius-lg:24px`), `--shadow:0 22px 60px rgba(0,0,0,.15)`. `--green-dark` se usará como literal `#008f70` en reglas puntuales (p. ej. cierre de Guardianes). NO es necesario crear tokens nuevos; mantener el set `--ct-*` existente.
- `--ct-line` pasa de `rgba(...)` a sólido `#dce3dd`: afecta bordes de tarjetas/inputs/header en TODA la home y subpáginas (biblioteca, legales). Es intencional y coincide con `--line`/`--light-line` del HTML. Revisar visualmente que no haya regresiones en biblioteca.

Verificación tras el paso 3: `cd concientic; npm run build` compila sin error de CSS; `npm run dev` y comprobar que fondos `var(--ct-soft)` se ven verdosos (#f5f7f3), texto base más oscuro/verdoso (#17231e) y líneas sólidas claras.

---

## 4. Botones — reescribir `.ct-button*` para que primario y CTA sean CIAN (reemplaza el patrón v12 de relleno oscuro deslizante)

Reglas objetivo del HTML (verbatim del `<style>`, bloques base + "v9"):

```
.nav-cta,.btn{border:0;display:inline-flex;align-items:center;justify-content:center;gap:9px;padding:13px 18px;border-radius:999px;font-weight:800;cursor:pointer;transition:transform .2s,background .2s}
.nav-cta,.btn-primary{background:var(--cyan);color:#0D182A}
.nav-cta:hover,.btn:hover{transform:translateY(-2px)}
.btn-secondary{background:transparent;color:inherit;border:1px solid currentColor}
```
```
.nav-cta,.btn{position:relative;isolation:isolate;overflow:hidden;transition:transform .24s var(--ease-out),box-shadow .24s ease,background .24s ease}
.nav-cta::before,.btn-primary::before{content:"";position:absolute;inset:0;z-index:-1;background:linear-gradient(110deg,transparent 20%,rgba(255,255,255,.38) 48%,transparent 72%);transform:translateX(-130%);transition:transform .6s var(--ease-out)}
.nav-cta:hover::before,.btn-primary:hover::before{transform:translateX(130%)}
.nav-cta:hover,.btn-primary:hover{box-shadow:0 9px 24px rgba(0,180,216,.2)}
.btn-secondary:hover{background:rgba(0,196,154,.07);box-shadow:0 7px 20px rgba(23,35,30,.06)}
```
(El HTML define `--ease-out:cubic-bezier(.22,1,.36,1)` en un `:root` secundario; en el front usar ese literal o añadir el token.)

Mapeo front → qué hacer (todo en `globals.css`, bloque `.ct-button*` líneas ~186-270):

1. `.ct-button` (base): mantener estructura inline-flex/radio/peso, pero ALINEAR a los valores del HTML: `border-radius:999px`, `font-weight:800`, `padding: 13px 18px` (hoy `0 24px` + `min-height:48px`; conservar `min-height` si se desea, pero el padding del HTML es `13px 18px`). Añadir `overflow:hidden; isolation:isolate; position:relative`. Transición: `transform .24s cubic-bezier(.22,1,.36,1), box-shadow .24s ease, background .24s ease`.

2. `.ct-button--primary`: cambiar `background: var(--ct-primary)` → `background: var(--ct-cyan)`; `color: var(--ct-dark)` se mantiene (ya es #0D182A tras el paso 3). Es decir primario = CIAN con texto oscuro.

3. CTA del nav: en el front el nav usa `.ct-button--dark` (ver `header.tsx`: "Encuentra tu ruta ↗"). El HTML define `.nav-cta` = CIAN igual que el primario. DECISIÓN: alinear `.ct-button--dark` a CIAN para que el CTA del header sea cian como en el HTML (en el HTML `.nav-cta` y `.btn-primary` comparten el estilo cian). Cambiar `.ct-button--dark { background: var(--ct-cyan); color: var(--ct-dark) }` y darle el mismo `::before` de brillo y la misma sombra hover que el primario. Eliminar el `::before` oscuro con `background: var(--ct-primary)` propio de `--dark`. Racional: el único CTA de nav de la home es ese botón, y el HTML lo quiere cian; mantenerlo oscuro contradiría la referencia.

4. Reescribir el hover: SUSTITUIR el patrón actual de relleno oscuro deslizante (`.ct-button::before{background:var(--ct-dark);transform:scaleX(0)...}` + `:hover::before{transform:scaleX(1)}` + `:hover{color:var(--ct-white)}`) por:
   - `.ct-button--primary::before` y `.ct-button--dark::before`: brillo diagonal translúcido `linear-gradient(110deg,transparent 20%,rgba(255,255,255,.38) 48%,transparent 72%)`, `position:absolute;inset:0;z-index:-1;transform:translateX(-130%);transition:transform .6s cubic-bezier(.22,1,.36,1)`.
   - `:hover::before` → `transform:translateX(130%)`.
   - `:hover` del primario/CTA → `transform:translateY(-2px); box-shadow:0 9px 24px rgba(0,180,216,.2)`.
   - Quitar las reglas `:hover{color:var(--ct-white)}` y `--dark:hover{color:var(--ct-dark)}` del patrón viejo (ya no hay cambio de color de texto en hover).

5. `.ct-button--secondary`: cambiar a `background: transparent; color: inherit; border: 1px solid currentColor` (hoy `background:var(--ct-white); border-color:var(--ct-line); color:var(--ct-dark)`). Hover: `background: rgba(0,196,154,.07); box-shadow: 0 7px 20px rgba(23,35,30,.06)`. Quitar el `border-color:var(--ct-dark)` del hover viejo.

6. Conservar `:active { transform: translateY(1px) }` o alinearlo; no es crítico (el HTML no lo define).

7. `prefers-reduced-motion`: NO añadir nada nuevo. `globals.css` ya tiene un bloque global (`@media (prefers-reduced-motion: reduce)`) que anula `transition`/`animation` con `!important`; cubre el nuevo brillo deslizante y el translateY. Verificar que ese bloque siga DESPUÉS de estas reglas (ya lo está, ~línea 1300+).

Verificación tras el paso 4: `npm run build` OK; `npm run dev` y comprobar: botón primario del hero y de Contacto en CIAN con texto oscuro; CTA del header ("Encuentra tu ruta ↗") en CIAN; al pasar el cursor, brillo diagonal + leve elevación + sombra cian; secundario del hero transparente con borde del color del texto y hover verde muy tenue. Con `prefers-reduced-motion` activo, sin animación de brillo.

---

## 5. Fondos de sección — alinear a los valores del HTML (todo en `globals.css`)

Reglas objetivo del HTML (verbatim):

```
.hero{background:#f4f7f2;color:var(--ink);...;padding:82px 0 88px}
section{padding:88px 0}
.ink-section{background:#f5f7f3!important}      /* override final aprobado */
.experience{background:#eaf7f8;border-top:3px solid var(--cyan);border-bottom:1px solid #d8e8ea}
.feature-grid ...  .feature-card{border:1px solid var(--light-line);...;background:white}
.offer{...;border:1px solid var(--light-line);background:white}
.offer.featured{background:linear-gradient(145deg,#e7fbf5,#f3f0ff);border-color:#bcefe0}
.final-cta{background:linear-gradient(110deg,#e5faf3,#f2efff 62%,#fff0f7);color:var(--ink);padding:70px 0}
footer{background:#e7eee8;color:#4e5d53;padding:28px 0;font-size:.82rem;border-top:1px solid #d8e1d9}
```

Mapeo front → cambios:

1. Hero — `.hero-section` (globals.css ~línea 300): cambiar el gradiente actual `linear-gradient(180deg,#fff 0%,#f7fffc 100%)` por `background:#f4f7f2`. Mantener `overflow:hidden` y el padding actual (o alinear a `82px 0 88px`). Color de texto ya correcto (hereda `--ct-text`=#17231e). (Opcional: los pseudo-fondos `:before/:after` del Hero del HTML son decorativos; NO son obligatorios para "igualar colores"; dejarlos fuera salvo que la revisión visual los pida.)

2. Experiencia — `.experiencia-section` (hoy es `ct-section ct-section--soft experiencia-section`, el `--soft` le da `var(--ct-soft)`). Añadir/ajustar regla `.experiencia-section { background:#eaf7f8; border-top:3px solid var(--ct-cyan); border-bottom:1px solid #d8e8ea }` para reproducir `.experience` del HTML (fondo celeste claro con borde cian superior). Esta regla debe ganar al `--soft`; como `.experiencia-section` es más específica/posterior, define `background` explícito. El recuadro `.experiencia-demo` del front ya es blanco con borde `--ct-line` y sombra; alinear borde a `#d7e0d9` y, si se quiere fidelidad, el `:checked` de opción a `border-color:var(--ct-cyan);background:#eafaff` (el HTML usa `.choice:has(input:checked){border-color:var(--cyan);background:#eafaff}`). Prioridad: fondo de sección + borde cian; el detalle del radio es secundario.

3. Ecosistema / Recursos (feature-grid / offer en el HTML): en el front son `ct-section` (Ecosistema, fondo blanco por defecto) y `ct-section--soft` (Recursos, fondo `--ct-soft`=#f5f7f3 tras paso 3). El HTML pinta estas tarjetas sobre fondo claro con tarjetas blancas y borde `--light-line`(#dce3dd). Con el paso 3 (`--ct-line`=#dce3dd, `--ct-soft`=#f5f7f3) las tarjetas `.info-card` (blancas, borde `--ct-line`) YA quedan alineadas. No se requiere cambio adicional de fondo de estas dos secciones salvo validar visualmente. (El HTML no tiene una sección "Recursos" de fondo oscuro; el front tampoco. OK.)

4. Metodología — en el front es `ct-section` (fondo blanco). En el HTML la metodología vive en `.ink-section{background:#f5f7f3!important}` (override final). DECISIÓN: dejar Metodología con fondo claro. Si se busca paridad exacta con el tono `#f5f7f3` del HTML, añadir `background:var(--ct-soft)` a la sección de metodología; pero como las secciones del front alternan blanco/`--soft` y Methodology está entre Recursos(soft) y Experiencia(celeste), mantener blanco es coherente y no contradice colores del HTML. Mantener BLANCO (sin cambio) salvo que la revisión visual pida `--ct-soft`.

5. DQUILIBRIO — `Dquilibrio` es `ct-section` (blanco). En el HTML el bloque DQUILIBRIO vive dentro de `.ink-section`/metodología con fondo `#f5f7f3` y diagrama transparente. El front usa tarjetas `.info-card` sobre blanco. Mantener BLANCO; los acentos de número ya usan tokens correctos. Sin cambio de color obligatorio.

6. Contacto — `Contact` es `ct-section` (blanco). El HTML cierra con `.final-cta{background:linear-gradient(110deg,#e5faf3,#f2efff 62%,#fff0f7)}`. DECISIÓN: el front separa "Contacto" (formulario) de un posible final-cta; la sección de contacto del front es un formulario funcional, no el banner `.final-cta`. NO forzar el gradiente del final-cta sobre el formulario (cambiaría el carácter de la sección y el formulario necesita contraste). Mantener fondo blanco del formulario. Si la revisión visual exige el banner degradado, tratarlo como ajuste aparte; no está en el alcance "corregir colores que no coinciden" de forma inequívoca. Dejar ANOTADO como asunción (ver §8).

7. Footer — `.site-footer` (globals.css ~línea final del bloque base): hoy `background:var(--ct-white); border-top:1px solid var(--ct-line); color:var(--ct-muted)`. El HTML usa `footer{background:#e7eee8;color:#4e5d53;border-top:1px solid #d8e1d9}`. DECISIÓN: alinear a `background:#e7eee8; color:#4e5d53; border-top:1px solid #d8e1d9` para igualar el HTML. Mantener padding/typografía del front.

Verificación tras el paso 5: `npm run build` OK; `npm run dev` y comparar lado a lado con el HTML de referencia: Hero `#f4f7f2`, Experiencia celeste `#eaf7f8` con borde superior cian, footer verde-grisáceo `#e7eee8`. Sin regresiones de contraste.

---

## 6. Sección Guardianes — convertir de fondo AZUL OSCURO a CLARO (corrección clave)

Estado actual (front): `Guardianes` renderiza `<section class="ct-section ct-section--dark guardianes-band">` → fondo `var(--ct-dark)` y texto blanco; dentro `guardianes-lead` (blanco), `guardianes-body` (blanco 72%), `guardianes-group` (7 PNG), `guardianes-competencias__item` (borde/relleno blancos translúcidos, texto blanco), `guardianes-closing` (blanco). El `SectionHeader` recibe `dark`.

Tratamiento visual objetivo (trasladado del HTML: `.guardians-section{background:#f4f7f2}`, `.guardian-visual{linear-gradient(145deg,#ffffff,#edf5f0);color:var(--ink);border:1px solid #dce5de}`, `.guardian-pills span{border:1px solid #d7e1d9;background:#fff;color:#34443a}`, `.guardian-copy p{color:#5c6960}`, `.guardians-closing{border-left:4px solid var(--green);background:#e8f8f2;color:#23372d;border-radius:0 12px 12px 0}`):

Cambios (combinación de CSS + un ajuste mínimo de className):

a) En `c:\Jose\RepoSiginex\WebConcienTIC\concientic\src\components\sections\guardianes.tsx`:
   - Quitar `ct-section--dark` del `className` de la `<section>`, dejando `className="ct-section guardianes-band"` (o `ct-section ct-section--soft guardianes-band` si se quiere el fondo `#f5f7f3`; preferir `guardianes-band` con fondo propio claro — ver (b)). Esto elimina el fondo oscuro y el color blanco heredados.
   - Quitar la prop `dark` del `<SectionHeader ...>` para que eyebrow/título/descripción usen los colores de sección clara.
   - NO cambiar textos, ids, anclas, estructura ni el `role="img"`/`alt` del grupo.

b) En `globals.css`, definir/ajustar las reglas `.guardianes-*` para el tratamiento claro (añadir una regla de fondo para `.guardianes-band` y reescribir las reglas de color que hoy son blancas):
   - `.guardianes-band { background:#f4f7f2; color:var(--ct-text) }` (fondo claro del HTML `.guardians-section`). Como ya no lleva `ct-section--dark`, el título/eyebrow usan sus colores normales (eyebrow verde `--ct-primary`, h2 `--ct-dark`).
   - `.guardianes-lead { color:var(--ct-text) }` (antes `--ct-white`). Mantener tamaño/peso.
   - `.guardianes-body { color:#5c6960 }` (antes `rgba(255,255,255,.72)`; usar el gris verdoso del HTML `.guardian-copy p`).
   - `.guardianes-competencias__item { border:1px solid #d7e1d9; background:#fff; color:#34443a }` (antes borde/relleno blancos translúcidos y texto blanco → ahora pills claras estilo `.guardian-pills span`). Conservar `border-radius:var(--ct-radius-sm)` y padding.
   - `.guardianes-closing`: trasladar el estilo de cierre del HTML `.guardians-closing`: `color:#23372d; border-left:4px solid var(--ct-primary); background:#e8f8f2; padding:17px 20px; border-radius:0 12px 12px 0; font-weight:800; line-height:1.5` (antes solo texto blanco). Esto le da el bloque de cita verde claro del HTML.
   - Si existiera cualquier regla `.guardianes-band .ct-section-header h2/p` con color blanco, eliminarla o dejar que apliquen los colores claros por defecto. (Actualmente la oscuridad del header venía de `.ct-section--dark .ct-section-header p`; al quitar `ct-section--dark` ya no aplica.)
   - Las imágenes `guardianes-group__item img` no necesitan cambio de color; validar que los PNG (pensados para fondo oscuro) se vean bien sobre `#f4f7f2`. Si los PNG tienen halos claros, dejar ANOTADO (no se modifican assets).

c) Breakpoints de Guardianes: el front ya tiene `@media (max-width:768px){ .guardianes-group{repeat(4,1fr)} .guardianes-competencias{repeat(2,1fr)} }`. El HTML usa 900px (grid a 1 col para `.guardian-row`) y 650px. Como el DOM difiere, MANTENER los breakpoints existentes del front (768px) que ya funcionan; no es necesario migrar a 900/650 para Guardianes porque su layout (grid de 7 PNG + lista de competencias) no es el `.guardian-row` de 2 columnas del HTML. Documentado como decisión: se conserva el responsive actual del front para esta sección.

Verificación tras el paso 6: `npm run build` OK y `npx tsc --noEmit` OK (se tocó un `.tsx`); `npm run dev` y comprobar que la sección Guardianes tiene fondo CLARO `#f4f7f2`, título/eyebrow legibles en claro, competencias como pills blancas con borde, y el cierre como bloque verde claro con borde izquierdo verde. Sin texto blanco invisible.

---

## 7. Breakpoints del HTML (referencia) y respuesta esperada

- `@media(max-width:900px)`: en el HTML, `.hero-grid,.two-col,.guardian-row,.experience-grid` pasan a 1 columna; `.feature-grid,.offer-grid` a 2 columnas. En el front el equivalente es `@media (max-width:1050px)` (hero/methodology/manifesto/contact a 1 col) y `@media (max-width:700px)` (grids a 1 col). DECISIÓN: NO re-mapear breakpoints estructurales del front (son estructura/layout, fuera del alcance visual). Mantener 1050px/768px/700px actuales.
- `@media(max-width:650px)`: en el HTML, nav colapsa a menú, `.hero-actions` apila botones a ancho completo (`.hero-actions .btn{width:100%}`). El front ya colapsa el nav en 1050px y apila el hero en 700px. Botones: opcionalmente, bajo `max-width:700px`, se puede añadir `width:100%` a `.ct-button` dentro de `.hero-actions` para replicar el HTML; es un ajuste menor de layout, no de color. Marcar como OPCIONAL.
- Guardianes y botones responden con los breakpoints existentes del front (768px para Guardianes; 1050/700px globales para botones/nav). No se introducen 900/650 nuevos.

Racional: el encargo es VISUAL (colores/botones/fondos/Guardianes claro). Los breakpoints son estructura/layout y ya están validados en v13; cambiarlos arriesga regresiones sin beneficio visual de paleta.

---

## 8. Supuestos y huecos (anotados, sin bloquear)

1. CTA del nav (`.ct-button--dark`) se alinea a CIAN porque el HTML define `.nav-cta` cian. Si se prefiere mantenerlo oscuro, es la única desviación discutible; el HTML manda → cian.
2. Sección Contacto: NO se le aplica el gradiente `.final-cta` del HTML porque el front usa esa sección como formulario funcional (necesita contraste y no es el banner final del HTML). Si se desea el banner degradado, es un ajuste separado fuera de "igualar colores".
3. Metodología y DQUILIBRIO se mantienen con fondo blanco (coherente con la alternancia del front); el HTML los pinta sobre `#f5f7f3`. No contradice la paleta; si la revisión visual exige `#f5f7f3`, aplicar `background:var(--ct-soft)` a esas secciones.
4. `--ct-line` pasa a sólido `#dce3dd`: afecta biblioteca y legales. Validar esas páginas tras el cambio.
5. Los PNG de Guardianes fueron pensados para fondo oscuro; sobre fondo claro `#f4f7f2` podrían mostrar halos. No se modifican assets (regla de `entregables_v7/AGENTS.md`: no sustituir assets). Si hay halos, reportar.
6. No hay suite de tests; la verificación de "igualdad visual" es manual (comparación con el HTML en `npm run dev`) + `npm run build`/`tsc`/`lint` verdes.

---

## 9. Secuencia de ejecución recomendada (cada paso deja el árbol compilable)

1. (Paso 1) Leer `11-css.md` de los docs de Next. 
2. (Paso 3) Actualizar los 6 valores de tokens `:root` en `globals.css`. Verificar: `cd concientic; npm run build`.
3. (Paso 4) Reescribir `.ct-button*` (primario/CTA cian + brillo diagonal + secundario transparente). Verificar: `npm run build` + revisión visual de botones.
4. (Paso 5) Ajustar fondos: `.hero-section`, `.experiencia-section`, `.site-footer`. Verificar: `npm run build` + visual.
5. (Paso 6) Guardianes: editar `guardianes.tsx` (quitar `ct-section--dark` y `dark`) y las reglas `.guardianes-*` en `globals.css`. Verificar: `npm run build` + `npx tsc --noEmit` + visual (fondo claro).
6. Cierre: `cd concientic; npm run lint` y `npm run format:check` (o `npm run format`) para dejar el diff limpio; `npm run build` final. Revisar biblioteca y legales por el cambio de `--ct-line`.

Comandos de verificación (todos desde `c:\Jose\RepoSiginex\WebConcienTIC\concientic`):
- `npm run build` → compila sin errores (verificación autoritativa).
- `npx tsc --noEmit` → sin errores de tipos (solo tras tocar `.tsx`).
- `npm run lint` → sin errores de eslint.
- `npm run format:check` → formato correcto.
- `npm run dev` → comparación visual contra `entregables_v7/docs/concientic_v13_reference.html`.
