# ConcienTIC · Requisitos y diseño técnico del sistema de logos oficiales

**Estado:** diseño revisado para implementación; este documento no descarga assets, no modifica código y no declara logos verificados.
**Workspace:** `C:\Jose\RepoSiginex\WebConcienTIC`.
**Frontend:** `C:\Jose\RepoSiginex\WebConcienTIC\concientic`.
**Revisión atendida:** `C:\Jose\RepoSiginex\WebConcienTIC\.agents\tasks\provider-logo-design-review.json` y `provider-logo-design-review.md`. Los hallazgos PLD-001 a PLD-008 quedan respondidos al final.

## Summary

La Biblioteca abierta conservará la tarjeta actual y su rediseño: tarjeta blanca, borde suave, radio generoso, zona superior pastel gobernada por el Guardián oficial, etiqueta de competencia, identidad del proveedor, título, descripción, metadatos, gratuidad, Guardián y CTA. Se sustituirá únicamente el marcador artificial de iniciales por un logo oficial local cuando el asset esté verificado y asignado al recurso. El CTA único será **`Explorar recurso oficial`** seguido de una flecha decorativa `→`.

El sistema separa `provider` (organización que publica u ofrece el recurso), `brand` o `program` (iniciativa), `product` (plataforma) y `resource` (curso, lección, marco o ruta). `data/providers.json` será el registro canónico de identidades y alias dirigidos; `data/provider-logo-registry.json` será el registro auditable de assets y procedencia; los assets publicables vivirán en `concientic/public/providers/<slug>/logo.svg|png|jpg`. `provider_logo` es la identidad principal de la tarjeta; `program_logo` y `product_logo` son referencias secundarias opcionales.

Un alias textual solo permite identificar una identidad candidata. No autoriza por sí mismo a mostrar un logo. La elegibilidad exige una asignación versionada y revisada por `resourceId` o URL exacta, además de un logo `verified`. Esta separación evita que un recurso legacy con `provider: "Google"`, por ejemplo, reciba un logo verificado sin evidencia de quién publica su URL.

La validación será determinista, local y sin descargas. `npm run validate:provider-logos` leerá los JSON, verificará contratos, relaciones, evidencia, assets y seguridad SVG, y generará `C:\Jose\RepoSiginex\WebConcienTIC\provider-logo-validation-report.json`. El backend Java, el esquema PostgreSQL y `GET /api/v1/resources` no cambian en esta integración.

## Supuestos y ambigüedades explícitos

- Este paso solo produce requisitos y diseño. Ningún registro comienza como `verified` sin fuente oficial, página de evidencia, nota de uso/licencia, fecha UTC, asset local y hash comprobados por la fase de implementación.
- `officialDomain` y `official_domain` son **hostnames sin esquema ni ruta**, por ejemplo `open.ac.uk`. `source_page` y `logo_url` son URLs completas `https://...` y nunca son rutas de renderizado.
- OpenLearn no es un provider. El provider es `The Open University` y OpenLearn es una marca/producto relacionado. La lista mínima se cumple mediante esa relación dirigida, no creando un provider ficticio `openlearn`.
- Para Santander, el provider canónico será la organización que la evidencia del recurso identifique; `Santander Open Academy` será una marca/programa explícito. Un alias ambiguo `Santander` no habilita ningún logo.
- El catálogo puede continuar enviando solo el string `provider`. En ese caso la tarjeta conserva identidad textual y fallback; no muestra logo hasta que exista asignación revisada por recurso/URL.
- `brandColor` se conserva para documentación del proveedor, pero nunca determina el fondo, borde o CTA de la tarjeta; esos estilos siguen perteneciendo a ConcienTIC y al Guardián.
- La revisión legal de uso no puede inferirse del checksum o del dominio. Si la nota de uso es ambigua, el registro queda en `REVIEW_REQUIRED` y no se publica.
- La validación SVG usará la dependencia de parser XML estricta `saxes@6.0.0`, fijada exactamente en `devDependencies` y en `package-lock.json`. No se usará regex como parser de seguridad. No se añade ningún framework de pruebas.
- Se revisaron `library-explorer.tsx`, `catalog.ts`, `static-courses.ts`, `guardians.ts`, `route.ts`, `package.json`, `next.config.ts`, `tsconfig.json`, la carpeta `public/` y la documentación instalada de Next 16.3.4 sobre `public` y `next/image`. No hay infraestructura existente de logos ni de tests.

## Functional Requirements

