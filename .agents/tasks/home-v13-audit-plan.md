# Implementation Plan — Auditoría de fidelidad visual Home v13

Objetivo: dejar la home de `concientic` (Next 16.3.4 + Turbopack) **igual** a
`entregables_v8/docs/concientic_v13_reference.html` (la referencia es la fuente
de verdad visual y **gana** sobre tokens del Manual de Marca / Sistema de Diseño
en cualquier conflicto). Esto es una pasada de AUDITORÍA + CORRECCIÓN sección por
sección sobre un rediseño v13 ya existente.

## Restricciones obligatorias (leer antes de tocar código)

- **Next.js 16.x + Turbopack**: ANTES de escribir cualquier código Next, el coder
  DEBE leer la guía relevante bajo
  `c:\Jose\RepoSiginex\WebConcienTIC\concientic\node_modules\next\dist\docs\`
  (para este trabajo: `01-app/01-getting-started/11-css.md`, `12-images.md`,
  `05-server-and-client-components.md`) y acatar los avisos de deprecación. NO
  eliminar el bloque autogenerado `nextjs-agent-rules` de
  `concientic\AGENTS.md` (lo re-escribe `next dev`; commitearlo tal cual).
- **Assets**: NO base64 en producción; NUNCA sustituir/generar assets de marca.
  La imagen de grupo de Guardianes de la referencia es base64 → la home compone
  los 7 PNG individuales de `concientic\public\guardians\` (no existe archivo de
  grupo). Mantener PNG reales.
- **Formulario de contacto**: nunca simular envío exitoso; la demo de Experiencia
  vive solo en memoria (sin fetch/persistencia). Se conserva.
- **Componentes v12 no montados** (cases, challenge, desinfoxication, evidence,
  references, services, manifesto, team, value-props, etc.): NO borrar, dejarlos.
- **No colisionar** con `.guardians-section { background: var(--ct-dark) }` (lo usa
  el componente Team no montado). La home usa `.guardians-feature`. No borrar esa
  regla navy.
- Textos: en español, **exactamente** como los muestra la referencia (abajo se
  transcriben verbatim por sección).

## Comandos de verificación (no hay test runner configurado)

Desde `c:\Jose\RepoSiginex\WebConcienTIC\concientic`:

- `npm run typecheck` — `tsc --noEmit`, sin errores.
- `npm run lint` — ESLint, sin errores.
- `npm run build` — build de producción Next/Turbopack OK.
- Verificación visual: comparar la sección renderizada contra la sección
  equivalente de `concientic_v13_reference.html` (abrir la referencia en el
  navegador). Los "grep" NO cuentan como verificación.

> Nota: `npm run build` o `next dev` puede re-escribir el bloque
> `nextjs-agent-rules` en `AGENTS.md`; es esperado, commitearlo.

---

## Tokens `:root` de la referencia vs `globals.css`

Referencia (`:root` autoritativo):

```
--ink:#17231e; --ink-2:#24332c; --paper:#f5f7f3; --white:#fff;
--green:#00C49A; --green-dark:#008f70; --blue:#0077FF; --cyan:#00B4D8;
--navy:#0D182A; --purple:#6C63FF; --pink:#FF68B4; --orange:#FF7A00; --yellow:#FFC857;
--muted:#75847a; --line:#dce3dd; --light-line:#dce3dd;
--radius:24px; --max:1160px; --shadow:0 22px 60px rgba(0,0,0,.15);
```

Mapeo y **diferencias** con `globals.css` (`--ct-*`). No hay que renombrar todos
los tokens; basta alinear valores y comportamientos derivados:

| Concepto | Referencia | globals.css (`--ct-*`) | Acción |
|---|---|---|---|
| texto/tinta | `--ink #17231e` | `--ct-text #17231e` | OK |
| papel/fondo página | `--paper #f5f7f3` | `--ct-soft #f5f7f3` | OK valor |
| **body background** | `var(--paper)` **#f5f7f3** | `body{background:var(--ct-white)}` (#fff) | **FIX: body a #f5f7f3** |
| verde | `--green #00C49A` | `--ct-primary #00c49a` | OK |
| verde oscuro | `--green-dark #008f70` | *no existe* | **FIX: añadir `--ct-green-dark:#008f70`** (eyebrows, CTAs de tarjeta) |
| cian | `--cyan #00B4D8` | `--ct-cyan #00b4d8` | OK |
| navy | `--navy #0D182A` | `--ct-dark #0d182a` | OK |
| purple | `--purple #6C63FF` | `--ct-secondary #6c63ff` | OK |
| pink | `--pink #FF68B4` | `--ct-pink #ff68b4` | OK |
| orange | `--orange #FF7A00` | `--ct-orange #ff7a00` | OK |
| yellow | `--yellow #FFC857` | `--ct-yellow #ffc857` | OK |
| muted | `--muted #75847a` | `--ct-muted #75847a` | OK |
| line | `--line #dce3dd` | `--ct-line #dce3dd` | OK |
| **radius base** | `--radius 24px` | `--ct-radius-lg 24px` (varios) | OK (usar el que aplique) |
| **ancho máx** | `--max 1160px` | `.ct-container 1240px` | **DECISIÓN: mantener 1240px** (la referencia final usa `.guardians-layout`=1240, `.philosophy .wrap`=1440; 1160 es solo el `.wrap` genérico). Documentar; no es diff visible crítico. Confirmar por sección si algún ancho desentona. |

> La paleta coincide casi en su totalidad; las únicas correcciones de token son:
> **(1) body background #f5f7f3**, **(2) añadir `--ct-green-dark:#008f70`** para
> los eyebrows verde-oscuro y los CTA de tarjeta que la referencia pinta en
> `#008f70`/`var(--green-dark)`.

---

## HEADER / NAV

