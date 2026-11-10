# Tareas de implementación

- [ ] 1. Configurar datos
  - [ ] 1.1 Reemplazar `WA = "57XXXXXXXXXX"` en `generator/catalogo.py` por el número real.
  - [ ] 1.2 Cargar precios en COP (último valor de cada fila) o dejar `None`.
  - [ ] 1.3 Confirmar texto de `.topbar` ("Encargos de Navidad abiertos") en `catalogo_v2.py`.
  - [ ] 1.4 Revisar la foto de Bolso Bahía (`assets/catalogo/bol-bahia.webp`) y reemplazar si no muestra el bolso.
- [ ] 2. Regenerar: `cd generator && python3 catalogo_v2.py` (escribe directamente en `site/catalogo.html`).
- [ ] 3. Publicar `site/catalogo.html` como `/elartequeconecta/catalogo` y `site/assets/` junto a los assets existentes (no sobrescribir `assets/fotos/` si ya existe; solo añadir los 2 archivos).
- [ ] 4. Agregar el enlace "Catálogo" al menú de las landings `escuela`, `bolsos` y `navidad`.
- [ ] 5. (Opcional) Evento GA4 `click_whatsapp` con `item_name` en cada `.pc-wa`, `#mwa` y `.sticky-wa`.
- [ ] 6. Verificar
  - [ ] 6.1 Filtros y contador en las 4 categorías.
  - [ ] 6.2 Modal abre/cierra (×, fuera, Esc) en escritorio y móvil.
  - [ ] 6.3 Cada botón abre WhatsApp con el nombre correcto de la pieza.
  - [ ] 6.4 Videos se reproducen en iOS Safari (muted + playsinline).
  - [ ] 6.5 Lighthouse móvil según Requisito 7.
  - [ ] 6.6 Comparar con `preview/` en 1280 px y 390 px.