1. Cada tarjeta debe mostrar, en orden semántico, proveedor real, título, descripción, metadatos, Guardián oficial y CTA `Explorar recurso oficial` + flecha `aria-hidden="true"`. El enlace usará `verifiedUrl ?? sourceUrl`, `target="_blank"` y `rel="noreferrer"` como actualmente.
2. Solo si el recurso tiene una asignación revisada, el `provider_id` resuelto coincide con el provider de esa asignación y el registro de logo tiene `status: "verified"` con asset local válido, se renderiza el logo oficial. El nombre visible del provider permanece junto al logo.
3. En `not_available`, `REVIEW_REQUIRED`, alias desconocido/ambiguo, asignación ausente, registro inválido o error de carga, se conserva la tarjeta y el nombre textual, se oculta la imagen fallida y se muestra un fallback accesible `Logo no disponible`. No se generan iniciales, círculos, pictogramas, texto estilizado ni logos de otra entidad.
4. El color, fondo, ilustración y nombre del Guardián se resuelven desde `guardians` importado desde `@/content/guardians`. No se crea un registro paralelo ni se restauran los ocho Guardianes visuales eliminados. Deben continuar los siete existentes: Emi, Locky, Lex, Byte, Detective DQ, Nexo y Nova.
5. El modelo debe transportar `providerId`, `brandId`, `programId`, `productId` y `resourceId` opcionales, además de referencias `provider_logo`, `program_logo` y `product_logo`. La referencia principal siempre tiene `scope: "provider"`; las secundarias nunca sustituyen automáticamente al provider.
6. Deben contemplarse mediante registros y alias dirigidos: Google, Microsoft, IBM, Cisco, Meta, MinTIC/Ministerio de Tecnologías de la Información y las Comunicaciones, UNESCO, Comisión Europea, The Open University/OpenLearn, Santander Open Academy, DQ Institute, MIT, Harvard, Stanford, Oxford, Cambridge, Carnegie Mellon, OpenAI, Anthropic, Google DeepMind, AWS, NVIDIA, Hugging Face, Mistral AI, Cohere, Salesforce, Oracle, SAP, Adobe, Mozilla, Internet Society y SENA. Contemplar una identidad no significa tener logo verificado.
7. Las relaciones obligatorias se modelan sin mezclar niveles: `The Open University → OpenLearn`; `European Commission → Joint Research Centre`; provider institucional MinTIC frente a programas; `IBM → IBM SkillsBuild`; `Microsoft → Microsoft Learn/Education/Copilot/Azure`; `Google → Applied Digital Skills/Grow/Actívate/AI`; y provider/brand explícitos para Santander Open Academy.
8. La procedencia respeta: brand assets/media kit oficial > press kit oficial > developer/design system oficial > sitio oficial > Wikimedia Commons solo con licencia adecuada y sin asset oficial utilizable. Pinterest, blogs, bancos de imágenes, páginas secundarias de logos, screenshots, buscadores de imágenes, contenido generado por usuarios y logos de otra marca del grupo están prohibidos.
9. Se prefiere SVG oficial sobre PNG y PNG sobre JPG. No se redibuja, recompone, recolorea, recorta ni modifica arbitrariamente un asset. Si el asset no puede almacenarse o licenciarse de forma verificable, se registra la referencia, pero no se sirve remotamente en la UI.
10. `resolveProviderIdentity` podrá resolver identidad por IDs explícitos, alias dirigido o nombre canónico, en ese orden. `resolveProviderLogo` aplicará una segunda comprobación: solo una asignación por `resourceId` o URL normalizada exacta, con evidencia revisada, puede devolver un asset elegible. Un texto heredado sin asignación produce `REVIEW_REQUIRED` y fallback textual aunque el alias sea inequívoco.
11. Los campos externos se validan en runtime antes de llegar a `ResourceCard`. `provider` es obligatorio, string no vacío de máximo 160 caracteres; los IDs son slugs opcionales; `resourceId` es obligatorio para una asignación explícita. Elementos inválidos se descartan con diagnóstico estable y, si el envelope es inválido, la UI usa los recursos estáticos y el mensaje de catálogo existente.
12. El sistema debe documentar y soportar el pipeline diario: `DISCOVER PROVIDER -> VERIFY PROVIDER -> DISCOVER OFFICIAL BRAND ASSETS -> VERIFY LOGO -> ADD PROVIDER REGISTRY -> DOWNLOAD/REFERENCE ASSET -> ASSIGN LOGO TO RESOURCE -> UPDATE CATALOG`.
13. El validador debe exigir el conjunto mínimo de providers y las relaciones obligatorias. La ausencia de una identidad mínima o de una relación requerida es error fatal, aunque la identidad esté en `REVIEW_REQUIRED` o `not_available`; no se exige que todos los logos estén verificados.
14. `npm run validate:provider-logos` debe producir un informe estable en la raíz del workspace, sin red, hora de ejecución, PID, rutas absolutas, stacks ni datos volátiles. El código de salida es distinto de cero ante errores de integridad.

## Non-Functional Requirements

