# Plan selectivo: Guardianes del último commit

## Alcance y evidencia

- Repositorio inspeccionado: `c:\Jose\RepoSiginex\WebConcienTIC`.
- Estado inicial: rama `main`, alineada con `origin/main`, con `.agents/` sin rastrear. Ese trabajo local debe conservarse; no usar `git reset`, `git checkout` global, `git revert`, `git clean` ni crear commits/push.
- Último commit: `906e802 (fase 3)`; padre: `590cb01 (fase 2.1)`.
- `git diff --name-status --find-renames HEAD^ HEAD` demuestra que los cambios relacionados con Guardianes están únicamente en:
  - `concientic/src/components/library/library-explorer.tsx` — sustituye la importación y resolución de los Guardianes oficiales por `libraryGuardians` y cambia la tarjeta para mostrar el nuevo personaje visual.
  - `concientic/src/content/library-guardians.ts` — registro nuevo de ocho personajes visuales.
  - `concientic/public/library-guardians/{informacion,discernimiento,seguridad,ia-innovacion,creacion,comunicacion,ciudadania,bienestar}.svg` — ocho assets nuevos, uno por personaje/categoría.
  - `concientic/src/app/globals.css` — bloque añadido desde la línea 1667 (`/* Guardianes visuales exclusivos... */`) para la nueva composición visual de las tarjetas.
  - `concientic/docs/catalog-library.md` — sección añadida desde la línea 169 (`## Guardianes visuales de la Biblioteca`).
- No hubo cambios de Guardianes en backend Java, migraciones/seeds, configuración oficial, tests, `README.md`, `config/guardians.json`, `concientic/src/content/guardians.ts` ni `concientic/src/content/static-courses.ts`. Los demás cambios de `fase 3` deben quedar intactos.

## Listas exactas

### Antes de `906e802` (`HEAD^`)

Los siete Guardianes oficiales definidos en `config/guardians.json` y `concientic/src/content/guardians.ts` eran:

1. **Emi** — Empatía y bienestar digital.
2. **Locky** — Seguridad y protección.
3. **Lex** — Pensamiento crítico y ética.
4. **Byte** — Aprendizaje e inteligencia digital.
5. **Detective DQ** — Investigación y verificación.
6. **Nexo** — Colaboración y comunidad.
7. **Nova** — Innovación y transformación.

### Después de `906e802` (`HEAD`)

Los siete oficiales anteriores permanecen sin modificaciones. Además, el commit añadió ocho Guardianes visuales exclusivos de la Biblioteca, registrados por categoría en `library-guardians.ts`:

1. **Robot de Información** — `informacion` / `informacion.svg`.
2. **Detective de Discernimiento** — `discernimiento` / `discernimiento.svg`.
3. **Gato protector de Seguridad** — `seguridad` / `seguridad.svg`.
4. **Exploradora de IA e Innovación** — `ia` / `ia-innovacion.svg`.
5. **Creadora Digital** — `creacion` / `creacion.svg`.
6. **Guardián de Comunicación** — `comunicacion` / `comunicacion.svg`.
7. **Guardián de Ciudadanía** — `ciudadania` / `ciudadania.svg`.
8. **Guardián de Bienestar** — `bienestar` / `bienestar.svg`.

**Resultado requerido:** conservar exactamente los siete Guardianes oficiales y eliminar los ocho personajes visuales nuevos. La tarjeta puede conservar el rediseño visual de `fase 3`, pero debe resolver el personaje desde `resource.guardianPrimary` contra `guardians`, como hacía `HEAD^`.

# Implementation Plan

- [ ] 1. Cambiar únicamente la fuente de Guardianes de la tarjeta de Biblioteca en `concientic/src/components/library/library-explorer.tsx`: restaurar la importación de `guardians` desde `@/content/guardians` y hacer que `guardianFor` compare `guardian.name === resource.guardianPrimary`. Mantener intactos el rediseño de la tarjeta introducido en `906e802` (envoltura `library-card__visual`, categoría, marca del proveedor, tamaños actuales, `library-card__body`, CTA y todas las consultas/filtros del catálogo); no restaurar el archivo completo desde `HEAD^`.
      Files: `c:\Jose\RepoSiginex\WebConcienTIC\concientic\src\components\library\library-explorer.tsx` (líneas 5-7 y 30-31 del `HEAD` actual).
      Verify: desde `c:\Jose\RepoSiginex\WebConcienTIC\concientic`, ejecutar `npm run typecheck` y `npm run lint`; ambos deben terminar correctamente y la importación de `library-guardians` ya no debe ser necesaria para compilar.

