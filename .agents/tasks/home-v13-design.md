# Diseño — Rediseño de la página principal ConcienTIC (v12 → v13)

> Artefacto: requisitos + diseño técnico (NO implementación).
> Alcance: solo la página principal (`concientic/src/app/page.tsx`) y lo estrictamente necesario de header, footer y contenido tipado para montarla según el paquete `entregables_v7`.
> Fuente de autoridad: `entregables_v7/kiro/specs/concientic-web-redesign.md` §14 y §15, `entregables_v7/AGENTS.md`, steering de `entregables_v7/kiro/steering/*.md`, Manual de Marca v2.0 y Sistema de Diseño Web v1.0 (resumidos en `design-system.md` y `reference-code/styles/design-tokens.css`).
> Regla transversal del usuario: "no asumir nada, hacerlo tal cual". Donde el paquete pide confirmación de Jeimy se aplica la **regla por defecto que el propio paquete define** y se cita; no se inventa ninguna decisión de producto.

---

## 1. Resumen

El paquete v7 define en §14 un **reemplazo total de la arquitectura de información** de la home, no una serie de ajustes. La home v12 (14 secciones montadas en `page.tsx`) pasa a **8 secciones de contenido** en orden fijo (§14.1): Hero, Ecosistema, Guardianes, Recursos, Metodología, Experiencia, DQUILIBRIO, Contacto, más Header y Footer.

Tres secciones son **nuevas** (Ecosistema, Recursos, Experiencia); dos se **conservan con su contenido** (Metodología, DQUILIBRIO); Guardianes se **transforma** de grilla de 7 tarjetas con bio a una **franja única** (imagen de grupo + 7 competencias + cierre); Hero y Contacto **cambian de copy** a los textos exactos de §14.3 y §14.9. Ocho secciones de v12 se **desmontan de la home pero sus archivos NO se borran** (§14.1, steering `structure.md`), pendientes de confirmación de Jeimy sobre su destino.

Los tokens de color, tipografía, radios, espaciado, sombras y tiempos **ya existen** en `concientic/src/app/globals.css` y coinciden con `reference-code/styles/design-tokens.css`. Se mantienen los **valores oficiales del Manual de Marca**, NO las 4 desviaciones del archivo de referencia v13 (§14.10) ni el cambio a logo de texto (§14.7), por mandato explícito del criterio §15 ("hasta entonces, la implementación usa los valores oficiales del Manual de Marca").

Stack bloqueado (steering `tech.md` + estado real del repo): **Next.js App Router + React 19 (canary vía App Router) + TypeScript estricto + Tailwind v4 con tokens CSS en `globals.css`**, `next/font/google` (Inter + Poppins ya cargadas en `layout.tsx`), `next/image` para raster. Esta versión de Next.js trae breaking changes (ver `concientic/AGENTS.md`); el diseño se apoya en la guía local `node_modules/next/dist/docs/` y en patrones ya presentes en el repo (p. ej. el tipo generado `LayoutProps<"/">` en `layout.tsx`).

---

## 2. Requisitos

### 2.1 Requisitos funcionales

- **RF1 — Orden de secciones.** `page.tsx` monta exactamente, y en este orden: Hero (`#inicio`) → Ecosistema (`#ecosistema`) → Guardianes (`#guardianes`) → Recursos (`#recursos`) → Metodología (`#metodologia`) → Experiencia (`#experiencia`) → DQUILIBRIO (`#dquilibrio`) → Contacto (`#contacto`). (§14.1)
- **RF2 — Navegación.** Menú principal del header en orden exacto: `Ecosistema · Guardianes · Aprende · Metodología · DQUILIBRIO` + botón CTA `Encuentra tu ruta ↗` que ancla a `#experiencia`. El footer repite los **mismos 5 enlaces (sin CTA)** + logotipo + `© ConcienTIC`. (§14.2)
- **RF3 — Hero.** Textos exactos de §14.3 (ver §9.1 de este doc), visual `equilibrio-concientic-mejorado.png` vía `next/image`, dos CTA con anclas `#experiencia` y `#metodologia`. El h1 está **hardcodeado en el JSX de `hero.tsx`**: debe editarse el componente para renderizar el título §14.3 (no basta con `site.ts`; ver nota de §3).
- **RF4 — Ecosistema (nueva).** Encabezado + intro + 3 tarjetas (DQnexus, TutorIA, Laboratorios IA con SIGINEX) + **nota de honestidad literal, visible y sin resumir**. Textos exactos §14.4 (ver §9.2).
- **RF5 — Guardianes (franja única).** Encabezado + cuerpo + 1 imagen de grupo de los 7 Guardianes + lista de 7 competencias (sin nombre de Guardián visible) + cierre. Textos exactos §14.5 (ver §9.3). NO grilla de 7 tarjetas con bio.
- **RF6 — Recursos (nueva).** Encabezado + intro + 3 tarjetas (Referentes y recursos abiertos → Contenidos; Academia ConcienTIC → Hotmart; Programas y acompañamiento). Textos exactos §14.6 (ver §9.4).
- **RF7 — Metodología.** Conserva contenido y el diagrama `ciclo-dquilibrio-mejorado.png`; se mantiene el id `#metodologia`. (§14.1)
- **RF8 — Experiencia (nueva, cliente).** Demo interactiva de 1 pregunta con 4 opciones + botón que muestra una orientación. Componente de cliente puramente ilustrativo, sin persistencia ni envío. **Aviso "no es el diagnóstico oficial / demostrativa" siempre visible y sin modificar.** Textos exactos §14.8 (ver §9.5).
- **RF9 — DQUILIBRIO.** Conserva contenido y las 4 tarjetas; los enlaces de las 4 tarjetas apuntan al **dominio absoluto** `https://www.concientic.com/dquilibrio/{atencion,informacion,discernimiento,autonomia}.html` con `target="_blank"` y `rel="noopener noreferrer"`. (§14.1, §15)
- **RF10 — Contacto.** Encabezado + cuerpo + CTA de §14.9 (ver §9.6). El formulario **nunca** simula envío exitoso: el estado de éxito solo aparece tras un `200` real de `/api/contact` (envío real vía Resend), con mensaje `role="status"` `aria-live="polite"`, nunca `alert()`. El mensaje de éxito actual ("Formulario de demostración / Maqueta de demostración…") es **falso respecto al backend real** y se reemplaza por una confirmación honesta (ver nota de §3). Decisión: conservar el pipeline real de envío; la elección maqueta vs. envío real queda como P11 para Jeimy.
- **RF11 — Logo header.** Se mantiene el **wordmark-imagen** `ConcienTIC Nexus` (componente/variantes claro-oscuro), NO el lockup de texto CSS de la referencia v13. (§14.7, regla por defecto)
- **RF12 — Dominio de producción.** `https://www.concientic.com` en todo el código, metadata y enlaces absolutos. (§14.6)
- **RF13 — Conservación de componentes.** Las secciones v12 que salen de la home se desmontan de `page.tsx` pero sus archivos de componente permanecen en el repo. (§14.1, `structure.md`)

### 2.2 Requisitos no funcionales

- **RNF1 — UX consciente (obligatorio).** Sin infinite scroll, autoplay, dark patterns, FOMO, streaks ni urgencia artificial. Animación sobria. (`product.md`, `AGENTS.md`)
- **RNF2 — Movimiento accesible.** Toda transición desactivada bajo `prefers-reduced-motion: reduce` (ya implementado globalmente en `globals.css`). (`design-system.md`)
- **RNF3 — Accesibilidad.** Skip link "Saltar al contenido" → `#main` al inicio del `<body>` (ya existe en `layout.tsx`; **conservar aunque la referencia v13 lo omita** — §14.11); `:focus-visible` global azul 2px offset 3px (ya en `globals.css`); contraste AA; jerarquía semántica; patrones hover/focus de `design-system.md`.
- **RNF4 — Marca.** Colores hex oficiales del Manual de Marca; Poppins para títulos, Inter para UI (ya cargadas). NO aplicar las 4 desviaciones de §14.10.
- **RNF5 — Tokens.** Contenedor 1240px; radios 12/18/24/34px; escala de espaciado 4→96px; sombras sm/md; tiempos 160/240ms (ya presentes en `globals.css`).
- **RNF6 — Assets.** Imágenes locales optimizadas con `next/image`; sin base64 en producción; sin reconstruir los diagramas DQUILIBRIO en SVG/CSS; sin sustituir assets de marca por aproximaciones; sin inventar casos/métricas/clientes/testimonios/nombres. (`AGENTS.md`)
- **RNF7 — Calidad.** `npm run build` sin errores de TypeScript ni lint; responsive a 360/768/1024/1440 px. (§15)

