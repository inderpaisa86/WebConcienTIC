# ConcienTIC Catalog Backend

Backend inicial del catálogo de recursos de aprendizaje digital.

## Requisitos

- Java 25 LTS.
- Gradle 9.x mediante Gradle Wrapper.
- PostgreSQL para ejecución completa.

## Ejecución local

```powershell
./gradlew bootRun
```

Variables principales:

```text
DATABASE_URL=jdbc:postgresql://localhost:5432/concientic
DATABASE_USERNAME=concientic
DATABASE_PASSWORD=concientic
INGESTION_TOKEN=local-development-token
TIMEZONE=America/Bogota
```

El primer incremento expone el catálogo y el contrato del orquestador. La investigación real de fuentes se habilitará después de implementar y probar los adaptadores especializados.

## Despliegue en Render con Docker

En Render crea un **Web Service** usando el repositorio actual y configura:

```text
Root Directory: backend/concientic-catalog
Runtime: Docker
Dockerfile Path: Dockerfile
Health Check Path: /actuator/health
```

No es necesario seleccionar un runtime Java: el `Dockerfile` fija Java 25 y ejecuta el Gradle Wrapper.

Variables de entorno requeridas en Render:

```text
DATABASE_URL=jdbc:postgresql://HOST_DIRECTO/concienticdb?sslmode=require
DATABASE_USERNAME=neondb_owner
DATABASE_PASSWORD=<configurar directamente en Render>
TIMEZONE=America/Bogota
INGESTION_TOKEN=<token seguro>
CORS_ALLOWED_ORIGINS=https://concientic.com
```
