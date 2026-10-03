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

## Catálogo híbrido

La Biblioteca combina dos fuentes:

1. `src/content/static-courses.ts`: conserva los 30 cursos que existían originalmente en la página HTML estática.
2. `/api/catalog/resources`: incorpora los recursos publicados por el motor de ConcienTIC.

Los cursos estáticos se muestran incluso si el backend no está disponible. Los recursos dinámicos se agregan solo cuando su URL no está ya presente en el catálogo estático; de esta forma no se duplican cursos heredados.

Para agregar un nuevo curso estático, añade una entrada a `staticCourses` usando los campos de la función `resource`: proveedor, competencia, título, descripción, nivel, duración, idioma, formato, Guardián y URL oficial.

La Biblioteca aplica los mismos filtros a ambas fuentes. La paginación se calcula sobre la colección combinada. Los recursos dinámicos pueden reemplazar la necesidad de mantener manualmente nuevos cursos, pero el catálogo heredado se conserva como respaldo editorial.

## Guardianes visuales de la Biblioteca

Las tarjetas de `/servicios/contenidos` utilizan un conjunto visual separado del sistema oficial de Guardianes del ecosistema. Estos personajes solo representan categorías de la Biblioteca y no modifican `config/guardians.json` ni `src/content/guardians.ts`.

El registro está en:

```text
src/content/library-guardians.ts
```

Los assets vectoriales están en:

```text
public/library-guardians/
```

Categorías disponibles:

```text
informacion       → informacion.svg
comunicacion      → comunicacion.svg
creacion          → creacion.svg
seguridad         → seguridad.svg
discernimiento    → discernimiento.svg
ia                → ia-innovacion.svg
bienestar         → bienestar.svg
ciudadania        → ciudadania.svg
```

Cada tarjeta toma el color y la ilustración a partir de `primaryCompetency`. Los recursos estáticos y dinámicos usan el mismo mapeo visual. Si un recurso no tiene competencia, la tarjeta conserva un tratamiento neutro y no inventa una categoría.