### 2.3 Fuera de alcance

- Implementar el código (este artefacto solo diseña).
- Borrar archivos de las secciones v12 desmontadas (prohibido hasta confirmación de Jeimy — §14.1).
- Cambiar el logo a lockup de texto CSS (§14.7, pendiente de Jeimy).
- Aplicar las 4 desviaciones de paleta/tipografía de §14.10.
- Modificar las subpáginas estáticas de DQUILIBRIO (`public/dquilibrio/*.html`) y Contenidos (`public/servicios/contenidos.html`): se sirven tal cual (§14.1, §15, `structure.md`).
- Crear una subpágina `/equipo` para las bios de Guardianes (solo se señala como pendiente de Jeimy).
- Quitar el bloque `BEGIN:nextjs-agent-rules` de `concientic/AGENTS.md`.

---

## 3. Correspondencia sección por sección (8 secciones nuevas)

Leyenda: **Reutiliza** = componente existente sirve con cambio de copy; **Transforma** = reescribir el componente; **Nuevo** = crear archivo.

| # | Sección v13 (id) | Componente | Archivo | Acción | Datos tipados en `src/content/` |
|---|------------------|------------|---------|--------|----------------------------------|
| 1 | Hero (`#inicio`) | `Hero` | `sections/hero.tsx` | Reutiliza (nuevo copy §14.3) — **editar también el JSX del h1** (ver nota) | `hero` en `site.ts` → reemplazar valores |
| 2 | Ecosistema (`#ecosistema`) | `Ecosistema` | `sections/ecosistema.tsx` | **Nuevo** | `ecosistema` en `site.ts` (nuevo) |
| 3 | Guardianes (`#guardianes`) | `Guardianes` | `sections/guardianes.tsx` | **Transforma** (franja única) | `guardianes` en `site.ts` (nuevo) + `competencias` |
| 4 | Recursos (`#recursos`) | `Recursos` | `sections/recursos.tsx` | **Nuevo** | `recursos` en `site.ts` (nuevo) |
| 5 | Metodología (`#metodologia`) | `Methodology` | `sections/methodology.tsx` | Reutiliza sin cambios | `methodology` en `site.ts` (sin cambios) |
| 6 | Experiencia (`#experiencia`) | `Experiencia` | `sections/experiencia.tsx` (`"use client"`) | **Nuevo** | `experiencia` en `site.ts` (nuevo) |
| 7 | DQUILIBRIO (`#dquilibrio`) | `Dquilibrio` | `sections/dquilibrio.tsx` | Reutiliza (ajustar hrefs a dominio absoluto + `target`) | `dquilibrio` en `site.ts` → ajustar `href` |
| 8 | Contacto (`#contacto`) | `Contact` + `ContactForm` | `sections/contact.tsx`, `sections/contact-form.tsx` | Reutiliza (nuevo copy §14.9) + **corregir el mensaje de éxito del formulario** (ver nota) | `contact` en `site.ts` → reemplazar valores |

Notas de correspondencia:

- **Hero**: `sections/hero.tsx` ya usa `equilibrio-concientic-mejorado.png`, dos CTA y eyebrow desde `site.ts`. **Verificado en código**: el h1 está **hardcodeado en el JSX** (`<h1>Aprender tecnología. <span>Sin perder humanidad.</span></h1>`) y **NO lee `hero.title`** — el campo `hero.title` de `site.ts` ni siquiera se usa en el h1. Por tanto, **reemplazar valores en `site.ts` NO basta**: si solo se edita `site.ts`, el h1 seguiría mostrando el texto v12 e incumpliría §14.3. La corrección obligatoria es **editar el JSX de `hero.tsx`**. Regla fijada (se elige la opción (a) para respetar "contenido separado de presentación", `AGENTS.md` §Implementación): renderizar `{hero.title}` desde data y **eliminar tanto el texto literal como el `<span>`** de color, de modo que el h1 muestre exactamente "La inteligencia digital empieza contigo." como un solo nodo de texto. El paquete no define resalte para este titular → **no inventar `<span>` de color** (si en el futuro se quisiera resaltar, sería decisión de Jeimy). `hero.eyebrow`, `hero.subtitle`, `hero.primaryCta` y `hero.secondaryCta` sí se aplican por data (el componente ya los lee), así que esos solo requieren editar `site.ts`. Se añade la "frase de cierre visual" (§14.3) como párrafo destacado al pie del bloque de copy (nuevo campo `hero.closing` en `site.ts` + nuevo `<p>` en el JSX, ya que hoy el componente no lo renderiza).
- **Guardianes**: `sections/team.tsx` actual (grilla 7 + quote + cierre de adjetivos) se **desmonta**; se crea `sections/guardianes.tsx` nuevo con la franja única. `ui/guardian-card.tsx` y `sections/team.tsx` **NO se borran** (reutilizables en una futura `/equipo` — §14.5, `structure.md`). La franja usa los 7 PNG individuales ya presentes en `public/guardians/` compuestos en una sola imagen de grupo (ver §5 edge cases).
- **DQUILIBRIO**: `sections/dquilibrio.tsx` ya renderiza las 4 tarjetas desde `dquilibrio.items`. Cambios: (a) `href` a dominio absoluto `https://www.concientic.com/dquilibrio/*.html`; (b) añadir `target="_blank"` (hoy no lo tiene). El contenido textual de las tarjetas se conserva.
- **Contacto** (decisión verificada contra el código, resuelve el hallazgo HIGH): `sections/contact-form.tsx` **NO es una maqueta**. Lectura verificada de `contact-form.tsx`, `src/app/api/contact/route.ts` y `src/lib/email.ts`:
  - El `onSubmit` hace un `POST` real a `/api/contact`; la ruta valida con zod, aplica rate-limiting por IP (`limitByIp`), tiene honeypot y llama a `sendContactEmail`.
  - `sendContactEmail` (`@/lib/email`) usa **Resend**. Si NO hay `RESEND_API_KEY`/`CONTACT_FROM_EMAIL`, devuelve `{ ok:false, error:"El servicio de correo no está configurado." }`; la ruta responde **502** y la UI muestra el **estado de error** ("No se pudo enviar el mensaje."), por lo que **el estado de éxito nunca se alcanza sin envío real**. Si Resend está configurado, el correo **se envía de verdad** y luego la UI muestra `trackLeadSubmitted()` + el mensaje "Formulario de demostración / Maqueta de demostración…".
  - Conclusión: el mensaje de éxito actual es **falso y contradictorio** (afirma que no hay envío justo cuando sí lo hubo). El patrón ARIA (`role="status"`/`aria-live="polite"`, sin `alert()`) ya es correcto; el problema es el **texto**, no la estructura.

  **Decisión fijada — opción (a) del hallazgo 1: conservar el pipeline real y corregir el mensaje.** Se elige conservar el envío real porque el paquete v7 no ordena desmontar backend existente y la regla del usuario es "hacerlo tal cual" (eliminar funcionalidad operativa sería una decisión de producto no mandatada). Cambios concretos en `contact-form.tsx`, bloque `if (status === "success")`:
    - Título: `Mensaje enviado` (reemplaza "Formulario de demostración").
    - Cuerpo: `Gracias, hemos recibido tu mensaje. Te responderemos al correo que indicaste.` (reemplaza el texto de "Maqueta de demostración… conecta este formulario a un servicio real").
    - Se **mantienen** `role="status"`, `aria-live="polite"` y el botón "Enviar otro mensaje".
  Con esto se cumple `AGENTS.md` §Interacción ("el formulario de contacto nunca debe simular un envío exitoso"): el estado de éxito **solo** se muestra tras un `200` real del backend, así que el mensaje refleja el resultado real y **no finge** nada. Esto corrige una afirmación falsa de la UI y **no altera el comportamiento de envío** (sigue dependiendo de que Resend esté configurado); no es un cambio de experiencia más allá de lo que el paquete exige.
  - El copy del encabezado/cuerpo/CTA de la sección cambia a §14.9 (ver §9.6). **Nota de arquitectura**: §14.9/§14.1 y la referencia v13 describen Contacto como un **CTA "Conversemos ↗"** (enlace a `#contacto`), no un formulario; el repo v12 ya trae el formulario. Se **conserva** el formulario (aporta función real y no está prohibido por el paquete) y se añade el copy §14.9 por encima; se marca como **P11** para Jeimy (ver §8) la elección entre: mantener el formulario con envío real vs. dejar solo el CTA/enlace de la referencia v13. Mientras tanto se aplica el default "conservar lo que ya funciona sin fingir".

