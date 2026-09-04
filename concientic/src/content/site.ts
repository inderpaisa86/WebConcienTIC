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
  { label: "Servicios", href: "#servicios" },
  { label: "El desafío", href: "#desafio" },
  { label: "Metodología", href: "#metodologia" },
  { label: "Evidencia", href: "#evidencia" },
  { label: "Equipo", href: "#equipo" },
  { label: "Referentes", href: "#referentes" },
] as const;

export const hero = {
  eyebrow: "Sistema de Inteligencia Digital",
  title: "Aprender tecnología. Sin perder humanidad.",
  subtitle:
    "ConcienTIC desarrolla Inteligencia Digital mientras ayuda a personas y organizaciones a construir una relación consciente, autónoma y equilibrada con la tecnología.",
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
      number: "01 · Atención",
      title: "Recuperar presencia",
      description: "Reconocer que la atención es limitada y diseñar experiencias que la respeten.",
    },
    {
      number: "02 · Información",
      title: "Reducir saturación",
      description: "Pasar de acumular información a seleccionar, contextualizar y sintetizar.",
    },
    {
      number: "03 · Discernimiento",
      title: "Decidir con criterio",
      description: "Verificar fuentes, reconocer sesgos y diferenciar evidencia de apariencia.",
    },
    {
      number: "04 · Autonomía",
      title: "Elegir conscientemente",
      description: "Usar IA y tecnología como herramientas al servicio de objetivos humanos.",
    },
  ],
} as const;

export const challenge = {
  id: "desafio",
  eyebrow: "El desafío",
  title: "Vivimos entre abundancia tecnológica y escasez humana.",
  description:
    "La investigación contemporánea conecta varios problemas que se refuerzan entre sí: atención limitada, sobrecarga informativa, diseño persuasivo, desinformación y nuevas formas de dependencia de sistemas automatizados.",
  items: [
    {
      number: "Fuerza 01",
      title: "Economía de la atención",
      description:
        "Cuando la información abunda, la atención se convierte en un recurso escaso y disputado.",
    },
    {
      number: "Fuerza 02",
      title: "Infoxicación",
      description:
        "La sobrecarga puede aumentar presión cognitiva, favorecer evitación y dificultar decisiones.",
    },
    {
      number: "Fuerza 03",
      title: "Desinformación",
      description:
        "La alfabetización mediática y el discernimiento son capacidades cada vez más necesarias.",
    },
    {
      number: "Fuerza 04",
      title: "IA y automatización",
      description:
        "La IA amplía capacidades, pero también introduce riesgos como desinformación y sesgos de automatización.",
    },
  ],
} as const;

export const desinfoxication = {
  id: "desinfoxicacion",
  eyebrow: "Desinfoxicación",
  title: "De la saturación informativa a la claridad.",
  description:
    "No se trata de consumir menos por obligación. Se trata de desarrollar mejores filtros: buscar, seleccionar, contextualizar, verificar, sintetizar y saber cuándo dejar de consumir información.",
  steps: [
    {
      title: "¿Qué merece mi atención?",
      label: "Filtrar",
      description: "Definir propósito antes de abrir una nueva fuente, canal o herramienta.",
    },
    {
      title: "¿Puedo confiar en esto?",
      label: "Verificar",
      description:
        "Contrastar fuentes, contexto, autoría, fecha, evidencia y señales de manipulación.",
    },
    {
      title: "¿Cuándo es suficiente?",
      label: "Cerrar",
      description:
        "Transformar información en criterio y permitir que la experiencia tenga un final.",
    },
  ],
} as const;

