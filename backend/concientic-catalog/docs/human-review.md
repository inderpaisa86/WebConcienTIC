# Revisión humana del catálogo

## Propósito

El módulo permite consultar recursos pendientes, ver la evidencia de las verificaciones y aprobar, corregir o rechazar un recurso. Las decisiones se guardan en `resource_human_decisions` y en `review_decision_audit`.

La ingesta posterior conserva una decisión humana existente y no reemplaza títulos, estados o modalidad de gratuidad aprobados, corregidos o rechazados.

## Despliegue

Flyway aplica automáticamente la migración `V4__human_review.sql` al iniciar el backend.

Configura en Render una variable opcional:

```text
REVIEW_TOKEN=un-token-distinto-y-seguro
```

Si `REVIEW_TOKEN` no existe, el backend usa temporalmente `INGESTION_TOKEN`. Para producción es preferible separar ambos tokens.

Después del deploy verifica que la migración se haya aplicado en PostgreSQL:

```sql
SELECT version, description, success
FROM flyway_schema_history
ORDER BY installed_rank;
```

Debe aparecer la versión `4` con descripción `human review` y `success = true`.

## Autenticación

Todas las rutas de revisión requieren:

```http
Authorization: Bearer REVIEW_TOKEN
```

No compartas el token en capturas, repositorios ni mensajes públicos.

## Listar pendientes

```http
GET https://webconcientic.onrender.com/internal/reviews?status=OPEN&page=0&size=24
Authorization: Bearer REVIEW_TOKEN
```

PowerShell:

```powershell
$headers = @{ Authorization = "Bearer $env:REVIEW_TOKEN" }
Invoke-RestMethod `
  -Method Get `
  -Uri "https://webconcientic.onrender.com/internal/reviews?status=OPEN&page=0&size=24" `
  -Headers $headers
```

La respuesta incluye `id`, `resourceId`, `title`, `candidateUrl`, `issue`, `evidence`, `agentDecision`, `status` y `version`.

Guarda el valor `id` del elemento que quieres revisar. También guarda `resourceId` y `version` para rastreo.

## Ver detalle

```http
GET https://webconcientic.onrender.com/internal/reviews/REVIEW_ID
Authorization: Bearer REVIEW_TOKEN
```

El detalle incluye el elemento de revisión y las comprobaciones HTTP asociadas al recurso.

## Aprobar

La aprobación exige una identificación del revisor y un `freeStatus` publicable. Si no envías `resourceStatus`, se usa `VERIFIED`.

```http
POST https://webconcientic.onrender.com/internal/reviews/REVIEW_ID/approve
Authorization: Bearer REVIEW_TOKEN
Content-Type: application/json

{
  "reviewerId": "patricia-amaya",
  "notes": "Revisado manualmente. El recurso es educativo y el acceso al contenido es gratuito.",
  "title": "Título confirmado del recurso",
  "description": "Descripción confirmada por revisión humana.",
  "freeStatus": "FREE",
  "resourceStatus": "VERIFIED"
}
```

Estados de gratuidad aceptados para publicación:

```text
FREE
FREE_CONTENT_PAID_CERTIFICATE
AUDIT_FREE
PARTIAL_FREE
```

Estados de recurso aceptados para publicación:

```text
ACTIVE
UPDATED
VERIFIED
LINK_CHANGED
```

Después de aprobar, la consulta pública debería poder incluir el recurso:

```http
GET https://webconcientic.onrender.com/api/v1/resources
```

## Corregir y aprobar

Usa `correct` cuando el título, descripción, estado de gratuidad o estado del recurso necesiten corrección.

```http
POST https://webconcientic.onrender.com/internal/reviews/REVIEW_ID/correct
Authorization: Bearer REVIEW_TOKEN
Content-Type: application/json

{
  "reviewerId": "patricia-amaya",
  "notes": "Se reemplazó el título provisional y se confirmó acceso gratuito al contenido.",
  "title": "Introduction to Cyber Security: Stay Safe Online",
  "description": "Curso introductorio sobre seguridad digital.",
  "freeStatus": "FREE_CONTENT_PAID_CERTIFICATE",
  "resourceStatus": "VERIFIED"
}
```

## Rechazar

El rechazo exige una justificación. El recurso queda con estado `REJECTED` y no aparece en el catálogo público.

```http
POST https://webconcientic.onrender.com/internal/reviews/REVIEW_ID/reject
Authorization: Bearer REVIEW_TOKEN
Content-Type: application/json

{
  "reviewerId": "patricia-amaya",
  "notes": "El proveedor bloquea el acceso automatizado y no se pudo confirmar la disponibilidad del contenido."
}
```

## Verificar en PostgreSQL

Pendientes abiertos:

```sql
SELECT
    rq.id,
    rq.resource_id,
    r.title,
    rq.candidate_url,
    rq.issue,
    rq.status,
    rq.version,
    rq.created_at
FROM review_queue rq
LEFT JOIN resources r ON r.id = rq.resource_id
WHERE rq.status = 'OPEN'
ORDER BY rq.created_at DESC;
```

Decisiones humanas:

```sql
SELECT
    resource_id,
    decision,
    reviewer_id,
    decision_notes,
    approved_title,
    approved_free_status,
    approved_resource_status,
    decided_at
FROM resource_human_decisions
ORDER BY decided_at DESC;
```

Auditoría:

```sql
SELECT
    action,
    reviewer_id,
    resource_id,
    decision_notes,
    created_at
FROM review_decision_audit
ORDER BY created_at DESC;
```

Conteo publicado:

```sql
SELECT COUNT(*) AS published_resources
FROM resources
WHERE status IN ('ACTIVE', 'UPDATED', 'VERIFIED')
  AND free_status NOT IN ('PAID', 'TRIAL');
```

## Respuestas de error esperadas

- `401`: falta el token o no coincide.
- `404`: no existe el elemento de revisión.
- `409`: el elemento ya fue resuelto o la versión está desactualizada.
- `422`: falta un campo obligatorio o el estado no es publicable.

## Secuencia recomendada para el piloto

1. Ejecutar la ingesta diaria.
2. Listar `OPEN` en `/internal/reviews`.
3. Verificar manualmente URL, título, gratuidad y evidencia.
4. Aprobar, corregir o rechazar cada elemento.
5. Consultar `/api/v1/resources`.
6. Ejecutar otra ingesta y comprobar que la decisión humana no se sobrescribe.
7. Revisar `review_decision_audit` si existe una discrepancia.

La revisión debe ser humana y documentada; no se deben aprobar automáticamente recursos que respondan `403`, tengan modalidad `UNKNOWN` o no tengan evidencia suficiente.
