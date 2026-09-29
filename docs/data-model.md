# Modelo de datos y contratos

## 1. Entidad principal: `Resource`

El dominio usa `resource`, no solo `course`, porque la Biblioteca puede publicar cursos, rutas, lecciones, bibliotecas, marcos, módulos y tutoriales.

### Campos

| Campo | Tipo | Obligatorio | Regla |
|---|---|---:|---|
| `id` | UUID/string estable | sí | Nunca cambia por una URL nueva |
| `title` | string | sí | Debe estar respaldado por evidencia |
| `short_description` | string | sí | No inventar ni copiar contenido extenso |
| `provider` | string | sí | Institución responsable/host |
| `provider_type` | enum/string | sí | government, university, company, platform, ngo, foundation, other |
| `country` | string/null | no | Null si no hay evidencia |
| `source_url` | URL | sí | URL descubierta |
| `canonical_url` | URL/null | no | Detectada/verificada |
| `verified_url` | URL/null | no | URL usada para acceso actual |
| `url_status` | enum | sí | ACTIVE, LINK_CHANGED, TEMPORARILY_UNAVAILABLE, REMOVED |
| `http_status` | integer/null | no | Resultado de última comprobación |
| `last_verified_at` | instant | sí | Última verificación técnica/semántica |
| `first_discovered_at` | instant | sí | Primera detección |
| `last_updated_at` | instant | sí | Último cambio confirmado |
| `free_status` | enum | sí | Política de gratuidad |
| `free_explanation` | string | sí | Explica alcance y límites |
| `language` | array<string> | sí | Idiomas confirmados |
| `level` | string/null | no | Null si no se puede confirmar |
| `duration` | string/null | no | Null si no se puede confirmar |
| `format` | string | sí | course, path, lesson, library, framework, module, tutorial, other |
| `certificate` | string/null | no | Nunca inventar |
| `primary_competency` | competency ID | sí | Una de las ocho |
| `secondary_competencies` | array<competency ID> | sí | Puede estar vacío |
| `guardian_primary` | Guardian name | sí | Uno de los siete oficiales |
| `guardian_secondary` | array<Guardian name> | sí | Puede estar vacío |
| `topics` | array<string> | sí | Solo términos sustentados |
| `audience` | array<string> | sí | Solo si se identifica |
| `trust_level` | A/B/C/D | sí | Política de confianza |
| scores | number 0..1 | sí | Trust, relevance, quality, freshness, verification, overall |
| `evidence` | array<Evidence> | sí | Evidencia mínima por decisión |
| `reason_for_inclusion` | string | sí | Justificación auditable |
| `status` | enum | sí | Estados de ciclo de vida |
| `duplicate_of` | resource ID/null | sí | Referencia si es duplicado |
| `source_last_checked` | instant | sí | Última revisión de fuente |
| `notes` | string | sí | Notas internas, no necesariamente públicas |

## 2. JSON Schema base

La implementación debe validar el contrato con JSON Schema o equivalente Java (`jakarta.validation` + schema exportado). Los valores de `guardian_primary` y `guardian_secondary` deben provenir de `config/guardians.json`.

```json
{
  "$schema": "https://json-schema.org/draft/2020-12/schema",
  "$id": "https://concientic.com/schemas/resource.schema.json",
  "type": "object",
  "required": ["id", "title", "short_description", "provider", "source_url", "free_status", "language", "format", "primary_competency", "guardian_primary", "trust_level", "status", "evidence"],
  "properties": {
    "id": {"type": "string", "minLength": 1},
    "title": {"type": "string", "minLength": 3},
    "short_description": {"type": "string", "minLength": 10},
    "provider": {"type": "string", "minLength": 1},
    "provider_type": {"type": "string"},
    "country": {"type": ["string", "null"]},
    "source_url": {"type": "string", "format": "uri", "pattern": "^https?://"},
    "canonical_url": {"type": ["string", "null"], "format": "uri"},
    "verified_url": {"type": ["string", "null"], "format": "uri"},
    "url_status": {"enum": ["ACTIVE", "LINK_CHANGED", "TEMPORARILY_UNAVAILABLE", "REMOVED"]},
    "http_status": {"type": ["integer", "null"], "minimum": 100, "maximum": 599},
    "free_status": {"enum": ["FREE", "FREE_CONTENT_PAID_CERTIFICATE", "AUDIT_FREE", "PARTIAL_FREE", "SCHOLARSHIP", "TRIAL", "PAID", "UNKNOWN"]},
    "free_explanation": {"type": "string", "minLength": 5},
    "language": {"type": "array", "items": {"type": "string"}, "minItems": 1},
    "format": {"type": "string"},
    "primary_competency": {"enum": ["informacion", "comunicacion", "creacion", "seguridad", "discernimiento", "ia", "bienestar", "ciudadania"]},
    "secondary_competencies": {"type": "array", "uniqueItems": true, "items": {"$ref": "#/$defs/competency"}},
    "guardian_primary": {"type": "string", "minLength": 1},
    "guardian_secondary": {"type": "array", "uniqueItems": true, "items": {"type": "string"}},
    "trust_level": {"enum": ["A", "B", "C", "D"]},
    "trust_score": {"type": "number", "minimum": 0, "maximum": 1},
    "relevance_score": {"type": "number", "minimum": 0, "maximum": 1},
    "quality_score": {"type": "number", "minimum": 0, "maximum": 1},
    "freshness_score": {"type": "number", "minimum": 0, "maximum": 1},
    "verification_score": {"type": "number", "minimum": 0, "maximum": 1},
    "overall_score": {"type": "number", "minimum": 0, "maximum": 1},
    "evidence": {"type": "array", "minItems": 1, "items": {"$ref": "#/$defs/evidence"}},
    "status": {"enum": ["DISCOVERED", "UNDER_REVIEW", "VERIFIED", "ACTIVE", "UPDATED", "LINK_CHANGED", "TEMPORARILY_UNAVAILABLE", "REMOVED", "PAYMENT_REQUIRED", "DUPLICATE", "REJECTED", "REVIEW_REQUIRED"]}
  },
  "$defs": {
    "competency": {"enum": ["informacion", "comunicacion", "creacion", "seguridad", "discernimiento", "ia", "bienestar", "ciudadania"]},
    "evidence": {
      "type": "object",
      "required": ["kind", "url", "observed_at", "summary"],
      "properties": {
        "kind": {"type": "string"},
        "url": {"type": "string", "format": "uri"},
        "observed_at": {"type": "string", "format": "date-time"},
        "summary": {"type": "string"},
        "hash": {"type": ["string", "null"]}
      }
    }
  }
}
```

## 3. Tablas PostgreSQL

- `sources`
- `source_discovery_candidates`
- `resources`
- `resource_competencies`
- `resource_guardians`
- `resource_checks`
- `resource_versions`
- `resource_changes`
- `ingestion_runs`
- `agent_runs`
- `evidence`
- `review_queue`
- `daily_reports`
- `catalog_snapshots`

Los cambios históricos son append-only. Los datos publicados se derivan de recursos `ACTIVE` con evidencia vigente y `free_status` publicable.
