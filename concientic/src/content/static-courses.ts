import type { CatalogResource } from "@/lib/catalog";

type StaticCourse = {
  provider: string;
  competency: string;
  title: string;
  description: string;
  level: string;
  duration: string;
  language: string;
  format: string;
  guardian: string;
  url: string;
};

function resource(index: number, course: StaticCourse): CatalogResource {
  return {
    id: `static-${index}`,
    title: course.title,
    shortDescription: course.description,
    provider: course.provider,
    providerType: "platform",
    sourceUrl: course.url,
    canonicalUrl: course.url,
    verifiedUrl: course.url,
    urlStatus: "STATIC_LEGACY",
    httpStatus: 200,
    lastVerifiedAt: null,
    freeStatus: "FREE",
    freeExplanation: "Recurso incluido en el catálogo estático de ConcienTIC.",
    language: [course.language],
    level: course.level,
    duration: course.duration,
    format: course.format,
    certificate: null,
    primaryCompetency: course.competency,
    guardianPrimary: course.guardian,
    topics: [],
    audience: [],
    overallScore: null,
    reasonForInclusion: "Catálogo estático heredado de la Biblioteca abierta.",
    status: "ACTIVE",
    notes: "Recurso legado; se conserva mientras el motor amplía la curaduría dinámica.",
  };
}

