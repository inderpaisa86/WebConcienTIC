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
