# Diseño · Catálogo de piezas (V2)

Referencia visual: `preview/Catalogo_V2_revision.html`, `preview/vista_escritorio.jpg`, `preview/vista_celular.jpg`.

## Arquitectura
HTML estático generado por Python (`generator/catalogo_v2.py`) a partir de los datos de `generator/catalogo.py`. Sin build de front-end. Un `<script>` inline con el objeto `D` (datos de piezas para el modal) y tres comportamientos: filtrar, abrir/cerrar modal, scroll a `#piezas`.

## Modelo de datos (`catalogo.py → P`)
```
(slug, nombre, categoría, imagen, "encaje;object-position", descripción, [detalles], precio|None)
```
- `categoría` ∈ `bolsos | accesorios | hogar | navidad`
- `precio`: entero COP o `None` → "Precio a consultar"
- Configuración: `WA` (número con indicativo, sin +), `MONEDA = "COP"`.

## Secciones (orden)
1. `.topbar` – mensaje de servicio (fondo `#5c3316`).
2. `header` sticky – marca + navegación (oculta en ≤600 px).
3. `.c-hero` – texto + video 4:5 con sello rosa "hecho con calma" y etiqueta "Así se ve de cerca".
4. `.cats` – 4 tarjetas 3:4 con degradado inferior.
5. `.filters#piezas` sticky + `.grid#grid` de `.pc`.
6. `.process` – bloque café con 3 videos.
7. `#personalizados` – texto + foto `assets/fotos/v3_1.webp`.
8. `#como-pedir` – 4 pasos numerados.
9. `.learn` – enlace a la Escuela con `instructora.webp`.
10. `#preguntas` – FAQ.
11. `.modal#modal`, `.sticky-wa`, `footer`.

## Responsive
| Ancho | Grilla | Otros |
|---|---|---|
| >980 | 4 col | hero 2 col |
| ≤980 | 3 col | |
| ≤860 | 3 col | hero, personalizados, aprende y modal a 1 col; categorías 2×2; videos del proceso en carrusel horizontal |
| ≤600 | 2 col | sin descripción en tarjeta, botón Pedir solo icono, menú oculto |

## Medición (recomendado)
Cada tarjeta `.pc` tiene `data-id` (slug); úsalo para medir clics por pieza (GA4 evento `click_whatsapp` con `item_name`).
