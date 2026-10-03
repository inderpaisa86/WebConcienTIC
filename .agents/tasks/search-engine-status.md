# Estado actual del motor de búsqueda de ConcienTIC

## Resumen ejecutivo

El motor de búsqueda **sí tiene un MVP implementado y conectado parcialmente a la Biblioteca abierta**, pero no está completo respecto de la arquitectura y el contrato documentados.

- El endpoint público implementado es `GET /api/v1/resources`, en `backend/concientic-catalog/src/main/java/com/concientic/catalog/api/ResourceController.java`, método `search(...)`.
- Soporta búsqueda textual con PostgreSQL Full Text Search sobre un `tsvector` generado y filtros exactos por competencia, idioma, nivel, formato y proveedor, además de paginación.
- Ordena por `resources.overall_score` descendente y luego por fecha de verificación; **no calcula ni ordena por la relevancia textual de la consulta** (`ts_rank`).
- La base tiene índice GIN para el documento de búsqueda, pero no se encontró implementación de `pg_trgm`, coincidencia aproximada, facets ni un parámetro público de ordenamiento.
- La UI `/servicios/contenidos` consume un proxy Next.js, combina la respuesta dinámica con 30 recursos estáticos heredados y vuelve a filtrar/paginar en el navegador.
- La ingesta, verificación, deduplicación, revisión humana e historial están implementados alrededor del catálogo, pero la persistencia automática todavía no llena varios campos que el buscador y las tarjetas esperan, especialmente idiomas, nivel, duración, certificado, temas, audiencia y listas secundarias.
- No existen en el backend los endpoints documentados `GET /api/v1/resources/{id}` ni `GET /api/v1/resources/facets`. Tampoco hay pruebas de integración HTTP/SQL contra PostgreSQL que validen resultados reales; las pruebas existentes son principalmente unitarias y de inspección de SQL.

## 1. Componentes encontrados

### Backend de catálogo

Ruta raíz: `c:\Jose\RepoSiginex\WebConcienTIC\backend\concientic-catalog`.

- `src/main/java/com/concientic/catalog/api/ResourceController.java`: endpoint público de búsqueda de recursos.
- `src/main/java/com/concientic/catalog/catalog/CatalogService.java`: normaliza página y tamaño, limita `size` a 100, solicita conteo y resultados al repositorio.
- `src/main/java/com/concientic/catalog/catalog/ResourceRepository.java`: contrato de búsqueda y conteo.
- `src/main/java/com/concientic/catalog/catalog/JdbcResourceRepository.java`: implementación JDBC con SQL dinámico, filtros, ordenamiento, paginación y `RowMapper`.
- `src/main/java/com/concientic/catalog/domain/ResourceResponse.java`: contrato de cada elemento devuelto.
- `src/main/java/com/concientic/catalog/api/PageResponse.java`: envoltorio paginado `{items, page, size, total, totalPages}`.
- `src/main/resources/db/migration/V1__catalog_schema.sql`: tabla `resources`, tablas de relaciones e índice GIN del buscador.
- `src/main/java/com/concientic/catalog/ingestion/JdbcResourceObservationStore.java`: upsert de observaciones verificadas, clasificación primaria, deduplicación, revisión y scores iniciales.
- `src/main/java/com/concientic/catalog/ingestion/*`: descubrimiento, adaptación de fuentes, verificación HTTP/semántica, ciclo de investigación, persistencia e historial.
- `src/main/java/com/concientic/catalog/review/*` y `api/InternalReviewController.java`: cola y decisiones de revisión humana.

### Frontend y proxy

