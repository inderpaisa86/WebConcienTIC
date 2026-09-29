# Skills y artefactos del sistema

Las skills son capacidades y políticas reutilizables por los agentes. No son prompts libres sin contrato: cada skill define entradas, salidas, reglas, evidencia y pruebas.

| Skill | Entradas | Salidas principales |
|---|---|---|
| `source-discovery` | seeds, queries, idiomas, brechas | candidatos de fuente/recurso |
| `resource-verification` | URL, fuente, candidato | checks HTTP/semánticos, evidencia |
| `competency-mapping` | recurso, taxonomía | competencia primaria/secundarias, explicación |
| `guardian-mapping` | recurso, taxonomía Guardianes | Guardián primario/secundarios, confianza |
| `catalog-curation` | hechos, scores, política | decisión de publicación/revisión |
| `change-detection` | versión anterior/nueva | eventos y diffs |
| `daily-report` | ejecución y cambios | reporte Markdown/JSON |

## Artefactos obligatorios

- `source_registry`: fuentes seed, descubiertas, estado y evidencia.
- `resource_schema`: contrato formal del recurso.
- `competency_taxonomy`: ocho territorios y sus IDs.
- `guardian_mapping`: catálogo oficial y reglas aprobadas.
- `free_content_policy`: definición de gratuidad.
- `trust_policy`: niveles A–D y reglas de publicación.
- `url_verification_policy`: HTTP, redirecciones y semántica.
- `duplicate_detection_policy`: URL, proveedor, título y similitud.
- `quality_assessment_policy`: criterios y cálculo de scores.
- `research_query_library`: consultas en español, inglés, portugués y francés.
- `language_strategy`: detección, preferencia y campos multidioma.
- `catalog_rules`: estados, transiciones y publicación.
- `change_detection_rules`: campos comparables y eventos.
- `review_queue`: formato de incertidumbre.
- `change_log`: historial inmutable de cambios.
- `daily_report_template`: resumen de cada ejecución.
- `catalog_export`: snapshot versionado consumible por web.

## Implementación inicial

Las skills del MVP serán código Java determinista + configuración JSON. Si posteriormente se utiliza un modelo local, deberá implementarse detrás de la misma interfaz, registrar proveedor/modelo/versión y no saltarse las políticas de evidencia.