---

## 4. Diseño de las 3 secciones nuevas

### 4.1 Ecosistema — `sections/ecosistema.tsx` (Server Component)

Server Component (sin interactividad). Reutiliza `SectionHeader` (`ui/section-header.tsx`) y el patrón visual de tarjetas `info-card`/`card-grid card-grid--three` ya definido en `globals.css`.

**Forma de datos** (`ecosistema` en `src/content/site.ts`, `as const`):

```ts
export const ecosistema = {
  id: "ecosistema",
  eyebrow: "El ecosistema ConcienTIC",      // o usar el título compuesto; ver nota
  title: "Tres territorios. Un propósito compartido.",
  intro:
    "Conectamos diagnóstico, acompañamiento inteligente y soluciones especializadas. Cada componente cumple una función concreta dentro de una visión común.",
  items: [
    {
      number: "01 · Diagnóstico y orientación",
      name: "DQnexus",
      description:
        "Reconoce fortalezas y oportunidades en tus competencias digitales para orientar próximos pasos relevantes.",
      cta: { label: "Explorar el diagnóstico ↗", href: "#experiencia" },
    },
    {
      number: "02 · Aprendizaje inteligente",
      name: "TutorIA",
      description:
        "Un acompañante para comprender conceptos, practicar, resolver dudas y avanzar con mayor autonomía.",
      cta: { label: "Conocer la experiencia ↗", href: "#experiencia" },
    },
    {
      number: "03 · Experimentación y aplicación",
      name: "Laboratorios IA",
      description:
        "Espacios para diseñar, probar y aplicar soluciones con inteligencia artificial a retos concretos. SIGINEX es una de las aplicaciones especializadas del ecosistema.",
      cta: { label: "Conversemos ↗", href: "#contacto", pending: true },   // §14.4 fija SOLO el destino #contacto; la etiqueta NO está en el paquete → provisional (flag pending), ver nota y P10
    },
  ],
  honesty:
    "La disponibilidad y el alcance de cada producto deben describirse según su estado real de desarrollo. Esta propuesta no presupone funcionalidades ya publicadas.",
} as const;
```

**Props del componente**: ninguna; importa `ecosistema` de `@/content/site`.

**Render / estructura**:
- `<section id="ecosistema" className="ct-section">`. **Fondo fijado** (resuelve ambigüedad): Ecosistema usa `.ct-section` (fondo claro/paper) para alternar con sus vecinas (Hero por encima, Guardianes `.ct-section--dark` por debajo), siguiendo la alternancia claro/soft/dark ya presente en `globals.css`. Contraste AA como restricción.
- **Encabezado — regla fijada (resuelve la ambigüedad del hallazgo 5)**: el texto §14.4 es la cadena literal con guion largo `El ecosistema ConcienTIC — Tres territorios. Un propósito compartido.`. Para no alterar ni parafrasear el texto exacto, se renderiza así: `ct-eyebrow` = `El ecosistema ConcienTIC` y `h2` = `Tres territorios. Un propósito compartido.`, **con la condición verificable de que la concatenación `eyebrow + " — " + title` reproduzca exactamente la cadena del spec, guion largo " — " incluido**. Es decir, el troceo es solo presentacional; el guion largo pertenece al texto y no se elimina (si no se quisiera el patrón eyebrow/título, la alternativa válida es un único nodo de texto con la cadena completa). No parafrasear.
- Grilla de 3 tarjetas (`card-grid--three`): cada tarjeta muestra `number`, `name` (como `h3`), `description` y un enlace CTA (patrón `site-nav`/enlace con subrayado animado o botón según diseño; las 3 CTA son anclas internas).
- **Nota de honestidad**: párrafo visible al pie de la sección (p. ej. `<p className="ecosistema-honesty">`), **texto literal, sin resumir ni ocultar tras un toggle** (§14.4, §15).

**Nota (regla "no inventar") — hallazgo 3**: §14.4 fija las etiquetas CTA de las tarjetas 01 ("Explorar el diagnóstico ↗") y 02 ("Conocer la experiencia ↗") y el destino de la 03 (`#contacto`), pero **no** fija la etiqueta literal del CTA de la tarjeta 03. Como el paquete no la define, la etiqueta **"Conversemos ↗" es provisional, NO copy del paquete**: se marca en los datos con `pending: true` (mismo tratamiento que el placeholder de Hotmart) y se anota como **P10** en §8 y en `06_REVISION_VISUAL.md`. Se reutiliza "Conversemos ↗" (etiqueta de §14.9 para el mismo destino `#contacto`) únicamente para **no inventar copy nuevo**; el implementador no debe presentarla como texto definitivo. Si Jeimy no confirma, la alternativa de cero invención es renderizar el enlace a `#contacto` con una etiqueta neutra igualmente marcada como placeholder.

### 4.2 Recursos — `sections/recursos.tsx` (Server Component)

Server Component. Mismo patrón de `SectionHeader` + grilla de 3 tarjetas.

**Forma de datos** (`recursos` en `src/content/site.ts`):

```ts
export const recursos = {
  id: "recursos",
  eyebrow: "Aprender con intención",
  title: "Contenido que orienta. Experiencias que transforman.",
  intro:
    "El contenido gratuito sigue siendo una puerta de entrada. Lo conectamos con rutas, práctica y acompañamiento opcional para que cada persona encuentre el nivel de apoyo que necesita.",
  items: [
    {
      number: "01/EXPLORAR",        // §14.6: separador LITERAL con barra, no " · "
      title: "Referentes y recursos abiertos",
      description:
        "Contenido curado, fuentes confiables y herramientas para comprender un tema sin perderse entre miles de enlaces.",
      cta: {
        label: "Ver contenidos gratuitos ↗",
        href: "https://www.concientic.com/servicios/contenidos",
        external: true,              // target="_blank" rel="noopener noreferrer" (§11/§14.6)
      },
    },
    {
      number: "02/APRENDER Y PROFUNDIZAR",
      title: "Academia ConcienTIC",
      description:
        "Formación y programas de aprendizaje disponibles a través de nuestra academia en Hotmart, conectados con rutas y objetivos prácticos.",
      cta: {
        label: "Explorar Academia ↗",
        href: "https://hotmart.com/",   // PLACEHOLDER — pendiente de URL real de Jeimy (§14.6)
        external: true,
        pending: true,                  // señal explícita de marcador pendiente
      },
    },
    {
      number: "03/PROFUNDIZAR",
      title: "Programas y acompañamiento",
      description:
        "Talleres, cohortes o apoyo experto para desafíos que requieren interacción y retroalimentación.",
      cta: null,                        // §14.6 no define CTA/enlace para la 3ª tarjeta
    },
  ],
} as const;
```