Archivos: `concientic\src\components\layout\header.tsx`, `globals.css`
(`.site-header`, `.header-inner`, `.site-nav`, `.header-actions`, `.brand-*`).

Referencia (markup):
- `<header>` sticky, `background:rgba(247,249,245,.94)`, `backdrop-filter:blur(16px)`,
  `border-bottom:1px solid #dfe6df`, altura nav `76px` (`66px` al hacer scroll,
  clase `is-scrolled`).
- Marca = **wordmark de texto** `<span>concienTIC<small>INTELIGENCIA DIGITAL CONSCIENTE</small></span>`.
  La referencia tiene `.brand-mark{display:none!important}` → **no se muestra
  cuadro/ícono**, solo el texto. `.brand{font-weight:850;letter-spacing:-.04em;
  font-size:1.25rem}`, `.brand small{font-size:.58rem;letter-spacing:.12em;color:#64736a;margin-top:-3px}`.
- Enlaces nav: `Ecosistema`, `Guardianes`, `Aprende`, `Metodología`, `DQUILIBRIO`
  (color `#405047`, hover `--green-dark`, subrayado animado cian en `::after`,
  `.is-active` en negrita color `--ink`).
- CTA nav: `.nav-cta` texto **"Encuentra tu ruta ↗"**, fondo **cian `#00B4D8`**,
  texto `#0D182A`, radio 999px, hover = elevación + brillo deslizante.
- Toggle móvil `.menu-toggle` texto "Menú ☰" (solo `<650px`).

Checklist de diffs:
- [ ] El componente actual usa `brand-mark` con `<Image>` del wordmark PNG. La
      referencia **oculta** el brand-mark y usa wordmark de texto. DECISIÓN de
      fidelidad: reproducir el **wordmark de texto** `concienTIC` + small
      `INTELIGENCIA DIGITAL CONSCIENTE` (coincide con el footer de referencia) y
      no mostrar ícono. (El PNG de marca queda disponible como asset pero la home
      lo renderiza como texto, igual que la referencia.)
- [ ] Verificar colores/alturas del header (76→66px scrolled, blur, borde) contra
      `globals.css .site-header`.
- [ ] CTA nav debe ser cian `#00B4D8` texto `#0D182A` (no usar `--ct-button--dark`
      si difiere) y label "Encuentra tu ruta ↗".
- [ ] Hover de enlaces = subrayado cian animado; estado activo por sección.

Verify: `npm run typecheck && npm run lint && npm run build`; abrir la home y
comparar el header con la referencia (wordmark de texto, CTA cian, sticky).

---

## 1. INICIO (hero) — `id="inicio"`, clase `.hero`

Archivos: `concientic\src\components\sections\hero.tsx`, `globals.css`
(`.hero-section`→debe comportarse como `.hero` de la referencia).

Referencia (estado final, tras overrides):
- Sección `background:#f4f7f2`, `padding:clamp(76px,9vw,124px) 0 clamp(78px,9vw,116px)`
  (override tardío; el `82px 0 88px` inicial queda sobreescrito).
- `::before` aura radial (purple/green/pink) animada; `::after` rejilla sutil.
- **`.hero-grid` = UNA sola columna**: `grid-template-columns:minmax(0,1fr)!important;gap:0!important`.
  `.hero-grid>div:first-child{max-width:920px}`. **NO hay panel/visual/imagen** a
  la derecha: el markup del hero contiene un único `<div>` con texto.
- Contenido (verbatim):
  - Pill: `<span class="pill"><span class="dot"></span> Aprende con intención. Aplica con criterio.</span>`
    (`.pill`: borde y fondo verde translúcido, color `#007f64`, radio 99px, con punto verde).
  - `<h1>La inteligencia digital empieza <em>contigo.</em></h1>` — `em` en
    **cian** (`.hero h1 em{color:var(--cyan)}`), no itálica. `h1`:
    `font-size:clamp(3rem,6.2vw,5.8rem);line-height:.99;letter-spacing:-.075em;max-width:920px`.
  - `<p class="hero-copy">No necesitas consumir más contenido. Necesitas reconocer lo que importa, desarrollar las competencias adecuadas y llevarlas a tu vida con propósito y equilibrio.</p>`
    (`color:#536158;font-size:1.13rem;max-width:760px`).
  - `.hero-actions`: `<a class="btn btn-primary" href="#experiencia">Descubre tu siguiente paso ↗</a>`
    (fondo cian `#00B4D8`, texto `#0D182A`) + `<a class="btn btn-secondary" href="#metodologia">Conoce nuestra metodología</a>`
    (transparente, borde `currentColor`).
  - `<p class="hero-note">Menos ruido. Más criterio. Aprendizaje que se convierte en acción.</p>`
    (`font-size:.82rem;color:#6f7d73`).

Checklist de diffs (impl actual difiere bastante):
- [ ] Impl usa `.hero-grid` **2 columnas** (`1.03fr .97fr`) con `.hero-visual`
      `<Image src="/diagrams/equilibrio-concientic-mejorado.png">`. La referencia
      es **1 columna, sin imagen**. → Quitar la columna visual del hero (eliminar
      `.hero-visual` y el `<Image>` del hero.tsx; dejar solo el bloque de texto).
      El PNG `equilibrio-...` no se usa en el hero de la referencia.
- [ ] Impl pone el eyebrow como `<p class="ct-eyebrow">` plano. La referencia usa
      `.pill` con `.dot` (píldora verde con punto). → Cambiar a la píldora.
- [ ] `<h1>` destaca con `<span>` en **verde primario**; la referencia usa `<em>`
      en **cian**. → Usar `em` cian y el texto exacto `contigo.`.
- [ ] Textos `subtitle`/`closing` en `site.ts` coinciden con la referencia
      (verificado) → mantener; solo asegurar que se renderizan como `hero-copy` y
      `hero-note` con los colores de referencia (`#536158`, `#6f7d73`).
