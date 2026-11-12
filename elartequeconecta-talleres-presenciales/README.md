# Talleres presenciales (Ibagué) · /elartequeconecta

Paquete para reemplazar la sección **"Talleres individuales"** (talleres virtuales) de
`https://www.concientic.com/elartequeconecta` por **"Talleres presenciales"**: un calendario con las
fechas, horarios y precios de los talleres en Ibagué y el enlace al formulario de inscripción.

La agenda aplica **solo para Ibagué**. La sección lo dice en un aviso visible y, por geolocalización
aproximada, muestra la agenda completa a quien visita desde Tolima. Al resto le muestra un aviso
compacto con el botón "Ver agenda de Ibagué".

## Contenido

| Ruta en el paquete | Destino en el proyecto | Qué es |
| --- | --- | --- |
| `src/data/elartequeconecta/talleres-presenciales.ts` | igual | Fechas, horarios, precios, enlace al formulario y configuración de región. **Único archivo a editar para cambiar la agenda.** |
| `src/components/elartequeconecta/TalleresPresenciales.tsx` | igual | Componente cliente de la sección. |
| `src/components/elartequeconecta/TalleresPresenciales.module.css` | igual | Estilos autocontenidos con la paleta de la marca. |
| `src/app/api/elartequeconecta/region/route.ts` | igual | Ruta que devuelve país, región y ciudad desde las cabeceras de Vercel. |
| `public/elartequeconecta/assets/talleres/*.webp` | igual | Fotos de Bolso París, Tula Verona y Bolso Santorini. |
| `preview/El_Arte_que_Conecta_Landing_V4_Talleres_Presenciales.html` | — | Referencia visual completa (abre en el navegador). Simula la región con `?ciudad=ibague` o `?ciudad=otra`. |
| `preview/*.png` | — | Capturas: escritorio, aviso de Ibagué y vista fuera de Ibagué. |

Si el proyecto no usa `src/`, quita ese prefijo en todas las rutas y en el alias `@/`.

## Cambios en la página /elartequeconecta

1. **Eliminar** la sección "Talleres individuales" (Maxibolso, Árbol de Navidad 3D, Tapiz Prana, Árbol de pared)
   y poner `<TalleresPresenciales />` en su lugar, entre "La escuela" y "Comunidad".
2. **Menú:** el enlace `#talleres` pasa a decir "Talleres presenciales". El botón secundario de la portada,
   también.
3. **Tarjetas de cursos** (ya no hay talleres virtuales):
   - Programa principal → `/elartequeconecta/escuela` (antes apuntaba a `#talleres`).
   - Curso de Bolsos → `/elartequeconecta/bolsos`.
   - Curso de Navidad → `/elartequeconecta/navidad`.
   - Botón "Conoce la escuela" de la portada → `/elartequeconecta/escuela`.
4. El botón de la sección Comunidad (`elartequeconecta.hotmart.host`) **queda igual** hasta que Jeimy decida.

## Cómo decide qué mostrar

Orden de prioridad (en el navegador; la página sigue siendo estática y cacheable):

1. Parámetro `?ciudad=ibague` en la URL. Úsalo en los anuncios segmentados a Ibagué: es más confiable que la IP.
2. Decisión ya tomada en la sesión (`sessionStorage`).
3. `GET /api/elartequeconecta/region`. Agenda completa si `pais === 'CO'` y región `TOL` (Tolima) o ciudad "ibague".
4. Si la consulta falla o tarda más de 2,5 s: aviso compacto.

Configuración en `GEO` (archivo de datos):

- `regionesPermitidas: ['TOL']`: todo Tolima, recomendado porque la ciudad por IP es imprecisa.
- `modoFuera: 'compacto' | 'oculto'`. Por defecto `compacto`. Con `oculto` la sección no se muestra fuera de la
  región; en ese caso oculta también el enlace "Talleres presenciales" del menú.

## Reglas automáticas

- Un taller desaparece cuando pasa su última sesión. Cuando no queda ninguno vigente, la sección entera desaparece.
- `cerrado: true` muestra la tarjeta atenuada con "Cupos cerrados" y sin botón.
- Los clics en "Inscribirme" envían `generate_lead` (GA4) y `Lead` (Meta) si `gtag` o `fbq` existen.

## Criterios de aceptación

- [ ] La sección "Talleres individuales" ya no existe; ningún enlace de la home lleva a talleres virtuales.
- [ ] Con `?ciudad=ibague` se ve el aviso "Esta agenda aplica solo para la ciudad de Ibagué", el calendario de
      noviembre y diciembre 2026 y las tarjetas de París, Tula Verona y Santorini (dic). Santorini (nov) aparece cerrado.
- [ ] Con `?ciudad=otra` se ve solo el aviso compacto. El botón "Ver agenda de Ibagué" despliega la agenda.
- [ ] En un deploy de preview en Vercel, `GET /api/elartequeconecta/region` devuelve `pais`, `region` y `ciudad`
      con valores (en local devuelve `null`; es normal).
- [ ] La respuesta de `/api/elartequeconecta/region` lleva `Cache-Control: private, no-store`.
- [ ] Clic en un día del calendario baja a la tarjeta del taller y la resalta.
- [ ] "Inscribirme" abre el formulario de Google en pestaña nueva.
- [ ] Sin errores de consola; se ve bien en 390 px, 768 px y 1280 px.
- [ ] Si se aplica la CSP propuesta en la auditoría, `connect-src 'self'` cubre la ruta de región.

## Pendientes de contenido (Jeimy)

- Dirección o barrio del taller en Ibagué (hoy: "se confirma al inscribirte").
- Validar las descripciones cortas de cada bolso.