**Render**:
- `<section id="recursos" className="ct-section ct-section--soft">`. **Fondo fijado** (resuelve ambigüedad): Recursos usa `.ct-section--soft` (fondo `--ct-soft #f5f7f9`) para contrastar con sus vecinas (Guardianes `.ct-section--dark` por encima, Metodología por debajo), según la alternancia ya presente en `globals.css`. Contraste AA como restricción.
- Tarjeta 01: enlace **absoluto** a `https://www.concientic.com/servicios/contenidos` con `target="_blank"` y `rel="noopener noreferrer"` (§14.6, §11). El contenido de esa subpágina no se toca.
- Tarjeta 02: enlace externo a `https://hotmart.com/` **marcado visible y claramente como pendiente** (p. ej. un badge "URL pendiente de confirmación" o comentario de código + nota en la revisión visual). **No inventar una URL real de Hotmart** (§14.6).
- Tarjeta 03: sin CTA (el paquete no define enlace). No inventar destino.

**Nota (texto literal — hallazgos 6 y 5)**: §14.6 fija los números **con barra**: `01/EXPLORAR`, `02/APRENDER Y PROFUNDIZAR`, `03/PROFUNDIZAR`. Los datos deben llevar **exactamente esa cadena con "/"** (no " · "); cualquier ajuste estético (p. ej. mostrar el prefijo numérico con otro tratamiento visual) se hace **solo en CSS**, nunca alterando la cadena de datos. Se acepta explícitamente que Recursos use "/" y Ecosistema use " · " porque **así los define cada subsección del paquete** (§14.6 usa "/", §14.4 usa " · "); no se unifican. (Con esto se elimina también la contradicción previa entre §4.2 y §9.4: ambas usan ahora "/".) El encabezado §14.6 `Aprender con intención — Contenido que orienta. Experiencias que transforman.` sigue la **misma regla verificable del hallazgo 5**: `ct-eyebrow` = `Aprender con intención`, `h2` = `Contenido que orienta. Experiencias que transforman.`, con la condición de que `eyebrow + " — " + title` reproduzca la cadena literal del spec, guion largo incluido.

### 4.3 Experiencia — `sections/experiencia.tsx` (Client Component)

**Directiva**: primera línea `"use client"` (requerido por esta versión de Next.js para estado y `onClick` — guía `01-app/01-getting-started/05-server-and-client-components.md` y `03-api-reference/01-directives/use-client.md`). Es el **único** componente de cliente nuevo de la home. Los datos (textos, opciones, recomendaciones) viven en `src/content/site.ts` (`as const`) e se importan; el componente no persiste ni envía nada.

**Forma de datos** (`experiencia` en `src/content/site.ts`):

```ts
export const experiencia = {
  id: "experiencia",
  eyebrow: "Empieza por ti",
  title: "Tu siguiente paso no tiene que ser aprenderlo todo.",
  notice:
    "Este pequeño ejemplo muestra cómo ConcienTIC podría orientar el primer paso. No es el diagnóstico oficial DQnexus ni guarda respuestas. En la experiencia real, las recomendaciones deberán apoyarse en criterios transparentes, recursos confiables y una ruta relacionada con tu objetivo.",
  demoLabel: "Experiencia demostrativa · 1 pregunta",
  question: "¿Qué te gustaría mejorar primero?",
  // Opciones y recomendaciones VERIFICADAS y transcritas LITERALMENTE del
  // objeto `recommendations` embebido en entregables_v7/docs/concientic_v13_reference.html
  // (líneas ~524-528 para las etiquetas; ~575-581 para el objeto JS). Cada recomendación
  // tiene título (resultTitle) y cuerpo (resultText); se guardan como par.
  // Los ids coinciden con los `value` de los radios del HTML de referencia (ia/info/product/balance).
  options: [
    {
      id: "ia",
      label: "Usar la IA con más criterio y seguridad.",
      recommendation: {
        title: "Empieza por una tarea real",
        body: "Elige una tarea que hagas con frecuencia. Practica cómo dar contexto a una IA, contrasta sus respuestas con fuentes fiables y revisa qué información no debes compartir.",
      },
    },
    {
      id: "info",
      label: "Evaluar información y reconocer la desinformación.",
      recommendation: {
        title: "Practica la verificación",
        body: "Toma una afirmación que hayas visto en línea. Busca la fuente original, compara evidencias independientes y distingue hechos, opiniones e incertidumbre.",
      },
    },
    {
      id: "product",
      label: "Ahorrar tiempo con herramientas digitales.",
      recommendation: {
        title: "Detecta una fricción cotidiana",
        body: "Elige una tarea repetitiva. Documenta cómo la haces hoy y explora una herramienta que simplifique un paso sin añadir complejidad innecesaria.",
      },
    },
    {
      id: "balance",
      label: "Recuperar equilibrio y atención en mi vida digital.",
      recommendation: {
        title: "Observa antes de cambiar",
        body: "Identifica un momento del día en que la tecnología interrumpe tu atención. Prueba un límite pequeño y deliberado y evalúa cómo te sientes.",
      },
    },
  ],
  submitLabel: "Ver una orientación inicial ↗",
} as const;
```

> **Contenido VERIFICADO (resuelve el hallazgo 4)**: §14.8 indica que la recomendación por opción es "un `switch`/objeto JS simple embebido en el archivo de referencia" y **no** transcribe los textos en §14. Se abrió `entregables_v7/docs/concientic_v13_reference.html` (ruta real; el documento decía `docs/…`, corregido) y se localizó el objeto `recommendations` (no un `switch`): las **4 cadenas existen y están transcritas literalmente arriba**, con sus títulos. Los `value` de los radios del HTML (`ia`, `info`, `product`, `balance`) se adoptan como `id` para mantener la correspondencia 1:1. Ya **no queda `"…"` placeholder** ni invitación a inventar; P9 deja de ser bloqueante (ver §8). El `resultTitle` genérico "Tu siguiente paso" del HTML es solo el estado inicial vacío del contenedor y no corresponde a ninguna opción.

**Arquitectura del componente** (estado local, sin persistencia):

```tsx
"use client";
import { useState } from "react";
import { experiencia } from "@/content/site";

export function Experiencia() {
  const [selected, setSelected] = useState<string | null>(null);  // id de opción elegida
  const [shown, setShown] = useState(false);                      // si ya se pulsó "Ver orientación"

  // recommendation es un objeto { title, body } (ver forma de datos arriba).
  const result = shown && selected
    ? experiencia.options.find((o) => o.id === selected)?.recommendation ?? null
    : null;
  // Render del resultado: result.title en un <strong> y result.body en un <p>,
  // dentro del contenedor role="status"/aria-live (misma estructura que el HTML de referencia).

  // Sin useEffect, sin fetch, sin localStorage, sin envío. Estado efímero en memoria.
  return (/* ... */);
}
```

- **Entrada (selección de opción)**: grupo de 4 controles. Patrón recomendado: `<fieldset>` + `<legend>` con la pregunta y 4 `radio` (`<input type="radio" name="experiencia">`) etiquetados — da semántica de "elige una", navegación por teclado nativa y foco correcto, mejor que 4 botones sueltos. **Validación**: si se pulsa "Ver una orientación inicial" sin selección, no se muestra recomendación y se comunica "Elige una opción para ver una orientación" en un contenedor con `aria-live="polite"` (no `alert()`). Al cambiar de opción tras ver un resultado, se puede recalcular en vivo o requerir re-pulsar; se elige **recalcular al pulsar** para que la acción sea explícita.
- **Acción**: botón "Ver una orientación inicial ↗" (`type="button"`) → `setShown(true)`. Sin `form`/submit porque no hay envío; evita cualquier petición de red.
- **Salida**: la recomendación se renderiza en un bloque con `role="status"` `aria-live="polite"` para anunciarla a lectores de pantalla (coherente con el patrón de accesibilidad del paquete; §14.11 menciona `aria-live` como parte de lo que v13 conserva).
- **Aviso fijo (obligatorio)**: `experiencia.notice` se renderiza **siempre visible**, por encima o junto a la demo, nunca condicionado al estado ni ocultable (§14.8, §15: "con el aviso de 'no es el diagnóstico oficial' visible siempre"). Texto literal, sin suavizar.
- **Sin dark patterns**: sin temporizadores, sin "te quedan X intentos", sin autoavance. Una pregunta, una orientación, fin (RNF1).

