# Plan de implementación — Sección GUARDIANES (home ConcienTIC)

Objetivo: rehacer la sección Guardianes del front Next.js para que su **markup y estilo**
coincidan EXACTAMENTE con `entregables_v7/docs/concientic_v13_reference.html` (sección
`#guardianes`). El problema no es solo el color: la estructura del componente actual
(`SectionHeader` + `.guardianes-band` + `.guardianes-group` + `<ul>.guardianes-competencias`)
es distinta a la del HTML de referencia (franja de UNA columna centrada con 2 eyebrows,
título, intro, imagen compuesta y fila de 7 "capabilities" con color/ícono/nombre).

## Hallazgos de exploración (base del plan)

- **Front**: Next.js 16.3.4 App Router, TS estricto, Tailwind v4 vía `@import` + CSS global
  en `concientic/src/app/globals.css`. Estilos de secciones se escriben a mano en globals.css
  con tokens `--ct-*`; NO se usan CSS Modules para estas secciones.
- **Regla Next del repo** (`concientic/AGENTS.md`): revisada la guía
  `concientic/node_modules/next/dist/docs/01-app/01-getting-started/11-css.md`. Confirma que
  el patrón vigente (CSS global importado en el root + utilidades Tailwind) es correcto; no hay
  breaking change que afecte a esta tarea (no se tocan APIs de Next, solo markup JSX + CSS).
  NO quitar el bloque `BEGIN:nextjs-agent-rules` de `AGENTS.md`.
- **Comandos reales de verificación** (de `concientic/package.json`; NO hay framework de tests):
  - `npm run typecheck` (`tsc --noEmit`)
  - `npm run lint` (`eslint`)
  - `npm run build` (`next build`)
  Todos se ejecutan con `cwd = c:\Jose\RepoSiginex\WebConcienTIC\concientic`.
- **COLISIÓN DE CLASE CRÍTICA** (difiere del brief y condiciona el plan): la clase
  `.guardians-section` NO es CSS muerto de "Equipo v12". Está **viva**: la usa
  `concientic/src/components/sections/team.tsx`
  (`<section id={team.id} className="ct-section ct-section--dark guardians-section">`),
  junto con `.guardian-grid`, `.guardian-quote`, `.guardian-card*` (componente
  `concientic/src/components/ui/guardian-card.tsx`) y `.team-principle*`. El bloque navy de
  globals.css (líneas ~847–~975: `.guardians-section`, `.guardians-section .ct-section-header *`,
  `.guardian-grid`, `.guardian-quote`, `.guardian-card*`) pertenece a ese componente Team.
  **`Team` NO está montado en `page.tsx`** (la home renderiza Hero, Ecosistema, Guardianes,
  Recursos, Methodology, Experiencia, Dquilibrio, Contact — sin `<Team/>`), pero el componente y
  su CSS existen y deben seguir compilando. Por eso **el nuevo componente Guardianes NO debe
  emitir la clase desnuda `.guardians-section`** (chocaría con el navy del Team): usaremos las
  clases del HTML tal cual EXCEPTO que la raíz de la sección se marca con una clase propia que no
  colisione (ver item 3). No se elimina el bloque navy: no es dead code.
- **Tokens del front** (globals.css `:root`): `--ct-primary:#00c49a`, `--ct-dark:#0d182a`,
  `--ct-text:#17231e`, `--ct-soft:#f5f7f3`, `--ct-muted:#75847a`, `--ct-line:#dce3dd`,
  `--ct-pink:#ff68b4`, `--ct-white:#ffffff`, `--ct-blue:#0077ff`, `--ct-cyan:#00b4d8`.
  No existe `--green-dark` (usar literal `#008f70` por indicación del brief) ni `--ease-out`
  (ver item 5). `.ct-container` = `width:min(1240px,calc(100% - 48px));margin-inline:auto`, idéntico
  al `.guardians-layout` del HTML. El eyebrow del front es `.ct-eyebrow` (no `.eyebrow`).
- **Assets**: `concientic/public/guardians/` SOLO tiene los 7 PNG individuales
  (`emi.png, locky.png, lex.png, byte.png, detective-dq.png, nexo.png, nova.png`). NO existe
  imagen de grupo. El HTML trae la imagen de grupo embebida en base64, PROHIBIDO en producción
  (`concientic/AGENTS.md` / `entregables_v7/AGENTS.md`) e igualmente prohibido sustituir assets de
  marca por aproximaciones. Se MANTIENE la solución actual: componer `guardian-image-wrap` con los
  7 PNG individuales (contenedor `role="img"` + alt oficial; cada PNG decorativo `alt=""`).