- **Accesibilidad:** `ProviderLogo` acepta `altMode`. En modo informativo usa exactamente `alt="Logo de <proveedor>"`; en modo decorativo usa `alt=""` y `aria-hidden="true"` cuando el nombre ya está visible. La tarjeta usará decorativo. El fallback conserva el nombre textual y anuncia `Logo no disponible` sin convertirlo en un nombre de provider falso. La imagen del Guardián mantiene `alt=""`.
- **Layout:** `next/image` recibirá siempre `width` y `height` derivados del registro o de una dimensión segura; el wrapper mantiene proporción estable, área de seguridad, límites responsive y `object-fit: contain`. Se usará `loading="lazy"`, no `preload`, en la rejilla. Next 16 documenta `preload` y no se usará el prop obsoleto `priority`.
- **Identidad visual:** tarjeta blanca y paleta de ConcienTIC intactas; solo `--guardian-color` puede gobernar la zona superior. No se aplican filtros, máscaras, `mix-blend-mode`, tintes o `brandColor` al logo.
- **Seguridad:** nunca se cargan logos remotos ni se añade `remotePatterns`. El validador inspecciona SVG con `saxes` en modo estricto, rechaza DTD/entidades, `script`, `foreignObject`, `style`, eventos, referencias `href`/`xlink:href`, `url(...)`, datos embebidos y elementos/atributos fuera de allowlist. PNG/JPG deben tener firma/mime compatible.
- **Rendimiento:** tamaño máximo 250 KB para SVG y 500 KB para PNG/JPG, salvo excepción documentada que mantenga `REVIEW_REQUIRED`. Los assets son locales y no añaden fetch por tarjeta.
- **Determinismo:** IDs, aliases, proveedores, logos, asignaciones, errores y warnings se ordenan por código y luego por identificador. Dos ejecuciones sobre los mismos bytes generan el mismo JSON.
- **Compatibilidad:** TypeScript estricto, Next.js `16.3.4`, React `19.2.8`, App Router, Tailwind CSS 4/CSS existente, `zod` ya instalado para el parseo runtime y Node `node:test` para pruebas. `saxes@6.0.0` será la única dependencia nueva y solo de desarrollo.

## Acceptance Criteria

