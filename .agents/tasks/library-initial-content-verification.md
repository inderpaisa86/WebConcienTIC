# Verificación de contenido inicial de la Biblioteca abierta

## Resultado

Se corrigió la carga inicial de `Biblioteca abierta` / `Selección curada` / `Recursos para comenzar`. El problema no estaba en la combinación de datos: `allResources` ya partía de `staticCourses`. La causa era que la rama de renderizado daba prioridad a `loading` y ocultaba esa colección durante el debounce y la consulta al backend, mostrando únicamente skeletons.

## Cambio realizado

Archivo funcional modificado:

- `concientic/src/components/library/library-explorer.tsx`

La colección visible ahora sigue este orden:

1. Si existen recursos filtrados visibles, se renderizan inmediatamente, aunque la consulta siga pendiente.
2. Si no hay recursos visibles y la consulta sigue pendiente, se muestran los skeletons.
3. El estado `Todavía no encontramos una coincidencia.` solo aparece cuando la consulta terminó y la colección filtrada está vacía.

No se modificaron `static-courses.ts`, `catalog.ts`, `page.tsx` ni la ruta API. Se conservó `mergeCatalogResources` sin cambios: los recursos estáticos son la base, aparecen primero, y los dinámicos se agregan después eliminando duplicados por URL normalizada. También se conservaron el debounce, el abortado de solicitudes, filtros, búsqueda, paginación, Guardianes oficiales, CTA y manejo de error.

## Revisión de flujo

- Estado inicial: `catalog` es `null`, pero `mergeCatalogResources(catalog?.items ?? [])` produce la colección de `staticCourses`; con la nueva prioridad, sus tarjetas se muestran antes de resolver `/api/catalog/resources`.
- Respuesta dinámica: al completar la consulta, `catalog.items` se fusiona después de los estáticos y se filtran duplicados por `normalizedUrl`.
- Backend lento, fallido o vacío: los estáticos permanecen en `allResources`; el error se comunica sin reemplazar las tarjetas y una respuesta vacía no produce una colección vacía mientras los estáticos satisfagan los filtros.
- No se hizo verificación visual en navegador ni captura de pantalla. La comprobación realizada fue revisión del flujo de código, diff, lint, typecheck y build.

## Validación ejecutada

Comandos ejecutados desde `c:\Jose\RepoSiginex\WebConcienTIC\concientic`:

- `npm run lint` — correcto, código de salida `0`.
- `npm run typecheck` — correcto, código de salida `0`.
- `npm run build` — correcto, código de salida `0`; Next.js 16.3.4 compiló, verificó TypeScript, generó 10 páginas estáticas y mostró `/servicios/contenidos` correctamente.

Comandos ejecutados desde `c:\Jose\RepoSiginex\WebConcienTIC`:

- `git diff --check` — correcto, código de salida `0`; Git mostró únicamente el aviso habitual de conversión LF/CRLF, sin errores de whitespace.
- Auditoría de `git diff --name-only -- concientic/src/content/guardians.ts` — `Official Guardianes unchanged`.
- Búsqueda de `library-guardians` en el código fuente — no se encontraron referencias en `concientic/src`; las coincidencias restantes pertenecen únicamente a documentos históricos de tareas de Guardianes.

No existía `.agents/tasks/library-review.json`, por lo que se trató como primera iteración. Los cambios locales no relacionados en `.agents/tasks/provider-logo-design*` y su JSON se conservaron sin editar. No se ejecutaron reset, revert, clean, commit ni push.
