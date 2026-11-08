# El Arte que Conecta: landings V8

## Qué cambió en V7

**Escuela** (`/elartequeconecta/escuela`) reproduce la estructura de la landing actual en Hotmart (elartequeconecta.hotmart.host), con mejoras. Mantiene la identidad visual de la V6: mismos colores, Fraunces y DM Sans, sin insignias.

Orden de las secciones:

1. Aviso superior de Noviembre Black.
2. Hero: "Aprende macramé paso a paso para decorar, vender o emprender", con botón de compra, precio y garantía.
3. Franja de confianza: garantía de 7 días, clases grabadas, acceso de por vida, pago único y acceso desde cualquier país.
4. "Todo lo que puedes aprender en la escuela": 3 niveles con carrusel de fotos reales.
   - Principiante: Llavero, Portamacetas, Tapiz de chakras y Strap.
   - Intermedio: Cojín, Bolso París, Tapiz de hojas y Bolso Bahía.
   - Avanzado: Espejo, Embarrilado, Tapiz Prana, Tula Verona, Maxibolso y Hombreras.
   - Al final, el temario de fundamentos en un bloque desplegable.
5. Oferta "Único pago de inscripción": precio en USD y su equivalente en COP, cuenta regresiva y lo que incluye la inscripción (video tutoriales, comunidad privada, 6 bonos, un proyecto nuevo cada mes, certificados y acceso desde cualquier país), junto a la garantía de 7 días.
6. Testimonios.
7. "¿Cómo funciona?": antes de entrar, dentro de la escuela y lo que lograrás.
8. Los 6 bonos de regalo.
9. Preguntas frecuentes, con las mismas de Hotmart más garantía, acceso de por vida, Árbol 3D y quién es El Arte que Conecta.
10. Sobre la instructora, Janeth Flórez, con su foto de la carpeta "Fotos de la autora".
11. Cierre: Comunidad y botón final.
12. Tarjetas hacia los cursos de Bolsos y Navidad Mágica, y el aviso de afiliada.

Las landings de Bolsos y Navidad Mágica no cambian respecto a la V6.

## Inconsistencias que corregí respecto a la landing de Hotmart

- **Precio:** Hotmart muestra USD 48,59 en unos bloques y USD 67,9 en otro. Aquí se usa USD 48,50 en todos (el 50% de USD 97, igual que tu captura).
- **Parámetro de afiliado:** un botón de Hotmart usa `ap=c2d6` y el resto `ap=7bed`. Aquí todos usan `ap=7bed`.
- **Hero duplicado:** Hotmart repite el hero y el bloque de precio. Aquí cada uno aparece una sola vez.
- **Ortografía y pie de página:** se corrigieron "empeza", "costo adiciona", "Floréz" y "desde que país", y se eliminó el pie de página en portugués.

## Pendientes

1. **El 1 de diciembre:** reemplazar los links de pago en `PAY` (archivo `build.py`).
2. **Video del Árbol 3D:** compartirlo como "Cualquier persona con el enlace" en Drive.
3. **Imágenes con el ícono de ChatGPT:** Árbol de pared y Accesorio Infinito (en Bolsos y Navidad).

## Cambio en V8

En las 3 landings, la sección "Lo que dicen las alumnas" ahora muestra, sin editar, 8 capturas del chat de la Comunidad de Tejedoras tomadas de la carpeta TESTIMONIOS de Drive. Los archivos están en `assets/testimonios/` con el prefijo `chat-`.

Capturas que no se usaron:
- Una que deja ver el número de teléfono completo de una alumna.
- Una con un mensaje de la propia instructora, que no es un testimonio.
- La captura de un comentario firmado por Jeimy Amaya.
