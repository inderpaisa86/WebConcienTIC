# Prompt 05 — QA, accesibilidad, performance y Vercel

Haz una auditoría final del sitio ConcienTIC.

Checklist:
- `npm run lint` o equivalente.
- `npm run typecheck` si existe.
- `npm run build`.
- Revisar consola del navegador.
- Revisar imágenes y tamaños.
- Confirmar que logo y Guardianes usan archivos locales transparentes.
- Confirmar que no existe ninguna imagen base64 en producción.
- Revisar alt text.
- Revisar headings y landmarks.
- Revisar keyboard navigation y visible focus.
- Revisar reduced motion.
- Revisar responsive 360/768/1024/1440.
- Revisar SEO title, description, canonical si aplica y Open Graph.
- Revisar enlaces externos.
- Revisar que no haya placeholders visibles.
- Revisar que el formulario no prometa un envío real si no existe backend.
- Revisar que no haya infinite scroll, autoplay, streaks, rankings, FOMO o dark patterns.

Después valida la configuración para Vercel. No agregues secretos al repositorio. Si el formulario necesita una variable de entorno, crea `.env.example` y documenta su uso.

Entrega un reporte final con errores encontrados, correcciones realizadas y estado del build/deployment.