**Testabilidad**: el componente es una función pura de `(selected, shown) → recomendación`; la lógica de mapeo opción→recomendación se puede **unit-testear** (React Testing Library: render, click en una radio, click en el botón, aserción del texto de recomendación y de que el aviso está presente en el DOM en todo momento). La ausencia de red/persistencia hace innecesario el mock de `fetch`/storage.

---

## 5. Edge cases y validación de entradas

- **Imagen de grupo de Guardianes (no existe como asset).** Verificado: `public/guardians/` y `entregables_v7/assets/` solo contienen los **7 PNG individuales** (`emi, locky, lex, byte, detective-dq, nexo, nova`). No hay un archivo de grupo. Regla por defecto de §14.5: "si no existe todavía, puede componerse con los 7 PNG individuales ya entregados… no hace falta pedir un archivo nuevo para avanzar." → La franja compone los 7 PNG en un contenedor (flex/grid) como una sola unidad visual. **Regla de accesibilidad fijada (resuelve el hallazgo 9)**: los 7 `<Image>` individuales van con `alt=""` (decorativos dentro del grupo, para no repetir texto alternativo), pero un `<div>` contenedor **no expone un nombre accesible por sí solo**; por tanto el contenedor de la composición lleva **`role="img"` + `aria-label`** con el alt oficial de §14.5, de modo que los lectores de pantalla anuncien el grupo una sola vez. Alt/`aria-label` oficial del grupo (§14.5, literal): "Los siete Guardianes de ConcienTIC: Emi, Locky, Lex, Byte, Detective DQ, Nexo y Nova, con sus colores y capacidades digitales." Si Jeimy entrega una imagen de grupo real, se sustituye la composición por un solo `<Image alt="…">` con ese mismo texto (y entonces no hace falta `role="img"`). Señalar la sustitución en la revisión visual.
- **Lista de 7 competencias sin nombre de Guardián.** §14.5 fija el texto: Empatía Digital, Seguridad Digital, Derechos Digitales, Alfabetización Digital, Pensamiento Crítico, Colaboración Digital, Innovación Digital. **Ojo**: esta lista NO coincide 1:1 con las `competence` de `src/content/guardians.ts` (que dice "Pensamiento crítico y ética", "Investigación y verificación", etc.). Se usa **la lista literal de §14.5** para la franja (es la fuente vigente de la home v13), guardada como `competencias` nuevo en `site.ts`; no se reescribe `guardians.ts` (sigue sirviendo a la biblioteca/futura `/equipo`). Las competencias de la franja NO muestran glifo de un Guardián concreto por nombre; si se usa un glifo/ícono, es genérico (§14.5 "sin nombre de Guardián visible en esta franja").
- **Enlace Academia Hotmart.** Entrada externa sin URL real. Validación: dejar `https://hotmart.com/` como placeholder **visiblemente marcado** como pendiente; no publicar sin la URL confirmada (§14.6, §15). El enlace abre en pestaña nueva con `rel="noopener noreferrer"`.
- **Anclas internas.** Todos los CTA de Hero/Ecosistema (`#experiencia`, `#metodologia`, `#contacto`) deben corresponder a ids existentes tras el reordenamiento (RF1). `:target { scroll-margin-top: 5.5rem }` ya está en `globals.css` para compensar el header sticky.
- **DQUILIBRIO con dominio absoluto.** Al pasar de ruta relativa (`/dquilibrio/*.html`) a absoluta (`https://www.concientic.com/dquilibrio/*.html`), en **preview/local** esos enlaces apuntarán a producción, no al `public/` local. Es lo que exige §15 ("abren `https://www.concientic.com/...` en pestaña nueva"); documentarlo para que no se confunda con un bug en entornos de preview.
- **Experiencia sin selección.** Cubierto en §4.3 (mensaje `aria-live`, no recomendación vacía, no `alert()`).
- **`prefers-reduced-motion`.** Las nuevas secciones no introducen animación propia más allá de los patrones hover/focus ya tokenizados; todos quedan anulados por la media query global ya presente en `globals.css`.

---

## 6. Secciones v12 a desmontar y componentes huérfanos

Se **quitan de `page.tsx`** (RF1/RF13) pero **sus archivos permanecen** en el repo (§14.1: "confirmar con Jeimy… antes de borrar código"; `structure.md`: "no borrar sus archivos todavía"):

| Sección v12 | Archivo (se conserva) | id v12 | Estado tras v13 |
|-------------|------------------------|--------|-----------------|
| PrincipleBand | `sections/principle-band.tsx` | — | Huérfano (no montado) |
| Services (5 tarjetas) | `sections/services.tsx` | `#servicios` | Huérfano |
| Challenge / El desafío | `sections/challenge.tsx` | `#desafio` | Huérfano |
| Desinfoxicación | `sections/desinfoxication.tsx` | `#desinfoxicacion` | Huérfano |
| Centro de Evidencia | `sections/evidence.tsx` | `#evidencia` | Huérfano |
| Equipo (grilla 7 bios) | `sections/team.tsx` | `#equipo` | Huérfano (reemplazado por `guardianes.tsx`) |
| Casos | `sections/cases.tsx` | `#casos` | Huérfano |
| Referentes | `sections/references.tsx` | `#referentes` | Huérfano |
| Manifiesto (cierre) | `sections/manifesto.tsx` | (sin ancla) | Huérfano |
| value-props | `sections/value-props.tsx` | — | Ya huérfano en v12 (no montado en `page.tsx` actual) |

Componentes UI que quedan **sin uso en la home** pero se conservan (pueden servir a futuras páginas; `structure.md` pide conservar `GuardianCard`):
- `ui/guardian-card.tsx` (lo usaba `team.tsx`).
- `ui/evidence-card.tsx` (lo usaba `evidence.tsx`/`references.tsx`).
- Datos tipados en `site.ts` de secciones desmontadas (`principleBand`, `services`, `challenge`, `desinfoxication`, `evidence`, `team`, `cases`, `references`, `manifesto`): **no se borran** (los consumen los componentes huérfanos; borrarlos rompería TypeScript). Quedan como código muerto controlado hasta decisión de Jeimy.

Importante para RNF7 (build sin errores): al desmontar, **solo se edita `page.tsx`** (quitar imports y JSX). No se tocan los archivos huérfanos, así que el build sigue compilando. Si lint marca "import no usado", el fix es quitar el import en `page.tsx`, no el archivo fuente.

---

## 7. Orden final de `page.tsx` y del menú

### 7.1 `page.tsx` (orden exacto, RF1)

```tsx
import { Hero } from "@/components/sections/hero";
import { Ecosistema } from "@/components/sections/ecosistema";
import { Guardianes } from "@/components/sections/guardianes";
import { Recursos } from "@/components/sections/recursos";
import { Methodology } from "@/components/sections/methodology";
import { Experiencia } from "@/components/sections/experiencia";
import { Dquilibrio } from "@/components/sections/dquilibrio";
import { Contact } from "@/components/sections/contact";

export default function Home() {
  return (
    <>
      <Hero />         {/* #inicio */}
      <Ecosistema />   {/* #ecosistema */}
      <Guardianes />   {/* #guardianes */}
      <Recursos />     {/* #recursos */}
      <Methodology />  {/* #metodologia */}
      <Experiencia />  {/* #experiencia */}
      <Dquilibrio />   {/* #dquilibrio */}
      <Contact />      {/* #contacto */}
    </>
  );
}
```

### 7.2 Menú (RF2, §14.2) — `navLinks` en `src/content/site.ts`

