# Catálogo de agentes

## Principios

Los agentes son módulos especializados del backend. No son personajes visuales ni identidades conversacionales. Los Guardianes son entidades de producto asignadas por `GuardianMappingAgent`; sus nombres y roles están cerrados en `config/guardians.json`.

Cada agente debe:

- recibir contexto tipado;
- devolver hechos, confianza, evidencia y siguiente acción;
- no inventar campos faltantes;
- usar `REVIEW_REQUIRED` ante incertidumbre;
- respetar timeout, límites de fuente y permisos;
- registrar `run_id`, duración y resultado.

## Agentes

| Agente | Responsabilidad | Puede escribir |
|---|---|---|
| `SourceDiscoveryAgent` | Encontrar fuentes y candidatos a partir de seeds, enlaces, feeds y consultas multilingües. | Candidatos de fuente/recurso, nunca publicación directa |
| `CourseResearchAgent` | Extraer metadatos verificables de un candidato. | Borrador de recurso y evidencias |
| `TrustVerificationAgent` | Evaluar autoridad, fuente primaria, legitimidad y nivel de confianza. | Evaluación de confianza |
| `LinkVerificationAgent` | Ejecutar HTTP y validación semántica, redirecciones y URL canónica. | `resource_checks`, estado URL |
| `FreeAccessVerificationAgent` | Determinar exactamente la modalidad de gratuidad. | `free_status`, `free_explanation`, evidencia |
| `CompetencyMappingAgent` | Asignar competencia primaria/secundarias entre los ocho IDs oficiales. | Relaciones y justificación |
| `GuardianMappingAgent` | Asignar `guardian_primary` y opcionales secundarios de los siete oficiales. | Relaciones, confianza y justificación |
| `DuplicateDetectionAgent` | Detectar URL, proveedor, título o equivalencia semántica duplicada. | `duplicate_of`, similitud |
| `QualityAssessmentAgent` | Evaluar relevancia, calidad, actualidad, utilidad y accesibilidad. | Scores documentados |
| `CatalogCuratorAgent` | Aplicar políticas y decidir publicar, mantener, actualizar, archivar o revisar. | Propuesta de estado |
| `ChangeMonitorAgent` | Comparar versión actual contra historial. | Eventos NEW/UPDATED/REMOVED/LINK_CHANGED/etc. |
| `CatalogUpdateAgent` | Persistir cambios y publicar snapshot válido. | Base de datos, exportaciones, versión catálogo |
| `DailyReportAgent` | Resumir la ejecución diaria. | Reporte Markdown/JSON |
| `HumanReviewQueueAgent` | Convertir incertidumbres y conflictos en tareas revisables. | `review_queue` |

## Orden de ejecución

```text
SourceDiscovery
  → CourseResearch
  → TrustVerification
  → LinkVerification
  → FreeAccessVerification
  → CompetencyMapping
  → GuardianMapping
  → DuplicateDetection
  → QualityAssessment
  → ChangeMonitor
  → CatalogCurator
  → HumanReviewQueue
  → CatalogUpdate
  → DailyReport
```

`SourceDiscoveryAgent` puede generar varios candidatos, pero la publicación necesita completar el pipeline. Las etapas independientes pueden paralelizarse por recurso después de la extracción, manteniendo límites por dominio.

## Permisos

- Discovery: crear candidatos.
- Research/verification: escribir hechos y evidencias.
- Mapping/quality: escribir propuestas y scores.
- Curator: proponer estado según política.
- Update: persistir únicamente resultados validados.
- Report: leer resultados; no alterar catálogo.
- Human review: modificar decisiones explícitas, nunca borrar historial.

## Manejo de errores

Un error de una fuente produce `PARTIAL_SUCCESS` para la ejecución si el resto puede continuar. Un agente no debe convertir un error de red aislado en `REMOVED`. La retirada requiere evidencia directa o confirmación según la política de verificación.
