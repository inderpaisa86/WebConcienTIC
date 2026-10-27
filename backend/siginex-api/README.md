# SIGINEX API Backend

Backend SIGINEX en Java 25 + Spring Boot 4. Arranca, conecta a la base de datos,
ejecuta las migraciones Flyway y expone el healthcheck de Fase 0. La **Fase 1**
añade autenticación por sesión y la gestión de usuarios por administrador.

La arquitectura sigue 4 capas con nombres literales: Controller (REST +
validación de formato) → Delegate (validaciones de negocio y autorización) →
Service (acceso a BD y servicios externos) → Repository (SQL/JDBC). El
aislamiento entre empresas (tenants) se aplica con Row-Level Security de
PostgreSQL: cada operación sujeta a RLS corre dentro de una transacción que fija
`app.tenant_id` con `set_config`.

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

## Endpoints de Fase 1

Rutas públicas: `/actuator/health`, `/actuator/info`, `/api/v1/status` y
`POST /auth/login`. El resto exige una sesión autenticada.

### Autenticación

```text
POST /auth/login
  body: { "email": "...", "password": "..." }
  200/204: fija la cookie de sesión __Host-siginex_session.
  401 genérico ("credenciales invalidas") si el email no existe, la
      contraseña es incorrecta, la cuenta está inactiva o bloqueada
      (resistencia a enumeración). Tras 5 fallos consecutivos la cuenta
      queda bloqueada temporalmente con backoff escalonado.

POST /auth/logout
  Requiere sesión. Revoca la sesión actual y limpia la cookie.

GET /auth/me
  Requiere sesión. Devuelve { usuarioId, tenantId, email, rol }.
```

### Gestión de usuarios (solo rol `admin`)

El usuario se crea siempre en el tenant del admin autenticado; el `tenant_id`
nunca se acepta del cliente. La autorización de negocio (es admin, mismo tenant)
vive en el Delegate; el aislamiento entre tenants lo garantiza RLS.

```text
POST /api/v1/usuarios
  body: { "email": "...", "nombre": "...", "password": "...", "rol": "admin|miembro" }
        (password mínimo 12 caracteres)
  201: devuelve UsuarioResponse (sin password_hash).
  403 si el actor no es admin. 409 si el email ya está registrado.

POST /api/v1/usuarios/{id}/activar
POST /api/v1/usuarios/{id}/desactivar
  200: devuelve UsuarioResponse.
  403 si el actor no es admin. 404 si el usuario no existe en el tenant del admin.
```

## Modelo de sesión y cookie

La sesión es un token opaco revocable. Solo se guarda su hash SHA-256 (nunca el
token en claro). La cookie `__Host-siginex_session` es `HttpOnly`, `Secure`,
`SameSite=Lax`, `Path=/`, con expiración absoluta de 8 horas. El `tenant_id` se
resuelve en el servidor a partir de la sesión; el cliente nunca lo envía.

Las contraseñas se almacenan con Argon2id y **nunca** se registran en logs ni en
auditoría; lo mismo aplica a los hashes y a los tokens de sesión.

CSRF está habilitado (cookie `XSRF-TOKEN` legible por el front) para todas las
operaciones con sesión; `POST /auth/login` se excluye por ser el arranque de la
sesión.

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

Compila las fuentes y ejecuta la suite de tests (unitarios de capa con Mockito +
AssertJ, sin Testcontainers, más `ApplicationContextWiringTest`, que arranca el
contexto Spring sin BD viva usando un `DataSource` mockeado y Flyway
deshabilitado) y produce `build/libs/siginex-api-0.1.0-SNAPSHOT.jar`.
