# Arquitectura del backend del catálogo de cursos

**Proyecto:** ConcienTIC  
**Estado:** propuesta inicial; no implementada  
**Fecha:** 2026-09-29  
**Alcance:** backend futuro para la sección “Recursos gratuitos para desarrollar tus capacidades digitales”

## 1. Objetivo

Construir un backend que recopile, normalice, filtre, clasifique y publique cursos y recursos digitales procedentes de fuentes externas confiables. El frontend de ConcienTIC consultará el catálogo mediante una API, en lugar de mantener los cursos directamente en el HTML o JavaScript de la página.

El sistema debe priorizar:

- Fuentes públicas y confiables.
- Cursos gratuitos o claramente identificados como gratuitos.
- Información en español o multidioma.
- Clasificación por competencias digitales.
- Trazabilidad hacia la fuente original.
- Coste cero durante el MVP, usando software open source y capas gratuitas de infraestructura.
- Posibilidad de crecer sin tener que rediseñar todo el backend.

## 2. Decisiones principales

| Área | Decisión inicial |
|---|---|
| Repositorio | Monorepo actual `WebConcienTIC` |
| Frontend | Next.js existente en `concientic/` |
| Backend | Java 25 + Spring Boot |
| Build | Gradle Kotlin DSL |
| Base de datos | PostgreSQL administrado en Neon Free |
| Hosting backend | Render Free como Web Service |
| Migraciones | Flyway Community, sin funciones comerciales |
| Búsqueda | PostgreSQL Full Text Search + `pg_trgm` |
| Ingesta | Adaptadores por fuente, ejecutados de forma programada |
| Programación gratuita | GitHub Actions llamando a un endpoint interno protegido |
| Buscador externo | No inicialmente; evaluar OpenSearch solo cuando exista una necesidad real |
| Servicios de pago | No en el MVP |
| Arquitectura | Monolito modular, no microservicios |

## 3. Arquitectura general

```text
┌────────────────────────────┐
│ Frontend Next.js           │
│ concientic/                │
│                            │
│ GET /api/v1/courses        │
└──────────────┬─────────────┘
               │ HTTPS
               ▼
┌────────────────────────────┐
│ Render Free                │
│ Spring Boot + Java 25     │
│                            │
│ API pública                │
│ Catálogo y filtros         │
│ Búsqueda                   │
│ Ingesta protegida          │
└──────────────┬─────────────┘
               │ PostgreSQL SSL
               ▼
┌────────────────────────────┐
│ Neon PostgreSQL Free       │
│                            │
│ Cursos                     │
│ Fuentes                    │
│ Competencias               │
│ Ejecuciones de ingesta     │
│ Índices de búsqueda        │
└────────────────────────────┘

┌────────────────────────────┐
│ GitHub Actions             │
│                            │
│ Ejecución diaria/semanal   │
│ POST /internal/ingestion/  │
│ run                        │
└──────────────┬─────────────┘
               │ token secreto
               └──────────────► Render
```

El backend no debe consultar páginas externas durante cada búsqueda del usuario. La ingesta será un proceso separado y programado:

```text
Fuente externa
    ↓
Adaptador de fuente
    ↓
Extracción de metadatos
    ↓
Normalización
    ↓
Validación de criterios
    ↓
Deduplicación
    ↓
Clasificación
    ↓
Revisión/publicación
    ↓
PostgreSQL
    ↓
API pública
```

## 4. Estructura prevista del monorepo

```text
WebConcienTIC/
├── .git/
├── concientic/                         # Frontend Next.js actual
├── backend/                            # Backend futuro
│   └── concientic-catalog/
│       ├── build.gradle.kts
│       ├── settings.gradle.kts
│       ├── gradlew
│       ├── gradlew.bat
│       └── src/
├── .github/
│   └── workflows/
│       └── ingestion.yml               # Cuando se implemente
├── docs/
│   └── arquitectura-backend-catalogo-cursos.md
└── README.md
```

No se debe crear un repositorio Git anidado dentro de `backend/`. Si en el futuro se necesita separar el backend, se podrá migrar a un repositorio independiente cuando existan razones operativas claras: equipos distintos, ciclos de despliegue independientes, permisos separados o reutilización del backend por otros productos.

## 5. Módulos del backend