## Contenido VERBATIM del HTML (verificado leyendo el archivo como UTF-8)

Eyebrows (2, ambos color `--green-dark` → `#008f70`):
1. `Los Guardianes`
2. `Inteligencia con propósito`

Título (`h2.guardians-title`):
`La IA tiene un papel. La persona conserva el protagonismo.`

Intro (`p.guardians-intro`):
`Los Guardianes se integran donde pueden aportar orientación, protección, comprensión o creatividad. Cada intervención debe tener una razón clara y un límite definido.`

Alt oficial de la imagen de grupo:
`Los siete Guardianes de ConcienTIC: Emi, Locky, Lex, Byte, Detective DQ, Nexo y Nova, con sus colores y capacidades digitales.`

7 capacidades (`guardian-capability`), EN ESTE ORDEN, cada una `--cap-color` + ícono (span
`aria-hidden="true"`) + nombre (`strong`):

| # | --cap-color | ícono | nombre |
|---|-------------|-------|--------|
| 1 | `#FF2F7D`   | `♥`   | `Empatía Digital` |
| 2 | `#0077FF`   | `▣`   | `Seguridad Digital` |
| 3 | `#FFB000`   | `⚖`   | `Derechos Digitales` |
| 4 | `#22A600`   | `▤`   | `Alfabetización Digital` |
| 5 | `#6C2BFF`   | `✥`   | `Pensamiento Crítico` |
| 6 | `#00A9B7`   | `♧`   | `Colaboración Digital` |
| 7 | `#FF5B00`   | `➤`   | `Innovación Digital` |

Los `--cap-color` van **literales** (NO mapear a tokens). Los íconos son los glifos Unicode
exactos de arriba (confirmados en UTF-8). Nota: estos nombres difieren a propósito de los "roles"
de los guardianes del resto del sitio; se usan tal cual del HTML.

Cierre (`div.guardians-closing`): un `<span aria-hidden="true">✦</span>` seguido de un espacio y
el texto:
`La mejor tecnología no decide por ti: te ayuda a decidir mejor.`

Contenedor de capacidades: `aria-label="Capacidades representadas por los Guardianes"`.

---

# Implementation Plan

- [ ] 1. Reemplazar el objeto `guardianes` en `src/content/site.ts` por la forma que exige el
      markup del HTML, sin romper otros consumidores. Hoy el objeto tiene
      `eyebrow`/`title`/`lead`/`body`/`groupAlt`/`images`/`competencias`/`closing` y SU ÚNICO
      consumidor es `guardianes.tsx` (verificado: no se importa `guardianes` en otros archivos).
      Nueva forma tipada (`as const`):
      - `id: "guardianes"` (SIN CAMBIOS — no tocar id ni posición en page.tsx)
      - `eyebrows: ["Los Guardianes", "Inteligencia con propósito"]`
      - `title: "La IA tiene un papel. La persona conserva el protagonismo."`
      - `intro: "Los Guardianes se integran donde pueden aportar orientación, protección, comprensión o creatividad. Cada intervención debe tener una razón clara y un límite definido."`
      - `groupAlt: "Los siete Guardianes de ConcienTIC: Emi, Locky, Lex, Byte, Detective DQ, Nexo y Nova, con sus colores y capacidades digitales."` (igual que hoy)
      - `images: ["/guardians/emi.png", "/guardians/locky.png", "/guardians/lex.png", "/guardians/byte.png", "/guardians/detective-dq.png", "/guardians/nexo.png", "/guardians/nova.png"]` (igual que hoy)
      - `capabilities: [{ color: "#FF2F7D", icon: "♥", name: "Empatía Digital" }, { color: "#0077FF", icon: "▣", name: "Seguridad Digital" }, { color: "#FFB000", icon: "⚖", name: "Derechos Digitales" }, { color: "#22A600", icon: "▤", name: "Alfabetización Digital" }, { color: "#6C2BFF", icon: "✥", name: "Pensamiento Crítico" }, { color: "#00A9B7", icon: "♧", name: "Colaboración Digital" }, { color: "#FF5B00", icon: "➤", name: "Innovación Digital" }]`
      - `closingIcon: "✦"`
      - `closing: "La mejor tecnología no decide por ti: te ayuda a decidir mejor."`
      Eliminar las claves ya no usadas (`eyebrow`, `lead`, `body`, `competencias`). Guardar el
      archivo en UTF-8 para preservar los glifos de íconos.
      Files: `concientic/src/content/site.ts`
      Verify: `npm run typecheck` (cwd `concientic`) compila sin errores; confirma que ningún otro
      archivo rompe por el cambio de forma (si `tsc` señala un consumidor inesperado, ajustarlo).

