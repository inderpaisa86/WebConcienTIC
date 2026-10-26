// Contenido tipado de la página /laboratorios (LaboratoriosIA).
// Reproduce "tal cual" los textos en español del mock
// concientic_laboratoriosIA_v2.html (solo las secciones <main>).
// Los SVG inline de los logo-slots viven en el componente de render, no aquí.

export const laboratorios = {
  meta: {
    title: "LaboratoriosIA — ConcienTIC",
    description:
      "Explora los laboratorios de inteligencia artificial de ConcienTIC: SIGINEX, EmprendeCreativo, DQ-Nexus y TutorIA.",
  },
  hero: {
    id: "inicio",
    pill: "Experimenta con intención. Aplica con criterio.",
    titleLead: "La inteligencia artificial empieza cuando la llevas",
    titleEmphasis: "a la práctica.",
    copy: "Conoce los laboratorios de ConcienTIC: espacios para explorar, aprender y aplicar inteligencia artificial en retos reales. Elige el que conecta con tu necesidad y continúa hacia su experiencia interactiva.",
    primaryCta: { label: "Explorar laboratorios", arrow: "↘", href: "#laboratorios" },
    secondaryCta: { label: "Conoce ConcienTIC ↗", href: "/" },
    note: "Menos ruido. Más criterio. Tecnología al servicio de las personas.",
  },
  labs: {
    id: "laboratorios",
    eyebrow: "Ecosistema de innovación aplicada",
    titleLead: "Cuatro laboratorios.",
    titleTail: "Un propósito compartido.",
    intro:
      "Cada laboratorio responde a un reto diferente. Descubre su propósito y accede a la herramienta inteligente que te permitirá explorar, aprender o actuar.",
    cards: [
      {
        key: "SIGINEX",
        variant: "",
        logoLabel: "Espacio reservado para el logo oficial de SIGINEX",
        tag: "01 · Gestión inteligente",
        name: "Sistema de Gestión SIGINEX",
        description:
          "Un laboratorio de inteligencia artificial orientado a la Seguridad y Salud en el Trabajo. Busca apoyar la gestión de información, la identificación de riesgos y la toma de decisiones preventivas, con criterio profesional y supervisión humana.",
        cardNumber: "LABORATORIO 01",
        linkLabel: "Explorar SIGINEX",
        dataLab: "SIGINEX",
        // Enruta a la app de Siginex servida bajo /siginex mediante rewrite (ver next.config.ts).
        href: "/siginex",
        // Abre SIGINEX en una pestaña nueva.
        newTab: true,
      },
      {
        key: "EmprendeCreativo",
        variant: "violet",
        logoLabel: "Espacio reservado para el logo oficial de EmprendeCreativo",
        tag: "02 · Creatividad y emprendimiento",
        name: "EmprendeCreativo",
        description:
          "Convierte ideas en proyectos creativos con apoyo de la inteligencia artificial. Explora oportunidades, estructura propuestas y descubre nuevas formas de diseñar soluciones digitales con propósito emprendedor.",
        cardNumber: "LABORATORIO 02",
        linkLabel: "Entrar al laboratorio",
        dataLab: "EmprendeCreativo",
        // EmprendeCreativo enruta a la propia página /laboratorios (decisión del usuario).
        href: "/laboratorios",
      },
      {
        key: "DQ-Nexus",
        variant: "blue",
        logoLabel: "Espacio reservado para el logo oficial de DQ-Nexus",
        tag: "03 · Inteligencia digital",
        name: "Autodiagnóstico de Inteligencia Digital DQ-Nexus",
        description:
          "Explora tus competencias de inteligencia digital, reconoce fortalezas e identifica oportunidades de desarrollo. Una experiencia de autoconocimiento para avanzar con mayor criterio y conciencia en entornos digitales.",
        cardNumber: "LABORATORIO 03",
        linkLabel: "Iniciar autodiagnóstico",
        dataLab: "DQ-Nexus",
        href: "#configurar-dqnexus",
      },
      {
        key: "TutorIA",
        variant: "orange",
        logoLabel: "Espacio reservado para el logo oficial de TutorIA",
        tag: "04 · Aprendizaje inteligente",
        name: "Aprende IA con TutorIA",
        description:
          "Aprende a tu ritmo con un tutor inteligente que acompaña la exploración de conceptos, ayuda a resolver dudas y facilita el acercamiento práctico a la inteligencia artificial desde tus preguntas e intereses.",
        cardNumber: "LABORATORIO 04",
        linkLabel: "Aprender con TutorIA",
        dataLab: "TutorIA",
        href: "#configurar-tutoria",
      },
    ],
    introBand: [
      {
        variant: "",
        miniLabel: "Cómo funciona",
        title: "Una introducción antes de la experiencia",
        description:
          "Esta página presenta el propósito de cada laboratorio y ayuda a elegir el punto de entrada adecuado antes de pasar al artefacto que opera el motor de IA.",
      },
      {
        variant: "accent",
        miniLabel: "Una experiencia coherente",
        title: "Identidades propias, visión compartida",
        description:
          "Cada laboratorio podrá incorporar su logo, lenguaje y color de acento, manteniendo la misma estructura editorial y el sistema visual de ConcienTIC.",
      },
    ],
  },
  enfoque: {
    id: "enfoque",
    eyebrow: "Tecnología con intención",
    title: "La IA tiene un papel. Las personas conservan el protagonismo.",
    paragraph:
      "En ConcienTIC, la inteligencia artificial es un medio para comprender, crear y decidir mejor. Cada experiencia debe responder a una necesidad concreta y dejar espacio para el criterio humano.",
    cta: { label: "Conoce nuestra visión ↗", href: "/" },
    cards: [
      {
        title: "Propósito antes que ruido",
        description:
          "Experiencias que empiezan por una necesidad clara, no por consumir más contenido.",
      },
      {
        title: "Criterio y responsabilidad",
        description:
          "La IA acompaña el análisis; las personas interpretan, verifican y deciden.",
      },
      {
        title: "Aprender haciendo",
        description: "Explorar, probar y aplicar el conocimiento a situaciones concretas.",
      },
      {
        title: "Equilibrio digital",
        description: "Usar la tecnología de forma consciente, con intención y límites claros.",
      },
    ],
  },
  finalCta: {
    eyebrow: "Tu siguiente paso empieza aquí",
    title: "Encuentra el laboratorio que conecta con tu reto.",
    paragraph:
      "Explora las opciones, elige la experiencia que necesitas y continúa hacia el artefacto correspondiente.",
    cta: { label: "Elegir un laboratorio ↗", href: "#laboratorios" },
  },
} as const;
