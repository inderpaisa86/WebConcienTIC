export const site = {
  name: "CONCIENTIC",
  domain: "https://concientic.com",
  contactEmail: "hola@concientic.com",
  tagline: "Inteligencia Digital con conciencia",
  description:
    "ConcienTIC desarrolla Inteligencia Digital para aprender, proteger, discernir, conectar e innovar sin perder humanidad.",
  social: {
    linkedin: "https://www.linkedin.com/company/concientic",
  },
} as const;

export const navLinks = [
  { label: "DQUILIBRIO", href: "#dquilibrio" },
  { label: "El desafío", href: "#desafio" },
  { label: "Servicios", href: "#servicios" },
  { label: "Metodología", href: "#metodologia" },
  { label: "Evidencia", href: "#evidencia" },
  { label: "Equipo", href: "#equipo" },
  { label: "Referentes", href: "#referentes" },
] as const;

export const hero = {
  eyebrow: "Inteligencia Digital con conciencia",
  title: "Aprender tecnología. Sin perder humanidad.",
  subtitle:
    "Acompañamos a personas y organizaciones a construir una relación más consciente, crítica y autónoma con la tecnología.",
  primaryCta: { label: "Conocer DQUILIBRIO", href: "#dquilibrio" },
  secondaryCta: { label: "Ver servicios", href: "#servicios" },
} as const;

export const principleBand = {
  statement: "No queremos más tiempo de tu vida.",
  emphasis: "Queremos ayudarte a recuperar el control sobre el tiempo que ya tienes.",
} as const;

export const dquilibrio = {
  id: "dquilibrio",
  eyebrow: "DQUILIBRIO",
  title: "La tecnología no es el enemigo. La falta de elección sí puede serlo.",
  description:
    "DQUILIBRIO es la filosofía transversal de ConcienTIC: aumentar nuestra capacidad de elegir cómo, cuándo, por qué y para qué usamos la tecnología.",
  items: [
    {
      number: "01",
      title: "Atención",
      description: "Reconocer qué reclama nuestra atención y qué queremos cuidar.",
    },
    {
      number: "02",
      title: "Criterio",
      description: "Comprender antes de reaccionar, compartir o automatizar.",
    },
    {
      number: "03",
      title: "Autonomía",
      description: "Diseñar condiciones para decidir, no solo para responder.",
    },
    {
      number: "04",
      title: "Humanidad",
      description: "Usar la tecnología para ampliar posibilidades, no para reducirlas.",
    },
  ],
} as const;

export const challenge = {
  id: "desafio",
  eyebrow: "El desafío",
  title: "Vivimos entre abundancia tecnológica y escasez humana.",
  description:
    "La investigación contemporánea conecta problemas que se refuerzan entre sí: atención limitada, sobrecarga informativa, diseño persuasivo, desinformación y nuevas formas de dependencia de sistemas digitales.",
  items: [
    {
      number: "01",
      title: "Atención fragmentada",
      description:
        "Interrupciones constantes que hacen más difícil estar presentes y terminar lo importante.",
    },
    {
      number: "02",
      title: "Infoxicación",
      description: "Más señales, noticias y datos de los que podemos procesar con calma.",
    },
    {
      number: "03",
      title: "Desinformación",
      description: "Contenido convincente que exige verificar antes de confiar o difundir.",
    },
    {
      number: "04",
      title: "IA y automatización",
      description: "Nuevas capacidades que necesitan criterio, transparencia y responsabilidad.",
    },
  ],
} as const;

export const desinfoxication = {
  id: "desinfoxicacion",
  eyebrow: "Desinfoxicación",
  title: "Menos ruido. Más capacidad para comprender.",
  description:
    "Un hábito sencillo para recuperar margen de decisión frente a la velocidad de la información.",
  steps: [
    { title: "Filtrar", description: "Decidir qué merece entrar en nuestro espacio de atención." },
    {
      title: "Verificar",
      description: "Buscar contexto, origen y evidencia antes de dar algo por cierto.",
    },
    {
      title: "Cerrar",
      description: "Poner límites a la exposición y volver a lo que elegimos hacer.",
    },
  ],
} as const;

export const services = {
  id: "servicios",
  eyebrow: "Servicios",
  title: "Convertimos la conciencia digital en práctica.",
  subtitle:
    "Diseñamos experiencias, conversaciones y herramientas para que la tecnología se entienda mejor y se use con más libertad.",
  items: [
    {
      title: "Estrategia digital consciente",
      description:
        "Mapeamos retos, hábitos y oportunidades para tomar decisiones tecnológicas con propósito.",
      tag: "Estrategia",
    },
    {
      title: "Formación y alfabetización en IA",
      description:
        "Traducimos conceptos complejos en aprendizajes útiles para equipos, docentes y comunidades.",
      tag: "Aprendizaje",
    },
    {
      title: "Diseño de experiencias",
      description:
        "Creamos productos y servicios digitales claros, accesibles y respetuosos con la atención.",
      tag: "Diseño",
    },
    {
      title: "Investigación y evidencia",
      description:
        "Ordenamos preguntas, fuentes y hallazgos para distinguir datos, interpretaciones y propuestas.",
      tag: "Criterio",
    },
    {
      title: "Cultura y bienestar digital",
      description:
        "Acompañamos cambios sostenibles en las formas de trabajar, aprender y relacionarse con la tecnología.",
      tag: "Cultura",
    },
  ],
} as const;