- [ ] 2. Reescribir el componente `src/components/sections/guardianes.tsx` para emitir la
      estructura EXACTA del HTML (una sola columna centrada), consumiendo el nuevo objeto
      `guardianes`. YA NO usa `SectionHeader`. Estructura, en orden:
      1. `<section id={guardianes.id} className="ct-section guardians-feature">` (ver item 3 sobre
         por qué `guardians-feature` y NO `guardians-section`).
      2. dentro, `<div className="ct-container guardians-layout">`.
      3. `<div className="guardians-kickers">` con DOS `<span className="ct-eyebrow guardians-kicker">`
         (uno por cada string de `guardianes.eyebrows`), color `#008f70` vía CSS (item 3), no inline.
      4. `<h2 className="guardians-title">{guardianes.title}</h2>`.
      5. `<p className="guardians-intro">{guardianes.intro}</p>`.
      6. `<div className="guardian-image-wrap" role="img" aria-label={guardianes.groupAlt}>` que
         mapea `guardianes.images` a `<span className="guardian-image__item"><Image src={src} alt=""
         width={160} height={160} sizes="(max-width: 700px) 30vw, 160px" /></span>` (misma técnica
         de composición actual; mantener comentario explicando la §14.5 / prohibición de base64).
         El wrap ocupa el lugar de la `img.guardian-image` del HTML.
      7. `<div className="guardian-capabilities" aria-label="Capacidades representadas por los Guardianes">`
         que mapea `guardianes.capabilities` a
         `<div className="guardian-capability" key={cap.name} style={{ "--cap-color": cap.color } as React.CSSProperties}>`
         `<span aria-hidden="true">{cap.icon}</span><strong>{cap.name}</strong></div>`.
      8. `<div className="guardians-closing"><span aria-hidden="true">{guardianes.closingIcon}</span>{" "}{guardianes.closing}</div>`
         (respetar el espacio tras el `✦` como en el HTML).
      Mantener `import Image from "next/image"`; eliminar `import { SectionHeader }`.
      Files: `concientic/src/components/sections/guardianes.tsx`
      Verify: `npm run typecheck` y `npm run lint` (cwd `concientic`) pasan sin errores.

