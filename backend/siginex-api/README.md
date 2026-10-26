# SIGINEX API Backend (Fase 0 skeleton)

Esqueleto del backend SIGINEX en Java 25 + Spring Boot 4. Esta Fase 0 arranca,
conecta a la base de datos, ejecuta la migración Flyway baseline y responde al
healthcheck. **No** incluye lógica de negocio, entidades, login ni reglas reales
de Spring Security (solo lo mínimo para dejar abierto el healthcheck).

## Requisitos

- Java 25 LTS.
- Gradle 9.x mediante Gradle Wrapper.
- PostgreSQL para la ejecución completa.

## Ejecución local

```powershell
./gradlew bootRun
```

Healthcheck:

```text
GET http://localhost:8080/actuator/health  ->  {"status":"UP"}
```

Endpoint mínimo de estado (sin BD):

```text
GET http://localhost:8080/api/v1/status  ->  {"status":"ok","application":"siginex-api"}
```

## Variables de entorno

Todas tienen un default local no sensible; los secretos nunca van en el repo.

```text
DATABASE_URL=jdbc:postgresql://localhost:5432/siginex
DATABASE_USERNAME=siginex
DATABASE_PASSWORD=siginex
DATABASE_POOL_SIZE=5
PORT=8080
CORS_ALLOWED_ORIGINS=https://concientic.com,http://localhost:3000
```

## Base de datos en producción (Neon / PostgreSQL)

En producción la base de datos es **Neon (PostgreSQL)**, alcanzada por JDBC
estándar a través de la cadena de conexión del **pooler** de Neon en
`DATABASE_URL`:

```text
DATABASE_URL=jdbc:postgresql://<project>-pooler.<region>.aws.neon.tech/<db>?sslmode=require
DATABASE_USERNAME=<usuario>
DATABASE_PASSWORD=<configurar como secreto en el entorno de despliegue>
```

El pool Hikari está ajustado para el cold start del pooler de Neon
(`connection-timeout` amplio e `initialization-fail-timeout: -1` para no fallar
al arranque).

## Build y verificación

```powershell
./gradlew.bat build
```

Compila las fuentes, ejecuta `ApplicationContextWiringTest` (que arranca el
contexto Spring sin BD viva usando un `DataSource` mockeado y Flyway deshabilitado)
y produce `build/libs/siginex-api-0.1.0-SNAPSHOT.jar`.
