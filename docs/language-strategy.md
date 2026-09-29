# Estrategia de idiomas e investigación

## Idiomas de investigación

La biblioteca de consultas debe cubrir:

- español (`es`)
- inglés (`en`)
- portugués (`pt`)
- francés (`fr`)

Las queries se almacenan en `config/research-queries.json` y pueden ampliarse según brechas del catálogo.

## Reglas

- Detectar idioma del título, descripción y página, pero no inferirlo solo por el dominio.
- Guardar todos los idiomas confirmados.
- Preferir español cuando la calidad sea equivalente, sin excluir recursos relevantes en otros idiomas.
- No traducir títulos o descripciones como si fueran originales; si se genera una traducción futura, marcarla como derivada.
- Un recurso sin idioma verificable pasa a `REVIEW_REQUIRED`.
