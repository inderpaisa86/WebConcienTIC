---
inclusion: always
---
# Estructura del paquete

```
site/                      → lo que se publica (HTML estático + assets)
  catalogo.html            → /elartequeconecta/catalogo
  assets/catalogo/*.webp   → fotos de las piezas
  assets/catalogo/video/   → loops MP4 (sin audio) + póster JPG
  assets/fotos/            → instructora.webp, v3_1.webp (compartidas con otras landings)
generator/                 → fuente: catalogo_v2.py genera site/catalogo.html
  catalogo.py              → DATOS: lista de piezas P, categorías, WA, FAQ
  catalogo_v2.py           → PLANTILLA V2 (layout, estilos, scripts)
  v3.css, v4.css           → estilos base compartidos con las demás landings
preview/                   → archivo de revisión autocontenido + capturas
```

- Fuente de verdad del contenido: `generator/catalogo.py`. Editar ahí y regenerar; no editar `site/catalogo.html` a mano.
- La página es estática: sin frameworks, sin dependencias de build, JS vanilla (<2 KB).
- Las rutas de assets son relativas (`assets/...`) y deben convivir con las carpetas de las otras landings (`escuela`, `bolsos`, `navidad`).
