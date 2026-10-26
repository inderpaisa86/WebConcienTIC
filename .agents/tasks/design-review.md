# Revisión de diseño — Rediseño home v12 → v13 ConcienTIC

**Documento revisado:** `.agents/tasks/home-v13-design.md`
**Autoridad contrastada (solo lectura):** `entregables_v7/` — `kiro/specs/concientic-web-redesign.md` §1-§15 (en especial §14 completa), `kiro/steering/{structure,design-system,tech,product,content}.md`, `AGENTS.md`, `reference-code/styles/design-tokens.css`, `docs/concientic_v13_reference.html`.
**Contrato del usuario:** "no asumir nada, hacerlo tal cual". Toda decisión de producto sin respaldo del paquete, todo texto parafraseado en vez de exacto, y toda desviación de la referencia v13 aplicada contra las reglas por defecto del paquete, es un hallazgo BLOQUEANTE.
**Naturaleza del documento:** es una **segunda iteración** que ya responde a una revisión previa (su §15). Esta revisión verifica de forma independiente las afirmaciones de esa iteración contra la fuente, sin tomarlas por ciertas.

---

## Resumen del veredicto

Se verificó de forma independiente cada afirmación portante del diseño contra la fuente real (spec §14, steering, tokens, el HTML v13 y los archivos de código que el diseño dice haber leído). **Todas las afirmaciones portantes resultaron correctas**: textos exactos, reglas por defecto del paquete, conservación de archivos, comportamiento del formulario, h1 hardcodeado, tokens y accesibilidad. No se encontró ninguna decisión de producto inventada ni ningún texto parafraseado que deba ser exacto.

No hay hallazgos HIGH ni MEDIUM. Se registran 3 NIT (mejoras de precisión que no bloquean). **Veredicto: APPROVED.**

---

## Hallazgos

Cada hallazgo indica severidad, problema, ubicación y corrección concreta.

1. **NIT — Colores/glifos de las 7 competencias de Guardianes no catalogados como desviación.**
   Ubicación: §4 edge cases / §4 (franja Guardianes) y tabla §11.
   El HTML v13 pinta cada competencia con hex propios que **no** son tokens oficiales ni figuran entre las 4 desviaciones de §14.10: `♥ #FF2F7D` (Empatía), `▣ #0077FF` (Seguridad), `⚖ #FFB000` (Derechos), `▤ #22A600` (Alfabetización), `✥ #6C2BFF` (Pensamiento Crítico), `♧ #00A9B7` (Colaboración), `➤ #FF5B00` (Innovación). El diseño dice que los glifos son "genéricos" y usa tokens oficiales, lo cual es correcto por la regla por defecto §15, pero **no deja anotada esta quinta familia de desviaciones** en la tabla §11 ni como pregunta a Jeimy.
   Fix concreto: añadir una fila a la tabla §11 ("Colores/iconos de las 7 competencias en v13 usan hex no oficiales `#FF2F7D/#FFB000/#22A600/#6C2BFF/#00A9B7/#FF5B00` → se usan tokens oficiales del Manual de Marca por §15; glifos genéricos") y, si se quiere mapear cada competencia a un color de marca, anotarlo como pregunta en `06_REVISION_VISUAL.md`. No bloquea porque §14.5 solo fija el **texto** de las competencias "con su glifo/ícono", sin fijar color.

2. **NIT — El aviso de Experiencia del paquete difiere del render literal del HTML; conviene dejar constancia de cuál manda.**
   Ubicación: §4.3 (`experiencia.notice`) y §9.5.
   El diseño transcribe el aviso de **§14.8/§9.5** (string continuo que incluye "En la experiencia real, las recomendaciones deberán apoyarse en criterios transparentes, recursos confiables y una ruta relacionada con tu objetivo."). El HTML v13 real reparte el texto en dos `<p>` (líneas 517-518) **y** antepone "Comienza identificando qué necesitas resolver." que el spec no incluye. El diseño hace lo correcto —el spec §14.8 es la autoridad, no el HTML— pero no explicita este desajuste HTML↔spec.
   Fix concreto: añadir una nota de una línea en §4.3/§9.5: "El HTML v13 divide este aviso en dos párrafos y antepone 'Comienza identificando qué necesitas resolver.'; se adopta el texto literal del spec §14.8 (autoridad), no el del HTML." Verificado: el texto que el diseño va a renderizar coincide carácter a carácter con §9.5.

