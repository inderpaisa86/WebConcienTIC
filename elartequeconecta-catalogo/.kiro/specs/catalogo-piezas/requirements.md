# Requisitos · Catálogo de piezas (V2)

## Introducción
Landing estática en `/elartequeconecta/catalogo` que muestra 22 piezas de macramé hechas a mano en 4 categorías y convierte la visita en una conversación de WhatsApp con un mensaje prellenado.

## Requisito 1 · Portada con video
**Historia:** Como visitante, quiero ver de inmediato piezas reales en movimiento para confiar en que son hechas a mano.
1. CUANDO cargue la página, el sistema DEBERÁ reproducir `assets/catalogo/video/hero.mp4` en bucle, silenciado, con `playsinline` y póster `hero.jpg`.
2. SI el navegador no reproduce video, el sistema DEBERÁ mostrar el póster.
3. La portada DEBERÁ incluir: etiqueta, título "Piezas tejidas *nudo a nudo,* para ti.", texto breve, CTA "Ver las piezas" (ancla `#piezas`), CTA secundario "Quiero algo personalizado" (ancla `#personalizados`) y 4 chips de confianza.

## Requisito 2 · Categorías
1. El sistema DEBERÁ mostrar 4 tarjetas (Bolsos, Accesorios, Hogar, Navidad) con foto, nombre, línea corta y conteo de piezas calculado de los datos.
2. CUANDO el usuario toque una categoría, el sistema DEBERÁ aplicar ese filtro y desplazarse a `#piezas`.

## Requisito 3 · Filtros y grilla
1. El sistema DEBERÁ mostrar una barra de filtros fija bajo el encabezado: Todo (n), Bolsos, Accesorios, Hogar, Navidad, y un contador "n piezas".
2. CUANDO se seleccione un filtro, el sistema DEBERÁ mostrar solo las piezas de esa categoría y actualizar el contador.
3. La grilla DEBERÁ ser de 4 columnas en escritorio, 3 en ≤980 px y 2 en ≤600 px.
4. Cada tarjeta DEBERÁ mostrar: foto 4:5, categoría, nombre, una frase (oculta en ≤600 px), precio o "Precio a consultar" y botón "Pedir".

## Requisito 4 · Vista rápida
1. CUANDO el usuario toque la foto de una pieza, el sistema DEBERÁ abrir un modal con foto, categoría, nombre, descripción completa, lista de detalles, precio y botón "Pedir por WhatsApp".
2. El modal DEBERÁ cerrarse con el botón ×, clic fuera o tecla Escape.

## Requisito 5 · Pedido por WhatsApp
1. Cada botón de pedido DEBERÁ abrir `https://wa.me/<WA>?text=<mensaje>` en pestaña nueva con: "Hola, El Arte que Conecta 👋 Me interesa el <pieza> del catálogo. ¿Me cuentas precio, colores disponibles y tiempo de entrega?"
2. El botón flotante "Hacer un pedido" y el CTA de personalizados DEBERÁN usar sus mensajes generales.
3. SI `WA` sigue en `57XXXXXXXXXX`, la página NO DEBERÁ publicarse.

## Requisito 6 · Secciones de cierre
Detrás de cada pieza (3 videos en bucle), Personalizados, Cómo pedir (4 pasos), Aprende (enlace a `/escuela`), Preguntas frecuentes (acordeón `<details>`), pie.

## Requisito 7 · Calidad
1. Lighthouse móvil: Performance ≥ 85, Accessibility ≥ 95, SEO ≥ 95.
2. Imágenes de la grilla con `loading="lazy"` y `alt` descriptivo.
3. Metadatos: title, description, canonical, Open Graph y JSON-LD `ItemList` de `Product`.
4. Sin errores en consola; funciona sin conexión a CDNs salvo Google Fonts.