1. `data/providers.json` y `data/provider-logo-registry.json` existen, son JSON válido y tienen `schemaVersion`, IDs únicos y contratos deterministas. Cada provider contiene exactamente los campos obligatorios `id`, `name`, `officialDomain`, `logo`, `brandColor`, `verifiedAt`, `status`, además de aliases y entidades relacionadas. `officialDomain` es host sin esquema; `logo` es ruta local o `null`; `brandColor` es string CSS o `null`; `verifiedAt` es ISO UTC o `null`.
2. El registro contiene todos los providers mínimos: Google, Microsoft, IBM, Cisco, Meta, MinTIC, UNESCO, Comisión Europea, The Open University, Santander, DQ Institute, MIT, Harvard, Stanford, Oxford, Cambridge, Carnegie Mellon, OpenAI, Anthropic, Google DeepMind, AWS, NVIDIA, Hugging Face, Mistral AI, Cohere, Salesforce, Oracle, SAP, Adobe, Mozilla, Internet Society y SENA. OpenLearn aparece como brand/product de The Open University, no como provider independiente.
3. Un alias se representa con `label`, `providerId`, `brandId`, `programId`, `productId` y `resourceId` opcional explícitos. Pruebas demuestran `OpenLearn → the-open-university + openlearn`, `DigComp → european-commission + joint-research-centre`, `IBM SkillsBuild → IBM + ibm-skillsbuild`, `Microsoft Learn → Microsoft + microsoft-learn`, y variantes de Google sin crear providers nuevos.
4. La semántica de estado está implementada y documentada: `verified` requiere identidad exacta, fuente oficial, nota de uso, fecha, asset local y hash/dimensiones válidos; `not_available` significa provider verificado y búsqueda documentada concluida sin asset utilizable/licenciable; `REVIEW_REQUIRED` significa evidencia, identidad, procedencia o licencia incompleta/ambigua. Solo revisión manual con evidencia completa permite transición a `verified`; el validador no la infiere.
5. Todo `status: "verified"` contiene `provider`, `provider_id`, `scope`, `official_domain`, `logo_url`, `asset_type`, `source_page`, `license_or_usage_note`, `verified_at`, `variant`, `local_asset`, `sha256`, `intrinsic_width` e `intrinsic_height` coherentes. El asset existe exactamente en `concientic/public/providers/<slug>/logo.svg|png|jpg`, sin traversal, query ni hash. Los estados no verificables no son elegibles para render.
6. El validador comprueba el conjunto mínimo y las relaciones obligatorias, tipos, límites, alias dirigidos, dominios HTTPS de evidencia, estado, fechas, extensiones, firmas de imagen, hash, dimensiones y seguridad SVG. La ausencia de provider o relación mínima es fatal; un provider pendiente correctamente descrito es warning.
7. La inspección SVG usa `saxes@6.0.0` con allowlist explícita y rechaza DTD, entidades, XML inválido, `script`, `foreignObject`, eventos, estilos ejecutables, referencias externas, `data:` y elementos desconocidos. No existe una implementación crítica basada solo en regex.
8. `library-explorer.tsx` elimina `resource.provider.slice(0, 2).toUpperCase()`, integra `ProviderLogo`, conserva nombre, título, descripción, metadatos, gratuidad, Guardián y CTA exacto `Explorar recurso oficial` + flecha. La tarjeta mantiene su estructura visual y no usa el color del provider.
9. La resolución y el parseo runtime se prueban con un recurso legacy `provider: "Google"` y URL no asignada: debe conservar texto y devolver `REVIEW_REQUIRED`, nunca el logo. También se prueban OpenLearn, Santander sin sustitución automática, MinTIC institucional/programa, IBM SkillsBuild, Microsoft Learn y Google Applied Digital Skills.
10. Un error/ausencia de imagen no rompe ni elimina la tarjeta: `ProviderLogo` oculta el elemento fallido, conserva altura, muestra `Logo no disponible`, registra warning solo en desarrollo y no muta el registro. Un provider desconocido o un estado no verificable nunca usa otro logo.
11. Las pruebas de accesibilidad verifican el `alt` informativo, `alt=""` + `aria-hidden="true"` decorativo, dimensiones, `object-fit: contain`, área de seguridad y `alt=""` de la imagen del Guardián. La UI sigue resolviendo Guardianes exclusivamente desde `src/content/guardians.ts` y conserva exactamente los siete nombres oficiales.
12. `npm run validate:provider-logos` genera `C:\Jose\RepoSiginex\WebConcienTIC\provider-logo-validation-report.json`; el reporte contiene `schemaVersion`, `status`, `summary`, `providers`, `logos`, `assignments`, `errors` y `warnings`, todo ordenado y sin timestamp. Dos ejecuciones consecutivas producen bytes idénticos. Los errores escriben diagnóstico a stderr y terminan con código 1.
13. `npm run test:provider-logos` ejecuta `node --test` sin Jest, Vitest, Playwright ni Testing Library. `tests/provider-logo.test.mjs` cubre normalización/alias dirigido, tuple de niveles, elegibilidad separada, estados, mínimo de providers/relaciones, rutas inseguras, SVG malicioso, firmas/tamaños, fallback y determinismo del reporte.
14. El catálogo externo usa `parseCatalogPage(value: unknown)` con `zod` antes de actualizar estado. Un JSON/envelope inválido conserva `staticCourses`, muestra el error existente y no ejecuta `ResourceCard` con datos sin validar. `provider` y URLs tienen límites explícitos; IDs opcionales solo aceptan slugs.
15. `npm run lint`, `npm run typecheck` y `npm run build` siguen siendo ejecutables desde `C:\Jose\RepoSiginex\WebConcienTIC\concientic`. No se modifica `src/app/api/catalog/resources/route.ts`, Java, PostgreSQL ni el contrato obligatorio de `GET /api/v1/resources`.

## Out of Scope

- Investigar, descargar o declarar como verificados logos concretos durante este paso de diseño.
- Rediseñar la tarjeta, cambiar la paleta de ConcienTIC, recuperar los ocho Guardianes visuales eliminados o reemplazar los siete Guardianes oficiales.
- Crear logos mediante CSS, IA, texto estilizado, iniciales, screenshots, recortes o combinaciones de imágenes.
- Servir logos desde URLs remotas, configurar CDN/`remotePatterns` o descargar assets automáticamente en build.
- Cambiar backend Java, Spring Boot, migraciones, tablas PostgreSQL o el endpoint obligatorio de catálogo.
- Crear CMS, interfaz de revisión legal, pipeline automático de adquisición o sistema de administración de marcas.
- Inferir por título, pertenencia corporativa o alias que una organización publica un recurso; asignaciones ambiguas quedan en revisión.
- Añadir un framework de pruebas o formatear globalmente archivos no relacionados.

## Diseño técnico

### Visión general y decisiones fijadas

La implementación usará TypeScript estricto sobre Next.js App Router `16.3.4`, React `19.2.8`, `next/image`, CSS/Tailwind 4 existente, `zod@4.5.4` ya instalado y Node `node:test`. Se elige un registro JSON en la raíz del workspace porque la investigación, el validador y la auditoría deben leer una fuente común; se evita duplicar identidades dentro de JSX. Se elige una asignación explícita por recurso/URL porque un texto de provider no demuestra la identidad legal ni quién publica una URL.

El runtime no leerá JSON fuera del paquete cliente directamente. `scripts/validate-provider-logos.mjs --write-runtime` validará los registros y generará atómicamente `concientic/src/content/provider-registry.generated.ts`, incluyendo datos tipados y el hash de entrada. Los scripts `predev`, `prelint`, `pretypecheck` y `prebuild` ejecutarán esa validación/generación; un error fatal detiene el proceso antes de compilar. El módulo generado no se edita a mano y no es una segunda fuente de datos.

