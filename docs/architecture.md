# ConcienTIC Digital Learning Intelligence — Architecture Decision Record

**Estado:** diseño previo a implementación  
**Fecha:** 2026-09-29  
**Alcance:** sistema autónomo de investigación, curaduría y actualización de la Biblioteca abierta  
**Fuente funcional:** especificación “ConcienTIC Digital Learning Intelligence” y auditoría del frontend existente

## 1. Decisión ejecutiva

Construiremos un backend independiente dentro del monorepo actual, como **monolito modular multiagente**:

```text
WebConcienTIC/
├── concientic/                 # frontend Next.js existente
├── backend/                    # Spring Boot + Java 25
├── config/                     # taxonomías, Guardianes, fuentes y queries
├── data/                       # snapshots de catálogo e historial
├── agents/                     # contratos operativos de agentes
├── skills/                     # capacidades reutilizables
├── reports/                    # reportes diarios
├── tests/                      # escenarios de aceptación
└── docs/                       # arquitectura y políticas
```

El sistema no empezará con microservicios, Kafka, OpenSearch ni APIs de IA de pago. Los agentes serán módulos especializados, inicialmente deterministas y auditables, coordinados por un pipeline con estado persistente. Se podrá incorporar un modelo local o un proveedor futuro detrás de interfaces, pero ningún agente podrá inventar datos ni publicar sin evidencia.

## 2. Contexto existente

La Biblioteca actual está en `concientic/public/servicios/contenidos.html` y contiene 30 recursos en arrays posicionales, filtros locales y renderizado con `innerHTML`. La home enlaza a esta página desde `src/content/site.ts`.

La fuente canónica actual de identidad de Guardianes está en `concientic/src/content/guardians.ts` y contiene exactamente siete Guardianes, con sus roles, colores y assets oficiales. La UI utiliza exactamente ocho categorías:

```text
informacion, comunicacion, creacion, seguridad,
discernimiento, ia, bienestar, ciudadania
```

La nueva plataforma debe conservar esos nombres, identificadores, assets y filtros. La futura integración reemplazará el arreglo estático por datos tipados provenientes de la API; no generará cambios diarios en el HTML o JSX.

## 3. Decisiones y ADRs

### ADR-001 — Monolito modular antes que microservicios

**Decisión:** un único backend desplegable con módulos de API, catálogo, ingesta, agentes, revisión y reportes.

**Motivo:** el volumen inicial es pequeño, el equipo es reducido y Neon/Render Free no justifican infraestructura distribuida. Los límites de módulo permitirán separar workers más adelante si la ingesta bloquea la API.

**Consecuencia:** el código debe respetar contratos internos y evitar dependencias circulares entre agentes.

### ADR-002 — Java 25 + Spring Boot + Gradle Kotlin DSL

**Decisión:** usar Java 25, Spring Boot compatible con Java 25 y Gradle Kotlin DSL.

**Motivo:** stack solicitado, tipado fuerte, buen soporte HTTP/SQL, testabilidad y facilidad para construir servicios modulares. Se usará el Gradle Wrapper para reproducibilidad.

### ADR-003 — PostgreSQL en Neon como fuente de verdad

**Decisión:** Neon PostgreSQL almacena recursos, fuentes, ejecuciones, verificaciones, versiones, cambios, relaciones y cola de revisión.

**Motivo:** capa Free suficiente para el MVP y capacidades nativas de Full Text Search, índices GIN y `pg_trgm`.

**Consecuencia:** se debe respetar SSL, límites de almacenamiento/cómputo y políticas de backup. Los snapshots JSON serán exportaciones, no la fuente primaria en producción.

### ADR-004 — Render Free + GitHub Actions para la operación diaria

**Decisión:** Render expone la API y un endpoint interno de ejecución; GitHub Actions inicia la ejecución diaria mediante token.

**Motivo:** Render Free puede suspender el Web Service; un `@Scheduled` dentro del proceso no garantiza ejecución. GitHub Actions permite programar el disparo sin mantener un worker permanente.

**Consecuencia:** las ejecuciones deben ser reintentables, tener timeout y ser idempotentes. Si el proceso crece, se evaluará un cron/worker dedicado.

### ADR-005 — Agentes especializados como módulos, no como personajes autónomos

**Decisión:** los nombres de Guardianes son identidad de producto y clasificación; los agentes técnicos son módulos especializados con permisos limitados. No se mezclará la identidad visual de un Guardián con un runtime LLM.

**Motivo:** evita inventar capacidades, reduce coste y permite auditar cada decisión. `GuardianMappingAgent` puede asignar Guardianes, pero no puede cambiar sus nombres, roles, colores o assets.

### ADR-006 — Evidencia obligatoria y revisión humana por incertidumbre

**Decisión:** cualquier decisión de publicación requiere evidencia mínima; candidatos ambiguos pasan a `review_queue`.

**Motivo:** la confiabilidad del catálogo es más importante que su tamaño. Un HTTP 200, una marca reconocida o una descripción promocional no bastan por sí solos.