- `c:\Jose\RepoSiginex\WebConcienTIC\concientic/src/app/api/catalog/resources/route.ts`: proxy same-origin; reenvía solo los parámetros permitidos al backend.
- `c:\Jose\RepoSiginex\WebConcienTIC\concientic/src/app/servicios/contenidos/page.tsx`: página pública `/servicios/contenidos`.
- `c:\Jose\RepoSiginex\WebConcienTIC\concientic/src/components/library/library-explorer.tsx`: búsqueda, filtros, estados de carga/error, combinación con catálogo estático y paginación visual.
- `c:\Jose\RepoSiginex\WebConcienTIC\concientic/src/lib/catalog.ts`: tipos `CatalogResource`, `CatalogPage`, filtros y construcción de query.
- `c:\Jose\RepoSiginex\WebConcienTIC\concientic/src/content/static-courses.ts`: 30 recursos estáticos heredados que se combinan con los dinámicos.
- `c:\Jose\RepoSiginex\WebConcienTIC\concientic/docs/catalog-library.md`: contrato operativo de la integración frontend/backend.

## 2. Búsqueda, filtros y ordenamiento implementados

### Consulta pública

`GET /api/v1/resources` acepta estos parámetros en `ResourceController.search(...)`:

| Parámetro | Comportamiento observado |
|---|---|
| `q` | Si no está vacío, añade `r.search_document @@ plainto_tsquery('simple', :query)`. El documento contiene título, descripción corta y proveedor. |
| `competency` | Coincide con `r.primary_competency` o con una fila de `resource_competencies` del mismo recurso. |
| `language` | Coincidencia exacta de un valor contra `ANY(r.languages)`. |
| `level` | Igualdad exacta contra `r.level`. |
| `format` | Igualdad exacta contra `r.format`. |
| `provider` | Igualdad exacta contra `r.provider`. |
| `page` | Predeterminado `0`; valores negativos se convierten en `0`. |
| `size` | Predeterminado `24`; se normaliza entre `1` y `100`. |

La consulta siempre restringe el catálogo público a:

- `status IN ('ACTIVE', 'UPDATED', 'VERIFIED', 'LINK_CHANGED')`.
- `free_status IN ('FREE', 'FREE_CONTENT_PAID_CERTIFICATE', 'AUDIT_FREE', 'PARTIAL_FREE')`.

El tamaño máximo está definido como `MAX_PAGE_SIZE = 100` en `CatalogService`. El offset se calcula como `page * size`, sin una validación explícita contra overflow o páginas fuera de rango.

### Ordenamiento

`JdbcResourceRepository.search(...)` usa:

```sql
ORDER BY COALESCE(r.overall_score, 0) DESC,
         r.last_verified_at DESC NULLS LAST
LIMIT :limit OFFSET :offset
```

Esto proporciona una priorización por score persistido y recencia, pero no usa `ts_rank` ni un score derivado de la coincidencia textual. La consulta textual solo filtra si hay coincidencia.

`JdbcResourceObservationStore.upsertResource(...)` inicializa `verification_score` y `overall_score` con `1.0` o `0.0` según `verification.semanticMatch()`. No se encontró un cálculo separado de `relevance_score`, `quality_score` o `freshness_score` durante la ingesta.

### Índice y modelo de búsqueda

`V1__catalog_schema.sql` define en `resources`:

```sql
search_document TSVECTOR GENERATED ALWAYS AS (
    to_tsvector('simple', coalesce(title, '') || ' ' ||
                coalesce(short_description, '') || ' ' ||
                coalesce(provider, ''))
) STORED
```

y crea:

```sql
CREATE INDEX resources_search_document_idx
    ON resources USING GIN (search_document);
```

No se encontró migración, dependencia o consulta usando `pg_trgm`, `similarity`, `word_similarity` o un índice trigram. Tampoco existe un índice específico compuesto para todos los filtros públicos; sí existe `resources_status_score_idx` y los índices de las relaciones y comprobaciones.

## 3. Contratos de respuesta

### Respuesta de lista

`PageResponse<T>` devuelve:

```json
{
  "items": [],
  "page": 0,
  "size": 24,
  "total": 0,
  "totalPages": 0
}
```

Cada elemento de `items` sigue `ResourceResponse` e incluye, entre otros:

- identidad y URLs: `id`, `title`, `sourceUrl`, `canonicalUrl`, `verifiedUrl`;
- proveedor y descripción: `provider`, `providerType`, `shortDescription`;
- verificación: `urlStatus`, `httpStatus`, `lastVerifiedAt`, `sourceLastChecked`;
- gratuidad: `freeStatus`, `freeExplanation`;
- metadata: `language`, `level`, `duration`, `format`, `certificate`, `topics`, `audience`;
- clasificación: `primaryCompetency`, `secondaryCompetencies`, `guardianPrimary`, `guardianSecondary`;
- scores: `trustScore`, `relevanceScore`, `qualityScore`, `freshnessScore`, `verificationScore`, `overallScore`;
- trazabilidad: `evidence`, `reasonForInclusion`, `status`, `duplicateOf`, `notes`.

La configuración `spring.jackson.default-property-inclusion: non_null` omite propiedades nulas del JSON.

### Campos que el mapper no está llenando completamente

Aunque el record `ResourceResponse` declara más datos, `JdbcResourceRepository.resourceRowMapper()` actualmente devuelve:

- `secondaryCompetencies = List.of()`;
- `guardianSecondary = List.of()`;
- `evidence = List.of()`.

Además, la consulta hace `SELECT r.*`, por lo que las relaciones `resource_competencies` y `resource_guardians` no se cargan en esas listas. La competencia y el Guardián primarios sí se leen de las columnas directas de `resources`.

## 4. Endpoints encontrados

### Públicos

#### `GET /api/v1/resources`

Implementado en `ResourceController`. Es el endpoint de búsqueda y listado descrito arriba.

#### `GET /api/v1/catalog/status`

Implementado en `CatalogStatusController`. Devuelve `CatalogStatusResponse` con:

```json
{
  "catalogVersion": null,
  "lastSuccessfulRun": null,
  "publishedResources": 0,
  "lastVerifiedAt": null
}
```

El conteo se obtiene desde `CatalogService.countPublished()`, pero los otros tres valores se construyen como `null` en el controlador. Con Jackson, los valores nulos se omiten; en la práctica la respuesta contiene al menos `publishedResources`.

### Internos relacionados con ingesta y curaduría

No son endpoints de búsqueda pública, pero forman parte del motor y afectan qué resultados aparecen:

- `POST /internal/runs/daily`, `InternalRunController`: exige `Authorization: Bearer <INGESTION_TOKEN>` y ejecuta el ciclo diario.
- `GET /internal/runs/{runId}/report`, `InternalReportController`: exige el token de ingesta y devuelve el reporte JSON persistido; responde `404` si no existe.
- `GET /internal/reviews?status=OPEN&page=0&size=24`, `InternalReviewController`: exige `REVIEW_TOKEN` (o el token de ingesta por fallback) y pagina la cola de revisión.
- `GET /internal/reviews/{reviewId}`: detalle de revisión y comprobaciones.
- `POST /internal/reviews/{reviewId}/approve`.
- `POST /internal/reviews/{reviewId}/correct`.
- `POST /internal/reviews/{reviewId}/reject`.

### Endpoints documentados pero no encontrados en controladores

`docs/architecture.md` y `docs/arquitectura-backend-catalogo-cursos.md` prevén:

- `GET /api/v1/resources/{id}`;
- `GET /api/v1/resources/facets`;
- en una versión conceptual antigua, `GET /api/v1/courses`, `GET /api/v1/courses/{id}` y `GET /api/v1/competencies`.

El código actual no contiene esos mappings. Tampoco hay un endpoint público para solicitar `freeStatus` como filtro, aunque la política de publicación lo aplica internamente.

## 5. Repositorios, servicios y persistencia que sostienen el buscador

### Catálogo público

`CatalogService.search(...)` tiene dos sobrecargas: una para los cinco filtros originales y otra que añade `provider`. Cuando `provider` está vacío delega a la primera. Ambas hacen el conteo con los mismos filtros y luego recuperan la página.