export const methodology = {
  id: "metodologia",
  eyebrow: "Metodología",
  title: "Antes de hacer, aprendemos a elegir.",
  subtitle: "Un ciclo práctico para pasar de la reacción automática a una decisión más consciente.",
  steps: [
    {
      step: "01",
      title: "DETENER",
      description: "Interrumpir el piloto automático y crear un poco de espacio.",
    },
    {
      step: "02",
      title: "OBSERVAR",
      description: "Mirar qué ocurre, qué sentimos y qué información tenemos.",
    },
    {
      step: "03",
      title: "DISCERNIR",
      description: "Separar hechos, interpretaciones, riesgos y posibilidades.",
    },
    {
      step: "04",
      title: "ELEGIR",
      description: "Tomar una decisión alineada con el propósito y el contexto.",
    },
    {
      step: "05",
      title: "EQUILIBRAR",
      description: "Revisar el resultado y ajustar para sostener el cambio.",
    },
  ],
} as const;

export const evidence = {
  id: "evidencia",
  eyebrow: "Centro de Evidencia",
  title: "No queremos que nos creas. Queremos que puedas comprobarlo.",
  subtitle:
    "Un espacio para distinguir hallazgos científicos, interpretaciones y propuestas propias de ConcienTIC.",
  items: [
    {
      type: "Hallazgo",
      title: "La atención también es un recurso de diseño",
      description:
        "Entender cómo se organiza la atención ayuda a crear entornos digitales más habitables.",
    },
    {
      type: "Interpretación",
      title: "La autonomía necesita condiciones",
      description:
        "Elegir no depende solo de la voluntad individual: también importa el contexto que diseñamos.",
    },
    {
      type: "Propuesta",
      title: "Diseñar pausas es diseñar libertad",
      description:
        "Introducir momentos de revisión puede abrir alternativas frente a la respuesta inmediata.",
    },
  ],
  caveat:
    "DQUILIBRIO es un marco de trabajo de ConcienTIC; no es una escala clínica ni sustituye asesoramiento profesional.",
} as const;

export const team = {
  id: "equipo",
  eyebrow: "Equipo",
  title: "Los Guardianes de la Inteligencia Digital.",
  subtitle:
    "Agentes virtuales especializados que representan las competencias que necesitamos para habitar el mundo tecnológico sin perder humanidad.",
  principle:
    "Ninguna competencia vive aislada: la inteligencia digital crece cuando podemos combinarlas.",
} as const;

export const cases = {
  id: "casos",
  eyebrow: "Casos",
  title: "Trabajo real, evidencia antes que promesas.",
  subtitle:
    "Estamos documentando experiencias y resultados con el cuidado necesario para no convertir una hipótesis en una métrica.",
  items: [
    {
      title: "Programas de aprendizaje",
      description:
        "Próximamente compartiremos experiencias de formación y alfabetización digital con contexto suficiente para entender qué funcionó.",
      status: "Documentación en preparación",
    },
    {
      title: "Diseño de experiencias",
      description:
        "Próximamente compartiremos procesos de diseño consciente y las decisiones que los hicieron posibles.",
      status: "Documentación en preparación",
    },
    {
      title: "Cultura digital",
      description:
        "Próximamente compartiremos aprendizajes sobre cambios sostenibles en equipos y comunidades.",
      status: "Documentación en preparación",
    },
  ],
} as const;

export const references = {
  id: "referentes",
  eyebrow: "Referentes",
  title: "Pensar con fuentes. Diseñar con responsabilidad.",
  subtitle:
    "Algunas instituciones y marcos que ayudan a ampliar la conversación sobre educación, tecnología y ciudadanía digital.",
  items: [
    {
      type: "Educación e IA",
      title: "UNESCO",
      description:
        "Orientaciones para abordar la inteligencia artificial generativa en educación e investigación.",
      href: "https://www.unesco.org/en/digital-education/artificial-intelligence",
    },
    {
      type: "Política digital",
      title: "OECD Digital",
      description:
        "Investigación y marcos para comprender el impacto social y económico de la transformación digital.",
      href: "https://www.oecd.org/digital/",
    },
    {
      type: "Ciudadanía",
      title: "Council of Europe",
      description:
        "Recursos para desarrollar ciudadanía digital, derechos y participación responsable.",
      href: "https://www.coe.int/en/web/digital-citizenship-education",
    },
  ],
} as const;

export const manifesto = {
  id: "manifiesto",
  eyebrow: "Manifiesto",
  title: "La tecnología que queremos también se diseña.",
  description:
    "Elegimos una inteligencia digital que amplíe la capacidad humana de comprender, decidir y cuidar. No se trata de volver atrás: se trata de avanzar con más conciencia.",
  principles: [
    {
      title: "La persona antes que la métrica",
      description: "Medimos lo que importa sin reducir la experiencia humana a un número.",
    },
    {
      title: "La evidencia antes que el ruido",
      description: "Explicamos qué sabemos, qué inferimos y qué todavía estamos aprendiendo.",
    },
    {
      title: "La autonomía antes que la dependencia",
      description: "Diseñamos herramientas que devuelven capacidad de decisión.",
    },
    {
      title: "La curiosidad antes que el miedo",
      description: "Aprendemos tecnología para poder participar en su futuro.",
    },
  ],
} as const;

export const contact = {
  id: "contacto",
  eyebrow: "Contacto",
  title: "¿Qué relación con la tecnología quieres construir?",
  subtitle:
    "Cuéntanos el reto de tu equipo, organización o comunidad. Abrimos una conversación sin recetas prefabricadas.",
} as const;

export const footer = {
  legalName: "CONCIENTIC",
  rights: "Todos los derechos reservados.",
  links: [
    { label: "Aviso de privacidad", href: "/privacidad" },
    { label: "Términos", href: "/terminos" },
  ],
} as const;

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