El backend será un monolito modular con límites claros:

```text
com.concientic.catalog
├── api/
│   ├── CourseController
│   ├── CourseResponse
│   └── ApiExceptionHandler
├── catalog/
│   ├── Course
│   ├── CourseService
│   ├── CourseRepository
│   └── CourseSearchService
├── competency/
│   ├── Competency
│   └── CompetencyRepository
├── source/
│   ├── Source
│   ├── SourceService
│   └── SourceRepository
├── ingestion/
│   ├── SourceAdapter
│   ├── IngestionService
│   ├── IngestionRun
│   ├── CandidateNormalizer
│   ├── CourseDeduplicator
│   └── adapters/
│       ├── GoogleAppliedSkillsAdapter
│       ├── MicrosoftLearnAdapter
│       ├── MinTicAdapter
│       └── OpenLearnAdapter
├── admin/
│   └── IngestionAdminController
└── shared/
    ├── configuration/
    ├── errors/
    └── security/
```

### Principios de los módulos

- La API no debe contener lógica de scraping.
- Cada fuente externa debe tener un adaptador independiente.
- La búsqueda debe operar sobre datos normalizados, no sobre HTML crudo.
- La clasificación debe ser auditable.
- Las operaciones internas de ingesta deben estar separadas de los endpoints públicos.
- Las reglas de selección no deben depender de valores ocultos dentro de los controladores.

## 6. Fuentes externas e ingesta

Se utilizará el siguiente orden de preferencia:

1. API oficial de la fuente.
2. RSS o sitemap oficial.
3. Página HTML pública.
4. Scraping específico y limitado como último recurso.

No se implementará inicialmente un crawler universal. Cada fuente tendrá un adaptador que conozca su formato, sus reglas y sus límites.

### Contrato conceptual del adaptador

```text
SourceAdapter
├── supports(source)
├── fetch(source)
├── parse(document)
└── mapToCandidate(item)
```

Todos los adaptadores deberán devolver un modelo común, por ejemplo:

```text
CourseCandidate
├── externalId
├── title
├── description
├── provider
├── canonicalUrl
├── language
├── level
├── format
├── duration
├── isFree
├── certificateAvailable
├── skills
├── sourceUpdatedAt
└── detectedAt
```

### Criterios de ingesta

Un curso podrá publicarse cuando cumpla los criterios definidos para el catálogo:

- La fuente sea una institución confiable.
- La URL sea pública y válida.
- Exista título y descripción suficiente.
- El curso sea gratuito o su condición esté claramente identificada.
- Tenga una competencia digital asociada.
- El idioma esté identificado.
- No sea un duplicado.
- No esté retirado o vencido.
- No incumpla las reglas de la fuente.

Los resultados ambiguos deberán quedar en estado `PENDING_REVIEW`, no publicarse automáticamente.

### Frecuencia prevista

Durante el MVP:

- Ingesta diaria o semanal según la fuente.
- Ejecución mediante GitHub Actions.
- GitHub Actions llama a `POST /internal/ingestion/run`.
- El endpoint requiere un token secreto.
- Se registra cada ejecución en `ingestion_runs`.

No se dependerá de un `@Scheduled` permanente dentro de Render Free, porque el Web Service gratuito puede suspenderse cuando no recibe tráfico.

## 7. Modelo de datos inicial

### `sources`

Representa una fuente autorizada.

Campos previstos:

- `id`
- `name`
- `base_url`
- `integration_type`
- `is_active`
- `sync_frequency`
- `last_successful_sync_at`
- `created_at`
- `updated_at`

### `courses`

Representa un curso normalizado y publicable.

Campos previstos:

- `id`
- `title`
- `description`
- `provider`
- `canonical_url`
- `language`
- `level`
- `format`
- `duration`
- `is_free`
- `certificate_available`
- `status`
- `quality_score`
- `source_updated_at`
- `last_seen_at`
- `created_at`
- `updated_at`

Estados previstos:

```text
PENDING_REVIEW
PUBLISHED
REJECTED
STALE
ARCHIVED
```

### `competencies`

Competencias digitales de ConcienTIC:

- Información
- Comunicación
- Creación
- Seguridad
- Discernimiento
- IA
- Bienestar
- Ciudadanía