export const services = {
  id: "servicios",
  eyebrow: "Servicios",
  title: "Capacidades para habitar el mundo tecnológico.",
  subtitle: "La propuesta combina aprendizaje, tecnología, IA, transformación y DQUILIBRIO.",
  items: [
    {
      title: "Competencias Digitales",
      description: "Rutas para desarrollar capacidades esenciales y transferibles.",
      tag: "Aprendizaje",
    },
    {
      title: "IA consciente",
      description: "Comprender, experimentar y adoptar IA con criterio humano.",
      tag: "Inteligencia",
    },
    {
      title: "Desinfoxicación",
      description: "Herramientas para navegar abundancia, fuentes y desinformación.",
      tag: "Discernimiento",
    },
    {
      title: "Transformación cultural",
      description: "Prácticas para integrar tecnología sin perder autonomía.",
      tag: "Organizaciones",
    },
    {
      title: "DQUILIBRIO",
      description: "Experiencias para recuperar atención, criterio y capacidad de elección.",
      tag: "Equilibrio",
    },
  ],
} as const;

export const methodology = {
  id: "metodologia",
  eyebrow: "Metodología",
  title: "Un ciclo, no una carrera.",
  subtitle:
    "La metodología ConcienTIC traduce la filosofía DQUILIBRIO en una práctica de aprendizaje y transformación. No es una escala clínica ni un instrumento científico validado.",
  steps: [
    { step: "01", title: "DETENER", description: "Interrumpir el piloto automático." },
    { step: "02", title: "OBSERVAR", description: "Hacer visible contexto y hábitos." },
    { step: "03", title: "DISCERNIR", description: "Contrastar información y riesgos." },
    { step: "04", title: "ELEGIR", description: "Decidir con intención." },
    { step: "05", title: "EQUILIBRAR", description: "Transferir, ajustar y volver a elegir." },
  ],
} as const;

export const evidence = {
  id: "evidencia",
  eyebrow: "Centro de Evidencia",
  title: "No queremos que nos creas. Queremos que puedas comprobarlo.",
  subtitle:
    "ConcienTIC construye su propuesta sobre investigación y reconoce también sus límites. La evidencia es amplia, pero heterogénea; distinguimos hallazgos, hipótesis y principios de diseño.",
  items: [
    {
      type: "Atención",
      title: "La atención es escasa",
      description:
        "La economía de la atención parte de una idea central: en entornos ricos en información, la atención humana es un recurso limitado.",
      href: "https://onlinelibrary.wiley.com/doi/full/10.1111/j.1468-0297.2008.02182.x",
      source: "Falkinger · 2008 ↗",
    },
    {
      type: "Sobrecarga",
      title: "La abundancia puede saturar",
      description:
        "Una revisión de 146 artículos caracteriza la sobrecarga como un fenómeno cognitivo, afectivo y estructural con consecuencias conductuales.",
      href: "https://doi.org/10.1108/JD-03-2026-0114",
      source: "Dote Pardo · 2026 ↗",
    },
    {
      type: "Discernimiento",
      title: "La alfabetización ayuda",
      description:
        "Un meta-análisis de 49 estudios experimentales encontró efectos positivos de intervenciones de alfabetización mediática frente a la desinformación.",
      href: "https://journals.sagepub.com/doi/10.1177/00936502241288103",
      source: "Huang, Jia & Yu · 2024 ↗",
    },
    {
      type: "IA",
      title: "La IA cambia el problema",
      description:
        "Las revisiones recientes muestran riesgos de desinformación generativa y sesgos de automatización, junto con oportunidades de mitigación.",
      href: "https://link.springer.com/article/10.1007/s00146-025-02620-3",
      source: "Park & Nan · 2026 ↗",
    },
  ],
  caveat:
    "Precisión científica: el lenguaje popular sobre “dopamina” simplifica un campo más complejo. ConcienTIC prefiere hablar de refuerzo, captura de atención, hábitos, diseño persuasivo, interrupciones, sobrecarga y autonomía.",
} as const;

export const team = {
  id: "equipo",
  eyebrow: "Nuestro equipo",
  title: "Guardianes de la Inteligencia Digital",
  subtitle:
    "Los Guardianes son los integrantes virtuales del equipo ConcienTIC: agentes especializados que educan, acompañan, protegen, investigan, conectan e impulsan la innovación.",
  principle: "Juntos construyen una inteligencia digital",
} as const;