`JdbcResourceRepository` genera el `WHERE`, usa parámetros nombrados y mantiene separada la consulta de conteo. La condición de competencia acepta tanto la columna primaria como la relación `resource_competencies`.

### Ingesta y publicación

`ResearchCycleService` ejecuta adaptadores y verificadores. `VerificationResult.isAutomaticallyPublishable()` exige acceso activo o redirigido, coincidencia semántica, ausencia de pago, título no vacío y ausencia de cuenta obligatoria.

`JdbcResourceObservationStore.persist(...)`:

1. calcula un identificador estable por URL semilla;
2. hace upsert de `resources`;
3. guarda cada comprobación en `resource_checks`;
4. guarda la clasificación primaria en `resource_competencies` y `resource_guardians`;
5. compara snapshots y guarda versiones/cambios;
6. detecta duplicados por URL canónica o proveedor+título normalizado;
7. crea elementos de revisión cuando corresponde;
8. genera un reporte diario.

La política pública se implementa en SQL mediante estados de recurso y gratuidad, no mediante una tabla o vista de snapshot publicada.

### Persistencia incompleta para búsqueda/tarjetas

El `INSERT INTO resources` de `JdbcResourceObservationStore.upsertResource(...)` escribe explícitamente título, descripción, proveedor, URLs, estados, formato `other`, competencia primaria, Guardián primario y scores de verificación. No escribe en ese upsert:

- `languages`;
- `level`;
- `duration`;
- `certificate`;
- `topics`;
- `audience`;
- `trust_level`/`trust_score`;
- `relevance_score`;
- `quality_score`;
- `freshness_score`.

Por tanto, aunque el endpoint acepta `language`, `level` y `format`, la ingesta automática actual no parece poblar de forma completa esos campos. El formato automático queda en `'other'`. Las tarjetas estáticas sí tienen esos metadatos, pero no equivalen a los recursos dinámicos de PostgreSQL.

## 6. Frontend y comportamiento efectivo de la Biblioteca

`route.ts` acepta y reenvía `q`, `competency`, `provider`, `language`, `level`, `format`, `page` y `size`. La UI no expone un selector de idioma y `CatalogFilters` tampoco contiene `language`.

`LibraryExplorer`:

1. llama al proxy con `page=0&size=100` para cada cambio de filtro, con un debounce de 220 ms;
2. mezcla la respuesta dinámica con `staticCourses`;
3. elimina recursos dinámicos cuya `sourceUrl` ya existe en los estáticos;
4. vuelve a aplicar localmente texto, competencia, proveedor, nivel y formato;
5. pagina la colección combinada a 12 tarjetas por página.

La búsqueda local de la UI solo concatena título, descripción, proveedor y competencia primaria. No incluye temas, audiencia, Guardián, URL ni competencias secundarias. Los recursos dinámicos pueden quedar limitados a los primeros 100 enviados por el backend antes de la paginación y filtrado del navegador.

La página muestra `verifiedUrl` o `sourceUrl` en una pestaña nueva y conserva los cursos estáticos si el backend falla. Esto es una decisión explícita documentada en `concientic/docs/catalog-library.md`, no un reemplazo completo del catálogo estático.

## 7. Documentación y decisiones existentes

### Documentación del backend

- `backend/concientic-catalog/README.md`: requisitos Java 25, Gradle Wrapper 9.x, PostgreSQL, ejecución `./gradlew bootRun` y despliegue en Render.
- `backend/concientic-catalog/docs/automatic-publication-phase-1.md`: estados publicables y filtros públicos por estado/gratuidad.
- `backend/concientic-catalog/docs/human-review.md`: contrato de revisión y consultas de la cola.
- `backend/concientic-catalog/docs/phase-3-history.md`: historial, cambios, duplicados y reportes; declara que la idempotencia de `Idempotency-Key` y el bloqueo de ejecuciones concurrentes quedan pendientes.
- `backend/concientic-catalog/src/main/resources/source-pages.json`: seis páginas semilla configuradas para el piloto.

