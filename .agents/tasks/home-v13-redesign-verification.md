# Reporte de verificación — Rediseño home v12 → v13 (ConcienTIC)

Entregable final del workflow. Verificación realizada sobre el front en
`concientic/`, contrastada contra el spec
`entregables_v7/kiro/specs/concientic-web-redesign.md` (§14) y el
`entregables_v7/AGENTS.md` (Manual de Marca v2.0 / Design System v1.0).

Estado general: **APROBADO**. Build y lint pasan sin errores; no fue
necesario corregir ningún error de compilación.

---

## 1. Resultado de build / lint

Comandos ejecutados en `c:\Jose\RepoSiginex\WebConcienTIC\concientic`:

| Comando | Resultado |
| --- | --- |
| `npm run lint` (eslint) | OK, exit 0, sin advertencias |
| `npm run build` (next build, Turbopack, Next 16.3.4) | OK, exit 0 |

Salida resumida del build:
- `Compiled successfully in 1129ms`.
- `Finished TypeScript in 2.8s` — sin errores de tipos.
- 10 rutas generadas correctamente, incluida `/` (estática) y
  `/servicios/contenidos`, `/privacidad`, `/terminos`, `/design-system`,
  más las API dinámicas `/api/contact` y `/api/catalog/resources`.

No se aplicaron correcciones de código: el proyecto compila tal cual.

---

## 2. Secciones montadas en la home (`concientic/src/app/page.tsx`)

Se montan exactamente **8 secciones**, en el orden del §14.1 (el Header y
el Footer los aporta el layout, no `page.tsx`):

| # | Componente | Sección | `id` | Fuente de `id` |
| --- | --- | --- | --- | --- |
| 1 | `Hero` | Hero | `inicio` | literal en `hero.tsx` |
| 2 | `Ecosistema` | Ecosistema | `ecosistema` | `ecosistema.id` |
| 3 | `Guardianes` | Guardianes | `guardianes` | `guardianes.id` |
| 4 | `Recursos` | Aprende / Recursos | `recursos` | `recursos.id` |
| 5 | `Methodology` | Metodología | `metodologia` | `methodology.id` |
| 6 | `Experiencia` | Experiencia (demo) | `experiencia` | `experiencia.id` |
| 7 | `Dquilibrio` | DQUILIBRIO | `dquilibrio` | `dquilibrio.id` |
| 8 | `Contact` | Contacto / CTA final | `contacto` | `contact.id` |

El orden en el JSX coincide 1:1 con §14.1 (Hero → Ecosistema → Guardianes →
Recursos → Metodología → Experiencia → DQUILIBRIO → Contacto).

### Menú del header (`concientic/src/components/layout/header.tsx` + `src/content/site.ts`)

`navLinks` en orden exacto del §14.2:

1. Ecosistema → `#ecosistema`
2. Guardianes → `#guardianes`
3. Aprende → `#recursos`
4. Metodología → `#metodologia`
5. DQUILIBRIO → `#dquilibrio`
6. CTA **"Encuentra tu ruta ↗"** → `#experiencia` (botón `ct-button--dark`,
   fuera de `navLinks`, replicado también en el menú móvil).

El footer (`footer.links` en `site.ts`) repite los mismos 5 enlaces sin el
CTA, conforme a §14.2.

---

## 3. Responsive estructural

Verificado en `concientic/src/app/globals.css` y los componentes de sección.
La maquetación es fluida (grids con `grid-template-columns`, `clamp()` en
tipografías, `width: min(1240px, …)` en el contenedor) con los siguientes
breakpoints reales declarados:

| Breakpoint declarado | Efecto estructural |
| --- | --- |
| `max-width: 1050px` | Oculta nav de escritorio y CTA, muestra botón móvil; `hero-grid`, `methodology-grid`, `manifesto-grid`, `contact-grid` pasan a 1 columna; grids de tarjetas 4/5/3 → 2 columnas; `guardian-grid` → 3 columnas |
| `max-width: 900px` | Ajustes de la subpágina Biblioteca (`library-*`) a 1–2 columnas |
| `max-width: 768px` | `guardianes-group` (franja v13) de 7 → 4 columnas; `guardianes-competencias` 4 → 2 columnas |
| `max-width: 700px` | Logo reducido; tarjetas a 1 columna; `hero` y secciones con padding reducido; footer en columna |
| `max-width: 620px` | Ajustes finos de Biblioteca a 1 columna |

Comportamiento esperado en los anchos objetivo:
- **360 px**: dentro del rango `≤700px` (y `≤620px`) → todo en una columna,
  nav colapsada en menú móvil. Correcto.
- **768 px**: activa el breakpoint `≤768px` de la franja de Guardianes y
  `≤1050px` (nav móvil, grids a 2 columnas). Correcto.
- **1024 px**: dentro de `≤1050px` → nav móvil, grids de tarjetas a 2
  columnas, `guardian-grid` a 3. Correcto.