```ts
export const navLinks = [
  { label: "Ecosistema",  href: "#ecosistema" },
  { label: "Guardianes",  href: "#guardianes" },
  { label: "Aprende",     href: "#recursos" },     // "Aprende" apunta a la sección Recursos
  { label: "Metodología", href: "#metodologia" },
  { label: "DQUILIBRIO",  href: "#dquilibrio" },
] as const;
```

- Header (`layout/header.tsx`): renderiza `navLinks` + botón CTA **`Encuentra tu ruta ↗`** que ancla a `#experiencia` (hoy el header tiene un botón "Hablemos" → `#contacto`; se cambia la etiqueta, el destino y se usa variante de botón existente `ct-button ct-button--dark`). El menú móvil replica los 5 enlaces + el CTA.
- Footer (`layout/footer.tsx`): los **mismos 5 enlaces (sin CTA)** + wordmark + `© ConcienTIC`. Se reemplaza la lista actual de 10 enlaces de `footer.links` por los 5 de `navLinks`. **El footer muestra las mismas 5 etiquetas literales del menú, incluida `Aprende` (no `Recursos`)** — el tercer enlace conserva la etiqueta `Aprende` apuntando a `#recursos`; no "corregir" la etiqueta al nombre de la sección (§14.2, coherente con la referencia v13 cuyo footer también dice "Aprende"). El `footer.tsx` actual ya no muestra tagline textual; añadir la línea `© ConcienTIC` (§14.2).

---

## 8. Pendientes de confirmación de Jeimy (reglas por defecto aplicadas mientras tanto)

Cada punto aplica ya la regla por defecto del paquete; la columna "Default aplicado" es lo que se implementa hasta que Jeimy responda.

| # | Tema | Default aplicado (cita) | Qué confirmar |
|---|------|--------------------------|----------------|
| P1 | Destino de las 8 secciones v12 eliminadas (Evidencia, Casos, Referentes, etc.) | Desmontar de la home, **conservar archivos** (§14.1; `structure.md`) | ¿Se descartan definitivamente o se reubican en otra página? |
| P2 | Bios individuales de los 7 Guardianes | La home v13 usa **franja única** sin bios; `team.tsx`/`GuardianCard` se conservan para una posible `/equipo` (§14.5) | ¿Se mantiene también la versión de 7 tarjetas en otra página? |
| P3 | URL real de la Academia en Hotmart | Placeholder `https://hotmart.com/` **marcado como pendiente**, no publicar así (§14.6, §15) | URL definitiva del curso/academia |
| P4 | Logo del header: imagen vs. lockup de texto CSS | Mantener **wordmark-imagen** `ConcienTIC Nexus` (§14.7; §15) | ¿Cambiar a logo de texto CSS? |
| P5 | Desviación `--navy` `#0D182A` vs `#0D1B2A` oficial | Usar **`#0D1B2A` oficial** (§14.10; §15) | ¿El hex de v13 es intencional? |
| P6 | Desviación `--pink` `#FF68B4` vs `#FF6EB6` oficial | Usar **`#FF6EB6` oficial** (§14.10; §15) | ¿El hex de v13 es intencional? |
| P7 | Desviación `--paper` `#f5f7f3` vs `#F5F7F9` oficial | Usar **`#F5F7F9` oficial** (§14.10; §15) | ¿El tono de fondo de v13 es intencional? |
| P8 | Ausencia de Poppins en v13 (todo Inter) | **Mantener Poppins** para títulos + Inter para UI (§14.10; §15) | ¿Se quita Poppins a propósito? |
| P9 | Texto de las 4 recomendaciones de la demo Experiencia | **RESUELTO**: extraídas y transcritas literalmente del objeto `recommendations` de `entregables_v7/docs/concientic_v13_reference.html` (ver §4.3 y §9.5). Ya no es bloqueante | (cerrado — solo verificar que no cambien en una futura versión del HTML) |
| P10 | Etiqueta del CTA de la 3ª tarjeta de Ecosistema | Etiqueta **provisional** "Conversemos ↗" (de §14.9, mismo destino `#contacto`), marcada `pending: true`; **no es copy del paquete** y no se presenta como definitiva | ¿Prefiere otra etiqueta para la tarjeta 03 de Ecosistema? |
| P11 | Formulario de Contacto: ¿envío real o maqueta? | **Default aplicado**: conservar el pipeline real (`/api/contact` + Resend) y **corregir el mensaje de éxito** para que sea honesto ("Mensaje enviado / Gracias, hemos recibido tu mensaje…"), ya que el actual ("Maqueta de demostración") es falso frente al envío real. §14.9 describe solo un CTA "Conversemos ↗", no un formulario | ¿Mantener el formulario con envío real vía Resend, o dejar únicamente el CTA/enlace "Conversemos ↗" de la referencia v13 sin formulario? |

Estas preguntas deben anotarse en `06_REVISION_VISUAL.md` (§14.10 lo pide explícitamente para las desviaciones de marca).

---

## 9. Textos exactos (verbatim del spec — no parafrasear)

> El implementador debe copiar estos textos **literalmente** en `src/content/site.ts`. La cita entre paréntesis indica la subsección del spec de origen.

### 9.1 Hero (§14.3)
- Etiqueta superior: `Aprende con intención. Aplica con criterio.`
- H1: `La inteligencia digital empieza contigo.`
- Cuerpo: `No necesitas consumir más contenido. Necesitas reconocer lo que importa, desarrollar las competencias adecuadas y llevarlas a tu vida con propósito y equilibrio.`
- CTA 1: `Descubre tu siguiente paso ↗` → `#experiencia`
- CTA 2: `Conoce nuestra metodología` → `#metodologia`
- Cierre visual: `Menos ruido. Más criterio. Aprendizaje que se convierte en acción.`

### 9.2 Ecosistema (§14.4)
- Encabezado: `El ecosistema ConcienTIC — Tres territorios. Un propósito compartido.`
- Intro: `Conectamos diagnóstico, acompañamiento inteligente y soluciones especializadas. Cada componente cumple una función concreta dentro de una visión común.`
- Tarjeta 01: `01 · Diagnóstico y orientación — DQnexus.` / `Reconoce fortalezas y oportunidades en tus competencias digitales para orientar próximos pasos relevantes.` / CTA `Explorar el diagnóstico ↗` → `#experiencia`
- Tarjeta 02: `02 · Aprendizaje inteligente — TutorIA.` / `Un acompañante para comprender conceptos, practicar, resolver dudas y avanzar con mayor autonomía.` / CTA `Conocer la experiencia ↗` → `#experiencia`
- Tarjeta 03: `03 · Experimentación y aplicación — Laboratorios IA.` / `Espacios para diseñar, probar y aplicar soluciones con inteligencia artificial a retos concretos. SIGINEX es una de las aplicaciones especializadas del ecosistema.` / CTA → `#contacto`
- Nota de honestidad (literal, visible, sin resumir): `La disponibilidad y el alcance de cada producto deben describirse según su estado real de desarrollo. Esta propuesta no presupone funcionalidades ya publicadas.`

### 9.3 Guardianes (§14.5)
- Encabezado: `Los Guardianes — Inteligencia con propósito.` / `La IA tiene un papel. La persona conserva el protagonismo.`
- Cuerpo: `Los Guardianes se integran donde pueden aportar orientación, protección, comprensión o creatividad. Cada intervención debe tener una razón clara y un límite definido.`
- Alt de la imagen de grupo: `Los siete Guardianes de ConcienTIC: Emi, Locky, Lex, Byte, Detective DQ, Nexo y Nova, con sus colores y capacidades digitales.`
- Lista de 7 competencias (en este orden): `Empatía Digital`, `Seguridad Digital`, `Derechos Digitales`, `Alfabetización Digital`, `Pensamiento Crítico`, `Colaboración Digital`, `Innovación Digital`.
- Cierre: `La mejor tecnología no decide por ti: te ayuda a decidir mejor.`