- [ ] 2. Eliminar las definiciones y assets de los ocho Guardianes visuales añadidos, después de que el componente deje de referenciarlos. Borrar `concientic/src/content/library-guardians.ts` y los ocho SVG de `concientic/public/library-guardians/`; no borrar ni modificar los siete PNG de `concientic/public/guardians/`. En `concientic/src/app/globals.css`, conservar las reglas visuales del rediseño de tarjetas porque siguen siendo utilizables con los Guardianes oficiales y cambiar solamente el comentario de la línea 1667 para que no afirme que son personajes exclusivos nuevos; no eliminar las reglas de layout, color, provider mark, imagen o CTA introducidas por `fase 3`.
      Files: `c:\Jose\RepoSiginex\WebConcienTIC\concientic\src\content\library-guardians.ts`; `c:\Jose\RepoSiginex\WebConcienTIC\concientic\public\library-guardians\bienestar.svg`; `ciudadania.svg`; `comunicacion.svg`; `creacion.svg`; `discernimiento.svg`; `ia-innovacion.svg`; `informacion.svg`; `seguridad.svg`; `c:\Jose\RepoSiginex\WebConcienTIC\concientic\src\app\globals.css` (bloque desde la línea 1667, modificando solo su comentario).
      Verify: desde `c:\Jose\RepoSiginex\WebConcienTIC\concientic`, ejecutar `npm run typecheck`, `npm run lint` y `npm run build`; deben pasar y el build no debe reportar referencias faltantes a `/library-guardians/*.svg`.

- [ ] 3. Retirar únicamente la documentación del registro visual nuevo y conservar el resto de la documentación de la Biblioteca. Eliminar en `concientic/docs/catalog-library.md` la sección `## Guardianes visuales de la Biblioteca` y su contenido añadido en las líneas 169-199, incluyendo las referencias a `src/content/library-guardians.ts`, `public/library-guardians/` y las ocho categorías; conservar la mención general de la tarjeta con Guardián y toda la documentación de catálogo híbrido, filtros, proxy, diagnóstico y validación.
      Files: `c:\Jose\RepoSiginex\WebConcienTIC\concientic\docs\catalog-library.md` (solo la sección añadida por `906e802`).
      Verify: desde `c:\Jose\RepoSiginex\WebConcienTIC\concientic`, ejecutar `npm run format:check`; debe finalizar sin errores de formato. Confirmar también que `config/guardians.json`, `concientic/src/content/guardians.ts` y `concientic/src/content/static-courses.ts` siguen sin modificaciones mediante la revisión selectiva del diff, sin revertir otros archivos.

- [ ] 4. Ejecutar la validación final completa y auditar que el cambio sea selectivo. En el frontend ejecutar, sin iniciar `npm run dev` ni ningún modo watch, `npm run format:check`, `npm run lint`, `npm run typecheck` y `npm run build` desde `c:\Jose\RepoSiginex\WebConcienTIC\concientic`. En el backend ejecutar `.\gradlew.bat clean test bootJar --no-daemon` desde `c:\Jose\RepoSiginex\WebConcienTIC\backend\concientic-catalog` (el comando documentado equivalente es `./gradlew clean test bootJar --no-daemon`; en Windows usar el wrapper `.bat`).
      Files: validación de `c:\Jose\RepoSiginex\WebConcienTIC\concientic` y `c:\Jose\RepoSiginex\WebConcienTIC\backend\concientic-catalog`; no se deben editar los seis archivos backend de `fase 3` (`phase-3-history.md`, `InternalReportController.java`, `CatalogHistoryRepository.java`, `JdbcResourceObservationStore.java`, `ResourceObservationStore.java`, `V5__catalog_history_reports.sql`).
      Verify: todos los comandos anteriores pasan; `git diff --check` no reporta errores; la revisión final del diff confirma que solo se eliminan los ocho SVG, el registro y la sección documental nuevos, se cambia la resolución de la tarjeta al conjunto oficial y se conserva el resto de `906e802`. El estado debe seguir mostrando el `.agents/` local sin rastrear y no debe haber commit ni push.

## Scripts y restricciones de validación descubiertos

- Frontend (`c:\Jose\RepoSiginex\WebConcienTIC\concientic\package.json`): `npm run format:check`, `npm run lint`, `npm run typecheck`, `npm run build`. No existe un script frontend dedicado de tests en `package.json`; no arrancar `npm run dev`, porque la tarea requiere validación sin servidores/watch.
- Backend (`c:\Jose\RepoSiginex\WebConcienTIC\backend\concientic-catalog\build.gradle.kts` y wrappers `gradlew`/`gradlew.bat`): `gradlew.bat clean test bootJar --no-daemon` desde el directorio del backend. `test` usa JUnit Platform y cubre las pruebas Java existentes; `bootJar` comprueba el empaquetado Spring Boot.
- La documentación `concientic/docs/catalog-library.md` registra los mismos comandos de frontend y el comando Gradle `./gradlew clean test bootJar --no-daemon`; se adapta únicamente la sintaxis del wrapper a Windows.
- No crear commits ni hacer push al terminar.
