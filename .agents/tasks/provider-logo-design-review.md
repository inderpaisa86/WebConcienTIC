# Revisión de diseño: sistema de logos oficiales de proveedores

**Documento revisado:** `C:\Jose\RepoSiginex\WebConcienTIC\.agents\tasks\provider-logo-design.md`  
**Alcance de la revisión:** diseño fresco contra el código actual de `concientic`. No se ejecutaron builds ni suites, conforme a la solicitud.  
**Veredicto:** `CHANGES_REQUESTED` porque hay hallazgos HIGH y MEDIUM.

## Hallazgos

1. **HIGH — El esquema de `data/providers.json` no contiene el contrato exigido.**  
   **Ubicación:** «Contratos de datos», ejemplo conceptual de `data/providers.json`, y criterio de aceptación 1. El diseño define `canonical_name` y `official_domain`, pero no define los campos requeridos `name`, `officialDomain`, `logo`, `brandColor`, `verifiedAt` y `status`. Además, usa `official_domain` como URL completa en un ejemplo del registro de logos y como dominio en otras reglas, sin fijar un tipo único. Por tanto, dos implementaciones pueden producir datos incompatibles y el validador no tendría un esquema determinista que hacer cumplir.  
   **Corrección concreta:** fijar el esquema antes de implementar. Por ejemplo:
   ```json
   {
     "schemaVersion": "1.0.0",
     "providers": [{
       "id": "the-open-university",
       "name": "The Open University",
       "officialDomain": "open.ac.uk",
       "logo": "/providers/the-open-university/logo.svg",
       "brandColor": null,
       "verifiedAt": null,
       "status": "REVIEW_REQUIRED",
       "aliases": [],
       "brands": [],
       "programs": [],
       "products": []
     }]
   }
   ```
   Documentar `logo` como ruta local o `null`, `brandColor` como valor de presentación no usado para tematizar la tarjeta, y `verifiedAt` como ISO UTC o `null`. Si el registro de auditoría conserva snake_case, definir expresamente el mapeo entre ambos contratos; no mezclar host (`open.ac.uk`) con URL (`https://.../`) en el mismo campo.

2. **HIGH — La diferenciación provider/brand/program/product/resource y los destinos de alias no están modelados de forma implementable.**  
   **Ubicación:** «Resolución de identidades y compatibilidad», `ResourceIdentity`, y el ejemplo de `providers.json`. `ResourceIdentity` omite `resourceId`, aunque el diseño afirma conservarlo; `providers.json` solo ejemplifica `brands` y no define los esquemas de `programs` ni `products`. Los alias son únicamente strings. Eso no permite representar sin ambigüedad que `OpenLearn` apunte simultáneamente a `the-open-university` y a `openlearn`, ni que `Microsoft Learn`, `IBM SkillsBuild`, Google Applied Digital Skills o Santander Open Academy tengan un destino de marca/producto concreto. Incluir `OpenLearn` en los aliases del provider y en los de la marca deja dos matches posibles.  
   **Corrección concreta:** añadir `resourceId` al contrato del recurso y convertir los alias en objetos dirigidos, por ejemplo:
   ```json
   {
     "label": "OpenLearn",
     "providerId": "the-open-university",
     "brandId": "openlearn",
     "programId": null,
     "productId": null
   }
   ```
   Definir `brands`, `programs` y `products` con `id`, `name`, `kind` y aliases dirigidos, y especificar que un alias solo puede producir el tuple completo declarado. El resolver debe devolver `provider`, `brand`, `program`, `product` y `resource` con IDs, no inferir los niveles faltantes por el texto del título.

3. **HIGH — La resolución heredada por nombre puede publicar un logo sin evidencia del proveedor real del recurso.**  
   **Ubicación:** requisitos funcionales 6 y 10, y «Resolución de identidades y compatibilidad». El diseño dice que los nombres heredados se resolverán mediante alias y que, mientras existan entradas heredadas, el adaptador resolverá `resource.provider`. Eso permite que una cadena como `Google`, `Microsoft` o `Santander` seleccione un logo `verified` aunque no exista una asignación revisada para ese recurso ni una comprobación de que la organización identificada publica la URL. El propio diseño reconoce que los nombres heredados no son prueba suficiente, por lo que ambas reglas son incompatibles.  
   **Corrección concreta:** separar `identityMatch` de `logoEligibility`. Un alias textual puede devolver identidad y diagnóstico, pero el logo solo será elegible si el recurso trae un `providerId` explícito validado o si existe una tabla versionada de asignaciones por `resourceId`/URL de origen con evidencia revisada. Para recursos legacy solo con `provider`, devolver `REVIEW_REQUIRED` y fallback textual, incluso si el alias es inequívoco. Añadir a las pruebas un caso `provider: "Google"` con URL no asignada que confirme que no se renderiza el logo.