### `course_competencies`

Relación muchos a muchos entre cursos y competencias.

### `ingestion_runs`

Historial técnico de cada sincronización:

- `id`
- `source_id`
- `started_at`
- `finished_at`
- `status`
- `items_detected`
- `items_created`
- `items_updated`
- `items_rejected`
- `error_message`

### `course_snapshots`

Opcional para una segunda fase. Permitirá guardar metadatos detectados en cada sincronización y comparar cambios de una fuente sin almacenar innecesariamente el contenido completo de terceros.

## 8. Búsqueda

La primera versión utilizará las capacidades nativas de PostgreSQL:

- Full Text Search para título y descripción.
- Índices `GIN`.
- `pg_trgm` para coincidencias aproximadas.
- Filtros por competencia, idioma, nivel, formato y gratuidad.
- Ordenamiento por relevancia y calidad.
- Paginación obligatoria.

Consulta conceptual:

```text
texto buscado
    ↓
normalización lingüística
    ↓
coincidencia en título/descripción/proveedor
    ↓
relevancia textual
    ↓
quality_score
    ↓
fecha de actualización
    ↓
resultado paginado
```

No se incorporará OpenSearch o Elasticsearch en el MVP. Se evaluará únicamente si el volumen, el número de consultas o las necesidades de relevancia superan las capacidades de PostgreSQL.

## 9. API pública prevista

### Listar cursos

```http
GET /api/v1/courses
```

Parámetros previstos:

```http
GET /api/v1/courses?q=inteligencia+artificial
GET /api/v1/courses?competency=ia&language=es
GET /api/v1/courses?level=initial&free=true
GET /api/v1/courses?format=course&page=0&size=24
```

### Detalle de curso

```http
GET /api/v1/courses/{id}
```

### Competencias disponibles

```http
GET /api/v1/competencies
```

### Endpoint interno de ingesta

```http
POST /internal/ingestion/run
Authorization: Bearer <INGESTION_TOKEN>
```

Este endpoint no será público para usuarios finales y deberá incluir protección contra ejecuciones simultáneas.

### Respuesta conceptual

```json
{
  "items": [
    {
      "id": "course-123",
      "title": "Introducción a la IA generativa",
      "description": "Fundamentos de inteligencia artificial...",
      "provider": "IBM",
      "url": "https://example.org/course",
      "language": "es",
      "level": "initial",
      "format": "course",
      "free": true,
      "skills": ["ia", "discernimiento"],
      "duration": "Variable"
    }
  ],
  "page": 0,
  "size": 24,
  "total": 128
}
```

## 10. Despliegue gratuito del MVP

### Neon

Neon será el PostgreSQL administrado del MVP. La documentación de Neon indica que su plan Free tiene límites de cómputo, almacenamiento y transferencia, por lo que se deberá monitorizar el consumo antes de publicar el catálogo ampliamente.

