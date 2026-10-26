# Revisión de código — Rediseño home ConcienTIC v12 → v13

Veredicto: **APPROVED** — sin hallazgos bloqueantes.

Fuente de autoridad cotejada: `entregables_v7/kiro/specs/concientic-web-redesign.md` §14/§15, `entregables_v7/AGENTS.md`, `concientic/AGENTS.md`, y el diseño/plan aprobados. Contrato del usuario: "no asumir nada, hacerlo tal cual".

## Evidencia de verificación (registrada por el coder)

Mensaje del commit `376bf3e` ("feat: rediseno home v12 a v13 segun paquete v7") registra, con cwd `c:\Jose\RepoSiginex\WebConcienTIC\concientic`:
- `npm run typecheck` → exit 0
- `npm run lint` → exit 0
- `npm run build` → exit 0 (compilado, 10/10 páginas)

La evidencia de build/lint/typecheck está presente; no se re-ejecutaron las suites. Spot-checks puntuales realizados: existencia de los 7 PNG de Guardianes (coinciden con `guardianes.images`) y presencia física de los archivos de componentes v12.

## Comprobaciones punto por punto

1. **Orden y anclas de las 8 secciones (§14.1).** `page.tsx` monta exactamente: `Hero` (#inicio) → `Ecosistema` (#ecosistema) → `Guardianes` (#guardianes) → `Recursos` (#recursos) → `Methodology` (#metodologia) → `Experiencia` (#experiencia) → `Dquilibrio` (#dquilibrio) → `Contact` (#contacto). Los `id` se emiten en cada componente de sección (`#inicio` en hero.tsx; resto desde `site.ts`). CUMPLE.

2. **Menú header y footer (§14.2).** `navLinks` = Ecosistema→#ecosistema · Guardianes→#guardianes · Aprende→#recursos · Metodología→#metodologia · DQUILIBRIO→#dquilibrio, en ese orden. `header.tsx` renderiza los 5 en desktop y móvil + CTA `Encuentra tu ruta ↗` → `#experiencia`. `footer.tsx` repite los mismos 5 enlaces (sin CTA) + wordmark-imagen + `© ConcienTIC`. CUMPLE.

3. **Textos exactos (§14.3–§14.9).** Cotejados literalmente contra §9 del diseño y §14 del spec:
   - Hero: eyebrow, h1, subtitle, 2 CTA y `closing` — literal. El h1 renderiza `{hero.title}` como nodo único, sin `<span>` ni literal v12. CUMPLE.
   - Ecosistema: encabezado troceado eyebrow/title (concatenación `eyebrow + " — " + title` reproduce la cadena §14.4), intro, 3 tarjetas (number con " · ", name, description, CTA), y **nota de honestidad literal, visible y sin resumir** (`ecosistema.honesty` renderizada siempre al pie). CUMPLE.
   - Guardianes: encabezado, lead, body, `groupAlt` oficial, 7 competencias literales en orden, cierre — literal. CUMPLE.
   - Recursos: encabezado, intro, 3 tarjetas con números CON BARRA (`01/EXPLORAR`, `02/APRENDER Y PROFUNDIZAR`, `03/PROFUNDIZAR`), descripciones literales. CUMPLE.
   - Experiencia: encabezado, aviso literal, demoLabel + question (= cadena §14.8), 4 opciones y 4 recomendaciones literales. CUMPLE.
   - Contacto: eyebrow/title (concatenación reproduce §14.9), subtitle, CTA "Conversemos ↗" — literal. CUMPLE.

4. **Experiencia (cliente, demo, sin persistir).** `experiencia.tsx` es `"use client"`, usa `useState` local (`selected`, `shown`), sin `useEffect`/`fetch`/`localStorage`/envío. 1 pregunta con 4 radios en `<fieldset>`/`<legend>`, botón `type="button"`. Aviso `experiencia.notice` SIEMPRE visible (no condicionado al estado). Resultado y mensaje vacío en bloque `role="status" aria-live="polite"`, sin `alert()`. CUMPLE.

5. **Contacto nunca simula éxito.** `contact-form.tsx`: el estado `success` solo se alcanza tras `res.ok` real de `POST /api/contact`; mensaje de éxito honesto ("Mensaje enviado / Gracias, hemos recibido tu mensaje…") en `div role="status" aria-live="polite"`, sin `alert()`. Pipeline (zod, honeypot, Resend) intacto. CUMPLE.

6. **Marca, tokens, accesibilidad, dominio.** `globals.css`: `--ct-dark #0d1b2a`, `--ct-pink #ff6eb6`, `--ct-soft #f5f7f9`, `--ct-primary #00c49a`, `--ct-secondary #6c63ff` — valores OFICIALES, ninguna de las 4 desviaciones §14.10. Poppins (títulos) + Inter (UI) cargadas en `layout.tsx`. `:focus-visible` global (azul 2px, offset 3px), bloque `@media (prefers-reduced-motion: reduce)`, skip link `#main` presentes. Logo = wordmark-imagen (no logo de texto) en header y footer. `next/image` en hero/guardianes/header/footer. `site.domain = https://www.concientic.com`; enlaces DQUILIBRIO y Recursos absolutos a ese dominio. URL de Hotmart = `https://hotmart.com/` marcada `pending:true` con badge visible "URL pendiente de confirmación" (no inventada). CUMPLE.

7. **Conservación de componentes.** Presentes en `src/components/sections/`: cases, challenge, desinfoxication, evidence, manifesto, principle-band, references, services, team, value-props. Presentes en `src/components/ui/`: guardian-card, evidence-card. Solo se desmontaron de `page.tsx`; ningún archivo borrado. Datos v12 en `site.ts` conservados. CUMPLE.

8. **Sin dark patterns / movimiento accesible.** Sin autoplay, infinite scroll, temporizadores ni urgencia en las secciones nuevas; animación anulada bajo `prefers-reduced-motion`. CUMPLE.

9. **Bloque BEGIN:nextjs-agent-rules.** Intacto en `concientic/AGENTS.md` (líneas 1–9). CUMPLE.

## Notas no bloqueantes (no requieren acción)
- Regla CSS residual `.hero-copy h1 span` queda sin uso tras eliminar el `<span>` del h1; es CSS muerto inofensivo, no afecta render ni build.
- §14.5 menciona "glifo/ícono" por competencia; el diseño aprobado lo fijó como opcional y genérico. La franja lista las 7 competencias sin nombre de Guardián, cumpliendo el requisito esencial.
- La tarjeta 03 de Ecosistema usa la etiqueta provisional "Conversemos ↗" (`pending:true`), conforme a la regla "no inventar copy" del diseño (P10). El destino `#contacto` sí es el fijado por §14.4.

Todos los requisitos aprobados se cumplen y no se introducen fallos de corrección, seguridad ni datos.
