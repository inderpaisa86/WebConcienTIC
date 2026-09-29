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