### Arquitectura y requisitos

- `docs/architecture.md`: decide monolito modular Spring Boot/PostgreSQL, API de recursos y PostgreSQL FTS; prevé `pg_trgm`, detalle y facets.
- `docs/arquitectura-backend-catalogo-cursos.md`: describe el diseño inicial, filtros por gratuidad, relevancia textual, calidad, paginación y fases futuras. Su estado inicial dice “propuesta inicial; no implementada”, por lo que debe interpretarse como especificación histórica, no como inventario exacto del código actual.
- `docs/taxonomy.md`: ocho competencias oficiales y siete Guardianes; cada recurso puede tener secundarios.
- `docs/verification.md`: políticas de confianza, gratuidad, evidencia y estados.
- `concientic/docs/catalog-library.md`: integración del proxy, filtros, catálogo híbrido y comandos de validación.
- `tests/README.md`: escenarios esperados, incluyendo filtros exactos, duplicados, clasificación, historial, reintentos y fallas parciales.

## 8. Pruebas disponibles y comprobaciones ejecutadas

### Pruebas fuente encontradas

- `src/test/java/com/concientic/catalog/api/ResourceContractTest.java`: verifica la forma y metadata de `PageResponse`.
- `src/test/java/com/concientic/catalog/catalog/CatalogServiceTest.java`: verifica que el tamaño se limite a 100 y que page/totalPages sean consistentes.
- `src/test/java/com/concientic/catalog/catalog/JdbcResourceRepositoryTest.java`: captura el SQL y comprueba la presencia de FTS, competencia, idioma, nivel y formato.
- `src/test/java/com/concientic/catalog/ApplicationContextWiringTest.java`: verifica wiring sin conectar a una base real.
- Las demás pruebas cubren ingesta, verificación HTTP/semántica, adaptadores, cambios, ciclo diario y seguridad de URLs; no son pruebas de resultados de búsqueda contra PostgreSQL.
- No se encontraron pruebas frontend específicas para `LibraryExplorer`, el proxy de catálogo o la combinación de recursos estáticos/dinámicos.

### Comprobaciones ejecutadas en esta investigación

- En `c:\Jose\RepoSiginex\WebConcienTIC\backend\concientic-catalog`: `./gradlew.bat test --no-daemon` — **BUILD SUCCESSFUL**; 5 tareas quedaron actualizadas o al día.
- En `c:\Jose\RepoSiginex\WebConcienTIC\concientic`: `npm run typecheck` — **terminó correctamente**.
- `git -C c:\Jose\RepoSiginex\WebConcienTIC status --short` — árbol Git limpio al finalizar la inspección, aparte del informe que se solicitó crear después de esa comprobación.

No se ejecutó `bootRun`, no se reinició ningún servidor y no se hizo una prueba contra PostgreSQL/Render ni contra URLs externas. La prueba de Gradle valida el conjunto de tests existente, pero no demuestra el funcionamiento con datos reales de PostgreSQL.

## 9. Qué quedó incompleto o pendiente

### Pendientes funcionales del buscador