export const cases = {
  id: "casos",
  eyebrow: "Casos",
  title: "La transformación se demuestra en la vida real.",
  subtitle:
    "Espacio preparado para documentar casos reales con problema, intervención, resultados verificables y aprendizaje.",
  items: [
    {
      meta: "Educación",
      title: "Competencias para la era de la IA",
      description:
        "Ruta para fortalecer capacidades digitales, pensamiento crítico y uso consciente de IA.",
      result: "Resultado → incorporar métrica real",
    },
    {
      meta: "Organizaciones",
      title: "Transformación humano + tecnología",
      description:
        "Diseño de capacidades y prácticas para integrar nuevas tecnologías sin perder autonomía ni criterio.",
      result: "Resultado → incorporar métrica real",
    },
    {
      meta: "Comunidades",
      title: "DQUILIBRIO en la vida cotidiana",
      description:
        "Experiencias para recuperar atención, criterio y capacidad de elección en entornos digitales.",
      result: "Resultado → incorporar métrica real",
    },
  ],
} as const;

export const references = {
  id: "referentes",
  eyebrow: "Referentes",
  title: "Tres territorios que inspiran el movimiento.",
  subtitle:
    "ConcienTIC reconoce referentes internacionales y construye una propuesta propia en la intersección entre tecnología, educación, atención y humanidad.",
  items: [
    {
      type: "Territorio 01 · Humane Technology",
      title: "Center for Humane Technology",
      description:
        "Cuestiona incentivos y diseños que explotan la atención y trabaja por tecnologías que sirvan a la humanidad.",
      href: "https://www.humanetech.com/",
    },
    {
      type: "Territorio 02 · Calm Technology",
      title: "Calm Technology",
      description:
        "Propone tecnología que requiere la menor atención posible y trabaja con la atención humana en lugar de competir contra ella.",
      href: "https://calmtech.com/",
    },
    {
      type: "Territorio 03 · Digital Wellness",
      title: "Digital Wellness Institute",
      description:
        "Desarrolla educación y prácticas alrededor del bienestar digital y la relación saludable con la tecnología.",
      href: "https://www.digitalwellnessinstitute.com/",
    },
  ],
} as const;

export const manifesto = {
  id: "",
  eyebrow: "El principio ConcienTIC",
  title: "Si una experiencia necesita robarte atención para funcionar, debemos rediseñarla.",
  description:
    "Por eso ConcienTIC evita deliberadamente patrones de economía de la atención. Queremos que la tecnología sea una herramienta de conciencia, no otra fuente de ruido.",
  principles: [
    { title: "Menos captura.", description: "Más atención consciente." },
    { title: "Menos consumo.", description: "Más creación y transferencia." },
    { title: "Menos dependencia.", description: "Más autonomía." },
    { title: "Menos ruido.", description: "Más profundidad." },
    { title: "Menos permanencia.", description: "Más valor fuera de la plataforma." },
    { title: "Más tecnología.", description: "Sin menos humanidad." },
  ],
} as const;

export const contact = {
  id: "contacto",
  eyebrow: "Contacto",
  title: "Hablemos de cómo habitar mejor la tecnología.",
  subtitle:
    "Cuéntanos qué reto estás viviendo en tu organización, equipo o comunidad. Podemos explorar una ruta de competencias digitales, IA, transformación o DQUILIBRIO.",
  closing: "No prometemos más tiempo conectado.",
  closingEmphasis: "Prometemos una conversación con intención.",
} as const;

export const footer = {
  tagline: "Conciencia Digital para un mundo mejor.",
  links: [
    { label: "DQUILIBRIO", href: "#dquilibrio" },
    { label: "Servicios", href: "#servicios" },
    { label: "El desafío", href: "#desafio" },
    { label: "Desinfoxicación", href: "#desinfoxicacion" },
    { label: "Metodología", href: "#metodologia" },
    { label: "Evidencia", href: "#evidencia" },
    { label: "Equipo", href: "#equipo" },
    { label: "Casos", href: "#casos" },
    { label: "Referentes", href: "#referentes" },
    { label: "Contacto", href: "#contacto" },
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