### Contratos de datos y resolución

`data/providers.json` tendrá un contrato estable como este; los nombres de propiedades son deliberadamente camelCase para el registro canónico:

```json
{
  "schemaVersion": "1.0.0",
  "providers": [{
    "id": "the-open-university",
    "name": "The Open University",
    "officialDomain": "open.ac.uk",
    "logo": null,
    "brandColor": null,
    "verifiedAt": null,
    "status": "REVIEW_REQUIRED",
    "aliases": ["Open University"],
    "brands": [{
      "id": "openlearn",
      "name": "OpenLearn",
      "kind": "brand",
      "aliases": ["OpenLearn"]
    }],
    "programs": [],
    "products": []
  }],
  "aliases": [{
    "label": "OpenLearn",
    "providerId": "the-open-university",
    "brandId": "openlearn",
    "programId": null,
    "productId": null
  }]
}
```

En la implementación final, las entradas de `providers[].aliases` y de cada entidad hija pueden ser strings solo como etiquetas locales, pero **toda etiqueta que el resolver pueda consumir debe aparecer también en la tabla raíz `aliases` con el tuple completo**. Para eliminar ambigüedad, el validador rechazará un label normalizado que tenga dos tuples distintos. Las entidades `brands`, `programs` y `products` usan `{ id, name, kind, aliases }`; sus IDs siempre pertenecen al provider padre. `kind` es `brand`, `program` o `product`.

`data/provider-logo-registry.json` conserva los campos exigidos por la solicitud en snake_case y fija sus tipos:

```json
{
  "schemaVersion": "1.0.0",
  "logos": [{
    "id": "the-open-university-provider-color",
    "provider": "The Open University",
    "provider_id": "the-open-university",
    "scope": "provider",
    "official_domain": "open.ac.uk",
    "logo_url": null,
    "asset_type": null,
    "source_page": null,
    "license_or_usage_note": "Pendiente de revisión oficial.",
    "verified_at": null,
    "variant": "color",
    "status": "REVIEW_REQUIRED",
    "local_asset": null,
    "sha256": null,
    "intrinsic_width": null,
    "intrinsic_height": null
  }],
  "assignments": []
}
```

Los campos `provider`, `official_domain`, `logo_url`, `asset_type`, `source_page`, `license_or_usage_note`, `verified_at`, `variant` y `status` son obligatorios en cada logo, aunque los valores de evidencia/asset sean `null` cuando el estado no es `verified`. `official_domain` siempre es host; `logo_url` y `source_page` son HTTPS o `null`; `asset_type` es `svg`, `png`, `jpg` o `null`; `variant` es `color`, `monochrome`, `light`, `dark` u `other`; `status` es exactamente `verified`, `not_available` o `REVIEW_REQUIRED`. `local_asset` es una ruta web que empieza por `/providers/`, sin `..`, query o fragmento.

`assignments` contiene la evidencia de pertenencia del recurso, no solo el logo:

```json
{
  "resourceId": "static-1",
  "sourceUrl": "https://www.open.edu/openlearn/digital-computing/digital-skills-succeeding-digital-world",
  "providerId": "the-open-university",
  "brandId": "openlearn",
  "programId": null,
  "productId": null,
  "provider_logo": "the-open-university-provider-color",
  "program_logo": null,
  "product_logo": null,
  "evidence_page": "https://www.open.ac.uk/",
  "review_status": "REVIEW_REQUIRED",
  "review_note": "Pendiente de verificación manual de publisher y asset."
}
```

Una assignment exige `resourceId` y `sourceUrl`; para recursos dinámicos se acepta además una URL canónica exacta si el backend no entrega ID estable, pero no una coincidencia por hostname solamente. El resolver devuelve `{ identityMatch, logoEligibility, provider, brand, program, product, resourceId, providerLogo, programLogo, productLogo, diagnostics }`. `identityMatch` puede existir sin `logoEligibility`; solo `verified` + assignment coincidente habilita el `ProviderLogo`.

La normalización de `normalizeProviderLabel` aplica Unicode NFKC, trim, minúsculas, eliminación de diacríticos y separación uniforme. Rechaza string vacío y longitudes mayores a 160. La precedencia es ID válido explícito, alias dirigido exacto, nombre canónico y finalmente desconocido. Un alias ambiguo no se resuelve por orden de registro. `normalizeResourceUrl` compara URL absoluta HTTPS sin fragmento y conserva path/query necesarios; no permite asignar por dominio solamente.

