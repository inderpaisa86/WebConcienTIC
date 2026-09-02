/**
 * Contenido centralizado del sitio CONCIENTIC.
 *
 * TODO (Fase 0): sustituir todos los textos "[PLACEHOLDER]" por el copy real extraído
 * del sitio actual (Canva). Editar aquí no requiere tocar los componentes.
 */

export const site = {
  name: "CONCIENTIC",
  domain: "https://concientic.com",
  // Correo al que llegan los mensajes del formulario de contacto.
  contactEmail: "hola@concientic.com",
  tagline: "Sistema de inteligencia digital",
  description:
    "CONCIENTIC integra estrategia, producto e inteligencia artificial para ayudar a las organizaciones a tomar mejores decisiones.",
  social: {
    linkedin: "https://www.linkedin.com/company/concientic",
    // instagram: "",
    // x: "",
  },
} as const;

/** Enlaces de navegación (anclas del one-page). */
export const navLinks = [
  { label: "Servicios", href: "#servicios" },
  { label: "Metodología", href: "#metodologia" },
  { label: "Casos", href: "#casos" },
  { label: "Equipo", href: "#equipo" },
  { label: "Contacto", href: "#contacto" },
] as const;

export const hero = {
  eyebrow: "[PLACEHOLDER] Eyebrow corto",
  title: "[PLACEHOLDER] Titular principal de CONCIENTIC",
  subtitle:
    "[PLACEHOLDER] Subtítulo que explica la propuesta de valor en una o dos frases claras.",
  primaryCta: { label: "Hablemos", href: "#contacto" },
  secondaryCta: { label: "Ver servicios", href: "#servicios" },
} as const;

export const valueProps = {
  title: "[PLACEHOLDER] Por qué CONCIENTIC",
  items: [
    {
      title: "[PLACEHOLDER] Diferenciador 1",
      description: "[PLACEHOLDER] Descripción breve del diferenciador 1.",
    },
    {
      title: "[PLACEHOLDER] Diferenciador 2",
      description: "[PLACEHOLDER] Descripción breve del diferenciador 2.",
    },
    {
      title: "[PLACEHOLDER] Diferenciador 3",
      description: "[PLACEHOLDER] Descripción breve del diferenciador 3.",
    },
  ],
} as const;

export const services = {
  id: "servicios",
  title: "[PLACEHOLDER] Servicios",
  subtitle: "[PLACEHOLDER] Frase introductoria de la sección de servicios.",
  items: [
    {
      title: "[PLACEHOLDER] Servicio 1",
      description: "[PLACEHOLDER] Qué incluye el servicio 1 y para quién.",
    },
    {
      title: "[PLACEHOLDER] Servicio 2",
      description: "[PLACEHOLDER] Qué incluye el servicio 2 y para quién.",
    },
    {
      title: "[PLACEHOLDER] Servicio 3",
      description: "[PLACEHOLDER] Qué incluye el servicio 3 y para quién.",
    },
  ],
} as const;

export const methodology = {
  id: "metodologia",
  title: "[PLACEHOLDER] Metodología",
  subtitle: "[PLACEHOLDER] Cómo trabajamos, en pasos.",
  steps: [
    {
      step: "01",
      title: "[PLACEHOLDER] Paso 1",
      description: "[PLACEHOLDER] Descripción del paso 1.",
    },
    {
      step: "02",
      title: "[PLACEHOLDER] Paso 2",
      description: "[PLACEHOLDER] Descripción del paso 2.",
    },
    {
      step: "03",
      title: "[PLACEHOLDER] Paso 3",
      description: "[PLACEHOLDER] Descripción del paso 3.",
    },
    {
      step: "04",
      title: "[PLACEHOLDER] Paso 4",
      description: "[PLACEHOLDER] Descripción del paso 4.",
    },
  ],
} as const;

export const cases = {
  id: "casos",
  title: "[PLACEHOLDER] Casos de éxito",
  subtitle: "[PLACEHOLDER] Resultados que hemos logrado con clientes.",
  items: [
    {
      client: "[PLACEHOLDER] Cliente 1",
      result: "[PLACEHOLDER] Resultado medible (ej. +30% en X).",
      description: "[PLACEHOLDER] Breve descripción del caso 1.",
    },
    {
      client: "[PLACEHOLDER] Cliente 2",
      result: "[PLACEHOLDER] Resultado medible.",
      description: "[PLACEHOLDER] Breve descripción del caso 2.",
    },
    {
      client: "[PLACEHOLDER] Cliente 3",
      result: "[PLACEHOLDER] Resultado medible.",
      description: "[PLACEHOLDER] Breve descripción del caso 3.",
    },
  ],
} as const;

