# ConcienTIC — Entregables para Kiro + Next.js + Vercel

Fecha: 2026-09-03

## Objetivo
Migrar y reconstruir el sitio https://concientic.vercel.app/ como una aplicación Next.js moderna, manteniendo la arquitectura conceptual de ConcienTIC y aplicando el Manual de Marca v2.0 y el Sistema de Diseño Web v1.0.

## Fuente visual de referencia
`docs/concientic_v5_reference.html` es el prototipo visual aprobado de referencia. No debe copiarse como arquitectura final: debe convertirse en componentes Next.js reutilizables.

## Documentos de autoridad
- `docs/Manual_de_Marca_ConcienTIC_v2_0.docx`
- `docs/Sistema_de_Diseno_Web_ConcienTIC_v1_0.docx`
- `kiro/steering/*`
- `kiro/specs/concientic-web-redesign.md`

## Assets
La carpeta `assets/` contiene el logo ConcienTIC sin fondo y los 7 Guardianes con transparencia real.

## Orden recomendado en Kiro
1. Ejecutar Prompt 00 — Auditoría.
2. Instalar/usar los steering files de `kiro/steering/` y `AGENTS.md`.
3. Ejecutar Prompt 01 — Base Next.js y arquitectura.
4. Ejecutar Prompt 02 — Design System y componentes.
5. Ejecutar Prompt 03 — Construcción de páginas/secciones.
6. Ejecutar Prompt 04 — Equipo / Guardianes.
7. Ejecutar Prompt 05 — QA, accesibilidad, performance y Vercel.

## Regla de trabajo
No empezar por “embellecer” el HTML existente. Primero comprender el repositorio actual, preservar lo útil y migrar hacia componentes, tokens y contenido estructurado.

## Stack objetivo
Next.js + TypeScript + React + CSS/Tailwind solo si ya existe o si Kiro justifica su incorporación. Preferir CSS Modules o una capa de tokens global si eso reduce dependencia y mantiene el sistema simple. App Router.

## Principio de producto
ConcienTIC no debe competir por atención. El sitio debe ser claro, pausado, útil y fácil de abandonar. No usar infinite scroll, autoplay, streaks, rankings, contadores artificiales, FOMO, dark patterns ni animaciones que busquen retención.