3. **NIT — El CTA de Contacto en el HTML v13 apunta a dominio sin `www`; el diseño ya lo corrige pero no lo documenta como desviación de la referencia.**
   Ubicación: §9.6 / RF12 / tabla §11.
   El HTML v13 (línea 554) usa `href="https://concientic.com/#contacto"` (sin `www`). El diseño mandata `https://www.concientic.com` en todo el código (RF12, correcto por §14.6/§15), pero la tabla §11 de "desviaciones de la referencia v13 que NO se aplican" no lista este caso.
   Fix concreto: añadir a la tabla §11 la fila "CTA Contacto en v13 usa `concientic.com` sin `www` → se usa `https://www.concientic.com` por §14.6/§15". No bloquea: es coherencia documental; la decisión ya es correcta.

---

## Suposiciones verificadas (confirmadas contra la fuente)

1. **Orden y las 8 secciones (§14.1).** El diseño (RF1, §7.1) lista Hero → Ecosistema → Guardianes → Recursos → Metodología → Experiencia → DQUILIBRIO → Contacto. Coincide con §14.1 verbatim.
2. **Menú (§14.2).** `Ecosistema · Guardianes · Aprende · Metodología · DQUILIBRIO` + CTA `Encuentra tu ruta ↗` → `#experiencia`; footer con los 5 enlaces (incluida la etiqueta `Aprende`, no `Recursos`) + `© ConcienTIC`. Confirmado contra el HTML v13 (header línea 401; footer línea 561) y §14.2.
3. **Textos exactos Hero (§9.1/§14.3).** H1 `La inteligencia digital empieza contigo.` confirmado en HTML v13; eyebrow, cuerpo, 2 CTA y cierre coinciden.
4. **Textos exactos Ecosistema (§9.2/§14.4).** Encabezado `El ecosistema ConcienTIC — Tres territorios. Un propósito compartido.`, intro, 3 tarjetas (DQnexus/TutorIA/Laboratorios IA + SIGINEX) y nota de honestidad confirmados contra HTML (líneas 428-431) y spec.
5. **Nota de honestidad de Ecosistema sin resumir (§14.4).** El diseño la marca literal, visible, "no resumir ni ocultar tras un toggle" (RF4, §4.1). Texto coincide con §9.2.
6. **Textos exactos Guardianes (§9.3/§14.5).** Encabezado, cuerpo, alt de imagen y cierre coinciden. La **lista de 7 competencias** (Empatía, Seguridad, Derechos, Alfabetización, Pensamiento Crítico, Colaboración, Innovación Digital) fue cotejada **una a una** contra el HTML v13 (`guardian-capability` líneas ~462): coincide exactamente. El diseño usa esta lista (autoridad §14.5) y señala correctamente que diverge de `guardians.ts`; no reescribe `guardians.ts`.
7. **Franja única de Guardianes, no grilla de 7 bios (§14.5, `structure.md`).** Confirmado en `structure.md` ("franja única, ya no grilla de 7 tarjetas") y en el HTML (una sola imagen de grupo + grid de competencias).
8. **Imagen de grupo inexistente → composición de los 7 PNG (§14.5).** Regla por defecto citada correctamente; el diseño fija `role="img"` + `aria-label` con el alt oficial para el contenedor compuesto. Coherente con §14.5.
9. **Textos exactos Recursos (§9.4/§14.6) y separador con barra.** `01/EXPLORAR`, `02/APRENDER Y PROFUNDIZAR`, `03/PROFUNDIZAR` confirmados contra el spec; el diseño exige la cadena literal con "/" en datos y ajustes solo en CSS. Enlace de la tarjeta 01 a `https://www.concientic.com/servicios/contenidos` con `target="_blank"/rel`, y Hotmart como placeholder marcado: ambos coinciden con §14.6 y el HTML (líneas 479-480).
10. **Demo de Experiencia sin persistencia, con aviso fijo (§14.8).** Las **4 recomendaciones** (título + cuerpo) fueron cotejadas verbatim contra el objeto `recommendations` del HTML v13 (líneas 575-581): coinciden exactamente. Los `value` de los radios (`ia/info/product/balance`, HTML líneas 522-525) y las 4 etiquetas de opción también coinciden. El diseño define componente cliente con estado efímero, sin `fetch`/`localStorage`/envío, y aviso siempre visible — conforme a §14.8/§15.
11. **Formulario de Contacto sin fingir envío (`AGENTS.md`, §8).** Verificado leyendo el código real: `contact-form.tsx` hace `POST` a `/api/contact` y solo pone `status="success"` tras `res.ok`; `api/contact/route.ts` devuelve **502** si `!result.ok` y **200** solo en éxito real (honeypot aparte); `lib/email.ts` devuelve `{ok:false}` si falta `RESEND_API_KEY`/`CONTACT_FROM_EMAIL`. Confirma que el estado de éxito **nunca** se alcanza sin envío real. El patrón `role="status"`/`aria-live="polite"` sin `alert()` ya existe en el código. El texto de éxito actual ("Maqueta de demostración…") es efectivamente falso frente al pipeline real; la corrección del diseño (texto honesto, conservar el pipeline, marcar P11) cumple `AGENTS.md` sin inventar una decisión de producto. Verificado.
12. **h1 de Hero hardcodeado (afirmación central del diseño).** Verificado en `hero.tsx`: el h1 es `Aprender tecnología. <span>Sin perder humanidad.</span>` literal y **no** lee `hero.title`; `hero.title` existe en `site.ts` pero está sin usar en el h1. Por tanto la conclusión del diseño (editar el JSX, no basta `site.ts`) es correcta.
13. **Logo: mantener wordmark-imagen (§14.7).** Verificado: el HTML v13 usa lockup de texto CSS (`concienTIC` + `<small>INTELIGENCIA DIGITAL CONSCIENTE</small>`, con `.brand-mark{display:none!important}`). La regla por defecto §14.7/§15 es mantener el wordmark-imagen hasta confirmación de Jeimy; el diseño (RF11) lo respeta.
14. **Skip link `#main` (§14.11).** Verificado: existe en `layout.tsx` (`<a class="skip-link" href="#main">Saltar al contenido</a>`); el HTML v13 no lo trae. El diseño exige conservarlo (RNF3). Conforme a §14.11.
15. **Tokens de `design-tokens.css`.** Cotejados uno a uno con `design-tokens.css`: `--ct-primary #00C49A`, `--ct-secondary #6C63FF`, `--ct-blue #0077FF`, `--ct-cyan #00B4D8`, `--ct-dark #0D1B2A`, `--ct-orange #FF7A00`, `--ct-yellow #FFC857`, `--ct-pink #FF6EB6`, `--ct-white #FFFFFF`, `--ct-soft #F5F7F9`, `--ct-muted #64748B`, `--ct-text #1E293B`; radios 12/18/24/34; sombras sm/md; tiempos 160/240ms; contenedor 1240px. El inventario de §10 del diseño coincide. `:focus-visible` azul 2px offset 3px y `:target{scroll-margin-top:5.5rem}` existen en `globals.css`.
16. **Conservación de archivos de componente v12 (§14.1, `structure.md`).** `structure.md` dice literalmente "no borrar sus archivos todavía". El diseño (RF13, §6, §2.3) desmonta de `page.tsx` pero conserva los archivos huérfanos y sus datos en `site.ts`. Conforme.
17. **Dominio de producción `www` y estado actual (§14.6).** Verificado: `site.ts` hoy define `domain: "https://concientic.com"` (sin `www`); el diseño (RF12, §11) exige cambiarlo a `https://www.concientic.com`, como fija §14.6/§15.
18. **Reglas por defecto en pendientes de Jeimy (§8, P1-P11).** Cada default citado (conservar archivos, franja única, placeholder Hotmart, wordmark-imagen, hex oficiales `#0D1B2A`/`#FF6EB6`/`#F5F7F9`, Poppins, etiqueta provisional de Ecosistema, pipeline de contacto) corresponde a la regla por defecto real del paquete (§14.7, §14.10, §15, `AGENTS.md`). No se detectó ninguna decisión tomada sin respaldo.
19. **4 desviaciones de paleta/tipografía §14.10.** Verificadas en el HTML v13: `--navy:#0D182A` (línea 13), `--pink:#FF68B4` (línea 13), `--paper:#f5f7f3` (líneas 8/12), y Poppins **ausente** (body usa Inter, línea 19). La tabla §11 del diseño las lista correctamente y aplica los valores oficiales. Conforme.

---

## Suposiciones no verificadas / incorrectas

No se encontró ninguna suposición incorrecta ni ninguna afirmación portante que no haya podido verificarse contra la fuente. Puntos de alcance acotado que quedan fuera de esta revisión (no son fallos del diseño):

- **`npm run build` / lint / responsive (RNF7).** No se verifica build ni código ejecutable en esta revisión por mandato del encargo (el código aún no existe). El diseño lo deja como verificación esperada del paso de implementación (§13).
- **Guía local de Next.js (`node_modules/next/dist/docs/`).** El diseño afirma apoyarse en ella para el patrón server/client; no se abrió esa carpeta en esta revisión. No afecta a la fidelidad de contenido/arquitectura v13, que es el objeto del encargo.
- **URL real de Hotmart (P3) y respuestas de Jeimy (P1-P11).** Son datos externos pendientes por diseño; el documento los trata correctamente como placeholders marcados, no como decisiones tomadas.

---

## Conteo y veredicto

- HIGH: 0
- MEDIUM: 0
- NIT: 3

Regla mecánica: HIGH + MEDIUM = 0 → **APPROVED**.