### ADR-007 — API de recursos como contrato de presentación

**Decisión:** el frontend consumirá `GET /api/v1/resources` y `GET /api/v1/catalog/status`. Las tarjetas se renderizan con datos, no con commits automáticos.

**Motivo:** separa contenido de presentación y permite actualizar el catálogo sin cambiar el diseño.

## 4. Arquitectura lógica

```text
GitHub Actions (schedule configurable)
        │ POST /internal/runs/daily
        ▼
Render — Spring Boot
        ├── RunOrchestrator
        ├── SourceDiscoveryAgent
        ├── CourseResearchAgent
        ├── TrustVerificationAgent
        ├── LinkVerificationAgent
        ├── FreeAccessVerificationAgent
        ├── CompetencyMappingAgent
        ├── GuardianMappingAgent
        ├── DuplicateDetectionAgent
        ├── QualityAssessmentAgent
        ├── CatalogCuratorAgent
        ├── ChangeMonitorAgent
        ├── CatalogUpdateAgent
        ├── DailyReportAgent
        └── HumanReviewQueueAgent
        │
        ▼
Neon PostgreSQL
        ├── fuentes y candidatos
        ├── recursos y versiones
        ├── verificaciones y cambios
        ├── competencias y Guardianes
        ├── ejecuciones y reportes
        └── cola de revisión
        │
        ▼
GET /api/v1/resources → Biblioteca abierta
```

## 5. Contrato común de agentes

Cada agente recibe un `AgentContext` con `run_id`, configuración, recurso/candidato, evidencias previas y límite de tiempo. Devuelve un `AgentResult`:

```json
{
  "agent": "LinkVerificationAgent",
  "run_id": "run-2026-09-29",
  "resource_id": "resource-123",
  "status": "SUCCESS",
  "confidence": 0.94,
  "facts": {},
  "evidence": [],
  "warnings": [],
  "next_action": "CONTINUE"
}
```

Estados de resultado:

```text
SUCCESS | PARTIAL | REVIEW_REQUIRED | REJECTED | FAILED
```

Ningún agente puede borrar definitivamente un recurso. Solo `CatalogCuratorAgent`, aplicando políticas y evidencia, puede proponer el estado; `CatalogUpdateAgent` persiste el resultado.

## 6. Pipeline de decisión

```text
DISCOVER
  ↓
FETCH
  ↓
EXTRACT
  ↓
VERIFY SOURCE
  ↓
VERIFY URL + SEMANTIC CONTENT
  ↓
VERIFY FREE ACCESS
  ↓
CLASSIFY COMPETENCIES
  ↓
MAP OFFICIAL GUARDIANS
  ↓
ASSESS QUALITY
  ↓
DETECT DUPLICATES
  ↓
COMPARE WITH PREVIOUS VERSION
  ↓
CURATE / REVIEW
  ↓
UPDATE DATABASE + SNAPSHOT
  ↓
GENERATE DAILY REPORT
```

Las etapas producen hechos y propuestas. La publicación ocurre solo después de validación, deduplicación y curaduría.

## 7. API prevista

```http
GET  /api/v1/resources
GET  /api/v1/resources/{id}
GET  /api/v1/resources/facets
GET  /api/v1/catalog/status
POST /internal/runs/daily
GET  /internal/runs/{runId}
GET  /internal/review-queue
```

Los endpoints internos requieren autenticación por token y no se exponen en la UI pública.

## 8. Seguridad y límites

- No guardar secretos en Git.
- No enviar contenido protegido completo a servicios externos.
- Usar `robots.txt`, términos de uso y límites de frecuencia.
- Permitir únicamente dominios configurados o candidatos en revisión.
- Validar URLs y evitar SSRF: bloquear localhost, redes privadas, metadata endpoints y esquemas no HTTP(S).
- Limitar tamaño de respuesta, redirecciones, tiempo y concurrencia por fuente.
- Sanitizar toda salida antes de renderizarla en el frontend.
- Registrar decisiones sin guardar tokens ni credenciales.
- Aplicar límites de costo y tiempo por ejecución.

## 9. No objetivos del MVP

- No crear un LMS.
- No crear cursos de la Academia ConcienTIC.
- No generar checkout, precios ni fechas de lanzamiento de Academia.
- No convertir la Biblioteca en ranking o infinite scroll.
- No maximizar número de recursos.
- No usar engagement como métrica principal.
- No incorporar automáticamente todas las organizaciones descubiertas.
- No usar agentes LLM externos de pago.
- No modificar automáticamente código visual del frontend.

## 10. Criterio de éxito arquitectónico

La arquitectura será aceptable cuando pueda ejecutar un ciclo diario reproducible que conserve evidencia, diferencie gratuidad, detecte cambios, mantenga historial, asocie únicamente Guardianes oficiales, produzca un reporte y exponga recursos publicados mediante API sin romper la presentación existente.
