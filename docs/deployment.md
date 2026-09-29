# Despliegue y operación del MVP

## Topología

```text
Next.js/Vercel o hosting actual
        │ HTTPS
        ▼
Render Free — Spring Boot Java 25
        │ SSL
        ▼
Neon Free — PostgreSQL

GitHub Actions — scheduler diario → endpoint interno Render
```

## Componentes gratuitos

- OpenJDK 25.
- Spring Boot.
- Gradle Wrapper.
- PostgreSQL en Neon Free.
- Flyway Community.
- GitHub Actions dentro de sus límites.
- Cliente HTTP del JDK/Spring.
- Jsoup si una fuente requiere HTML.
- JUnit y Testcontainers para pruebas.

No usar Flyway Teams/Enterprise, servicios de búsqueda administrados, APIs de IA de pago, servicios de scraping de pago ni Redis administrado durante el MVP.

## Variables de entorno

```text
SPRING_PROFILES_ACTIVE=production
DATABASE_URL=...
DATABASE_USER=...
DATABASE_PASSWORD=...
INGESTION_TOKEN=...
TIMEZONE=America/Bogota
CORS_ALLOWED_ORIGINS=https://concientic.com
MAX_SOURCE_CONCURRENCY=...
HTTP_TIMEOUT_SECONDS=...
```

Los valores reales solo viven en Render/GitHub Secrets. `.env.example` tendrá nombres sin secretos.

## Neon

- Usar SSL.
- Usar la cadena de conexión recomendada por Neon.
- Ejecutar migraciones de forma controlada durante despliegue.
- Monitorizar almacenamiento, cómputo y transferencia.
- Exportar snapshots del catálogo, pero mantener PostgreSQL como fuente de verdad.

## Render

- Desplegar el backend como Web Service.
- Usar Gradle Wrapper o una imagen reproducible con Java 25.
- Definir health check.
- Configurar variables de entorno.
- Aceptar cold start del plan Free.
- No depender de un proceso permanente para la ingesta.

## GitHub Actions

El workflow diario debe:

1. leer `TIMEZONE` y secretos;
2. generar una clave idempotente;
3. llamar al endpoint interno;
4. fallar ante respuesta no exitosa;
5. conservar logs;
6. permitir ejecución manual;
7. no publicar secretos en logs.

## Seguridad de red

La API pública solo expone lectura del catálogo. La ingesta, revisión y reportes internos requieren token. El backend debe aplicar CORS explícito, validación de entrada, límites HTTP y protección SSRF.

## Evolución

Si Render Free no soporta duración o disponibilidad necesarias, separar la ingesta en un worker/cron pago o autoalojado. Si PostgreSQL no cubre la búsqueda, evaluar OpenSearch. Ninguna migración se hace sin métricas.