- [ ] CTA primario debe ser cian (btn-primary), secundario transparente con borde.
- [ ] Padding del hero según override `clamp(...)`; fondo `#f4f7f2`.

Verify: `npm run build` OK; comparar hero contra la referencia (1 columna,
pill con punto, h1 con "contigo." en cian, dos botones, sin imagen).

---

## 2. ECOSISTEMA — `id="ecosistema"`

Archivos: `concientic\src\components\sections\ecosistema.tsx`, `globals.css`.

Referencia:
- Sección con **`style="background:#edf2ee"`** (verde muy tenue). `padding:88px 0`.
- `.section-head`: eyebrow `<span class="eyebrow" style="color:#008f70">El ecosistema ConcienTIC</span>`;
  `<h2>Tres territorios.<br/>Un propósito compartido.</h2>` (salto de línea real);
  `<p>`intro (color `#59665e`). `h2`: `clamp(2rem,4vw,3.55rem);letter-spacing:-.06em`.
- Grid: `.feature-grid` (3 col, gap 16). Tarjetas **`.feature-card`** (no
  `info-card`): borde `--light-line`, radio 23px, fondo blanco, `min-height:280px`,
  flex column, `padding-top:28px`, `.icon-box{display:none}`.
  - `.feature-card .tag`: `font-size:.72rem;font-weight:850;letter-spacing:.1em;
    text-transform:uppercase;color:#007c60;margin-bottom:22px`.
  - `h3`: `font-size:1.55rem;letter-spacing:-.055em`.
  - `a` (CTA): `font-weight:850;color:#087b60;margin-top:auto`.
  - Variantes: tarjeta 2 `.feature-card.purple` (tag/CTA color `#5b51df`);
    tarjeta 3 `.feature-card.lab-card` (fondo `linear-gradient(145deg,#fff,#fff4e9)`,
    tag/CTA color `#b65a17`).
- Contenido (verbatim):
  - Card 1 (DQnexus): tag `01 · Diagnóstico y orientación`; `<h3>DQnexus</h3>`;
    p `Reconoce fortalezas y oportunidades en tus competencias digitales para orientar próximos pasos relevantes.`;
    CTA `Explorar el diagnóstico ↗` → `#experiencia`.
  - Card 2 (TutorIA, purple): tag `02 · Aprendizaje inteligente`; `<h3>TutorIA</h3>`;
    p `Un acompañante para comprender conceptos, practicar, resolver dudas y avanzar con mayor autonomía.`;
    CTA `Conocer la experiencia ↗` → `#experiencia`.
  - Card 3 (Laboratorios IA, lab-card): tag `03 · Experimentación y aplicación`;
    `<h3>Laboratorios IA</h3>`;
    p `Espacios para diseñar, probar y aplicar soluciones con inteligencia artificial a retos concretos. SIGINEX es una de las aplicaciones especializadas del ecosistema.`;
    CTA **`Explorar laboratorios ↗`** → `#contacto`.
  - Nota: `<p class="small-note">La disponibilidad y el alcance de cada producto deben describirse según su estado real de desarrollo. Esta propuesta no presupone funcionalidades ya publicadas.</p>`
    (`font-size:.78rem;color:#7c8c82;margin-top:15px`).

Checklist de diffs:
- [ ] Fondo de sección debe ser `#edf2ee` (impl usa `.ct-section` default/blanco).
- [ ] Cambiar las tarjetas de `.info-card` (con número y `::after "Abrir"`) al
      patrón **`.feature-card`** con `.tag` + `h3` + `p` + `<a>` CTA al pie, y las
      variantes `.purple` (card 2) y `.lab-card` (card 3). Tag en mayúsculas verde.
- [ ] **Texto del CTA de Laboratorios**: la referencia dice `Explorar laboratorios ↗`
      (destino `#contacto`). `site.ts` tiene `Conversemos ↗` marcado `pending`.
      Fidelidad → usar **`Explorar laboratorios ↗`** y destino `#contacto` (actualizar
      `ecosistema.items[2].cta.label` a "Explorar laboratorios ↗", `href:"#contacto"`;
      el flag `pending` deja de aplicar porque la referencia no muestra aviso de
      pendiente). Mantener el resto de textos (coinciden verbatim).
- [ ] `h2` con `<br/>` ("Tres territorios." / "Un propósito compartido.").
- [ ] Eyebrow color `#008f70` (verde-oscuro).
- [ ] La nota de honestidad usa estilo `.small-note` (`.78rem`, `#7c8c82`), no el
      `.ecosistema-honesty` actual si difiere — alinear tamaño/color/margen.
- [ ] Impl no renderiza `pending` como "URL pendiente de confirmación" en esta
      tarjeta (la referencia no lo muestra).

Verify: `npm run build` OK; comparar sección (fondo verdoso, 3 feature-cards con
variante purple y lab gradient, textos y CTAs exactos).

---

## 3. GUARDIANES — `id="guardianes"`, clase raíz home `.guardians-feature`

Archivos: `concientic\src\components\sections\guardianes.tsx`, `src\content\site.ts`
(`guardianes`), `globals.css` (`.guardians-feature`, `.guardians-layout`,
`.guardians-kickers`, `.guardians-title`, `.guardians-intro`, `.guardian-image-*`,
`.guardian-capabilit*`, `.guardians-closing`).

Referencia (sección CLARA, 1 columna centrada):
- Sección `background:#f4f7f2` (NO navy), `padding:clamp(54px,7vw,92px) 0`.
- `.guardians-layout{width:min(1240px,calc(100% - 48px));margin:auto}`.
- `.guardians-kickers`: dos eyebrows apilados `Los Guardianes` / `Inteligencia con
  propósito`, color `var(--green-dark)` (#008f70). (`display:flex;justify-content:space-between`
  en desktop; columna en móvil.)
