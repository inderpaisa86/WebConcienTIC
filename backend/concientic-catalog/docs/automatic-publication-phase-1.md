# Primera fase: publicación automática

## Objetivo

La primera fase del motor publica automáticamente recursos que cumplen criterios mínimos verificables. No requiere aprobación humana para los casos que pasan todas las validaciones.

El módulo de revisión humana permanece disponible para una fase posterior y para diagnósticos, pero no bloquea los recursos que cumplen la política automática.

## Criterios de publicación automática

Un recurso puede publicarse automáticamente cuando:

- la solicitud HTTP fue exitosa;
- el contenido tiene señales semánticas de aprendizaje;
- se encontró un título válido;
- no se detectó pago obligatorio;
- no se requiere una cuenta para acceder;
- la URL no fue removida ni bloqueada;
- la modalidad se puede clasificar automáticamente como `FREE`.

El recurso se guarda normalmente con estado `VERIFIED` y `free_status = FREE`.

## Recursos que no se publican

Los siguientes casos se guardan en PostgreSQL, pero quedan fuera del catálogo público:

- HTTP `403`, `404`, `410` o errores `5xx`;
- respuestas temporalmente no disponibles;
- títulos ausentes, que quedan con título provisional y estado `REVIEW_REQUIRED`;
- contenido sin señales educativas suficientes;
- recursos que requieren pago;
- recursos que requieren cuenta cuando no se puede confirmar su modalidad;
- modalidad `UNKNOWN`;
- URLs bloqueadas o inseguras.

Por ejemplo, una respuesta `403` de OpenLearn puede generar una fila en `resources`, pero no será visible mediante `/api/v1/resources`.

## Estados de la ejecución

El endpoint diario devuelve:

```text
SUCCESS
```

cuando al menos un candidato cumplió los criterios automáticos.

```text
PERSISTED_NOT_PUBLISHED
```

cuando la ejecución funcionó y guardó resultados, pero ningún candidato cumplió los criterios de publicación.

```text
FAILED
```

cuando la ejecución no pudo completarse.

## Filtros públicos

El catálogo público solo incluye recursos con:

```text
status:
ACTIVE, UPDATED, VERIFIED, LINK_CHANGED
```

y con:

```text
free_status:
FREE, FREE_CONTENT_PAID_CERTIFICATE, AUDIT_FREE, PARTIAL_FREE
```

Los valores `UNKNOWN`, `PAID` y `TRIAL` no se muestran automáticamente.

## Verificación después del deploy

Ejecuta la ingesta:

```http
POST https://webconcientic.onrender.com/internal/runs/daily
Authorization: Bearer INGESTION_TOKEN
```

Consulta el catálogo:

```http
GET https://webconcientic.onrender.com/api/v1/resources
```

Consulta el estado:

```http
GET https://webconcientic.onrender.com/api/v1/catalog/status
```

Verifica en PostgreSQL:

```sql
SELECT
    status,
    free_status,
    COUNT(*) AS total
FROM resources
GROUP BY status, free_status
ORDER BY status, free_status;
```

Y el conteo realmente publicable:

```sql
SELECT COUNT(*) AS published_resources
FROM resources
WHERE status IN ('ACTIVE', 'UPDATED', 'VERIFIED', 'LINK_CHANGED')
  AND free_status IN ('FREE', 'FREE_CONTENT_PAID_CERTIFICATE', 'AUDIT_FREE', 'PARTIAL_FREE');
```

## Interpretación para OpenLearn

Si Render continúa recibiendo `403` de OpenLearn, el resultado esperado es:

```text
candidatesDiscovered > 0
candidatesVerified > 0
activeCandidates = 0
status = PERSISTED_NOT_PUBLISHED
```

Eso indica que el motor funciona y protege el catálogo, pero la fuente no permite verificación automática desde el servidor. Para publicar contenido se necesitará una fuente accesible, un adaptador permitido o una revisión humana posterior.

## Límite de esta fase

La inferencia de `FREE` es conservadora pero automática: se basa en que el contenido es accesible, no presenta señales de pago ni exige cuenta. No sustituye una validación contractual o legal de las condiciones de cada proveedor.

## Source Registry del piloto

El registro efectivo está en:

```text
backend/concientic-catalog/src/main/resources/source-pages.json
```

Cada entrada declara una página oficial concreta, no una página principal genérica. Incluye:

- `sourceName` y `provider`;
- `providerType`;
- `url`;
- `expectedTitle` como pista, nunca como evidencia verificada;
- `expectedLanguages` como expectativa, no como idioma confirmado;
- `freeStatusHint` como pista semilla;
- `competencyHint` y `guardianHint` validados contra los catálogos oficiales.

El adaptador `ConfiguredSourcePageAdapter` carga el registro y lo combina con `OpenLearnAdapter`. Las URLs configuradas no sustituyen la verificación: cada una pasa por el mismo verificador HTTP/semántico.

La ruta puede cambiarse mediante:

```text
SOURCE_PAGES=classpath:source-pages.json
```

Para usar un archivo externo compatible con el entorno de ejecución, configura una ubicación `file:` y conserva el mismo esquema JSON.

Las semillas actuales pertenecen a [Microsoft Learn](https://learn.microsoft.com/training/support/learn-content-types), [freeCodeCamp](https://www.freecodecamp.org/learn/scientific-computing-with-python), [Khan Academy](https://www.khanacademy.org/computing/computers-and-internet), [Google Applied Digital Skills](https://applieddigitalskills.withgoogle.com/c/middle-and-high-school/en/discover-ai-in-daily-life/overview.html) e [IBM SkillsBuild](https://skillsbuild.org/job-seekers). Se seleccionaron como páginas oficiales concretas para el piloto; su disponibilidad final debe confirmarse desde Render.

## Logs esperados

```text
Source discovery completed source=ConfiguredSourcePages candidates=6
```

Para una página accesible y verificable:

```text
Resource upserted ... resourceStatus=VERIFIED freeStatus=FREE publishable=true
```

Para una página bloqueada o incompleta:

```text
Resource upserted ... resourceStatus=TEMPORARILY_UNAVAILABLE publishable=false
```

El motor conserva el recurso y su comprobación, pero no lo muestra en el catálogo público.
