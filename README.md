# ConcienTIC

Sitio web institucional de **ConcienTIC**, un ecosistema de Inteligencia Digital que ayuda a personas y organizaciones a desarrollar competencias digitales, alfabetización en IA, discernimiento y autonomía para habitar la tecnología sin perder humanidad.

La página presenta la propuesta de ConcienTIC a través de la filosofía **DQUILIBRIO**: aumentar la capacidad de elegir cómo, cuándo, por qué y para qué usamos la tecnología.

## Características principales

- Landing page responsive para dispositivos móviles, tabletas y escritorio.
- Diseño basado en el sistema visual de ConcienTIC:
  - Poppins para marca y títulos.
  - Inter para navegación y textos de lectura.
  - Paleta de colores institucional.
  - Wordmark ConcienTIC Nexus.
- Ilustraciones oficiales para el Hero y la sección Metodología.
- Sección de Guardianes de la Inteligencia Digital.
- Navegación por anclas entre las secciones de la página.
- Formulario de contacto con validación, protección honeypot y rate limiting.
- Envío de mensajes mediante Resend.
- Integración opcional con Google Analytics 4 y Upstash Redis.
- Rutas legales de privacidad y términos y condiciones.
- Skip link, foco visible, textos alternativos y estados accesibles del formulario.
- Soporte para `prefers-reduced-motion`.

## Orden de la página

La página principal se renderiza en este orden:

1. Hero — `#inicio`
2. Banda de principio
3. DQUILIBRIO — `#dquilibrio`
4. Servicios — `#servicios`
5. El desafío — `#desafio`
6. Desinfoxicación — `#desinfoxicacion`
7. Metodología — `#metodologia`
8. Centro de Evidencia — `#evidencia`
9. Equipo / Guardianes — `#equipo`
10. Casos — `#casos`
11. Referentes — `#referentes`
12. Manifiesto
13. Contacto — `#contacto`
14. Footer

## Secciones

### Hero

Presenta el mensaje principal:

> Aprender tecnología. Sin perder humanidad.

Incluye los llamados a la acción para conocer DQUILIBRIO y consultar los servicios, junto con la ilustración oficial de equilibrio tecnológico.

### DQUILIBRIO

Explica la filosofía transversal de ConcienTIC y sus dimensiones principales: atención, información, discernimiento y autonomía.

### Servicios

Presenta las líneas de servicio relacionadas con competencias digitales, inteligencia artificial, transformación, alfabetización y DQUILIBRIO.

### El desafío

Describe los problemas relacionados con la economía de la atención, la sobrecarga informativa, la desinformación y la automatización.

### Desinfoxicación

Propone prácticas para filtrar, verificar y cerrar el ciclo de consumo de información de forma consciente.

### Metodología

Muestra el ciclo:

`DETENER → OBSERVAR → DISCERNIR → ELEGIR → EQUILIBRAR`

La sección utiliza la ilustración oficial `ciclo-dquilibrio-mejorado.png`.

### Centro de Evidencia

Presenta investigaciones y referencias sobre atención, sobrecarga, alfabetización mediática e inteligencia artificial, diferenciando hallazgos, hipótesis y principios de diseño.

### Equipo / Guardianes

Incluye siete agentes virtuales especializados:

- **Emi:** Empatía y bienestar digital.
- **Locky:** Seguridad y protección.
- **Lex:** Pensamiento crítico y ética.
- **Byte:** Aprendizaje e inteligencia digital.
- **Detective DQ:** Investigación y verificación.
- **Nexo:** Colaboración y comunidad.
- **Nova:** Innovación y transformación.

La sección también incluye una tarjeta de cita y un cierre con colores territoriales para los conceptos consciente, crítica, creativa, colaborativa y transformadora.

### Casos

Espacio preparado para documentar casos reales con problema, intervención, resultados verificables y aprendizajes. No se inventan métricas ni clientes.

### Referentes

Incluye referencias externas relacionadas con Humane Technology, Calm Technology y Digital Wellness.

### Manifiesto

Expone los principios de diseño consciente de ConcienTIC, evitando patrones de captura de atención, dependencia o permanencia artificial.

### Contacto

Permite enviar consultas sobre retos de organizaciones, equipos y comunidades relacionados con competencias digitales, IA, transformación y DQUILIBRIO.

## Tecnologías

- Next.js `16.3.4` con App Router.
- React `19.2.8`.
- TypeScript.
- Tailwind CSS `4` y estilos CSS propios basados en tokens.
- React Hook Form.
- Zod para validación.
- Resend para el envío de correos.
- Upstash Redis / Vercel KV para rate limiting opcional.
- Google Analytics 4 opcional.
- Lucide React para iconos.

## Requisitos

- Node.js compatible con Next.js 16.
- npm.
- Variables de entorno configuradas para activar el envío real del formulario y los servicios opcionales.

## Instalación

Desde la carpeta del proyecto:

```bash
npm install
```

Copia el archivo de variables de entorno:

```bash
copy .env.example .env.local
```

En macOS o Linux:

```bash
cp .env.example .env.local
```

Completa las variables necesarias en `.env.local`.

## Desarrollo local

Inicia el servidor de desarrollo:

```bash
npm run dev
```