- `<h2 class="guardians-title">La IA tiene un papel. La persona conserva el protagonismo.</h2>`
  (`clamp(2.3rem,5.8vw,5.1rem);line-height:.99;letter-spacing:-.065em;color:var(--ink)`).
- `<p class="guardians-intro">Los Guardianes se integran donde pueden aportar orientación, protección, comprensión o creatividad. Cada intervención debe tener una razón clara y un límite definido.</p>`
  (`color:#5c6b61`).
- `.guardian-image-wrap`: en la referencia es **una sola imagen base64 de grupo**
  (`<img class="guardian-image">`). PROHIBIDO base64 → la home **compone los 7 PNG
  individuales** de `public/guardians/` (ya implementado): cada PNG sobre un **halo
  circular del color de marca del Guardián (~24% opacidad)** con su **nombre en una
  píldora** del color de marca. `role="group"` con el alt oficial; cada `<img alt="">`.
  Colores de marca oficiales (de `src/content/guardians.ts`): Emi #FF6EB6, Locky
  #0077FF, Lex #FFC857, Byte #00C49A, Detective DQ #6C63FF, Nexo #00B4D8, Nova #FF7A00.
- `.guardian-capabilities` (grid 7 col, `gap:10px`): 7 tarjetas blancas
  `.guardian-capability` (`border:1px solid #e1e9e2;border-radius:13px;background:#fff;
  color:#24332b`, icono coloreado vía `--cap-color`), en ESTE orden:
  1. `♥` `Empatía Digital` — `--cap-color:#FF2F7D`
  2. `▣` `Seguridad Digital` — `#0077FF`
  3. `⚖` `Derechos Digitales` — `#FFB000`
  4. `▤` `Alfabetización Digital` — `#22A600`
  5. `✥` `Pensamiento Crítico` — `#6C2BFF`
  6. `♧` `Colaboración Digital` — `#00A9B7`
  7. `➤` `Innovación Digital` — `#FF5B00`
- `.guardians-closing`: `<div><span>✦</span> La mejor tecnología no decide por ti: te ayuda a decidir mejor.</div>`
  (`border-left:4px solid var(--green);background:#e8f8f2;color:#23372d;font-weight:800;
  border-radius:0 12px 12px 0;padding:17px 20px`; el `✦` en `--green-dark`).

Checklist de diffs (sección ya muy alineada — auditar detalles):
- [ ] Confirmar que `guardianes.tsx` usa `.guardians-feature` (no `.guardians-section`
      navy) — ya lo hace. Verificar que NO se aplica el navy de Team.
- [ ] Verificar valores exactos en `globals.css`: fondo `#f4f7f2`, kickers color
      `--green-dark`, título `clamp(2.3rem,5.8vw,5.1rem)`, capability border
      `#e1e9e2`/radio 13px, closing `border-left:4px --green` + `#e8f8f2`.
- [ ] Verificar los 7 colores `--cap-color` y los 7 nombres/íconos en el orden
      exacto (en `site.ts.guardianes.capabilities`) — ya coinciden; confirmar.
- [ ] Verificar halos circulares: cada halo con el color de marca del Guardián a
      ~24% opacidad y píldora de nombre con ese color (ya implementado en
      `.guardian-image__art::before` / `.guardian-image__name`); ajustar opacidad/
      forma si difiere de "halo circular".
- [ ] Mantener los 7 PNG reales; `role="group"` + alt oficial; `alt=""` por imagen.

Verify: `npm run build` OK; comparar: franja clara, 2 eyebrows verde-oscuro,
título, intro, 7 personajes con halo+píldora de color, fila de 7 capacidades con
íconos de color en el orden dado, y la barra de cierre verde.

---

## 4. RECURSOS — `id="recursos"`

Archivos: `concientic\src\components\sections\recursos.tsx`, `globals.css`.

Referencia:
- Sección **sin background especial** (fondo `--paper` del body, `#f5f7f3`),
  `padding:88px 0`. (Impl usa `.ct-section--soft` → mismo tono claro; verificar
  que no quede blanco puro ni un tono distinto.)
- `.section-head`: eyebrow `<span style="color:#008f70">Aprender con intención</span>`;
  `<h2>Contenido que orienta.<br/>Experiencias que transforman.</h2>` (con `<br/>`);
  `<p>`intro.
- Grid `.offer-grid` (3 col, gap 15). Tarjetas `.offer`
  (`padding:24px;border-radius:22px;border:1px solid --light-line;background:#fff`).
  - `.offer .num`: `font-weight:900;font-size:.75rem;color:#007c60;letter-spacing:.12em`.
  - `.offer h3`: `font-size:1.25rem;letter-spacing:-.035em`.
  - `.offer p`: `font-size:.9rem;color:#5d6a61`.
  - Tarjeta 2 `.offer.featured`: fondo `linear-gradient(145deg,#e7fbf5,#f3f0ff)`,
    borde `#bcefe0`, `.num` color `#008f70`.
- `.offer-cta`: `display:inline-flex;gap:7px;margin-top:18px;font-weight:850;
  color:#008f70`, hover `translateY(-2px);opacity:.82`.
