# Restauración de los Guardianes originales en la Biblioteca

El cambio elimina los ocho Guardianes visuales añadidos en el último commit, junto con su registro, assets y documentación específica. La tarjeta de la Biblioteca vuelve a resolver el personaje desde el registro canónico `@/content/guardians` mediante `guardianPrimary`, mientras conserva el rediseño visual de la tarjeta introducido por fase 3. Los archivos canónicos de Guardianes, el catálogo estático, las consultas del motor y los cambios backend permanecen sin modificaciones locales. **Watch for:** **confirmado** — no hay un bloqueo funcional en este parche; la evidencia sí registra que `npm run format:check` continúa fallando por formato preexistente en 11 archivos, incluido el componente ya desformateado por fase 3, y no existe un script frontend dedicado de tests.

**Verdict**: APPROVED

## High-level view

La frontera del cambio elimina el sistema paralelo de ocho Guardianes de Biblioteca y devuelve las tarjetas a los siete Guardianes canónicos que existían antes de `HEAD`, sin modificar el contrato del catálogo ni el motor de búsqueda.

La selección usa el nombre de `guardianPrimary`; el rediseño visual, los filtros, las consultas, la paginación, los recursos estáticos/dinámicos y los cambios backend de fase 3 permanecen intactos.

**confirmado** — La evidencia cubre whitespace, lint, TypeScript, build y backend; el formato global sigue fallando por archivos preexistentes y no hay un script frontend dedicado de tests.

<details>
<summary>Issues (2)</summary>

1. **Chequeo global de formato preexistente** — **confirmado**: `npm run format:check` falla en 11 archivos, incluido `library-explorer.tsx` por el rediseño anterior; resolver la limpieza en un cambio separado sin ampliar este parche.
2. **Cobertura frontend dedicada** — **confirmado**: `package.json` no define un script de tests frontend; añadir pruebas específicas o aceptar explícitamente la cobertura de lint, typecheck y build para este cambio.

</details>

<details>
<summary>Details</summary>

## Registro canónico frente a Guardianes visuales nuevos

La comparación de `concientic/src/content/guardians.ts` en `HEAD^` con el archivo actual conserva exactamente los siete Guardianes originales: `Emi`, `Locky`, `Lex`, `Byte`, `Detective DQ`, `Nexo` y `Nova`. `config/guardians.json` también permanece sin cambios. El parche local elimina `src/content/library-guardians.ts` y los ocho SVG bajo `public/library-guardians/`, que son precisamente los artefactos añadidos por `HEAD`; no elimina los PNG del sistema oficial.

La tarjeta ya no importa el registro paralelo ni busca por categoría. La resolución `guardians.find((guardian) => guardian.name === resource.guardianPrimary)` recupera el comportamiento previo para los recursos estáticos y dinámicos que declaran nombres canónicos, y conserva el fallback neutro cuando no hay coincidencia.

## Rediseño de la tarjeta y límites del motor de búsqueda

El cambio local en `library-explorer.tsx` se limita a la fuente y la clave de resolución del Guardián. Permanecen la envoltura visual, la categoría, la marca del proveedor, el cuerpo de la tarjeta, la CTA, la paginación, los filtros, `buildCatalogQuery` y la combinación de recursos estáticos y dinámicos. `globals.css` solo cambia el comentario que describía personajes exclusivos; las reglas visuales añadidas por fase 3 siguen intactas.

No hay cambios locales en `concientic/src/lib/catalog.ts`, `static-courses.ts`, las rutas API ni los seis cambios backend de fase 3. El resto del último commit permanece en la historia y el parche no revierte el motor de búsqueda.

## Referencias huérfanas y límites de validación

**confirmado** — La búsqueda en el código fuente no encuentra referencias a `library-guardians`, `libraryGuardians` ni `/library-guardians`; la tarjeta solo importa el registro canónico. `git diff --check` no reporta whitespace inválido. Según `guardians-verification.md`, `npm run lint`, `npm run typecheck` y `npm run build` terminaron correctamente, y el build no detectó referencias faltantes a los SVG eliminados.

**confirmado** — `clean test bootJar --no-daemon` terminó correctamente con 22 tests backend ejecutados, de modo que los cambios de fase 3 conservados no quedaron rotos por esta reversión selectiva.

**confirmado** — `npm run format:check` falló con 11 archivos fuera de formato. La evidencia identifica archivos ajenos a la tarea y `library-explorer.tsx`, cuyo formato ya pertenecía al rediseño existente de fase 3; no se aplicó un formateo global que pudiera sobrescribir trabajo local no relacionado.

</details>

<details>
<summary>File map</summary>

- `concientic/src/components/library/library-explorer.tsx` — restaura la resolución de tarjetas contra los Guardianes canónicos y conserva el rediseño de fase 3.
- `concientic/src/app/globals.css` — ajusta únicamente el comentario del bloque visual de la Biblioteca.
- `concientic/src/content/library-guardians.ts` — elimina el registro de ocho Guardianes visuales nuevos.
- `concientic/public/library-guardians/*.svg` — elimina los ocho assets visuales nuevos.
- `concientic/docs/catalog-library.md` — elimina la documentación del sistema paralelo.

El diff completo está disponible con `git -C C:\Jose\RepoSiginex\WebConcienTIC diff`.

</details>