export const team = {
  id: "equipo",
  title: "[PLACEHOLDER] Equipo",
  subtitle: "[PLACEHOLDER] Quiénes están detrás de CONCIENTIC.",
  members: [
    {
      name: "[PLACEHOLDER] Nombre 1",
      role: "[PLACEHOLDER] Rol 1",
      bio: "[PLACEHOLDER] Bio breve.",
    },
    {
      name: "[PLACEHOLDER] Nombre 2",
      role: "[PLACEHOLDER] Rol 2",
      bio: "[PLACEHOLDER] Bio breve.",
    },
  ],
} as const;

export const contact = {
  id: "contacto",
  title: "[PLACEHOLDER] Hablemos",
  subtitle:
    "[PLACEHOLDER] Cuéntanos tu reto y te respondemos. Frase de cierre orientada a conversión.",
} as const;

export const footer = {
  legalName: "CONCIENTIC",
  rights: "Todos los derechos reservados.",
  links: [
    { label: "Aviso de privacidad", href: "/privacidad" },
    { label: "Términos", href: "/terminos" },
  ],
} as const;

/**
 * Contenido de las páginas legales.
 *
 * TODO (legal): revisar/reemplazar estos textos base con la política real de CONCIENTIC,
 * validada por su equipo legal antes de publicar. Ajustar `lastUpdated`.
 */
export const legal = {
  privacy: {
    title: "Aviso de privacidad",
    lastUpdated: "2026-01-01",
    intro:
      "En CONCIENTIC valoramos tu privacidad. Este aviso describe cómo recopilamos, usamos y protegemos los datos personales que nos facilitas a través de este sitio.",
    sections: [
      {
        heading: "Datos que recopilamos",
        body: "Recopilamos los datos que nos proporcionas voluntariamente a través del formulario de contacto (nombre, correo, empresa y mensaje), así como datos de uso anónimos mediante herramientas de analítica, sujetos a tu consentimiento.",
      },
      {
        heading: "Finalidad del tratamiento",
        body: "Usamos tus datos para responder a tus solicitudes, gestionar la relación comercial y mejorar nuestros servicios y este sitio.",
      },
      {
        heading: "Base legal",
        body: "Tratamos tus datos con base en tu consentimiento y en nuestro interés legítimo de atender tus consultas.",
      },
      {
        heading: "Cookies y analítica",
        body: "Utilizamos cookies de analítica solo si aceptas su uso en el banner de consentimiento. Puedes cambiar tu elección en cualquier momento borrando las cookies del sitio.",
      },
      {
        heading: "Tus derechos",
        body: "Puedes solicitar el acceso, la rectificación o la eliminación de tus datos, así como oponerte a su tratamiento, escribiéndonos al correo de contacto.",
      },
      {
        heading: "Contacto",
        body: "Para cualquier consulta sobre este aviso, contáctanos a través del correo indicado en la sección de contacto.",
      },
    ],
  },
  terms: {
    title: "Términos y condiciones",
    lastUpdated: "2026-01-01",
    intro:
      "El uso de este sitio implica la aceptación de los presentes términos y condiciones. Si no estás de acuerdo, te pedimos que no utilices el sitio.",
    sections: [
      {
        heading: "Uso del sitio",
        body: "Este sitio se ofrece con fines informativos. Te comprometes a usarlo de forma lícita y a no realizar acciones que puedan dañarlo o afectar su funcionamiento.",
      },
      {
        heading: "Propiedad intelectual",
        body: "Los contenidos, marcas y logotipos de este sitio son propiedad de CONCIENTIC o de sus titulares. No está permitida su reproducción sin autorización.",
      },
      {
        heading: "Limitación de responsabilidad",
        body: "CONCIENTIC procura mantener la información actualizada y correcta, pero no garantiza la ausencia de errores ni la disponibilidad continua del sitio.",
      },
      {
        heading: "Enlaces a terceros",
        body: "Este sitio puede contener enlaces a sitios de terceros. No nos hacemos responsables del contenido ni de las prácticas de privacidad de dichos sitios.",
      },
      {
        heading: "Cambios",
        body: "Podemos actualizar estos términos en cualquier momento. La versión vigente será siempre la publicada en esta página.",
      },
    ],
  },
} as const;