- [ ] 3. Portar el CSS de la sección Guardianes del HTML a `src/app/globals.css`, reemplazando el
      bloque actual `/* ── Rediseño v13: franja de Guardianes ── */` (líneas ~1849–1917:
      `.guardianes-band`, `.guardianes-lead`, `.guardianes-body`, `.guardianes-group`,
      `.guardianes-group__item`, `.guardianes-group__item img`, `.guardianes-competencias`,
      `.guardianes-competencias__item`, `.guardianes-closing`). Reglas a escribir (traducidas del
      bloque autoritativo del HTML líneas ~306–321 — que gana en cascada sobre el bloque base
      ~229–267 — con mapeo a tokens `--ct-*`; `--cap-color` queda literal; breakpoints 900/600px):

      ```css
      /* ── v13: sección Guardianes (franja de 1 columna, markup = reference) ── */
      /* Clase raíz propia para NO colisionar con .guardians-section (Team, navy). */
      .guardians-feature { padding: clamp(54px, 7vw, 92px) 0; background: #f4f7f2; }
      .guardians-layout { /* hereda el ancho de .ct-container; margin-inline auto ya aplicado */ }
      .guardians-kickers { display: flex; align-items: center; justify-content: space-between; gap: 24px; margin-bottom: 18px; }
      .guardians-kicker { margin: 0; font-size: .82rem; letter-spacing: .16em; font-weight: 900; text-transform: uppercase; color: #008f70; }
      .guardians-title { max-width: 1000px; font-size: clamp(2.3rem, 5.8vw, 5.1rem); line-height: .99; letter-spacing: -.065em; margin: 0 0 22px; color: var(--ct-text); }
      .guardians-intro { max-width: 100%; font-size: clamp(1rem, 1.5vw, 1.25rem); line-height: 1.6; color: #5c6b61; margin: 0 0 28px; }
      .guardian-image-wrap { width: 100%; margin: 0; overflow: hidden; border-radius: 18px; background: transparent; line-height: 0; }
      /* Composición de los 7 PNG ocupando el lugar de la img.guardian-image del HTML: tira/mosaico centrado */
      .guardian-image-wrap { display: grid; grid-template-columns: repeat(7, 1fr); gap: 16px; align-items: end; justify-items: center; padding: 10px 0; }
      .guardian-image__item { display: flex; justify-content: center; width: 100%; }
      .guardian-image__item img { width: 100%; height: auto; max-width: 160px; object-fit: contain; }
      .guardian-capabilities { display: grid; grid-template-columns: repeat(7, minmax(0, 1fr)); gap: 10px; margin-top: 14px; width: 100%; }
      .guardian-capability { min-width: 0; display: flex; flex-direction: column; align-items: center; justify-content: center; gap: 8px; text-align: center; padding: 15px 9px 14px; border: 1px solid #e1e9e2; border-radius: 13px; background: #fff; color: #24332b; transition: transform .28s cubic-bezier(.22,1,.36,1), box-shadow .28s ease, border-color .28s ease, background-color .28s ease; }
      .guardian-capability span { display: grid; place-items: center; width: 28px; height: 28px; color: var(--cap-color); font-size: 1.25rem; line-height: 1; }
      .guardian-capability strong { font-size: clamp(.68rem, .85vw, .84rem); line-height: 1.25; overflow-wrap: anywhere; }
      .guardian-capability:hover { transform: translateY(-4px); border-color: var(--cap-color); box-shadow: 0 12px 25px rgba(23, 35, 30, .07); }
      .guardians-closing { margin-top: 24px; padding: 17px 20px; border-left: 4px solid var(--ct-primary); background: #e8f8f2; color: #23372d; font-weight: 800; line-height: 1.5; border-radius: 0 12px 12px 0; }
      .guardians-closing span { color: #008f70; margin-right: 8px; }
      @media (max-width: 900px) {
        .guardian-capabilities { grid-template-columns: repeat(4, minmax(0, 1fr)); }
        .guardians-title { max-width: 850px; }
      }
      @media (max-width: 600px) {
        .guardians-layout { width: calc(100% - 30px); }
        .guardians-kickers { align-items: flex-start; flex-direction: column; gap: 8px; }
        .guardians-title { font-size: clamp(2.15rem, 11vw, 3.5rem); }
        .guardians-intro { margin-bottom: 20px; }
        .guardian-image-wrap { border-radius: 12px; grid-template-columns: repeat(4, 1fr); }
        .guardian-capabilities { grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 8px; }
        .guardian-capability { padding: 12px 8px; }
        .guardian-capability strong { font-size: .78rem; }
        .guardians-closing { padding: 14px 15px; }
      }
      ```

      Notas de mapeo y decisiones:
      - `.guardians-section` del HTML (`background:#f4f7f2`) se renombra a **`.guardians-feature`**
        para no pisar el `.guardians-section` navy del Team. Mismo padding e idéntico fondo claro.
        (Decisión: evita la colisión de clase sin tocar el CFG del Team; coste mínimo, un nombre.)
      - `var(--green)` del HTML → `var(--ct-primary)` (borde del closing). `var(--green-dark)` →
        literal `#008f70` (eyebrows e ícono del closing). `var(--ink)` → `var(--ct-text)` (título).
        Los grises `#5c6b61`, colores `#e8f8f2`, `#23372d`, `#e1e9e2`, `#24332b`, `#f4f7f2` van
        literales (el HTML los usa literales, no por token).
      - `.guardian-image`/`.guardian-image-wrap` del HTML asumen UNA `<img>`; como aquí el wrap
        contiene 7 PNG, se adapta el wrap a grid de 7 (tira/mosaico centrada) tomando el gap y el
        `max-width:160px` del CSS actual `.guardianes-group*`, y conservando `border-radius`/
        `overflow` del HTML. Esto reemplaza la antigua `.guardianes-group`.
      - `--ease-out` del HTML (`cubic-bezier(.22,1,.36,1)`) NO existe como token en el front:
        se inlinea el cubic-bezier en la transición de `.guardian-capability` (no se añade un token
        global nuevo para no tocar el `:root` ya validado).
      - NO se replica el zoom `.guardian-image-wrap:hover .guardian-image{transform:scale(1.012)}`
        del HTML porque aquí no hay una sola `<img>` de grupo; el hover relevante (las capabilities)
        sí se replica.
      Files: `concientic/src/app/globals.css`
      Verify: `npm run build` (cwd `concientic`) compila sin errores de CSS/JSX.

