# Biblioteca abierta conectada al catálogo

## Arquitectura

La ruta pública de la Biblioteca es:

```text
/servicios/contenidos
```

La interfaz consulta el proxy same-origin de Next.js:

```text
/api/catalog/resources
```

El proxy consulta el backend:

```text
/api/v1/resources
```

Esto evita que el navegador dependa directamente de CORS del backend.

## Configuración

En desarrollo copia `.env.example` como `.env.local` y configura:

```text
CATALOG_API_URL=https://webconcientic.onrender.com
```

En Vercel configura la misma variable en el entorno correspondiente:

```text
Project Settings → Environment Variables → CATALOG_API_URL
```

No es necesario usar `NEXT_PUBLIC_` porque la URL solo la utiliza el Route Handler del servidor.

Si no se define la variable, se usa temporalmente:

```text
https://webconcientic.onrender.com
```

## Consulta directa del proxy

```http
GET http://localhost:3000/api/catalog/resources?page=0&size=12
```

Filtros disponibles:

```text
q
competency
provider
level
format
language
page
size
```

Ejemplo:

```http
GET http://localhost:3000/api/catalog/resources?competency=ia&provider=Google%20Applied%20Digital%20Skills&page=0&size=12
```

## Página pública

```text
http://localhost:3000/servicios/contenidos
```

La Biblioteca incluye:

- búsqueda de texto;
- filtro por las ocho competencias oficiales;
- filtro por fuente;
- filtro por nivel;
- filtro por formato;
- paginación;
- estados de carga, error y catálogo vacío;
- tarjeta con Guardián, proveedor, metadata, gratuidad y CTA oficial;
- sección Academia ConcienTIC marcada como `Próximamente`.

## Ejecución local

Desde `concientic/`:

```powershell
npm install
npm run dev
```

Abre:

```text
http://localhost:3000/servicios/contenidos
```

Si el backend está desplegado, la página consultará Render. Si se ejecuta un backend local, usa por ejemplo:

```text
CATALOG_API_URL=http://localhost:8080
```

## Validación

Comandos ejecutados para esta integración:

```powershell
npm run typecheck
npm run lint
npm run build
```

Los tres terminan correctamente.

El backend también fue validado con:

```powershell
./gradlew clean test bootJar --no-daemon
```

## Diagnóstico

Si la pantalla muestra el estado de error:

1. Comprueba que `CATALOG_API_URL` apunte solo a la URL base, sin `/api/v1/resources`.
2. Comprueba directamente:

```text
https://webconcientic.onrender.com/api/v1/resources
```

3. Revisa que existan recursos publicables en PostgreSQL:

```sql
SELECT COUNT(*)
FROM resources
WHERE status IN ('ACTIVE', 'UPDATED', 'VERIFIED', 'LINK_CHANGED')
  AND free_status IN ('FREE', 'FREE_CONTENT_PAID_CERTIFICATE', 'AUDIT_FREE', 'PARTIAL_FREE');
```

4. Si la respuesta contiene `items: []`, la interfaz está funcionando pero el catálogo no tiene recursos que cumplan los filtros públicos.
5. Si el backend devuelve `5xx`, revisar los logs de Render.

## Regla de contenido

La interfaz no inventa recursos ni modifica el catálogo. Solo presenta los recursos que el backend publica y enlaza a `verifiedUrl` o `sourceUrl` en una pestaña nueva.