### 9.4 Recursos (§14.6)
- Encabezado: `Aprender con intención — Contenido que orienta. Experiencias que transforman.`
- Intro: `El contenido gratuito sigue siendo una puerta de entrada. Lo conectamos con rutas, práctica y acompañamiento opcional para que cada persona encuentre el nivel de apoyo que necesita.`
- Tarjeta 01: `01/EXPLORAR — Referentes y recursos abiertos.` / `Contenido curado, fuentes confiables y herramientas para comprender un tema sin perderse entre miles de enlaces.` / CTA `Ver contenidos gratuitos ↗` → `https://www.concientic.com/servicios/contenidos` (`target="_blank"`, `rel="noopener noreferrer"`)
- Tarjeta 02: `02/APRENDER Y PROFUNDIZAR — Academia ConcienTIC.` / `Formación y programas de aprendizaje disponibles a través de nuestra academia en Hotmart, conectados con rutas y objetivos prácticos.` / CTA `Explorar Academia ↗` → `https://hotmart.com/` (PLACEHOLDER pendiente de Jeimy)
- Tarjeta 03: `03/PROFUNDIZAR — Programas y acompañamiento.` / `Talleres, cohortes o apoyo experto para desafíos que requieren interacción y retroalimentación.`

### 9.5 Experiencia (§14.8)
- Encabezado: `Empieza por ti — Tu siguiente paso no tiene que ser aprenderlo todo.`
- Aviso (literal, siempre visible, no suavizar): `Este pequeño ejemplo muestra cómo ConcienTIC podría orientar el primer paso. No es el diagnóstico oficial DQnexus ni guarda respuestas. En la experiencia real, las recomendaciones deberán apoyarse en criterios transparentes, recursos confiables y una ruta relacionada con tu objetivo.`
- Etiqueta: `Experiencia demostrativa · 1 pregunta — ¿Qué te gustaría mejorar primero?`
- 4 opciones (literal del HTML de referencia, con punto final): `Usar la IA con más criterio y seguridad.` / `Evaluar información y reconocer la desinformación.` / `Ahorrar tiempo con herramientas digitales.` / `Recuperar equilibrio y atención en mi vida digital.`
- Botón: `Ver una orientación inicial ↗`
- Recomendaciones por opción — **transcritas literalmente del objeto `recommendations` en `entregables_v7/docs/concientic_v13_reference.html`** (verificado; ya no es pendiente). Cada una es título + cuerpo:
  - `Usar la IA con más criterio y seguridad.` → **Empieza por una tarea real** · `Elige una tarea que hagas con frecuencia. Practica cómo dar contexto a una IA, contrasta sus respuestas con fuentes fiables y revisa qué información no debes compartir.`
  - `Evaluar información y reconocer la desinformación.` → **Practica la verificación** · `Toma una afirmación que hayas visto en línea. Busca la fuente original, compara evidencias independientes y distingue hechos, opiniones e incertidumbre.`
  - `Ahorrar tiempo con herramientas digitales.` → **Detecta una fricción cotidiana** · `Elige una tarea repetitiva. Documenta cómo la haces hoy y explora una herramienta que simplifique un paso sin añadir complejidad innecesaria.`
  - `Recuperar equilibrio y atención en mi vida digital.` → **Observa antes de cambiar** · `Identifica un momento del día en que la tecnología interrumpe tu atención. Prueba un límite pequeño y deliberado y evalúa cómo te sientes.`

### 9.6 Contacto (§14.9)
- Encabezado: `El siguiente paso empieza aquí — Haz que la tecnología tenga sentido para ti.`
- Cuerpo: `Cuéntanos qué quieres comprender, mejorar o transformar. Podemos ayudarte a identificar un punto de partida.`
- CTA: `Conversemos ↗`

---

## 10. Tokens exactos y dónde viven

Todos ya presentes en `concientic/src/app/globals.css` (`:root`) y coinciden con `entregables_v7/reference-code/styles/design-tokens.css`. **No hay que crear tokens nuevos**; las secciones nuevas consumen estos.

> **Cotejo (hallazgo 11)**: se verificó que los hex de color de `globals.css` coinciden con los valores oficiales del Manual de Marca listados en `AGENTS.md` (§Identidad) y en `design-system.md`. El documento afirma la coincidencia con `design-tokens.css`; si en implementación apareciera cualquier diferencia campo a campo frente a ese archivo espejo, **manda el valor oficial del Manual de Marca** (§15 default), no el de `design-tokens.css`. Este punto es NIT y no bloquea.

| Token | Valor | En `globals.css` |
|-------|-------|------------------|
| Contenedor | `min(1240px, calc(100% - 48px))` | `.ct-container` |
| Radio sm/md/lg/xl | 12 / 18 / 24 / 34 px | `--ct-radius-sm/md/lg/xl` |
| Espaciado | escala 4→96 px | (escala del design-system; en `globals.css` se usan valores directos) |
| Sombra sm | `0 8px 24px rgba(13,27,42,.07)` | `--ct-shadow-sm` |
| Sombra md | `0 16px 50px rgba(13,27,42,.08)` | `--ct-shadow-md` |
| Movimiento rápido / base | 160 / 240 ms | usados en transiciones (`.ct-button`, cards, nav) |
| Logo header alto | 52–58 px desktop | `.brand-mark { height: 58px }` |

Colores oficiales (Manual de Marca), ya en `:root`: `--ct-primary #00c49a`, `--ct-secondary #6c63ff`, `--ct-blue #0077ff`, `--ct-cyan #00b4d8`, `--ct-dark #0d1b2a`, `--ct-orange #ff7a00`, `--ct-yellow #ffc857`, `--ct-pink #ff6eb6`, `--ct-soft #f5f7f9`, `--ct-white #ffffff` (usado por `ct-button--secondary`, `info-card`, `ct-section--dark h2`), `--ct-muted #64748b`, `--ct-text #1e293b`. Tipografía: `--font-inter` (Inter, UI) y `--font-poppins` (Poppins, títulos) cargadas en `layout.tsx` con `next/font/google`.

Clases utilitarias reutilizables ya existentes para las secciones nuevas: `.ct-section`, `.ct-section--soft`, `.ct-section--dark`, `.ct-section-header`, `.ct-eyebrow`, `.ct-button(--primary/--secondary/--dark)`, `.card-grid--three`, `.info-card`. El CSS nuevo que haga falta para la franja de Guardianes, la grilla de competencias y la demo de Experiencia se añade en `globals.css` siguiendo las mismas convenciones (clases `ct-`/BEM ligero) y usando los tokens anteriores.

---

## 11. Tabla de desviaciones de la referencia v13 que NO se aplican

Mandato del criterio §15: *"Las cuatro desviaciones de marca de §14.10 … y el cambio de logo-imagen a logo-texto de §14.7 están resueltas con una respuesta explícita de Jeimy antes de publicar en producción; hasta entonces, la implementación usa los valores oficiales del Manual de Marca."*

| Desviación en `concientic_v13_reference.html` | Valor v13 (ref) | Valor oficial que SÍ se usa | Cita |
|-----------------------------------------------|-----------------|------------------------------|------|
| `--navy` (oscuro) | `#0D182A` | `#0D1B2A` | §14.10, §15 |
| `--pink` (Rosa Humano) | `#FF68B4` | `#FF6EB6` | §14.10, §15 |
| `--paper` (fondo `<body>`) | `#f5f7f3` | `#F5F7F9` (Superficie) | §14.10, §15 |
| Tipografía titulares | solo Inter (sin Poppins) | Poppins títulos + Inter UI | §14.10, §15 |
| Logo header | lockup de texto CSS (sin `<img>`) | wordmark-imagen `ConcienTIC Nexus` (variantes claro/oscuro) | §14.7, §15 |

Además, dos cosas de la referencia v13 que **sí hay que conservar aunque v13 las omita o cambie**:
- **Skip link** "Saltar al contenido" → `#main`: v13 no lo trae, pero §14.11 obliga a mantenerlo (requisito del Design System). Ya existe en `layout.tsx`.
- **Dominio con `www`**: el `site.ts` actual define `domain: "https://concientic.com"` (sin `www`); §14.6/§15 fijan `https://www.concientic.com`. Ajustar `site.domain` a `https://www.concientic.com` para que metadata (`metadataBase`, canonical, Open Graph en `layout.tsx`) y los enlaces absolutos (Recursos, DQUILIBRIO) usen el dominio correcto.