- Contenido (verbatim):
  - Card 1: num `01 / EXPLORAR`; `<h3>Referentes y recursos abiertos</h3>`;
    p `Contenido curado, fuentes confiables y herramientas para comprender un tema sin perderse entre miles de enlaces.`;
    CTA `.offer-cta` `Ver contenidos gratuitos ↗` →
    `https://www.concientic.com/servicios/contenidos`
    (`aria-label="Ver contenidos gratuitos de ConcienTIC"`).
  - Card 2 (featured): num `02 / APRENDER Y PROFUNDIZAR`;
    `<h3>Academia ConcienTIC</h3>`;
    p `Formación y programas de aprendizaje disponibles a través de nuestra academia en Hotmart, conectados con rutas y objetivos prácticos.`;
    CTA `Explorar Academia ↗` → `https://hotmart.com/` (`target="_blank" rel="noopener"`,
    estilo inline `color:#5b51df`).
  - Card 3: num `03 / PROFUNDIZAR`; `<h3>Programas y acompañamiento</h3>`;
    p `Talleres, cohortes o apoyo experto para desafíos que requieren interacción y retroalimentación.`;
    **sin CTA**.

Checklist de diffs:
- [ ] Impl usa `.info-card` con números `01/EXPLORAR` (sin espacios) y CTA
      `.info-card__cta`. La referencia usa `.offer`/`.offer.featured` con `.num`
      `01 / EXPLORAR` (con espacios) y `.offer-cta`. → Migrar a `.offer` + variante
      `.featured` en card 2; usar `.num` con el formato exacto `NN / TEXTO`.
- [ ] Ajustar los labels de número en `site.ts.recursos.items[].number` a
      `01 / EXPLORAR`, `02 / APRENDER Y PROFUNDIZAR`, `03 / PROFUNDIZAR`.
- [ ] CTA Academia: color `#5b51df` (purple), target blank. CTA card 1 color
      `#008f70`. Card 3 sin CTA.
- [ ] Impl renderiza `pending` como "URL pendiente de confirmación" bajo la
      Academia; la referencia **no muestra** ese aviso → ocultarlo para fidelidad
      (dejar el `href` de Hotmart tal cual). (Decisión: la referencia manda sobre
      el aviso de pendiente.)
- [ ] `h2` con `<br/>`; eyebrow `#008f70`; fondo de sección = `#f5f7f3` (paper).

Verify: `npm run build` OK; comparar 3 tarjetas offer (featured con gradiente
verde→lila), números con formato `NN / TEXTO`, CTAs y colores exactos.

---

## 5. METODOLOGÍA — `id="metodologia"`, clase `.ink-section`

Archivos: `concientic\src\components\sections\methodology.tsx`, `src\content\site.ts`
(`methodology`), `globals.css` (`.methodology-grid` + nuevas reglas
`.method-cycle`/`.dq-*`).

Referencia (estado final):
- Sección `.ink-section{background:#f5f7f3!important}` (override tardío; el `#f4f7f2`
  inicial queda sobreescrito), `padding:88px 0`.
- Layout `.method-cycle` (grid `.9fr 1.1fr`, gap 42, `background:transparent`): copy
  a la izquierda, **diagrama CSS** a la derecha.
- Copy (`.method-cycle-copy`):
  - eyebrow `<span style="color:var(--green-dark)">Nuestra metodología</span>`.
  - `<h3>Un ciclo, no una carrera.</h3>` (`clamp(1.8rem,3vw,2.5rem);letter-spacing:-.055em`).
  - `<p>`: **texto de referencia** (difiere de `site.ts`):
    `La metodología ConcienTIC traduce la filosofía DQUILIBRIO en una práctica de aprendizaje y transformación. No es una escalera rígida ni un instrumento clínico: es una invitación a detenerse, mirar el contexto, decidir con intención y volver a ajustar.`
  - `.method-steps` (grid 2 col, gap 12): 5 `.method-step`
    (`border-top:2px solid var(--step-color);background:#fff;border-radius:0 0 12px 12px`),
    cada uno `<b>NN · TÍTULO</b><span>desc</span>` con `--step-color`:
    1. `01 · DETENER` / `Interrumpir el piloto automático.` — `--step-color:var(--green)`
    2. `02 · OBSERVAR` / `Hacer visible el contexto y los hábitos.` — `var(--cyan)`
    3. `03 · DISCERNIR` / `Contrastar información y riesgos.` — `var(--purple)`
    4. `04 · ELEGIR` / `Decidir con intención.` — `var(--orange)`
    5. `05 · EQUILIBRAR` / `Transferir, ajustar y volver a elegir.` — `var(--pink)`
- Diagrama `.dq-diagram` (final: `background:transparent;border:0;overflow:visible`),
  con `.dq-orbit` (anillo cónico `conic-gradient(from -35deg,green,cyan,purple,orange,pink,green)`,
  270×270, anima `orbitBreathe`), `.dq-core` (círculo 124px, `#17231e`, texto blanco:
  `DQUILIBRIO` + `<small>tecnología<br/>con conciencia</small>`), y 5 `.dq-node`
  posicionados (n-att/n-hum/n-ai/n-auto/n-crit) con los textos
  `01 · DETENER … 05 · EQUILIBRAR`. `role="img"` con aria-label del ciclo.

Checklist de diffs (impl difiere sustancialmente):
- [ ] Impl usa un **PNG** `/diagrams/ciclo-dquilibrio-mejorado.png` en `.cycle`.
      La referencia usa un **diagrama CSS** (`.dq-diagram`/`.dq-orbit`/`.dq-core`/
      `.dq-node`). → Reemplazar el `<Image>` por el diagrama CSS reproduciendo el
      markup de referencia (orbe cónico + core + 5 nodos). Añadir las reglas
      `.dq-*`, `.method-cycle`, `.method-steps`, `.method-step` a `globals.css`.
      (El PNG deja de usarse en esta sección.)
- [ ] Grid: usar `.9fr 1.1fr` (impl `.72fr 1.28fr`).
- [ ] Encabezado: la referencia usa `<h3>` (no `<h2>`) "Un ciclo, no una carrera."
      con eyebrow "Nuestra metodología" (impl `site.ts` tiene eyebrow "Metodología"
      y usa `<h2>`). → eyebrow a `Nuestra metodología`; usar el nivel/estilo de
      `.method-cycle-copy h3`.