4. **MEDIUM — Los tres estados están enumerados, pero no está definida su semántica operativa.**  
   **Ubicación:** «Supuestos y ambigüedades explícitos» y sección del validador. Un registro sin asset utilizable puede quedar en `not_available` o `REVIEW_REQUIRED`, pero no se establece cuándo corresponde cada estado, qué evidencia mínima permite pasar de uno al otro ni qué evento autoriza la transición a `verified`. Ambos estados son no elegibles para UI, pero significan cosas distintas para auditoría y para el pipeline diario.  
   **Corrección concreta:** fijar esta tabla en el diseño y en el validador: `verified` = identidad exacta, fuente oficial, nota de uso, fecha, asset local y hash comprobados; `not_available` = identidad y proveedor verificados, búsqueda documentada concluida, pero no existe asset utilizable/licenciable; `REVIEW_REQUIRED` = identidad, correspondencia, licencia o procedencia aún ambiguas/incompletas. Solo un cambio manual de revisión con evidencia completa puede pasar a `verified`; el validador debe rechazar transiciones implícitas y reportar la razón del estado.

5. **MEDIUM — La lista mínima de proveedores no es una invariante validada.**  
   **Ubicación:** requisito funcional 7 y criterios de aceptación 1 y 9. El diseño enumera los proveedores iniciales en prosa, pero el validador solo describe unicidad, tipos, aliases y referencias. No exige que existan los IDs mínimos ni que sus relaciones obligatorias (OpenLearn/Open University, JRC/Comisión Europea, IBM SkillsBuild, Microsoft Learn, Google Applied Digital Skills y Santander Open Academy) estén presentes. Un `providers.json` vacío o incompleto podría pasar el esquema estructural y violar el alcance solicitado.  
   **Corrección concreta:** declarar una constante versionada `REQUIRED_PROVIDER_IDS` y un mapa de relaciones obligatorias dentro del contrato/validador. El comando debe emitir error fatal si falta cualquier provider mínimo o su relación requerida; añadir un test que compare ese conjunto, sin exigir que todos tengan logo `verified`.

6. **MEDIUM — La entrada del catálogo sigue siendo un cast no validado y no se especifica el adaptador runtime.**  
   **Ubicación:** «Compatibilidad» y criterio de aceptación 13, contrastados con `src/app/api/catalog/resources/route.ts` y `library-explorer.tsx`. La ruta actual reenvía el body del backend sin transformación y el cliente hace `response.json() as CatalogPage`; no hay validación en runtime de `provider`, IDs opcionales, arrays ni URLs. Decir que `CatalogResource` aceptará propiedades opcionales no define cómo se rechaza o degrada un payload externo malformado, ni cómo se conserva el fallback sin una excepción durante el render.  
   **Corrección concreta:** especificar una función `parseCatalogPage(value: unknown): CatalogPage` en el adaptador, con un esquema runtime (usar el `zod` ya presente o una validación manual explícita) que exija `provider` string no vacío de máximo 160 y valide `providerId`, `brandId`, `programId`, `productId` y `resourceId` como slugs opcionales. Un elemento inválido debe producir diagnóstico/fallback o descartarse según una regla escrita; nunca debe llegar por cast directo a `ResourceCard`. Esto mantiene la ruta backend opaca y no requiere cambios Java/PostgreSQL.

7. **HIGH — La validación segura de SVG no es factible con las restricciones técnicas actualmente fijadas.**  
   **Ubicación:** requisitos de seguridad y «Validación `scripts/validate-provider-logos`». Se exige verificar estructura XML, `viewBox`, scripts, `foreignObject`, `href`/`xlink:href` externos y referencias externas, pero también se exige Node estándar y ninguna dependencia de parser. El diseño no define un parser ni un algoritmo seguro; comprobaciones por regex no garantizan detectar XML válido, namespaces, entidades o todas las formas de referencia ejecutable/externa. La aceptación podría declarar verificado un SVG que el validador no inspeccionó correctamente.  
   **Corrección concreta:** elegir explícitamente una implementación segura antes de codificar: permitir una dependencia XML fijada en `package-lock.json` y usar un parser SAX seguro con una allowlist de elementos/atributos, rechazando DTD, entidades, `script`, `foreignObject`, estilos peligrosos y cualquier referencia externa; o, si se mantiene la prohibición de parser, retirar SVG del conjunto aceptable y aceptar solo PNG/JPG firmados. No dejar una validación XML crítica como un requisito abierto de “inspeccionar”.