Referencia: [Neon Free plan limits and quotas](https://neon.com/faqs/free-plan-limits-and-quotas)

### Render

Render alojará el backend como Web Service. El plan Free puede suspender el servicio después de un periodo sin tráfico y producir un arranque en frío en la siguiente petición.

Referencia: [Render Deploy for Free](https://render.com/docs/free/)

### GitHub Actions

GitHub Actions se utilizará para programar la ingesta del MVP. El workflow deberá:

1. Ejecutarse según una frecuencia definida.
2. Llamar al endpoint interno de Render.
3. Usar secretos del repositorio o del entorno.
4. Fallar si el backend devuelve un error.
5. Mantener logs suficientes para diagnosticar una ejecución.

La frecuencia no deberá ser excesiva para no sobrecargar las fuentes externas ni consumir innecesariamente los límites gratuitos.

## 11. Costes y licencias

El MVP debe usar únicamente software y dependencias gratuitas/open source:

- OpenJDK 25.
- Spring Boot.
- Gradle.
- PostgreSQL.
- Flyway Community.
- Jsoup.
- Cliente HTTP del JDK o Spring.
- JUnit.
- Testcontainers.

No se utilizarán inicialmente:

- Flyway Teams o Enterprise.
- Flyway Desktop o Pipelines.
- APIs comerciales de búsqueda.
- APIs de scraping de pago.
- APIs de IA de pago.
- Algolia u otro buscador administrado.
- OpenSearch administrado de pago.
- Redis administrado.
- Servicios de crawling permanente.

“Gratuito” se refiere a las capas y ediciones seleccionadas, no a una garantía permanente de que toda infraestructura productiva carezca de costo. Los límites y condiciones de Neon, Render, GitHub y otras plataformas pueden cambiar.

## 12. Seguridad y configuración

Secretos previstos:

- `DATABASE_URL`
- `DATABASE_USER`
- `DATABASE_PASSWORD`
- `INGESTION_TOKEN`
- `CORS_ALLOWED_ORIGINS`

Reglas:

- Nunca guardar secretos en Git.
- Mantener `.env.example` sin valores reales.
- Usar SSL para la conexión con Neon.
- No exponer credenciales de Neon al frontend.
- No exponer el endpoint de ingesta sin autenticación.
- Validar y limitar los parámetros de búsqueda.
- Aplicar paginación obligatoria.
- Registrar errores sin guardar contraseñas o tokens.
- Limitar el tiempo y frecuencia de las ingestas.
- Mantener una lista explícita de fuentes autorizadas.

## 13. Consideraciones legales y operativas

El sistema debe consultar fuentes externas de forma responsable:

- Preferir APIs, feeds y sitemaps oficiales.
- Revisar `robots.txt` y los términos de uso de cada fuente.
- Respetar límites de frecuencia.
- No copiar el contenido completo de los cursos.
- Guardar principalmente metadatos, una descripción breve y el enlace original.
- Mostrar siempre la institución propietaria.
- Mantener la URL original y la fecha de última comprobación.
- Detectar cursos que hayan desaparecido o cambiado.
- Permitir desactivar una fuente o un curso manualmente.

La información del catálogo debe tratarse como curaduría informativa, no como garantía de disponibilidad, certificación o calidad académica de terceros.

## 14. Fases de implementación

### Fase 1 — Backend mínimo

- Crear `backend/` dentro del monorepo.
- Inicializar Java 25, Spring Boot y Gradle Kotlin DSL.
- Configurar perfiles local y producción.
- Conectar PostgreSQL local y Neon.
- Configurar Flyway Community.
- Crear tablas de fuentes, cursos y competencias.
- Cargar datos de prueba.
- Exponer `GET /api/v1/courses`.

### Fase 2 — Búsqueda y filtros

- Añadir Full Text Search.
- Añadir `pg_trgm`.
- Añadir filtros y paginación.
- Añadir ordenamiento por relevancia.
- Definir respuesta estable para el frontend.
- Documentar el contrato OpenAPI.

### Fase 3 — Primera ingesta

- Implementar dos o tres adaptadores.
- Crear normalización.
- Crear deduplicación por URL canónica y claves externas.
- Registrar `ingestion_runs`.
- Marcar cursos no encontrados como `STALE`.
- Añadir revisión manual básica.

### Fase 4 — Integración con frontend

- Sustituir el arreglo estático de cursos por llamadas a la API.
- Mantener filtros y diseño actuales.
- Añadir estados de carga, error y catálogo vacío.
- Configurar CORS o proxy de forma segura.

### Fase 5 — Automatización gratuita

- Crear workflow programado en GitHub Actions.
- Añadir endpoint interno protegido.
- Ejecutar ingesta diaria o semanal según las fuentes.
- Registrar fallos y resultados.
- Añadir alertas simples mediante logs o notificación gratuita disponible.

### Fase 6 — Calidad y crecimiento

- Panel administrativo.
- Revisión y aprobación de candidatos.
- Puntuación de calidad explicable.
- Métricas de fuentes y cursos.
- Pruebas de carga.
- Evaluar OpenSearch solo con evidencia de necesidad.

## 15. Criterios para cambiar de arquitectura

No se migrará a microservicios, workers permanentes u OpenSearch por anticipación. Se reconsiderará cuando exista evidencia de alguno de estos problemas:

- El catálogo supera ampliamente la capacidad práctica de PostgreSQL.
- La ingesta bloquea o degrada la API pública.
- Se necesitan múltiples workers concurrentes.
- Render Free no ofrece disponibilidad suficiente.
- Se requiere una cola durable.
- Existen varios equipos con despliegues independientes.
- La búsqueda necesita capacidades que PostgreSQL no cubre razonablemente.

## 16. Checklist antes de comenzar la implementación

- [ ] Confirmar las fuentes iniciales.
- [ ] Confirmar criterios de gratuidad y publicación.
- [ ] Confirmar competencias y taxonomía.
- [ ] Confirmar si se usará el monorepo actual.
- [ ] Crear cuenta/proyecto Neon.
- [ ] Crear servicio Render.
- [ ] Confirmar disponibilidad de Java 25 en la estrategia de build elegida.
- [ ] Confirmar límites actuales de los planes Free.
- [ ] Definir contrato inicial de la API.
- [ ] Definir esquema inicial de PostgreSQL.
- [ ] Definir política de revisión manual.
- [ ] Revisar términos de uso de cada fuente.
- [ ] Decidir la frecuencia de ingesta.
- [ ] Crear la spec técnica antes de implementar.

## 17. Decisión pendiente

La siguiente decisión de producto antes de programar no es todavía el framework, sino la lista inicial de fuentes y los criterios exactos para considerar un curso publicable. Esa definición determinará los primeros adaptadores, el modelo de datos y las reglas de calidad.


---

# Anexo A — Requisitos operativos del catálogo vivo

Este anexo precisa el comportamiento esperado del catálogo para la ruta:

```text
Servicios → Competencias Digitales → Contenidos → Biblioteca abierta
```

## A.1 Definición del producto

El sistema no debe limitarse a buscar “cursos gratis”. Debe descubrir, evaluar, verificar, clasificar y mantener **recursos de aprendizaje digital confiables y realmente accesibles**.

Un recurso puede ser un curso, ruta, lección, biblioteca, marco, módulo, tutorial u otra experiencia de aprendizaje digital que permita desarrollar una o más competencias de ConcienTIC.

La condición de publicación debe considerar simultáneamente:

- Confiabilidad de la fuente.
- Disponibilidad actual del enlace.
- Accesibilidad real del recurso.
- Condición de gratuidad verificada.
- Idioma disponible.
- Modalidad y duración identificables.
- Relación con una competencia digital.
- Relación con uno o más Guardianes.
- Evidencia suficiente para explicar por qué el recurso fue publicado.

La palabra `free` no deberá determinarse únicamente por el título del recurso. Debe existir una verificación reciente de su acceso y de sus condiciones.

## A.2 Principio de operación

El proceso debe ser:

```text
AUTOMÁTICO → DIARIO → VERIFICABLE → ACUMULATIVO
```

- **Automático:** una ejecución programada inicia el ciclo sin intervención manual.
- **Diario:** el sistema intenta realizar una sincronización cada día.
- **Verificable:** cada decisión y cada cambio conserva fecha, fuente, resultado y evidencia técnica.
- **Acumulativo:** el sistema conserva el historial; no reemplaza silenciosamente el estado anterior.

La automatización no significa publicar ciegamente cualquier resultado. Los candidatos dudosos pueden quedar en revisión, mientras que las comprobaciones técnicas rutinarias sí deben ejecutarse automáticamente.

## A.3 Ciclo diario completo

Cada ejecución diaria debe realizar, como mínimo, estas etapas:

1. **Investigar nuevas fuentes** dentro de los canales permitidos.
2. **Revisar las fuentes registradas** y comprobar si siguen activas.
3. **Detectar nuevos recursos**.
4. **Revisar recursos previamente registrados**.
5. **Detectar recursos retirados o temporalmente inaccesibles**.
6. **Detectar cambios de URL**, incluyendo redirecciones y URL canónica.
7. **Detectar cambios de idioma**.
8. **Detectar cambios de duración**.
9. **Detectar cambios de modalidad o formato**.
10. **Detectar cambios en la condición de gratuidad**.
11. **Verificar que los enlaces funcionen**.
12. **Clasificar cada recurso**.
13. **Asociar cada recurso con una o más competencias digitales**.
14. **Asociar cada recurso con uno o más Guardianes**.
15. **Calcular o actualizar la puntuación de calidad**.
16. **Actualizar el estado del recurso**.
17. **Guardar el historial y los cambios detectados**.
18. **Actualizar el índice de búsqueda**.
19. **Publicar el nuevo snapshot del catálogo**.
20. **Generar los datos que consumen las tarjetas de Biblioteca abierta**.
21. **Registrar métricas, errores y resumen de la ejecución**.

## A.4 Descubrimiento de nuevas fuentes

El descubrimiento diario de fuentes debe ser controlado. El sistema no debe aceptar automáticamente cualquier dominio encontrado en Internet.

Canales posibles, en este orden:

1. Catálogos, APIs, RSS y sitemaps de fuentes ya confiables.
2. Enlaces recomendados por fuentes registradas.
3. Listas públicas de instituciones educativas, gubernamentales o internacionales.
4. Descubrimiento web limitado y sujeto a validación.
5. Incorporación manual de una fuente propuesta.

Las nuevas fuentes encontradas se guardarán como candidatas en `source_discovery_candidates`, con estado `PENDING_REVIEW`. Una fuente solo podrá alimentar recursos publicados cuando haya sido aprobada y configurada en `sources`.

Esto permite que la investigación sea automática sin convertirla en una publicación automática sin control.

## A.5 Verificación de disponibilidad y accesibilidad

Cada recurso debe tener una verificación reciente. La verificación no se limita a comprobar que el servidor responde `200`.

Se deben registrar, cuando sea posible:

- Código HTTP.
- URL solicitada.
- URL final después de redirecciones.
- URL canónica detectada.
- Fecha y hora de comprobación.
- Tiempo de respuesta.
- Tipo de contenido.
- Si requiere autenticación.
- Si requiere una cuenta gratuita.
- Si solicita pago.
- Si está bloqueado por región o navegador.
- Si muestra que el contenido fue retirado.
- Huella o hash de metadatos relevantes.

Estados de acceso previstos:

```text
PUBLIC_FREE
FREE_ACCOUNT_REQUIRED
ACCESSIBLE_WITH_LIMITATIONS
PAYWALLED
UNAVAILABLE
BLOCKED
UNKNOWN
```

Solo `PUBLIC_FREE`, `FREE_ACCOUNT_REQUIRED` y, si se decide expresamente, `ACCESSIBLE_WITH_LIMITATIONS` podrán ser candidatos a publicación. Los demás estados deben retirar el recurso de las tarjetas o enviarlo a revisión.

Un recurso no debe desaparecer inmediatamente ante un único error de red. El sistema debe aplicar una política de reintentos y confirmar la indisponibilidad en ejecuciones posteriores antes de marcarlo como `STALE` o `ARCHIVED`, salvo que la propia página indique que fue retirado.

## A.6 Detección de cambios

Cada comprobación debe comparar el resultado actual con el último estado conocido.

Cambios que deben detectarse:

- Título.
- Descripción.
- Proveedor.
- URL solicitada.
- URL final o canónica.
- Idioma.
- Duración.
- Modalidad o formato.
- Condición de gratuidad.
- Disponibilidad.
- Certificación.
- Competencias asociadas.
- Guardianes asociados.
- Fecha de actualización de la fuente.

Cada cambio debe producir un registro en `resource_changes`:

```text
resource_changes
├── id
├── resource_id
├── ingestion_run_id
├── field_name
├── previous_value
├── current_value
├── detected_at
├── change_type
└── evidence_reference
```

Tipos de cambio previstos:

```text
CREATED
UPDATED
URL_CHANGED
REDIRECT_CHANGED
LANGUAGE_CHANGED
DURATION_CHANGED
FORMAT_CHANGED
PRICE_CHANGED
AVAILABILITY_CHANGED
CLASSIFICATION_CHANGED
GUARDIAN_CHANGED
STALE_MARKED
ARCHIVED
```

Los valores anteriores no deben perderse. Para campos importantes se conservarán snapshots o versiones.

## A.7 Modelo complementario de datos

Además de las tablas descritas en el documento principal, el proceso diario necesitará las siguientes entidades.

### `resource_checks`

Una fila por cada comprobación de disponibilidad de un recurso.

Campos previstos:

- `id`
- `resource_id`
- `ingestion_run_id`
- `requested_url`
- `final_url`
- `canonical_url`
- `http_status`
- `response_time_ms`
- `access_status`
- `requires_account`
- `requires_payment`
- `language_detected`
- `checked_at`
- `evidence_hash`
- `error_code`
- `error_message`

### `resource_versions`

Representa una versión conocida de los metadatos de un recurso.

Campos previstos:

- `id`
- `resource_id`
- `version_number`
- `title`
- `description`
- `language`
- `duration`
- `format`
- `is_free`
- `canonical_url`
- `captured_at`
- `content_hash`
- `source_check_id`

### `resource_changes`

Registra diferencias entre la versión anterior y la actual. Debe permitir responder qué cambió, cuándo cambió y durante qué ejecución se detectó.

### `source_discovery_candidates`

Representa fuentes nuevas encontradas automáticamente o propuestas manualmente.

Estados previstos:

```text
PENDING_REVIEW
APPROVED
REJECTED
DUPLICATE
BLOCKED
```

### `guardians`

Catálogo de Guardianes de ConcienTIC. Como referencia inicial, el frontend actual contiene Guardianes como Byte, Detective DQ, Emi, Lex, Locky, Nexo y Nova. La lista definitiva y la semántica de cada Guardián deberán confirmarse antes de implementar la clasificación.

### `resource_guardians`

Relación muchos a muchos entre recursos y Guardianes, incluyendo:

- `resource_id`
- `guardian_id`
- `assignment_source`
- `confidence_score`
- `is_primary`
- `assigned_at`

### `resource_competencies`

Relación muchos a muchos entre recursos y competencias. Puede reemplazar o especializar la tabla `course_competencies` cuando el catálogo deje de tratar exclusivamente cursos.

## A.8 Clasificación y Guardianes

La clasificación debe ser explicable. Para cada asociación se debe poder conocer si provino de:

- Regla explícita.
- Metadato de la fuente.
- Coincidencia de palabras clave.
- Clasificación automática.
- Revisión manual.

La asociación con Guardianes debe incluir un nivel de confianza y, cuando sea posible, una regla o evidencia. Por ejemplo:

```text
Seguridad + privacidad + ciberseguridad → Locky
Información + búsqueda + alfabetización → Byte
Discernimiento + fuentes + desinformación → Detective DQ / Lex
IA + tecnología + creación → Nova
Bienestar + uso equilibrado → Emi
Comunicación + colaboración → Nexo
```

Estas asociaciones son ejemplos iniciales de diseño y no deben considerarse taxonomía definitiva hasta validarlas con el producto.

Un recurso puede tener:

- Una competencia principal.
- Varias competencias secundarias.
- Un Guardián principal.
- Varios Guardianes relacionados.

## A.9 Catálogo y tarjetas de Biblioteca abierta

Las tarjetas no deben generarse modificando diariamente archivos HTML, JSX o JavaScript. El backend debe generar datos de catálogo y el frontend debe renderizarlos.

Flujo previsto:

```text
PostgreSQL
    ↓
Vista de recursos publicados
    ↓
API /api/v1/resources
    ↓
Frontend Biblioteca abierta
    ↓
Tarjetas filtrables
```

La respuesta de la API debe incluir todos los datos necesarios para una tarjeta:

- Identificador.
- Título.
- Descripción breve.
- Fuente.
- URL original.
- URL final verificada.
- Competencias.
- Guardianes.
- Idioma.
- Nivel.
- Duración.
- Modalidad.
- Gratuidad verificada.
- Estado de disponibilidad.
- Fecha de última verificación.
- Etiquetas.

Endpoint previsto:

```http
GET /api/v1/resources
GET /api/v1/resources/{id}
GET /api/v1/resources/facets
GET /api/v1/catalog/status
```

`GET /api/v1/catalog/status` debe permitir mostrar o registrar:

```json
{
  "catalogVersion": "2026-09-29T03:00:00Z",
  "lastSuccessfulRun": "2026-09-29T03:00:00Z",
  "publishedResources": 128,
  "lastVerifiedAt": "2026-09-29T03:00:00Z"
}
```

La generación de datos para tarjetas significa construir el snapshot y la respuesta API del catálogo. No significa crear commits automáticos ni reescribir el código del frontend cada día.

## A.10 Ejecución idempotente y acumulativa

Cada ejecución diaria debe tener un `ingestion_run_id` único y ser idempotente:

- Repetir una ejecución no debe duplicar recursos.
- La URL canónica y la identificación externa deben usarse para deduplicar.
- Un error parcial no debe borrar el catálogo válido anterior.
- La publicación del snapshot debe ocurrir solo después de finalizar las validaciones.
- Los recursos anteriores deben conservarse hasta confirmar su retiro.
- La ejecución debe poder reintentarse sin corromper los estados.
- Los cambios deben quedar asociados a la ejecución que los detectó.

La actualización se puede modelar como dos pasos:

```text
1. Ingestar y validar en estado de trabajo
2. Publicar atómicamente el nuevo snapshot válido
```

Si una fuente falla, el sistema debe conservar la última versión válida y marcar la fuente o el recurso con una advertencia interna, sin eliminar automáticamente todas sus tarjetas.

## A.11 Resultado verificable de cada día

Cada ejecución deberá producir un resumen consultable:

```text
DailyCatalogRun
├── runId
├── startedAt
├── finishedAt
├── status
├── sourcesInvestigated
├── newSourcesFound
├── resourcesDetected
├── resourcesCreated
├── resourcesUpdated
├── resourcesUnavailable
├── urlsChanged
├── classificationsChanged
├── guardiansAssigned
├── cardsPublished
├── warnings
└── errors
```

Estados de ejecución:

```text
STARTED
PARTIAL_SUCCESS
SUCCESS
FAILED
```

La ejecución `PARTIAL_SUCCESS` es válida cuando algunas fuentes funcionan y otras fallan, siempre que el catálogo anterior se mantenga consistente y el fallo quede registrado.

## A.12 Criterios de aceptación funcional

La futura implementación deberá demostrar que:

- [ ] El proceso puede ejecutarse automáticamente una vez al día.
- [ ] El proceso registra qué fuentes investigó.
- [ ] Los nuevos recursos no duplican recursos existentes.
- [ ] Un recurso retirado puede pasar a `STALE` o `ARCHIVED` con evidencia.
- [ ] Un cambio de URL queda registrado sin perder la URL anterior.
- [ ] Un cambio de idioma, duración, modalidad o gratuidad queda registrado.
- [ ] Los enlaces se verifican y tienen fecha de última comprobación.
- [ ] Los recursos se clasifican por una o más competencias.
- [ ] Los recursos se asocian con uno o más Guardianes.
- [ ] Las asociaciones automáticas tienen origen y confianza.
- [ ] Los cambios se conservan acumulativamente.
- [ ] Las tarjetas se alimentan del catálogo publicado mediante API.
- [ ] Una falla de una fuente no elimina el catálogo válido anterior.
- [ ] El resultado diario es consultable y auditable.
- [ ] No se generan cambios de código automáticos para actualizar las tarjetas.

## A.13 Ajuste a las fases de implementación

Las fases del documento principal deben interpretarse así:

### Fase 1 — Catálogo y contrato

- Usar el concepto `resource` además de `course`.
- Definir estados de disponibilidad.
- Definir competencias y Guardianes.
- Definir el contrato de las tarjetas.

### Fase 2 — Verificación e historial

- Implementar `resource_checks`.
- Implementar `resource_versions`.
- Implementar `resource_changes`.
- Implementar redirecciones y URL canónica.
- Implementar reintentos y estados `STALE`.

### Fase 3 — Clasificación

- Implementar competencias.
- Implementar Guardianes.
- Registrar la procedencia y confianza de cada asociación.
- Añadir revisión manual para casos ambiguos.

### Fase 4 — Automatización diaria

- Ejecutar el ciclo completo mediante GitHub Actions.
- Proteger el endpoint interno.
- Registrar el resumen diario.
- Publicar snapshots válidos.
- Mantener el resultado anterior ante fallos parciales.

### Fase 5 — Frontend dinámico

- Reemplazar el arreglo estático de recursos por `GET /api/v1/resources`.
- Mantener filtros, tarjetas y diseño de la Biblioteca abierta.
- Mostrar la fecha de actualización o verificación cuando corresponda.

### Fase 6 — Mejora continua

- Añadir nuevas fuentes aprobadas.
- Mejorar reglas de calidad y clasificación.
- Medir precisión de las asociaciones con Guardianes.
- Evaluar búsqueda avanzada solo si los datos lo justifican.