- [ ] **Texto del párrafo**: actualizar `methodology.subtitle` al texto de
      referencia citado arriba (difiere del actual).
- [ ] Pasos: formato `NN · TÍTULO` + descripción, con `--step-color` por paso
      (green/cyan/purple/orange/pink) y estilo `.method-step` (borde superior de
      color, fondo blanco). Los textos de pasos deben ser los de la referencia
      (p.ej. paso 02 "Hacer visible el contexto y los hábitos.", paso 05
      "Transferir, ajustar y volver a elegir.").
- [ ] Fondo de sección `#f5f7f3` (ink-section override).
- [ ] Respetar `prefers-reduced-motion` (sin animación del orbe).

Verify: `npm run build` OK; comparar: copy + diagrama CSS (orbe cónico con core
"DQUILIBRIO" y 5 nodos), 5 method-steps con colores, textos exactos.

---

## 6. EXPERIENCIA — `id="experiencia"`, clase `.experience`

Archivos: `concientic\src\components\sections\experiencia.tsx`, `src\content\site.ts`
(`experiencia`), `globals.css` (`.experiencia-section` / clases demo).

Referencia:
- Sección `.experience{background:#eaf7f8;border-top:3px solid var(--cyan);
  border-bottom:1px solid #d8e8ea}`, `padding:88px 0`.
- `.experience-grid` (2 col, gap 30): copy a la izquierda, demo a la derecha.
- Copy:
  - eyebrow `<span style="color:#008f70">Empieza por ti</span>`.
  - `<h2>Tu siguiente paso no tiene que ser aprenderlo todo.</h2>`
    (`clamp(2rem,4vw,3.4rem);letter-spacing:-.06em`).
  - **Dos párrafos** (la referencia divide el texto; difiere del `notice` único):
    - P1: `Comienza identificando qué necesitas resolver. Este pequeño ejemplo muestra cómo ConcienTIC podría orientar el primer paso. No es el diagnóstico oficial DQnexus ni guarda respuestas.`
    - P2: `En la experiencia real, las recomendaciones deberán apoyarse en criterios transparentes, recursos confiables y una ruta relacionada con tu objetivo.`
- Demo `.demo` (card blanca, radio 26, sombra):
  - `.demo-label` `Experiencia demostrativa · 1 pregunta`.
  - `<h3>¿Qué te gustaría mejorar primero?</h3>`.
  - 4 `.choice` (radio `name="need"`, borde 15px; `:has(input:checked)` →
    `border-color:var(--cyan);background:#eafaff`):
    - ia: `Usar la IA con más criterio y seguridad.`
    - info: `Evaluar información y reconocer la desinformación.`
    - product: `Ahorrar tiempo con herramientas digitales.`
    - balance: `Recuperar equilibrio y atención en mi vida digital.`
  - `<button class="btn btn-primary">Ver una orientación inicial ↗</button>`
    (ancho 100%).
  - `.demo-result` (oculto hasta mostrar): `<strong>` título + `<p>` texto.
    Recomendaciones por opción (del script; coinciden con `site.ts`):
    - ia → `Empieza por una tarea real` / `Elige una tarea que hagas con frecuencia…`
    - info → `Practica la verificación` / `Toma una afirmación…`
    - product → `Detecta una fricción cotidiana` / `Elige una tarea repetitiva…`
    - balance → `Observa antes de cambiar` / `Identifica un momento del día…`

Checklist de diffs (impl muy cercana):
- [ ] Verificar fondo `#eaf7f8` + borde superior cian 3px (impl `.experiencia-section`
      ya lo tiene; confirmar el `border-bottom:1px solid #d8e8ea`).
- [ ] **Copy**: la referencia muestra el eyebrow+h2 **y dos párrafos** de intro
      junto a la demo; la impl usa `SectionHeader` (solo eyebrow+title) + un
      `experiencia.notice` único encima de la demo. → Alinear: renderizar los **dos
      párrafos** de la referencia como copy a la izquierda, dentro de `.experience-grid`.
      Añadir en `site.ts` los dos párrafos (P1/P2) y usarlos; conservar la demo
      como lógica en memoria. (Decisión: la referencia manda sobre la redacción;
      el párrafo "Este pequeño ejemplo…" NO se pierde — queda como P1.)
- [ ] Layout 2 columnas `.experience-grid` (copy | demo).
- [ ] `.choice` seleccionado → borde cian + fondo `#eafaff`; radios `accent-color:var(--cyan)`.
- [ ] Botón ancho 100%, label `Ver una orientación inicial ↗`.
- [ ] Mantener: aviso de estado `aria-live`, sin envío; mensaje vacío
      `Elige una opción para ver una orientación.` (el `emptyMessage` de impl no
      existe en la referencia pero es mejora accesible admisible; conservar).
- [ ] Nota: `experiencia.notice` actual (texto largo "Este pequeño ejemplo…")
      se reparte en P1/P2; verificar que no quede texto duplicado.

Verify: `npm run build` OK; comparar sección (fondo turquesa, borde cian, copy
2 párrafos + demo con 4 opciones y resultado).

---

## 7. DQUILIBRIO — `id="dquilibrio"`, clase `.philosophy-section`

Archivos: `concientic\src\components\sections\dquilibrio.tsx`, `src\content\site.ts`
(`dquilibrio`), `globals.css` (`.philosophy-section`/`.philosophy-panel`/
`.philosophy-cards`/`.philosophy-card`).

Referencia (estado final tras overrides):
- Sección `.philosophy-section{background:#f5f7f3!important;padding-block:clamp(54px,7vw,92px)}`
  (el `#f1f6f2`/`#f1f6f2` inline y previos quedan sobreescritos por `#f5f7f3`).
