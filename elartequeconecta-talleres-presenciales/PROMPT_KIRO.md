# Prompt para Kiro

Copia y pega en Kiro con este paquete abierto en el repositorio del sitio.

```text
Contexto: el sitio concientic.com es Next.js (App Router) desplegado en Vercel. En la página
/elartequeconecta hay una sección "Talleres individuales" con 4 talleres virtuales que ya no se ofrecen.

Tarea: reemplázala por la sección "Talleres presenciales" que viene en la carpeta
elartequeconecta-talleres-presenciales/, siguiendo su README.md.

Pasos:
1. Copia a sus rutas en el proyecto:
   - src/data/elartequeconecta/talleres-presenciales.ts
   - src/components/elartequeconecta/TalleresPresenciales.tsx y .module.css
   - src/app/api/elartequeconecta/region/route.ts
   - public/elartequeconecta/assets/talleres/*.webp
   Ajusta el alias "@/" y el prefijo src/ si el proyecto usa otra estructura.
2. En la página /elartequeconecta, elimina la sección "Talleres individuales" e inserta
   <TalleresPresenciales /> en su lugar (entre "La escuela" y "Comunidad"). El componente ya trae
   id="talleres", así que el ancla del menú sigue funcionando.
3. Cambia el texto del menú y del botón secundario de la portada a "Talleres presenciales".
4. Cambia los enlaces de las tarjetas de cursos: Programa principal → /elartequeconecta/escuela,
   Curso de Bolsos → /elartequeconecta/bolsos, Curso de Navidad → /elartequeconecta/navidad,
   y el botón "Conoce la escuela" → /elartequeconecta/escuela. No cambies el botón de Comunidad.
5. No conviertas la página en dinámica: no leas headers() ni cookies() en la página ni en el layout.
   La región se consulta en el navegador mediante la ruta /api/elartequeconecta/region.
6. Compara el resultado con preview/El_Arte_que_Conecta_Landing_V4_Talleres_Presenciales.html y sus
   capturas (abre el HTML con ?ciudad=ibague y con ?ciudad=otra).
7. Verifica los criterios de aceptación del README en un deploy de preview y reporta el resultado,
   incluido lo que devuelve /api/elartequeconecta/region desde la URL de preview.

No modifiques el contenido de la agenda (fechas, precios, textos): sale del formulario de inscripción y
está en el archivo de datos.
```