- [ ] 4. Eliminar del `@media (max-width: 768px)` de globals.css (líneas ~2020–2028) las reglas
      huérfanas que apuntaban a las clases retiradas (`.guardianes-group { grid-template-columns:
      repeat(4,1fr) }` y `.guardianes-competencias { grid-template-columns: repeat(2,1fr) }`),
      dejando intactas las reglas de `.experiencia-demo` de ese mismo media query. Los breakpoints
      de Guardianes ahora viven en los `@media (max-width:900px|600px)` del item 3.
      Files: `concientic/src/app/globals.css`
      Verify: `npm run build` (cwd `concientic`) sigue compilando; `npm run lint` pasa.

- [ ] 5. Verificar que NO queda CSS que ponga un fondo navy sobre la nueva sección Guardianes y
      que NO quedan selectores huérfanos de la franja vieja (`.guardianes-band`, `.guardianes-lead`,
      `.guardianes-body`, `.guardianes-group`, `.guardianes-competencias*`). Buscar esas clases en
      `globals.css` y en `src/**/*.tsx`: no deben existir referencias restantes (el único
      `.guardians-section` que debe permanecer es el navy del Team en ~847, que es correcto porque
      lo usa `team.tsx`; NO eliminarlo). Si aparece alguna referencia residual a las clases viejas,
      eliminarla.
      Files: (verificación; sin cambios salvo limpieza residual) `concientic/src/app/globals.css`
      Verify: `grep` de `guardianes-band|guardianes-lead|guardianes-body|guardianes-group|guardianes-competencias`
      sobre `concientic/src` no devuelve coincidencias; `grep` de `guardians-section` solo aparece
      en el bloque navy del Team y en `team.tsx`, nunca sobre la nueva sección Guardianes.

- [ ] 6. Verificación integral de la sección. Ejecutar `npm run typecheck`, `npm run lint` y
      `npm run build` (cwd `concientic`); los tres deben pasar. Levantar `npm run dev` y comparar
      visualmente `#guardianes` con la sección `#guardianes` de
      `entregables_v7/docs/concientic_v13_reference.html`: una columna centrada, fondo CLARO
      (`#f4f7f2`, NO navy), 2 eyebrows en `#008f70`, título grande, intro gris, tira centrada de los
      7 personajes, fila de 7 tarjetas de capacidad (cada ícono con su `--cap-color`, hover que
      colorea el borde), y bloque de cierre verde claro con el `✦`. Los breakpoints 900/600 deben
      pasar a 4 y 2 columnas respectivamente.
      Files: (sin cambios)
      Verify: `npm run build` OK y paridad visual con el HTML de referencia en `#guardianes`.

## Lo que NO se toca (recordatorio)
- Las otras 7 secciones de la home (orden/textos/estilo ya validados).
- Tokens globales `:root` ya alineados (`--ct-dark #0d182a`, `--ct-pink #ff68b4`, `--ct-soft`,
  `--ct-muted`, `--ct-text`, `--ct-line`): no revertir ni añadir tokens nuevos.
- El bloque navy `.guardians-section`/`.guardian-grid`/`.guardian-quote`/`.guardian-card*`/
  `.team-principle*` (lo usa `team.tsx`): se conserva.
- Bloque `BEGIN:nextjs-agent-rules` de `AGENTS.md`, logo wordmark, formulario, skip link,
  focus-visible, prefers-reduced-motion, dominio `www.concientic.com`.

## Supuestos (gaps resueltos con decisión razonable)
- El componente `Team` no está en la home, pero su CSS/colisión de clase obliga a usar
  `.guardians-feature` para la sección Guardianes. Se eligió renombrar la raíz de Guardianes en vez
  de refactorizar `Team` para minimizar el blast radius y no tocar una sección no pedida.
- Sin framework de tests en el repo: la verificación se apoya en `typecheck` + `lint` + `build`
  + comparación visual contra el HTML de referencia (es una tarea de paridad visual).