En `catalog.ts`, `CatalogResource` añade campos opcionales `providerId`, `brandId`, `programId`, `productId`, `resourceId`, `provider_logo`, `program_logo`, `product_logo`. `static-courses.ts` asigna `resourceId` estable y tuples explícitos para los casos que se hayan revisado; mientras tanto sus logos permanecen en fallback. Los filtros actuales siguen comparando el label de `provider` para no romper la UI.

### Parseo runtime del catálogo y errores

Crear `parseCatalogPage(value: unknown)` en `concientic/src/lib/catalog-runtime.ts`, usando `zod` ya instalado. El envelope exige `items` array, `page`, `size`, `total` y `totalPages` enteros no negativos; cada item exige `id`, `title`, `shortDescription`, `provider` no vacío de hasta 160 caracteres, URL HTTPS válida y los campos actuales con tipos correctos. Arrays (`language`, `topics`, `audience`) solo contienen strings con límites; IDs opcionales son slugs ASCII de 1–80 caracteres.

Si el JSON no parsea, el HTTP no es 2xx o el envelope es inválido, `LibraryExplorer` conserva `staticCourses`, muestra el `role="alert"` existente y permite reintentar. Si solo algunos items son inválidos, el parser devuelve los válidos y un diagnóstico estable; no se entrega un item inválido a `ResourceCard`. El `route.ts` permanece opaco: no valida logos, no transforma el body ni requiere cambios Java/PostgreSQL.

### ProviderLogo y tarjeta

Crear `concientic/src/components/library/provider-logo.tsx`. Recibe `providerName`, `assetPath`, `status`, `altMode`, dimensiones y clases. Rechaza internamente cualquier `assetPath` que no sea ruta local validada bajo `/providers/`, aunque el registro ya lo haya comprobado. Usa `next/image` con `src` local, `width`, `height`, `sizes="(max-width: 768px) 50vw, 180px"`, `loading="lazy"`, `style/object-fit: contain` y sin `preload`.

El componente será Client Component por `onError`. El estado de fallo es local: no reintenta, no eleva estado a `verified`, no modifica JSON y en desarrollo puede emitir un único `console.warn` con el ID del logo. En modo informativo usa `alt={`Logo de ${providerName}`}`; en modo decorativo usa `alt=""` y `aria-hidden="true"`. `ResourceCard` elegirá decorativo porque el nombre está visible; otras superficies pueden elegir informativo.

En `library-explorer.tsx`, `ResourceCard` llamará a `resolveProviderLogo(resource)` y sustituirá solamente el `library-card__provider-mark`. `guardianFor` seguirá usando `guardians.find((guardian) => guardian.name === resource.guardianPrimary)`. La ilustración del Guardián seguirá con `Image`, `alt=""`, `132x122`; el wrapper del provider conserva altura aunque falte asset. El CSS de `globals.css` añadirá `min-inline-size`, `block-size`, `padding` de área de seguridad, `aspect-ratio` y `object-fit: contain`, sin deformar ni alterar artwork.

La ruta CTA será `Explorar recurso oficial <span aria-hidden="true">→</span>`. No se conservará el literal anterior `Explorar recurso`, porque el requisito funcional fija explícitamente el nuevo texto.

### Assets locales, parser y validador

Los logos de provider se guardan en `concientic/public/providers/<provider-id>/logo.svg` o `.png`/`.jpg`. Next 16 sirve los archivos de `public` desde `/`; por ello el `src` del componente es `/providers/<provider-id>/logo.svg`. `next/image` requiere `alt` y dimensiones para una ruta string local, y las dimensiones reservan espacio y evitan CLS. No se configura `remotePatterns`.

`scripts/validate-provider-logos.mjs` será ESM y resolverá rutas desde `import.meta.url`, nunca desde `process.cwd()`: `data/` y `provider-logo-validation-report.json` están en la raíz del workspace; `concientic/public/providers` y el módulo generado están bajo el frontend. Leerá los dos JSON con UTF-8, validará antes de generar cualquier runtime y escribirá el informe de mejor esfuerzo de forma determinista.

Para SVG, `saxes@6.0.0` se instancia en modo estricto y namespace-aware. El algoritmo debe: rechazar cualquier evento `doctype`/entidad; permitir solo un conjunto explícito de elementos SVG de presentación (`svg`, `g`, `path`, `rect`, `circle`, `ellipse`, `line`, `polyline`, `polygon`, `defs`, `clipPath`, `mask`, `title`); exigir namespace SVG, `viewBox` numérico o width/height numéricos; rechazar `script`, `foreignObject`, `style`, `animate`, eventos `on*`, atributos desconocidos peligrosos y cualquier `href`/`xlink:href`; rechazar valores con `url(`, `http:`, `https:`, `data:`, `javascript:` o referencias externas. Se aceptan únicamente atributos de presentación y geometría definidos por allowlist. El archivo se procesa como bytes locales, sin ejecutar ni resolver red.

