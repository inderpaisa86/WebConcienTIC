# Spec — Rediseño web ConcienTIC

## Objetivo
Actualizar `https://concientic.vercel.app/` desde su estado actual con placeholders hacia el sitio ConcienTIC V5, implementado como Next.js mantenible y desplegable en Vercel.

## Requisitos funcionales
1. Header con logo ConcienTIC sin fondo y navegación clara.
2. Hero: “Aprender tecnología. Sin perder humanidad.”
3. DQUILIBRIO como filosofía transversal.
4. Sección El desafío: atención, infoxicación, desinformación, IA/automatización.
5. Desinfoxicación: filtrar, verificar, cerrar.
6. Servicios.
7. Metodología con ciclo visual DETENER → OBSERVAR → DISCERNIR → ELEGIR → EQUILIBRAR.
8. Centro de Evidencia.
9. Equipo con los 7 Guardianes como agentes virtuales.
10. Casos sin métricas inventadas.
11. Referentes.
12. Manifiesto/principios de diseño consciente.
13. Contacto.
14. Footer.

## Equipo
Cada Guardián debe mostrar: imagen transparente, nombre, rol/competencia, descripción breve y color territorial. El layout debe funcionar desde móvil hasta escritorio.

## Assets
Copiar los assets entregados en `assets/` a `public/brand` y `public/guardians`. Usar `next/image` y conservar transparencia.

## Criterios de aceptación
- `npm run build` exitoso.
- Sin errores de TypeScript/lint.
- Responsive en 360, 768, 1024 y 1440 px.
- Navegación por anclas operativa.
- Accesibilidad semántica y teclado.
- `prefers-reduced-motion` respetado.
- Logo correcto y sin documento de diseño como imagen.
- Guardianes sin fondos cuadriculados.
- No base64 para assets en producción.
- No placeholders visibles en la versión final salvo casos reales aún no disponibles, claramente marcados internamente.
- Vercel deployment sin configuración innecesaria.
