# Escenarios de prueba del sistema

Antes de una ejecución real deben cubrirse como mínimo:

- URL válida y URL rota.
- Redirección y cambio de URL.
- HTTP 200 con contenido retirado.
- `FREE`, `AUDIT_FREE`, `FREE_CONTENT_PAID_CERTIFICATE`, `PARTIAL_FREE`, `TRIAL` y `PAID`.
- Duplicado por URL, proveedor, título y similitud semántica.
- Fuente no confiable.
- Idioma no confirmado.
- Clasificación primaria/secundaria.
- Guardian primario/secundarios oficiales.
- Snapshot e historial.
- Falla parcial de fuente.
- Idempotencia y reintento.
- Filtros exactos de la Biblioteca abierta.
