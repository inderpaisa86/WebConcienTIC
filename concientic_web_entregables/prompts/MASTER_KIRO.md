# MASTER PROMPT — ConcienTIC Web / Next.js / Vercel

Quiero que actualices el repositorio que actualmente despliega `https://concientic.vercel.app/`.

## CONTEXTO
ConcienTIC es un ecosistema de Inteligencia Digital. La propuesta central es:
**“Aprender tecnología. Sin perder humanidad.”**

DQUILIBRIO es la filosofía transversal: aumentar nuestra capacidad de elegir cómo, cuándo, por qué y para qué usamos la tecnología.

Los Guardianes de la Inteligencia Digital son el equipo virtual oficial de ConcienTIC. Son agentes especializados, no mascotas decorativas.

## FUENTES DE VERDAD
Lee antes de implementar:
- `AGENTS.md`
- `.kiro/steering/*`
- `kiro/specs/concientic-web-redesign.md`
- `docs/Manual_de_Marca_ConcienTIC_v2_0.docx`
- `docs/Sistema_de_Diseno_Web_ConcienTIC_v1_0.docx`
- `docs/concientic_v5_reference.html`
- `reference-code/content/guardians.ts`
- `reference-code/styles/design-tokens.css`

Si el repositorio actual tiene reglas mejores o más específicas, consérvalas cuando no contradigan la identidad de ConcienTIC.

## OBJETIVO
Transformar la implementación actual en un sitio Next.js + TypeScript mantenible y preparado para Vercel, eliminando placeholders y aplicando la identidad y el Design System.

## ORDEN DE EJECUCIÓN
1. Audita el repositorio y determina stack real.
2. Si ya es Next.js, conserva la arquitectura compatible y refactoriza. Si no lo es, migra con cuidado a Next.js App Router.
3. Implementa tokens y componentes base.
4. Implementa todas las secciones.
5. Implementa Equipo/Guardianes.
6. Ejecuta QA visual, funcional, accesibilidad y performance.
7. Ejecuta build.
8. Deja el proyecto listo para Vercel.

## PALETA OBLIGATORIA
Primario: #00C49A.
Secundario: #6C63FF.
Azul: #0077FF.
Cian: #00B4D8.
Oscuro: #0D1B2A.
Naranja: #FF7A00.
Amarillo: #FFC857.
Rosa: #FF6EB6.
Fondo suave: #F5F7F9.
Texto: #1E293B.
Muted: #64748B.

## TIPOGRAFÍA
Poppins para títulos, marca y mensajes de alto impacto.
Inter para UI y lectura.

## SECCIONES
Header → Hero → DQUILIBRIO → El desafío → Desinfoxicación → Servicios → Metodología → Evidencia → Equipo → Casos → Referentes → Manifiesto → Contacto → Footer.

## METODOLOGÍA
No usar una lista vertical como elemento visual principal. Crear un diagrama circular accesible y estático:
DETENER → OBSERVAR → DISCERNIR → ELEGIR → EQUILIBRAR → retorno al inicio.
El ciclo debe funcionar sin animación.

## EQUIPO
Usar los siete PNG transparentes entregados:
- emi.png
- locky.png
- lex.png
- byte.png
- detective-dq.png
- nexo.png
- nova.png

Copiarlos a `public/guardians/`. Usar `next/image`. No usar fondos cuadriculados. No incrustar base64.

Logo: usar `logo-concientic.png` en `public/brand/`, con transparencia.

## DISEÑO CONSCIENTE
No agregar:
- infinite scroll
- autoplay
- FOMO
- streaks
- rankings
- contadores de permanencia
- dark patterns
- urgencia artificial
- popups innecesarios

La navegación debe ser clara y fácil de abandonar.

## CONTENIDO
No inventes clientes, métricas, testimonios ni resultados. Si falta información real, usa un bloque interno marcado como TODO o contenido neutro no engañoso.

No presentar DQUILIBRIO como escala clínica validada.
No utilizar “dopamina” como explicación científica simplista.

## SEO Y ACCESIBILIDAD
Implementa metadata, Open Graph, headings semánticos, alt text, keyboard navigation, focus visible, contraste AA y reduced motion.

## VERCEL
La aplicación debe funcionar con el comando de build definido por el proyecto. No almacenar secretos. Crear `.env.example` si se requiere configuración externa.

## ENTREGA
Al finalizar:
1. Resume arquitectura final.
2. Enumera archivos creados/modificados.
3. Reporta lint/typecheck/build.
4. Reporta cualquier limitación.
5. Indica exactamente qué falta para hacer el deploy final en Vercel, si falta algo.
