# Automatización, pipeline y scheduler

## 1. Ciclo diario

El ciclo debe ser automático, diario, verificable y acumulativo. La zona horaria se configura con `TIMEZONE`; nunca se hardcodea.

Horario inicial configurable:

```text
00:00  discovery/research
00:30  link + semantic verification
01:00  free access + classification
01:30  change detection + curation
02:00  catalog snapshot/export
02:15  daily report
```

El horario es una propuesta; GitHub Actions puede iniciar una ejecución que tarde más o menos. El sistema debe usar `run_id`, locks y estados para evitar ejecuciones concurrentes.

## 2. Disparador gratuito del MVP

GitHub Actions ejecuta un workflow diario y llama:

```http
POST /internal/runs/daily
Authorization: Bearer $INGESTION_TOKEN
Idempotency-Key: $RUN_DATE-$TIMEZONE
```

Render Free puede suspenderse por inactividad, por eso no se confía en `@Scheduled` dentro del Web Service. La ejecución debe ser corta, reintentable y con límites por fuente.

## 3. Pipeline

```text
DISCOVER
→ FETCH
→ EXTRACT
→ VERIFY SOURCE
→ VERIFY URL
→ VERIFY FREE ACCESS
→ CLASSIFY
→ MAP GUARDIAN
→ QUALITY CHECK
→ DEDUPLICATE
→ COMPARE
→ CURATE
→ UPDATE DATA
→ GENERATE REPORT
```

Cada etapa escribe resultados de trabajo asociados a `run_id`; la publicación se realiza solo al final de una validación consistente.

## 4. Idempotencia y fallos

- Un mismo candidato no crea dos recursos.
- Reintentos no duplican verificaciones lógicas ni cambios.
- Una fuente fallida no elimina sus recursos válidos.
- Se conserva el último snapshot publicable.
- Fallos parciales producen `PARTIAL_SUCCESS`.
- Lock de ejecución evita dos ciclos diarios simultáneos.
- Cada fuente tiene timeout, límite de páginas y rate limit.
- Los agentes pueden continuar con otros recursos si una fuente falla.

## 5. Reporte diario

El reporte debe incluir:

- `run_id`, inicio, fin, zona horaria y estado;
- fuentes investigadas y nuevas candidatas;
- recursos nuevos, verificados, actualizados y retirados;
- enlaces corregidos;
- cambios de idioma, duración, modalidad o gratuidad;
- duplicados;
- recursos en revisión;
- errores y advertencias;
- cantidad final publicada;
- versión del snapshot.

Se generan JSON para máquinas y Markdown para lectura humana en `reports/daily/`.

## 6. Recuperación

El endpoint interno permite ejecución manual con un `run_id` distinto. El workflow debe conservar logs. Para reanudar, el orquestador consulta estados persistidos y evita repetir etapas exitosas cuando sea seguro.