8. **MEDIUM — El texto exacto del CTA es contradictorio.**  
   **Ubicación:** requisito funcional 1, criterio de aceptación 5, «Componentes e integración» y código actual. El diseño exige `Explorar recurso oficial →`, pero también dice conservar la llamada existente. En `src/components/library/library-explorer.tsx` el texto actual es `Explorar recurso` más una flecha decorativa `→`; no contiene `oficial`. Esta discrepancia deja abierta una regresión de contenido y hace que “CTA exacto” no sea verificable.  
   **Corrección concreta:** fijar un único literal en todos los apartados y en el test/inspección de aceptación. Si el requisito es el del diseño, usar exactamente `Explorar recurso oficial` y mantener `→` con `aria-hidden="true"`; si se debe preservar la UI actual, cambiar el requisito y el criterio para exigir exactamente `Explorar recurso`. No usar “conservar el actual” y “exacto” simultáneamente.

## Verified Assumptions

- `src/content/guardians.ts` contiene exactamente siete Guardianes: Emi, Locky, Lex, Byte, Detective DQ, Nexo y Nova, con sus colores e imágenes. `library-explorer.tsx` importa ese módulo y `guardianFor` busca por `guardian.name === resource.guardianPrimary`.
- La tarjeta actual sigue generando el marcador artificial con `resource.provider.slice(0, 2).toUpperCase()`. El diseño identifica correctamente ese punto de sustitución.
- La imagen del Guardián en la tarjeta usa `alt=""`, dimensiones `132x122` y `object-fit` se conserva como preocupación del diseño; no hay evidencia en el diseño de que el logo de proveedor vaya a controlar el color del Guardián.
- La ruta `src/app/api/catalog/resources/route.ts` solo permite reenviar parámetros al endpoint `/api/v1/resources`, reenvía el body como texto y no descarga ni transforma logos. No se observó necesidad de modificar backend, migraciones o PostgreSQL para esta integración frontend.
- El catálogo estático actual usa solo `provider` textual y `guardianPrimary`; contiene casos heredados de OpenLearn, Santander, IBM SkillsBuild dentro de IBM, Microsoft Learn y Google Applied Digital Skills, por lo que la compatibilidad de alias debe resolver datos reales, no solo fixtures nuevos.
- El `package.json` actual no tiene scripts de test ni `validate:provider-logos`; tampoco se encontró infraestructura de provider logos. La elección propuesta de `node:test` es compatible con el runner disponible en Node, pero aún debe implementarse y probarse.
- El diseño sí fija, de forma consistente, que los estados distintos de `verified` no renderizan un asset, que no se usan URLs remotas en runtime, que el fallback mantiene el nombre textual y que el reporte debe ser determinista y sin timestamp.

## Unverified/Wrong Assumptions

- Es incorrecto asumir que el esquema propuesto de `providers.json` satisface el contrato solicitado: faltan `name`, `officialDomain`, `logo`, `brandColor`, `verifiedAt` y `status`.
- No está verificado que ningún registro inicial tenga evidencia oficial, licencia, checksum o asset local; el propio diseño declara esa investigación fuera de alcance. Por ello, ningún registro puede comenzar como `verified` sin la fase posterior documentada.
- No está verificado que un alias textual de `resource.provider` pruebe quién publica la URL del recurso. El diseño no puede usar esa cadena como evidencia individual y a la vez cumplir su regla de no inferencia.
- No está definido ni verificado cómo se representan los alias dirigidos, `resourceId`, programas y productos en JSON; el ejemplo actual no resuelve las relaciones OpenLearn, Santander Open Academy, JRC, IBM SkillsBuild, Microsoft Learn y Google Applied Digital Skills de manera inequívoca.
- No está verificado que una implementación sin parser XML pueda cumplir de forma segura todos los controles SVG exigidos. Esa compatibilidad debe resolverse explícitamente antes de implementar el validador.
- El claim de conservar “el CTA actual” no coincide con el literal exigido por otras secciones: el código actual no muestra la palabra `oficial`.

No se ejecutaron builds, lint, typecheck ni suites, porque esta tarea solicita revisar el diseño, no validar código implementado.