1. **Definir y cerrar el contrato público.** Decidir si la fuente canónica seguirá siendo `resources` y si se implementarán realmente detalle y facets. La documentación mezcla `/courses` histórico con `/resources` actual.
2. **Relevancia textual real.** Añadir un score de consulta (por ejemplo `ts_rank`) y combinarlo de manera explícita con calidad/verificación/recencia, o documentar que el orden actual es únicamente por score persistido. No asumir ranking de popularidad: la arquitectura prohíbe convertir la Biblioteca en ranking de engagement.
3. **Coincidencia aproximada.** `pg_trgm` está previsto en la arquitectura, pero no existe en migraciones ni código. Decidir si hace falta después de medir consultas reales; no incorporarlo por anticipación.
4. **Metadatos indexables.** Completar la extracción y persistencia de idioma, nivel, duración, formato, certificado, temas y audiencia antes de prometer esos filtros para recursos dinámicos.
5. **Relaciones de clasificación.** Mapear competencias y Guardianes secundarios, además de `evidence`, en `ResourceResponse`, o retirar esos campos del contrato si no forman parte del MVP.
6. **Filtros adicionales.** La especificación menciona gratuidad, pero el endpoint no acepta `freeStatus`/`free`; actualmente la política solo deja fuera estados no publicables. Definir si se requiere filtrar entre los cuatro estados publicables.
7. **Paginación consistente.** La UI solicita 100 elementos al backend y luego pagina 12 en cliente; esto limita el catálogo visible y hace que `total` del backend no represente el total efectivo de la colección híbrida. Definir paginación server-side para dinámicos, o un contrato de facets/consulta acorde.
8. **Datos de estado del catálogo.** `CatalogStatusController` devuelve nulos para versión, última ejecución exitosa y última verificación. Hace falta conectar esos valores con `ingestion_runs`/historial si la UI o la operación los necesitan.
9. **Idempotencia operativa.** El workflow `.github/workflows/catalog-daily.yml` envía `Idempotency-Key`, pero `InternalRunController` no lo recibe ni lo procesa. El propio documento de fase 3 lo declara como endurecimiento pendiente.
10. **Errores y fuentes parciales.** Debe verificarse con pruebas de integración que una fuente fallida conserve resultados válidos y que los estados de retiro/inaccesibilidad sigan la política; la implementación actual no prueba ese comportamiento por HTTP/DB real.

### Pendientes de calidad y pruebas

- Añadir pruebas de controlador para parámetros, autorización solo donde corresponda, respuestas JSON y límites de página.
- Añadir pruebas de repositorio con PostgreSQL/Testcontainers o una base de integración para validar `plainto_tsquery`, arrays, índices y conteos reales.
- Cubrir explícitamente proveedor, conteo con cada combinación, página fuera de rango, query vacía, acentos, duplicados y estados publicables.
- Añadir pruebas frontend para proxy, errores 502, merge sin duplicados y paginación/filtros de la colección híbrida.
- Documentar un ejemplo de respuesta real y un contrato versionado; hoy hay ejemplos conceptuales que no coinciden completamente con `ResourceResponse`.

## 10. Conclusión y recomendación de continuación

El trabajo anterior no quedó en cero: existe una base funcional de catálogo con FTS, filtros, paginación, publicación condicionada, ingesta verificable, historial y una integración frontend tolerante a fallos. La siguiente iteración debería tratarlo como **endurecimiento y cierre del MVP**, no como una reescritura del motor.

Orden recomendado:

1. fijar el contrato `/api/v1/resources` y decidir si detalle/facets entran en alcance;
2. poblar de forma confiable todos los campos que la UI filtra o muestra;
3. definir el algoritmo de ordenamiento con relevancia textual explícita y pruebas de aceptación;
4. corregir el mapeo de relaciones/evidencia y el estado del catálogo;
5. alinear la paginación frontend con el backend y resolver el límite de 100;
6. implementar idempotencia del disparo diario y pruebas de integración con PostgreSQL;
7. evaluar `pg_trgm` solo con evidencia de consultas que FTS no resuelva.

### Ambigüedades que deben resolverse

- “Motor de búsqueda” puede referirse al buscador SQL del backend o a la experiencia completa de Biblioteca, que hoy incluye 30 recursos estáticos; este informe cubre ambos.
- La documentación de arquitectura presenta varias rutas previstas y fases históricas que no son mappings actuales. Se tomó el código fuente como autoridad para el estado actual y la documentación como evidencia de intención/pedientes.
- No hay datos de una base PostgreSQL ni un despliegue consultado en esta investigación; por ello no se afirma cuántos recursos dinámicos están publicados en producción ni si las consultas funcionan allí con datos existentes.
- `overall_score` existe en el modelo, pero no hay evidencia de que represente relevancia textual; se recomienda no describirlo como ranking de búsqueda hasta definir su cálculo.