export const staticCourses: CatalogResource[] = [
  resource(1, { provider: "OpenLearn", competency: "informacion", title: "Digital skills: succeeding in a digital world", description: "Curso práctico sobre identidad digital, bienestar, seguridad, información y sobrecarga.", level: "Inicial", duration: "24 h / 8 semanas", language: "Inglés", format: "course", guardian: "Byte", url: "https://www.open.edu/openlearn/digital-computing/digital-skills-succeeding-digital-world" }),
  resource(2, { provider: "Google", competency: "informacion", title: "Google Applied Digital Skills", description: "Biblioteca de lecciones y proyectos para desarrollar habilidades digitales mediante práctica.", level: "Inicial", duration: "Variable", language: "Español", format: "library", guardian: "Byte", url: "https://applieddigitalskills.withgoogle.com/s/es-419/learn" }),
  resource(3, { provider: "Comisión Europea", competency: "informacion", title: "DigComp 3.0", description: "Marco europeo: 21 competencias en cinco áreas, con IA integrada transversalmente.", level: "Todos", duration: "Marco", language: "Inglés", format: "framework", guardian: "Detective DQ", url: "https://joint-research-centre.ec.europa.eu/scientific-activities/key-competences-lifelong-learning/digital-competence-framework-digcomp/digcomp-30_en" }),
  resource(4, { provider: "Google", competency: "comunicacion", title: "Usa Google para conseguir un empleo", description: "Proyecto práctico para utilizar herramientas digitales en búsqueda y preparación para el empleo.", level: "Inicial", duration: "Variable", language: "Español", format: "lesson", guardian: "Nexo", url: "https://applieddigitalskills.withgoogle.com/c/college-and-continuing-education-spanish/es-419/usa-google-para-conseguir-un-empleo/details.html" }),
  resource(5, { provider: "Meta", competency: "comunicacion", title: "Meta Blueprint", description: "Catálogo de aprendizaje sobre comunicación, comunidades, creación y marketing digital.", level: "Inicial", duration: "Variable", language: "Español", format: "library", guardian: "Nexo", url: "https://www.facebookblueprint.com/student/catalog?locale=es" }),
  resource(6, { provider: "Google", competency: "creacion", title: "Google Workspace: Hojas de cálculo — Parte 1", description: "Lección práctica para crear, organizar y dar formato a información.", level: "Inicial", duration: "Variable", language: "Español", format: "lesson", guardian: "Nova", url: "https://applieddigitalskills.withgoogle.com/c/college-and-continuing-education-spanish/es-419/google-workspace-hojas-de-c%C3%A1lculo-parte-1/overview.html" }),
  resource(7, { provider: "Microsoft", competency: "creacion", title: "Creación de contenidos y recursos digitales", description: "Módulo sobre creación y co-creación, licencias abiertas, diseño y accesibilidad.", level: "Inicial", duration: "58 min", language: "Español", format: "module", guardian: "Nova", url: "https://learn.microsoft.com/es-es/training/modules/creacion-recursos-digitales/" }),
  resource(8, { provider: "Cisco", competency: "creacion", title: "Create Digital Content, Communicate and Collaborate Online", description: "Curso para crear documentos digitales, comunicar y colaborar en línea.", level: "Inicial", duration: "6 h", language: "Inglés", format: "course", guardian: "Nexo", url: "https://www.netacad.com/courses/create-communicate-collaborate" }),
  resource(9, { provider: "MinTIC", competency: "seguridad", title: "Seguridad Digital — Talento GovTech 2026", description: "Formación gratuita y certificable dentro de la oferta vigente de MinTIC.", level: "Inicial / intermedio", duration: "Variable", language: "Español", format: "course", guardian: "Locky", url: "https://lms.mintic.gov.co/course/index.php?categoryid=21" }),
  resource(10, { provider: "MinTIC", competency: "seguridad", title: "Seguridad y Privacidad de la Información — 2026", description: "Contenido institucional sobre seguridad y privacidad.", level: "Inicial / intermedio", duration: "Variable", language: "Español", format: "course", guardian: "Locky", url: "https://gobiernodigital.mintic.gov.co/portal/Cursos-Talento-GovTech/" }),
  resource(11, { provider: "OpenLearn", competency: "seguridad", title: "Introduction to Cyber Security: Stay Safe Online", description: "Introducción a malware, phishing, criptografía, identidad y gestión de riesgos.", level: "Inicial", duration: "Variable", language: "Inglés", format: "course", guardian: "Locky", url: "https://www.open.edu/openlearn/digital-computing/introduction-cyber-security-stay-safe-online/" }),
  resource(12, { provider: "Cisco", competency: "seguridad", title: "Introduction to Cybersecurity", description: "Curso introductorio sobre amenazas, vulnerabilidades y fundamentos de ciberseguridad.", level: "Inicial", duration: "6 h", language: "Multidioma", format: "course", guardian: "Locky", url: "https://www.netacad.com/courses/introduction-to-cybersecurity" }),
  resource(13, { provider: "DQ Institute", competency: "discernimiento", title: "DQ Framework", description: "Marco global con 24 competencias en ocho áreas de la vida digital.", level: "Todos", duration: "Marco", language: "Inglés", format: "framework", guardian: "Detective DQ", url: "https://www.dqinstitute.org/global-standards/" }),
  resource(14, { provider: "UNESCO", competency: "discernimiento", title: "Competencias y habilidades digitales", description: "Recursos sobre competencias digitales, inclusión, participación y uso prudente de información.", level: "Todos", duration: "Variable", language: "Español", format: "library", guardian: "Lex", url: "https://www.unesco.org/es/digital-competencies-skills?hub=394" }),
  resource(15, { provider: "Microsoft", competency: "discernimiento", title: "Facilitar la Competencia Digital a los estudiantes", description: "Ruta sobre alfabetización informacional, comunicación, colaboración y resolución de problemas.", level: "Inicial", duration: "2 h 7 min", language: "Español", format: "route", guardian: "Lex", url: "https://learn.microsoft.com/es-es/training/" }),
  resource(16, { provider: "MinTIC", competency: "ia", title: "Inteligencia Artificial Aplicada — 2026", description: "Formación sobre IA aplicada, riesgos y criterios para decisiones informadas.", level: "Inicial / intermedio", duration: "Variable", language: "Español", format: "course", guardian: "Nova", url: "https://lms.mintic.gov.co/course/index.php?categoryid=21" }),
  resource(17, { provider: "IBM", competency: "ia", title: "Introducción a la IA generativa", description: "Fundamentos de IA generativa y consideraciones éticas en IBM SkillsBuild.", level: "Inicial", duration: "Variable", language: "Español", format: "course", guardian: "Nova", url: "https://skillsbuild.org/es/learning-catalog/university-catalog" }),
  resource(18, { provider: "Microsoft", competency: "ia", title: "Fluency de IA", description: "Ruta para comprender conceptos, usos y capacidades de inteligencia artificial.", level: "Inicial", duration: "4 h 38 min", language: "Español", format: "route", guardian: "Nova", url: "https://learn.microsoft.com/es-es/training/paths/ai-fluency/" }),
  resource(19, { provider: "Microsoft", competency: "ia", title: "Conceptos de inteligencia artificial", description: "Ruta introductoria para comprender fundamentos y conceptos clave de IA.", level: "Inicial", duration: "3 h 51 min", language: "Español", format: "route", guardian: "Nova", url: "https://learn.microsoft.com/es-es/training/paths/ai-concepts/" }),
  resource(20, { provider: "Santander", competency: "ia", title: "Formación en IA, datos y tecnología", description: "Cursos gratuitos sobre IA, datos, prompting y uso seguro y responsable.", level: "Inicial / intermedio", duration: "Variable", language: "Español", format: "library", guardian: "Nova", url: "https://www.santanderopenacademy.com/es/sites/courses/tech.html" }),
  resource(21, { provider: "OpenLearn", competency: "bienestar", title: "Digital skills: succeeding in a digital world", description: "Incluye bienestar, identidad digital, herramientas, información y gestión de sobrecarga.", level: "Inicial", duration: "24 h / 8 semanas", language: "Inglés", format: "course", guardian: "Emi", url: "https://www.open.edu/openlearn/digital-computing/digital-skills-succeeding-digital-world" }),
  resource(22, { provider: "DQ Institute", competency: "bienestar", title: "Balanced & Healthy Use of Technology", description: "Competencia relacionada con uso equilibrado y autorregulado de tecnología.", level: "Todos", duration: "Marco", language: "Inglés", format: "framework", guardian: "Emi", url: "https://www.dqinstitute.org/global-standards/" }),
  resource(23, { provider: "Comisión Europea", competency: "bienestar", title: "DigComp 3.0 · Bienestar y derechos digitales", description: "El marco incorpora bienestar, derechos, responsabilidades y elección consciente.", level: "Todos", duration: "Marco", language: "Inglés", format: "framework", guardian: "Emi", url: "https://joint-research-centre.ec.europa.eu/scientific-activities/key-competences-lifelong-learning/digital-competence-framework-digcomp/digcomp-30_en" }),
  resource(24, { provider: "MinTIC", competency: "ciudadania", title: "Plataforma de formación MinTIC", description: "Oferta gratuita sobre habilidades digitales, ciudadanía, uso seguro de Internet y herramientas.", level: "Inicial / intermedio", duration: "Variable", language: "Español", format: "library", guardian: "Lex", url: "https://formacionapropiacion.mintic.gov.co/" }),
  resource(25, { provider: "Comisión Europea", competency: "ciudadania", title: "DigComp 3.0 · Ciudadanía digital", description: "Marco para participación responsable, derechos, seguridad, información y uso consciente.", level: "Todos", duration: "Marco", language: "Inglés", format: "framework", guardian: "Lex", url: "https://joint-research-centre.ec.europa.eu/scientific-activities/key-competences-lifelong-learning/digital-competence-framework-digcomp/digcomp-30_en" }),
  resource(26, { provider: "DQ Institute", competency: "ciudadania", title: "DQ Framework · Digital Citizenship", description: "Competencias para identidad, uso, seguridad, comunicación, derechos y alfabetización.", level: "Todos", duration: "Marco", language: "Inglés", format: "framework", guardian: "Lex", url: "https://www.dqinstitute.org/global-standards/" }),
  resource(27, { provider: "Google", competency: "comunicacion", title: "Google Workspace: Presentaciones — Parte 1", description: "Lección práctica para crear presentaciones digitales y comunicar información.", level: "Inicial", duration: "Variable", language: "Español", format: "lesson", guardian: "Nexo", url: "https://applieddigitalskills.withgoogle.com/s/es-419/learn" }),
  resource(28, { provider: "IBM", competency: "informacion", title: "IBM SkillsBuild", description: "Plataforma gratuita con formación en IA, datos, ciberseguridad y tecnología.", level: "Inicial", duration: "Variable", language: "Español", format: "library", guardian: "Byte", url: "https://skillsbuild.org/es" }),
  resource(29, { provider: "Cisco", competency: "informacion", title: "Digital Awareness", description: "Curso para desarrollar conciencia digital y prácticas esenciales en entornos digitales.", level: "Inicial", duration: "6 h", language: "Español", format: "course", guardian: "Byte", url: "https://www.netacad.com/courses/digital-awareness" }),
  resource(30, { provider: "Santander", competency: "creacion", title: "Santander Open Academy · Tecnología", description: "Cursos gratuitos de herramientas digitales, programación, datos, IA y tecnología.", level: "Inicial / intermedio", duration: "Variable", language: "Español", format: "library", guardian: "Nova", url: "https://www.santanderopenacademy.com/es/sites/courses/tech.html" }),
];