- `.philosophy-section>.wrap{width:min(1440px,calc(100% - 44px))}` (más ancho).
- `.philosophy-panel` final: `background:transparent;border:0;padding:0` (sin
  recuadro contenedor).
- `.section-head`: eyebrow `<span style="color:#008f70">La filosofía que nos guía · DQUILIBRIO</span>`;
  `<h2>La tecnología no es el enemigo. La falta de elección sí puede serlo.</h2>`
  (`clamp(2.4rem,5vw,4.2rem);line-height:1.02;letter-spacing:-.065em`);
  `<p>DQUILIBRIO es la filosofía transversal de ConcienTIC: aumentar nuestra capacidad de elegir cómo, cuándo, por qué y para qué usamos la tecnología.</p>`.
- `.philosophy-cards` (grid 4 col, gap 16). Tarjetas `.philosophy-card`
  (`background:#fff;border:1px solid #e5e9e5;border-radius:20px;padding:clamp(20px,2.4vw,30px);
  min-height:220px;border-top:1px solid #e5e9e5` — en el estado final el borde
  superior NO es de color; `--accent` solo tiñe `.num` y el CTA). Flex column.
  - `.philosophy-card .num`: `font-size:.72rem;font-weight:900;letter-spacing:.11em;color:var(--accent)`.
  - `h3`: `font-size:1.12rem`. `p`: `font-size:.94rem;color:#5c6a61`.
  - `.card-cta`: `color:var(--accent);margin-top:auto;padding-top:18px;font-weight:850`.
- Contenido (verbatim), cada tarjeta con `style="--accent:..."`:
  - 01 (`--accent:#00C49A`): num `01 · ATENCIÓN`; `<h3>Recuperar presencia</h3>`;
    p `Reconocer que la atención es limitada y diseñar experiencias que la respeten.`;
    CTA `Explorar Atención ↗` → `https://www.concientic.com/dquilibrio/atencion.html`.
  - 02 (`--accent:#0077FF`): num `02 · INFORMACIÓN`; `<h3>Reducir saturación</h3>`;
    p `Pasar de acumular información a seleccionar, contextualizar y sintetizar.`;
    CTA `Explorar Información ↗` → `.../informacion.html`.
  - 03 (`--accent:#6C63FF`): num `03 · DISCERNIMIENTO`; `<h3>Decidir con criterio</h3>`;
    p `Verificar fuentes, reconocer sesgos y diferenciar evidencia de apariencia.`;
    CTA `Explorar Discernimiento ↗` → `.../discernimiento.html`.
  - 04 (`--accent:#FF7A00`): num `04 · AUTONOMÍA`; `<h3>Elegir conscientemente</h3>`;
    p `Usar IA y tecnología como herramientas al servicio de objetivos humanos.`;
    CTA `Explorar Autonomía ↗` → `.../autonomia.html`.

Checklist de diffs (impl difiere en estructura y textos):
- [ ] Impl usa `.card-grid--four` de `.info-card` **clicables completas** (toda la
      tarjeta es `<a>`, sin CTA textual, con `::after "Abrir →"`). La referencia usa
      `.philosophy-card` (no es enlace completo) con un **CTA textual** `Explorar X ↗`
      al pie, teñido por `--accent`. → Migrar a `.philosophy-card` con `.num`+`h3`+`p`+
      `.card-cta`, y envolver en `.philosophy-section`/`.philosophy-panel`.