Para PNG/JPG se comprueban firmas binarias, extensión/mime, dimensiones positivas y límite de bytes. Para todos los assets se compara SHA-256. Un asset `verified` con error XML, firma, hash, ruta, tamaño o dimensiones es error fatal. Un archivo pendiente no se publica ni se inspecciona como elegible.

El validador comprueba:

- JSON, schemaVersion, arrays, campos obligatorios, límites, IDs y unicidad.
- Providers mínimos y mapa versionado `REQUIRED_PROVIDER_IDS`/`REQUIRED_RELATIONSHIPS`.
- Alias dirigidos sin colisiones y existencia de todos los IDs referenciados.
- `officialDomain`/`official_domain` como host válido; `source_page`/`logo_url` HTTPS y host igual al oficial o subdominio declarado explícitamente.
- Semántica de estados y transición: `verified` exige evidencia completa; `not_available` exige identidad verificada, búsqueda documentada y ausencia justificada de asset; `REVIEW_REQUIRED` exige nota de revisión y nunca es elegible.
- Asignaciones con `resourceId`, URL exacta, tuple coherente y `provider_logo` de scope provider.
- Rutas locales, extensiones, existencia, hash, dimensiones, firmas y parser SVG seguro.

Errores de lectura, JSON, esquema, mínimos, colisión, `verified` sin evidencia, asset inseguro/ausente/hash incorrecto o asignación contradictoria son fatales y devuelven código 1. Provider pendiente correctamente descrito, ausencia de asset para `not_available` y logo no verificable producen warning y código 0. Si no puede escribirse el informe, el proceso devuelve 1 y escribe el diagnóstico disponible en stderr.

El informe contiene `schemaVersion`, `status` (`passed`, `passed_with_warnings` o `failed`), `summary`, `providers`, `logos`, `assignments`, `errors` y `warnings`. Cada diagnóstico tiene `code`, `id` y `message`; no incluye fecha de ejecución, rutas absolutas, stack ni entorno. Se ordena por `code`, `id` y `message`; se serializa con indentación fija y salto final fijo.

### Scripts, generación y pruebas

Añadir en `concientic/package.json`:

```json
{
  "scripts": {
    "predev": "node scripts/validate-provider-logos.mjs --write-runtime",
    "prelint": "node scripts/validate-provider-logos.mjs --write-runtime",
    "pretypecheck": "node scripts/validate-provider-logos.mjs --write-runtime",
    "prebuild": "node scripts/validate-provider-logos.mjs --write-runtime",
    "validate:provider-logos": "node scripts/validate-provider-logos.mjs",
    "test:provider-logos": "node --test ../tests/provider-logo.test.mjs"
  }
}
```

Añadir `saxes: "6.0.0"` exacto en `devDependencies` y actualizar el lockfile mediante el gestor existente; no instalar un runner de tests. Las funciones puras del validador se exportan sin ejecutar CLI al importarlas. `tests/provider-logo.test.mjs` usa `node:test` y `node:assert/strict`; puede usar fixtures temporales en `tests/fixtures/` para probar JSON inválido, alias ambiguo, asset traversal, SVG inseguro, hash y determinismo.

Las pruebas unitarias cubren normalización Unicode, tuple dirigido, precedencia de IDs, elegibilidad separada, estados, URL exacta, OpenLearn/OU, Santander, MinTIC, JRC, IBM, Microsoft y Google. Las pruebas de integración leen los JSON reales, verifican mínimos y ejecutan el validador dos veces comparando bytes del reporte. Las pruebas de UI se limitan a typecheck, lint, build y revisión/smoke manual porque no existe DOM runner; la lógica importante permanece en funciones puras.

### Pipeline diario y errores operativos

1. **DISCOVER PROVIDER:** identificar organización legal, dominio y publisher del recurso; crear candidato.
2. **VERIFY PROVIDER:** comprobar relación entre organización y URL; resolver alias dirigido; si es ambiguo, `REVIEW_REQUIRED`.
3. **DISCOVER OFFICIAL BRAND ASSETS:** buscar en el orden de procedencia autorizado.
4. **VERIFY LOGO:** comprobar correspondencia exacta, variante, licencia/usage note, checksum, dimensiones y formato.
5. **ADD PROVIDER REGISTRY:** actualizar ambos JSON, incluyendo campos pendientes y razón del estado.
6. **DOWNLOAD/REFERENCE ASSET:** conservar el asset oficial local solo si es utilizable/licenciable; jamás publicar remoto.
7. **ASSIGN LOGO TO RESOURCE:** añadir tuple y `resourceId`/URL exacta en `assignments`; no inferir desde el título.
8. **UPDATE CATALOG:** propagar IDs opcionales al catálogo sin cambiar `guardianPrimary`.

Una URL oficial caída o un asset ausente es recuperable y afecta solo a ese registro. Un JSON corrupto o un `verified` inconsistente es fatal para CI. Un payload externo inválido conserva los cursos estáticos y permite reintento. Un fallo de imagen es recuperable en UI. Una duda de identidad/licencia bloquea ese logo, no toda la Biblioteca.