- **1440 px**: por encima de todos los breakpoints → layout de escritorio
  completo con contenedor tope de 1240 px centrado. Correcto.

Observación (no es un fallo): los cortes NO están fijados exactamente en
360/768/1024/1440; el diseño es fluido y usa 1050/900/768/700/620 px. Los
cuatro anchos objetivo caen en el rango correcto y se resuelven sin
desbordes estructurales. Se deja anotado por transparencia; no se modificó
nada porque la instrucción es verificar, no re-maquetar.

---

## 4. Componentes v12 conservados (huérfanos, NO borrados)

Confirmado: ningún componente de sección v12 fue eliminado. Solo se
desmontaron de la home (`page.tsx` ya no los importa). Siguen en disco:

En `concientic/src/components/sections/`:
- `cases.tsx`
- `challenge.tsx`
- `desinfoxication.tsx`
- `evidence.tsx`
- `manifesto.tsx`
- `principle-band.tsx`
- `references.tsx`
- `services.tsx`
- `team.tsx`
- `value-props.tsx`

En `concientic/src/components/ui/`:
- `guardian-card.tsx`

Los 11 archivos existen (verificado con `Test-Path`). Su contenido/datos
siguen en `src/content/site.ts` (`challenge`, `desinfoxication`, `services`,
`evidence`, `team`, `cases`, `references`, `manifesto`, `principleBand`), de
modo que reubicarlos si Jeimy lo decide no requiere reescribir contenido.

---

## 5. Verificación de textos vs. spec §14

Los textos provienen de `src/content/site.ts` (contenido separado de
presentación) y se renderizan literalmente en cada componente. Cotejados
contra el spec sin parafrasear:

- **Hero (§14.3)** — eyebrow "Aprende con intención. Aplica con criterio.",
  h1 "La inteligencia digital empieza contigo.", cuerpo, dos CTA
  ("Descubre tu siguiente paso ↗" → `#experiencia`, "Conoce nuestra
  metodología" → `#metodologia`) y cierre "Menos ruido. Más criterio.
  Aprendizaje que se convierte en acción." → coinciden.
- **Ecosistema (§14.4)** — encabezado + intro + 3 tarjetas (DQnexus,
  TutorIA, Laboratorios IA con mención a SIGINEX) y la **nota de honestidad
  literal** ("…Esta propuesta no presupone funcionalidades ya publicadas.")
  → coinciden. (Sobre el CTA de la tarjeta 3, ver §7.)
- **Guardianes (§14.5)** — encabezado "Los Guardianes — Inteligencia con
  propósito.", lead, cuerpo, alt-text oficial de grupo, las 7 competencias
  en el orden del spec y cierre "La mejor tecnología no decide por ti: te
  ayuda a decidir mejor." → coinciden.
- **Recursos (§14.6)** — encabezado + intro + 3 tarjetas (Referentes y
  recursos abiertos → absoluto `https://www.concientic.com/servicios/contenidos`
  con `target=_blank`/`rel=noopener noreferrer`; Academia ConcienTIC; Programas
  y acompañamiento sin CTA) → coinciden. (URL de Hotmart, ver §7.)
- **Experiencia (§14.8)** — encabezado "Empieza por ti — …", el **aviso
  demostrativo literal** siempre visible, etiqueta "Experiencia
  demostrativa · 1 pregunta", pregunta, 4 opciones y botón "Ver una
  orientación inicial ↗". Implementada como componente cliente sin
  persistencia ni envío, con `aria-live` en el resultado → coinciden.
- **Contacto (§14.9)** — eyebrow "El siguiente paso empieza aquí" + título
  "Haz que la tecnología tenga sentido para ti." (reproduce la cadena
  "El siguiente paso empieza aquí — Haz que la tecnología tenga sentido
  para ti."), cuerpo y CTA "Conversemos ↗" → coinciden.

No se detectó texto inventado ni parafraseado en estas 6 secciones.

---

## 6. Desviaciones del archivo de referencia v13 dejadas SIN aplicar (intencional)

Se conservan los **valores oficiales del Manual de Marca v2.0 / Design
System v1.0** y NO se adoptan las desviaciones del archivo v13, por
decisión explícita del spec: §14.10 (paleta) y §14.7 (logo) indican "no
corregir/implementar sin confirmación de Jeimy" y, mientras tanto, "usar
los valores oficiales del Manual de Marca". §15 (criterios de aceptación)
reitera que estas diferencias deben resolverse con respuesta explícita de
Jeimy antes de producción; hasta entonces rige lo oficial.

| # | Desviación en el archivo v13 | Valor oficial que SE mantiene | Dónde | Justificación |
| --- | --- | --- | --- | --- |
| 1 | `--navy` = `#0D182A` | `--ct-dark: #0D1B2A` | `globals.css :root` | §14.10 — usar hex oficial hasta confirmación |
| 2 | `--pink` = `#FF68B4` | `--ct-pink: #FF6EB6` | `globals.css :root` | §14.10 — usar hex oficial hasta confirmación |
| 3 | `--paper` = `#f5f7f3` (fondo `body`) | Superficie `#F5F7F9` (`--ct-soft`); `body` usa `--ct-white` | `globals.css` | §14.10 — tono oficial de Superficie hasta confirmación |
| 4 | Poppins **no se carga** en v13 (todo en Inter) | Poppins cargada vía `next/font` y aplicada a titulares (`--font-poppins`) | `layout.tsx`, `globals.css` (h1–h4, bandas, nombres) | §14.10 — Poppins para marca/titulares hasta confirmación |
| 5 | Logo del header como lockup de **texto CSS** ("concienTIC" + tagline) | **Wordmark-imagen** `concientic-nexus-wordmark-transparente-fondo-claro.png` vía `next/image` | `header.tsx` | §14.7 — no cambiar a logo-texto sin confirmación; usar el wordmark de imagen vigente (§2) |

Las cuatro desviaciones de marca + el cambio de logo quedan como preguntas
abiertas, tal como pide §14.10 / §14.7 / §15.

---

## 7. Pendientes de confirmación de Jeimy

1. **URL de la Academia en Hotmart** — el archivo v13 trae el placeholder
   genérico `https://hotmart.com/` (§14.6). Se deja ese marcador, NO se
   inventa una URL real; en el código la tarjeta está marcada
   `pending: true` y la UI muestra "URL pendiente de confirmación"
   (`recursos.tsx`). Falta la URL definitiva del curso.
2. **Destino de las secciones v12 eliminadas de la home** — §14.1 pide
   confirmar si "El desafío", "Desinfoxicación", "Servicios" (5 tarjetas),
   "Centro de Evidencia", "Casos", "Referentes" y el Manifiesto de cierre se
   descartan definitivamente o se reubican. Los componentes y su contenido
   siguen conservados (ver §4) a la espera de esa decisión.
3. **Bios / tarjetas individuales de los Guardianes** — §14.5 pide confirmar
   si la franja v13 reemplaza definitivamente la sección Equipo de 7
   tarjetas con rol y bio (§5) o si esa versión debe vivir en otra página
   (p. ej. `/equipo`). v13 no incluye bios ni roles individuales en la home.
4. **Logo del header** — §14.7: ¿lockup de texto CSS (v13) o wordmark-imagen
   (oficial)? Actualmente wordmark-imagen (ver §6, fila 5).
5. **Desviaciones de marca** — las 4 diferencias de paleta/tipografía de
   §14.10 (ver §6, filas 1–4).
6. **CTA de la tarjeta 3 de Ecosistema (Laboratorios IA)** — §14.4 fija solo
   el destino (`#contacto`), no la etiqueta literal. Se usa "Conversemos ↗"
   de forma provisional (reutiliza la etiqueta de §14.9 para el mismo
   destino) y está marcada `pending: true` en `site.ts`. Falta confirmar la
   etiqueta definitiva.

---

## 8. Assets faltantes

- **Imagen de grupo de Guardianes (§14.5):** NO existe un asset único de
  grupo en el paquete v7; `public/guardians/` solo trae los 7 PNG
  individuales (emi, locky, lex, byte, detective-dq, nexo, nova). Conforme a
  §14.5 ("si no existe todavía, puede componerse con los 7 PNG individuales"),
  la franja compone la imagen con los 7 PNG dentro de un contenedor
  `role="img"` que lleva el alt-text oficial; los PNG individuales se marcan
  decorativos (`alt=""`). Se señala como pendiente el asset de grupo único
  por si Jeimy decide proveerlo; no se inventó ninguna imagen.
- No se detectaron otros assets faltantes. Hero (`equilibrio-concientic-mejorado.png`)
  y Metodología (`ciclo-dquilibrio-mejorado.png`) usan los diagramas locales
  de `public/diagrams/`; el wordmark del header/footer existe en
  `public/brand/`.

---

## 9. Conclusión

- Las 8 secciones se montan en el orden de §14.1 con los `id` correctos.
- El menú respeta §14.2 (5 enlaces + CTA "Encuentra tu ruta ↗" → `#experiencia`).
- Responsive estructural correcto en 360/768/1024/1440 (diseño fluido con
  breakpoints 1050/900/768/700/620).
- Los 11 componentes v12 se conservan; solo se desmontaron de la home.
- Los textos de Hero, Ecosistema, Guardianes, Recursos, Experiencia y
  Contacto coinciden literalmente con §14.
- Las 4 desviaciones de marca y el logo-imagen se mantienen en valores
  oficiales a la espera de Jeimy (§14.7 / §14.10 / §15).
- `npm run lint` y `npm run build` pasan sin errores.

Quedan 6 puntos pendientes de confirmación de Jeimy (§7) y el asset de grupo
único de Guardianes señalado como opcional (§8). Ninguno bloquea el build.