Abre [http://localhost:3000](http://localhost:3000).

La página principal se encuentra en:

```text
src/app/page.tsx
```

## Scripts disponibles

```bash
# Servidor de desarrollo
npm run dev

# Comprobación de lint
npm run lint

# Comprobación de tipos TypeScript
npm run typecheck

# Formatear el proyecto
npm run format

# Comprobar formato
npm run format:check

# Crear build de producción
npm run build

# Servir el build de producción
npm run start
```

## Variables de entorno

Las variables de ejemplo se encuentran en `.env.example`.

### Resend

Necesarias para enviar los mensajes del formulario:

```env
RESEND_API_KEY=
CONTACT_FROM_EMAIL=
```

Opcionalmente se puede definir otro destinatario:

```env
CONTACT_TO_EMAIL=
```

`CONTACT_FROM_EMAIL` debe ser un remitente perteneciente a un dominio verificado en Resend.

### Rate limiting

Para activar el límite de solicitudes por IP:

```env
UPSTASH_REDIS_REST_URL=
UPSTASH_REDIS_REST_TOKEN=
```

Si Upstash no está configurado, el sistema degrada de forma segura y permite continuar sin rate limiting persistente.

### Google Analytics 4

La analítica es opcional y se activa con:

```env
NEXT_PUBLIC_GA_ID=
```

## API de contacto

El formulario utiliza:

```text
POST /api/contact
```

Campos enviados:

```json
{
  "name": "Nombre de la persona",
  "email": "persona@ejemplo.com",
  "company": "Empresa opcional",
  "message": "Mensaje de contacto",
  "consent": true,
  "website": ""
}
```

`website` es un campo honeypot oculto para detectar bots y debe permanecer vacío.

Validaciones principales:

- `name`: entre 2 y 100 caracteres.
- `email`: correo válido.
- `company`: opcional, máximo 120 caracteres.
- `message`: entre 10 y 2000 caracteres.
- `consent`: debe ser `true`.
- `website`: debe estar vacío.

Respuestas principales:

- `200`: solicitud procesada correctamente.
- `400`: cuerpo JSON inválido.
- `422`: datos que no cumplen la validación.
- `429`: demasiadas solicitudes desde la misma IP.
- `502`: servicio de correo no disponible o no configurado.

## Estructura del proyecto

```text
concientic/
├── public/
│   ├── brand/
│   │   ├── concientic-nexus-wordmark-transparente-fondo-claro.png
│   │   ├── concientic-nexus-wordmark-transparente-fondo-oscuro.png
│   │   └── logo-concientic.png
│   ├── diagrams/
│   │   ├── ciclo-dquilibrio-mejorado.png
│   │   └── equilibrio-concientic-mejorado.png
│   └── guardians/
├── src/
│   ├── app/
│   │   ├── api/contact/route.ts
│   │   ├── design-system/page.tsx
│   │   ├── privacidad/page.tsx
│   │   ├── terminos/page.tsx
│   │   ├── globals.css
│   │   ├── layout.tsx
│   │   └── page.tsx
│   ├── components/
│   │   ├── analytics/
│   │   ├── layout/
│   │   ├── legal/
│   │   ├── sections/
│   │   └── ui/
│   ├── content/
│   │   ├── guardians.ts
│   │   └── site.ts
│   └── lib/
│       ├── analytics.ts
│       ├── email.ts
│       ├── rate-limit.ts
│       ├── utils.ts
│       └── validations/contact.ts
├── .env.example
├── next.config.ts
├── package.json
└── README.md
```

## Archivos importantes

- `src/app/page.tsx`: composición y orden de la página principal.
- `src/app/layout.tsx`: fuentes, metadata, header, footer y servicios globales.
- `src/app/globals.css`: tokens, layout, responsive, interacción y accesibilidad.
- `src/content/site.ts`: copy, navegación, servicios, secciones y metadata de contenido.
- `src/content/guardians.ts`: datos de los siete Guardianes.
- `src/components/layout/header.tsx`: wordmark y navegación principal.
- `src/components/layout/footer.tsx`: wordmark y enlaces del footer.
- `src/components/sections/contact-form.tsx`: formulario de contacto.
- `src/app/api/contact/route.ts`: endpoint de contacto.
- `src/lib/validations/contact.ts`: esquema Zod del formulario.
- `src/lib/email.ts`: integración con Resend.

## Rutas disponibles

- `/`: página principal.
- `/design-system`: página de referencia del sistema de diseño.
- `/privacidad`: aviso de privacidad.
- `/terminos`: términos y condiciones.
- `/api/contact`: endpoint POST del formulario.
- `/robots.txt`: configuración para rastreadores.
- `/sitemap.xml`: mapa del sitio.

## Accesibilidad y experiencia consciente

La interfaz incluye:

- Enlace para saltar directamente al contenido principal.
- Landmarks semánticos y navegación etiquetada.
- `alt` descriptivo en las imágenes.
- Foco visible para navegación con teclado.
- Mensajes de estado accesibles en el formulario.
- Desactivación de transiciones bajo `prefers-reduced-motion`.
- Sin autoplay, scroll infinito, rankings, FOMO ni urgencia artificial.

## Despliegue en Vercel

1. Importa el repositorio en [Vercel](https://vercel.com/).
2. Configura las variables de `.env.example` en **Project Settings → Environment Variables**.
3. Define las variables para Preview y Production según corresponda.
4. Usa los comandos de build predeterminados de Next.js:

```bash
npm run build
npm run start
```

Antes de publicar, verifica que el dominio de envío esté validado en Resend y que `CONTACT_FROM_EMAIL` sea correcto.

## Validación antes de entregar

Ejecuta:

```bash
npm run format:check
npm run lint
npm run typecheck
npm run build
```

## Licencia y contenido

El contenido, marca, textos e imágenes de ConcienTIC pertenecen a sus respectivos titulares. No reutilices los assets de marca ni los datos de contacto en otros proyectos sin autorización.