---

## 12. Notas de stack / Next.js (versión del repo)

Por `concientic/AGENTS.md`, esta versión de Next.js puede traer breaking changes; verificado contra `node_modules/next/dist/docs/`:
- **Server/Client Components**: layouts y páginas son Server Components por defecto; `Experiencia` necesita `"use client"` por usar `useState` y `onClick` (guía `05-server-and-client-components.md`, `use-client.md`). Las demás secciones nuevas (Ecosistema, Recursos) son Server Components.
- **`next/image`**: `src` local desde `/public` (p. ej. `/guardians/emi.png`), con `width`/`height` o `fill` + `sizes`, como ya hacen `hero.tsx`, `methodology.tsx`, `guardian-card.tsx`. Optimización a WebP la gestiona el componente; sin base64 (RNF6).
- **`next/font/google`**: Inter y Poppins ya instanciadas en `layout.tsx` con `variable`; no cambiar.
- **Tipos generados**: el repo usa helpers de tipos de esta versión (p. ej. `LayoutProps<"/">` en `layout.tsx`). Las secciones nuevas no reciben props de ruta, así que no los necesitan; mantener TypeScript estricto.
- **No** quitar el bloque `BEGIN:nextjs-agent-rules` de `concientic/AGENTS.md`.

---

## 13. Verificación esperada (para el paso de implementación)

- `npm run build` sin errores de TypeScript ni lint (RNF7, §15). Revisar imports no usados en `page.tsx` tras el desmontaje.
- Tests unitarios recomendados para `Experiencia` (RTL): selección de opción → orientación correcta por opción; aviso `notice` presente en el DOM en todos los estados; sin llamadas de red. Si no hay framework de test configurado, instalarlo según el estándar del ecosistema (Vitest/Jest + RTL) como tarea aparte.
- Responsive 360/768/1024/1440 (RNF7). Verificar que el menú de 5 enlaces + CTA colapsa correctamente (el header ya tiene menú móvil).
- Accesibilidad: foco visible, navegación por teclado en la demo (radios), skip link operativo, contraste AA (RNF3).

---

## 14. Fuentes

- `entregables_v7/kiro/specs/concientic-web-redesign.md` §14 (§14.1–§14.11) y §15 — organización, textos, criterios.
- `entregables_v7/AGENTS.md` — prioridad, identidad, logo, Guardianes, UX consciente, implementación.
- `entregables_v7/kiro/steering/`: `structure.md` (conservación de archivos), `design-system.md` (tokens, logo, movimiento, accesibilidad), `tech.md` (stack), `product.md` (filosofía), `content.md` (voz).
- `entregables_v7/reference-code/styles/design-tokens.css` — tokens espejo del Manual/Design System.
- `concientic/` verificado: `src/app/page.tsx`, `layout.tsx`, `globals.css`, `src/components/sections/*`, `src/components/layout/{header,footer}.tsx`, `src/components/ui/*`, `src/content/{site,guardians,static-courses}.ts`, `public/` (assets confirmados).
- `concientic/node_modules/next/dist/docs/` — comportamiento de esta versión de Next.js (imágenes, componentes de cliente, fuentes, metadata).
- **Archivos verificados en esta revisión**: `concientic/src/components/sections/hero.tsx`, `contact-form.tsx`, `concientic/src/app/api/contact/route.ts`, `concientic/src/lib/email.ts`, `entregables_v7/docs/concientic_v13_reference.html` (objeto `recommendations`), `entregables_v7/kiro/specs/concientic-web-redesign.md` §14.4/§14.8/§14.9/§14.10.

---

## 15. Respuestas a la revisión de diseño (`design-review.json`, veredicto CHANGES_REQUESTED)

Se abordó cada hallazgo contra la lectura real del código y del paquete v7. Resumen por hallazgo:

| # | Sev. | Resolución |
|---|------|-----------|
| 1 | HIGH | **Corregido.** Verificado leyendo `contact-form.tsx` + `api/contact/route.ts` + `lib/email.ts`: el `POST` es real (Resend); el estado de éxito solo se alcanza tras un envío real, por lo que el mensaje "Maqueta de demostración" es falso. Decisión fijada (opción (a) del hallazgo): conservar el pipeline real y reemplazar el texto de éxito por una confirmación honesta ("Mensaje enviado / Gracias, hemos recibido tu mensaje…"), manteniendo `role="status"`/`aria-live`. Esto cumple `AGENTS.md` ("nunca simular un envío exitoso") sin alterar el comportamiento de envío. Documentado en §3 (fila/nota Contacto), RF10 y P11. Se marca P11 para que Jeimy elija formulario-real vs. solo-CTA de la referencia v13. |
| 2 | HIGH | **Corregido.** Verificado: el h1 de `hero.tsx` está hardcodeado y no lee `hero.title`. §3 (fila 1 + nota) y RF3 ahora exigen **editar el JSX**: renderizar `{hero.title}` eliminando el literal v12 y el `<span>`, sin inventar resalte. Se añade `hero.closing` para la frase de cierre §14.3. |
| 3 | MEDIUM | **Corregido.** La etiqueta "Conversemos ↗" de la tarjeta 03 de Ecosistema se marca `pending: true` en datos y como **P10** (provisional, no copy del paquete) en §4.1 y §8; no se presenta como definitiva. |
| 4 | MEDIUM | **Corregido.** Se abrió `entregables_v7/docs/concientic_v13_reference.html` y se transcribieron **literalmente** las 4 recomendaciones (título + cuerpo) del objeto `recommendations` a §4.3 y §9.5. Se eliminó el placeholder `"…"`. P9 pasa a RESUELTO. |
| 5 | MEDIUM | **Corregido.** Regla verificable fijada para los encabezados con guion largo de Ecosistema (§4.1) y Recursos (§4.2): troceo eyebrow/título solo presentacional, con la condición de que `eyebrow + " — " + title` reproduzca exactamente la cadena del spec, guion largo incluido. |
| 6 | MEDIUM | **Corregido.** Los números de Recursos usan la forma literal con barra `01/EXPLORAR`, `02/APRENDER Y PROFUNDIZAR`, `03/PROFUNDIZAR` en datos; cualquier ajuste estético, solo en CSS. Resuelta la contradicción §4.2↔§9.4 (ambas usan "/"). Se documenta que Ecosistema usa " · " y Recursos "/" porque así los define cada subsección. |
| 7 | NIT | **Corregido.** Añadido `--ct-white #ffffff` al inventario de §10. |
| 8 | NIT | **Corregido.** §7.2 ahora explicita que el footer repite las 5 etiquetas del menú, incluida `Aprende` (no `Recursos`). |
| 9 | NIT | **Corregido.** §5 fija `role="img"` + `aria-label` (alt oficial §14.5) en el contenedor de la composición de 7 PNG; sustitución por un solo `<Image alt>` si Jeimy entrega la imagen de grupo. |
| 10 | NIT | **Corregido.** Fondos fijados: Ecosistema `.ct-section`, Recursos `.ct-section--soft`, siguiendo la alternancia de `globals.css` y manteniendo contraste AA (§4.1, §4.2). |
| 11 | NIT | **Atendido.** Nota de cotejo añadida en §10: se verificó la coincidencia de hex con `AGENTS.md`/`design-system.md`; ante cualquier diferencia frente a `design-tokens.css`, manda el valor oficial del Manual de Marca (§15). |

Ningún hallazgo requirió cambiar lo que el usuario ve o cómo se comporta el sistema más allá de lo que el paquete v7 define. El único cambio con efecto visible es el mensaje de éxito del formulario de contacto (hallazgo 1), que **corrige** una afirmación falsa para cumplir la regla explícita de `AGENTS.md` de no fingir envíos; la elección de fondo de maqueta vs. envío real se deja como pendiente P11 para Jeimy sin alterar el pipeline existente mientras tanto.
