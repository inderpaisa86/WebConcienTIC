# Fase 3: historial, cambios, duplicados y reportes

## Qué se implementó

Cada observación persistida ahora:

1. Busca la última versión conocida del recurso.
2. Ejecuta el upsert y la verificación.
3. Construye un snapshot actual.
4. Compara el snapshot anterior con el actual usando `ChangeMonitor`.
5. Guarda una versión inmutable en `resource_versions`.
6. Guarda cada diferencia en `resource_changes`.
7. Busca duplicados por URL canónica o por proveedor y título normalizado.
8. Marca el duplicado con `duplicate_of` y estado `DUPLICATE` cuando corresponde.
9. Guarda un reporte JSON en `daily_reports`.

La primera observación de un recurso crea una versión base y no genera cambios. Las siguientes ejecuciones pueden producir eventos como `UPDATED`, `LINK_CHANGED`, `STATUS_CHANGED`, `PRICE_CHANGED`, `LANGUAGE_CHANGED`, `DURATION_CHANGED` o `FORMAT_CHANGED`.

## Migración

Flyway aplica:

```text
V5__catalog_history_reports.sql
```

Crea:

```text
resource_versions
daily_reports
```

Las tablas existentes `resource_changes` y `resources.duplicate_of` se reutilizan.

## Reporte de una ejecución

El reporte interno requiere el mismo token de ingesta:

```http
GET https://webconcientic.onrender.com/internal/runs/RUN_ID/report
Authorization: Bearer INGESTION_TOKEN
```

Ejemplo:

```http
GET https://webconcientic.onrender.com/internal/runs/72f8555e-eaaa-4161-a042-9804b4653f00/report
```

El JSON incluye:

- candidatos descubiertos y verificados;
- recursos persistidos;
- comprobaciones realizadas;
- revisiones creadas;
- cambios detectados;
- duplicados detectados;
- advertencias;
- estado de la ejecución.

## Consultas SQL de validación

Versiones:

```sql
SELECT
    rv.resource_id,
    r.title,
    rv.version_number,
    rv.ingestion_run_id,
    rv.access_status,
    rv.free_status,
    rv.snapshot_hash,
    rv.captured_at
FROM resource_versions rv
JOIN resources r ON r.id = rv.resource_id
ORDER BY rv.captured_at DESC;
```

Cambios:

```sql
SELECT
    rc.resource_id,
    r.title,
    rc.ingestion_run_id,
    rc.field_name,
    rc.previous_value,
    rc.current_value,
    rc.change_type,
    rc.detected_at
FROM resource_changes rc
JOIN resources r ON r.id = rc.resource_id
ORDER BY rc.detected_at DESC;
```

Duplicados:

```sql
SELECT
    duplicate.id,
    duplicate.title,
    duplicate.source_url,
    duplicate.status,
    winner.id AS winner_id,
    winner.title AS winner_title
FROM resources duplicate
JOIN resources winner ON winner.id = duplicate.duplicate_of
WHERE duplicate.status = 'DUPLICATE';
```

Reportes:

```sql
SELECT
    run_id,
    status,
    generated_at,
    report_json
FROM daily_reports
ORDER BY generated_at DESC;
```

## Criterios de deduplicación

La fase 3 usa dos reglas deterministas:

- misma `canonical_url`;
- mismo `provider` y título normalizado.

El título se normaliza eliminando acentos, espacios y signos. No se afirma todavía una similitud semántica avanzada; esa mejora queda para una fase posterior.

Los recursos con una decisión humana `APPROVED` o `CORRECTED` no se marcan automáticamente como duplicados.

## Límites actuales

- No se marcan recursos como `REMOVED` solo porque no aparezcan en una ejecución; un adaptador puede entregar un inventario parcial.
- Idioma, duración y formato todavía son campos limitados en la observación automática, por lo que algunos cambios aparecerán solo cuando exista evidencia de esos campos.
- El reporte se genera durante la persistencia y se consulta mediante un endpoint interno.
- La idempotencia de `Idempotency-Key` y el bloqueo de ejecuciones concurrentes quedan como siguiente endurecimiento operativo.