- [ ] **Textos divergentes** — actualizar `site.ts.dquilibrio`:
      - eyebrow → `La filosofía que nos guía · DQUILIBRIO` (impl: `DQUILIBRIO`).
      - numbers en MAYÚSCULA: `01 · ATENCIÓN`, `02 · INFORMACIÓN`,
        `03 · DISCERNIMIENTO`, `04 · AUTONOMÍA` (impl: `01 · Atención`,
        `02 · Información`, `03 · Criterio`, `04 · Autonomía`).
      - descripción card 2: `Pasar de acumular información a seleccionar, contextualizar y sintetizar.`
        (impl: "...y comprender.").
      - descripción card 3: `Verificar fuentes, reconocer sesgos y diferenciar evidencia de apariencia.`
        (impl: "Distinguir evidencia, interpretación y opinión antes de actuar.").
      - descripción card 4: `Usar IA y tecnología como herramientas al servicio de objetivos humanos.`
        (impl: "Usar la tecnología por decisión propia, no por inercia.").
      - añadir `cta` por tarjeta (`Explorar Atención/Información/Discernimiento/Autonomía ↗`)
        y `color`/`--accent` por tarjeta (#00C49A/#0077FF/#6C63FF/#FF7A00).
- [ ] Fondo `#f5f7f3`, `.wrap` ancho 1440, panel transparente (sin recuadro).
- [ ] Los enlaces abren las subpáginas DQUILIBRIO (target/rel según referencia:
      la referencia NO pone target _blank en estas `.card-cta`; respetar: enlaces
      normales same-tab salvo que se decida lo contrario — fidelidad = sin `target`).

Verify: `npm run build` OK; comparar panel (4 philosophy-cards blancas con num
coloreado, CTA "Explorar X ↗" teñido por accent, textos exactos).

---

## 8. CONTACTO — `id="contacto"`, clase `.final-cta`

Archivos: `concientic\src\components\sections\contact.tsx`, `src\content\site.ts`
(`contact`), `globals.css` (`.final-cta`/`.final-inner`).

Referencia:
- Sección `.final-cta{background:linear-gradient(110deg,#e5faf3,#f2efff 62%,#fff0f7);
  color:var(--ink);padding:70px 0}` con `::after` glow radial purple.
- `.final-inner` (**flex, fila**, `justify-content:space-between`, gap 30):
  - Columna texto: eyebrow `<span style="color:var(--green)">El siguiente paso empieza aquí</span>`;
    `<h2>Haz que la tecnología tenga sentido para ti.</h2>`
    (`clamp(2rem,4vw,3.7rem);letter-spacing:-.065em`);
    `<p>Cuéntanos qué quieres comprender, mejorar o transformar. Podemos ayudarte a identificar un punto de partida.</p>`.
  - CTA: `<a class="btn btn-primary" href="https://concientic.com/#contacto">Conversemos ↗</a>`
    (cian).
- **No hay formulario** en la sección final de la referencia.

Checklist de diffs (divergencia estructural):
- [ ] Impl renderiza un **formulario de contacto** (`contact-grid` + `ContactForm`).
      La referencia muestra una **banda CTA horizontal sin formulario**. DECISIÓN
      de fidelidad ("tal cual la referencia"): reproducir la **banda `.final-cta`**
      (texto + un botón "Conversemos ↗"), SIN formulario en la home.
      - Mantener el componente `contact-form.tsx` en el repo (no borrar; AGENTS.md
        exige un formulario real en el sitio y prohíbe simular envíos) pero **no
        montarlo** en la home. Queda disponible para una futura página/subruta de
        contacto. Registrar esta decisión como nota al equipo (el formulario de la
        home desaparece porque la referencia v13 no lo incluye).
- [ ] Layout: `.final-inner` flex fila (en móvil columna), no grid con form.
- [ ] Fondo degradado `linear-gradient(110deg,#e5faf3,#f2efff 62%,#fff0f7)` + glow.
- [ ] CTA `Conversemos ↗` cian; `href` → `#contacto` (o el `https://concientic.com/#contacto`
      de la referencia; usar `#contacto` interno para navegación dentro del sitio —
      decisión menor, documentar). Eyebrow color `var(--green)`.
- [ ] Textos eyebrow/title/subtitle ya coinciden con `site.ts` (verificado).

Verify: `npm run build` OK; comparar banda final (degradado, texto a la izquierda,
botón cian a la derecha, sin formulario).

---

## FOOTER

Archivos: `concientic\src\components\layout\footer.tsx`, `globals.css` (`.site-footer`).

Referencia:
- `<footer>`: `background:#e7eee8;color:#4e5d53;padding:28px 0;font-size:.82rem;
  border-top:1px solid #d8e1d9`.
- `.footer-inner` (flex, space-between): marca **wordmark de texto**
  `<a class="brand"><span>concienTIC<small>INTELIGENCIA DIGITAL CONSCIENTE</small></span></a>`;
  `.footer-links` (`Ecosistema`,`Guardianes`,`Aprende`,`Metodología`,`DQUILIBRIO`,
  hover `--green-dark`, `translateY(-2px)`); `<span>© <año> ConcienTIC</span>`.

Checklist de diffs:
- [ ] Impl usa `<Image>` del wordmark PNG en `.footer-brand-mark`. La referencia usa
      **wordmark de texto** (igual que el header). → Reproducir el wordmark de texto
      `concienTIC` + small `INTELIGENCIA DIGITAL CONSCIENTE`.
- [ ] Copyright: la referencia muestra `© <año actual> ConcienTIC` (año dinámico);
      impl muestra `© ConcienTIC`. → Añadir el año (`new Date().getFullYear()`),
      renderizado en servidor es aceptable.
- [ ] Verificar fondo `#e7eee8`, color texto, borde superior, hover de enlaces.

Verify: `npm run build` OK; comparar footer (fondo verde-grisáceo, wordmark de
texto, enlaces, © año ConcienTIC).

---

## Orden de ejecución sugerido

1. Tokens globales (`globals.css` `:root`, body background, `--ct-green-dark`) —
   base para el resto. Verify: `npm run build`.
2. Header + Footer (wordmark de texto, CTA cian). Verify: build.
3. Hero (1 columna, pill, h1 cian, sin imagen). Verify: build.
4. Ecosistema (feature-cards + variantes, fondo `#edf2ee`, textos). Verify: build.
5. Guardianes (auditar detalles; ya alineado). Verify: build.
6. Recursos (offer-cards + featured, formato de números). Verify: build.
7. Metodología (diagrama CSS `dq-*`, method-steps, texto). Verify: build.
8. Experiencia (2 columnas, 2 párrafos, demo). Verify: build.
9. DQUILIBRIO (philosophy-cards + CTAs, textos). Verify: build.
10. Contacto (banda final-cta, sin form). Verify: build.
11. Pasada final: `npm run typecheck && npm run lint && npm run build`; revisión
    visual sección por sección contra la referencia; responsive (900/650/560px);
    `prefers-reduced-motion`.

## Supuestos / vacíos (decisiones tomadas)

- La referencia **gana** sobre `site.ts` cuando el texto difiere (CTA de
  Laboratorios, descripciones DQUILIBRIO, párrafo de Metodología, intro de
  Experiencia). Se actualizan los textos en `site.ts` para que coincidan verbatim.
- El hero de la referencia **no tiene imagen**; el PNG `equilibrio-...` deja de
  usarse en el hero (no se borra el asset).
- La metodología de la referencia usa **diagrama CSS**, no el PNG
  `ciclo-dquilibrio-mejorado.png` (el asset no se borra).
- La sección de contacto de la referencia **no tiene formulario**; se monta la
  banda `.final-cta`. `contact-form.tsx` se conserva sin montar (AGENTS.md exige
  un formulario funcional en el sitio; la home v13 simplemente no lo muestra).
- Ancho máximo: se mantiene `.ct-container` (1240px) y los anchos específicos de
  Guardianes (1240) y DQUILIBRIO (1440) de la referencia; el `--max:1160px` del
  `.wrap` genérico no es un diff visible crítico.
- Imagen de grupo de Guardianes: se compone con los 7 PNG individuales (base64
  prohibido); comportamiento ya implementado, solo se audita.