### Archivos e integración

Modificar o añadir únicamente, salvo evidencia de necesidad:

- `C:\Jose\RepoSiginex\WebConcienTIC\data\providers.json`: providers, aliases y entidades relacionadas.
- `C:\Jose\RepoSiginex\WebConcienTIC\data\provider-logo-registry.json`: logos, evidencia y assignments.
- `C:\Jose\RepoSiginex\WebConcienTIC\concientic\public\providers\<slug>\logo.svg|png|jpg`: solo assets verificables.
- `C:\Jose\RepoSiginex\WebConcienTIC\concientic\src\lib\catalog.ts`: campos opcionales tipados.
- `C:\Jose\RepoSiginex\WebConcienTIC\concientic\src\lib\catalog-runtime.ts`: parseo runtime con zod.
- `C:\Jose\RepoSiginex\WebConcienTIC\concientic\src\content\static-courses.ts`: resource IDs y assignments explícitos revisados.
- `C:\Jose\RepoSiginex\WebConcienTIC\concientic\src\content\provider-registry.generated.ts`: salida generada, no editable.
- `C:\Jose\RepoSiginex\WebConcienTIC\concientic\src\lib\provider-registry.ts`: acceso tipado al generado.
- `C:\Jose\RepoSiginex\WebConcienTIC\concientic\src\lib\provider-resolution.ts`: normalización, tuple e idoneidad.
- `C:\Jose\RepoSiginex\WebConcienTIC\concientic\src\components\library\provider-logo.tsx`: render, accesibilidad y error de imagen.
- `C:\Jose\RepoSiginex\WebConcienTIC\concientic\src\components\library\library-explorer.tsx`: integración mínima de tarjeta y parseo runtime.
- `C:\Jose\RepoSiginex\WebConcienTIC\concientic\src\app\globals.css`: wrapper responsive del logo.
- `C:\Jose\RepoSiginex\WebConcienTIC\concientic\scripts\validate-provider-logos.mjs`: CLI, parser seguro y reporte.
- `C:\Jose\RepoSiginex\WebConcienTIC\concientic\package.json` y `package-lock.json`: scripts y `saxes@6.0.0` fijado.
- `C:\Jose\RepoSiginex\WebConcienTIC\tests\provider-logo.test.mjs`: pruebas `node:test`.
- `C:\Jose\RepoSiginex\WebConcienTIC\docs\provider-logo-system.md`: operación, procedencia y licencias.
- `C:\Jose\RepoSiginex\WebConcienTIC\provider-logo-validation-report.json`: artefacto generado, no editable.

No modificar `C:\Jose\RepoSiginex\WebConcienTIC\concientic\src\content\guardians.ts`, la ruta `src/app/api/catalog/resources/route.ts`, código Java, migraciones, PostgreSQL ni archivos no relacionados. Si el contrato opcional exige una adaptación futura del backend, debe aprobarse y justificarse por separado.

## Respuestas a la revisión de diseño

- **PLD-001 HIGH — atendido.** Se fijan `name`, `officialDomain`, `logo`, `brandColor`, `verifiedAt` y `status` en `providers.json`; se define `officialDomain`/`official_domain` como host sin esquema y se documenta el mapeo camelCase/snake_case.
- **PLD-002 HIGH — atendido.** El modelo incluye `resourceId`, entidades `brands`/`programs`/`products` y alias dirigidos con todos los destinos explícitos. El resolver devuelve el tuple completo y no infiere niveles por título.
- **PLD-003 HIGH — atendido.** Se separan `identityMatch` y `logoEligibility`; solo assignment revisada por `resourceId` o URL exacta más logo `verified` permite render. El provider textual legacy queda en fallback.
- **PLD-004 MEDIUM — atendido.** Se definen semántica, evidencia y transición manual de `verified`, `not_available` y `REVIEW_REQUIRED`, además de códigos de validador.
- **PLD-005 MEDIUM — atendido.** Se establecen `REQUIRED_PROVIDER_IDS` y `REQUIRED_RELATIONSHIPS` versionados; la ausencia es fatal y el estado pendiente no lo es.
- **PLD-006 MEDIUM — atendido.** Se define `parseCatalogPage(value: unknown)` con zod, límites, IDs, URLs y degradación para envelope/items inválidos antes de `ResourceCard`.
- **PLD-007 HIGH — atendido.** Se elige `saxes@6.0.0` fijado, modo estricto, allowlist de XML/SVG y rechazos concretos de DTD, entidades, scripts, estilos y referencias externas; no se delega seguridad a regex.
- **PLD-008 MEDIUM — atendido.** Se fija un único CTA: `Explorar recurso oficial` con flecha decorativa `→`, y se exige el mismo literal en tarjeta y pruebas.
