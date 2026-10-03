export type LibraryGuardian = {
  category: string;
  color: string;
  image: string;
  alt: string;
};

/** Visual characters exclusive to the open library cards. */
export const libraryGuardians: LibraryGuardian[] = [
  { category: "informacion", color: "#5EDCC5", image: "/library-guardians/informacion.svg", alt: "Robot de Información" },
  { category: "discernimiento", color: "#F2A7C5", image: "/library-guardians/discernimiento.svg", alt: "Detective de Discernimiento" },
  { category: "seguridad", color: "#8BC8F1", image: "/library-guardians/seguridad.svg", alt: "Gato protector de Seguridad" },
  { category: "ia", color: "#C8B5F2", image: "/library-guardians/ia-innovacion.svg", alt: "Exploradora de IA e Innovación" },
  { category: "creacion", color: "#F2B36D", image: "/library-guardians/creacion.svg", alt: "Creadora Digital" },
  { category: "comunicacion", color: "#6DDCC5", image: "/library-guardians/comunicacion.svg", alt: "Guardián de Comunicación" },
  { category: "ciudadania", color: "#8DC9EF", image: "/library-guardians/ciudadania.svg", alt: "Guardián de Ciudadanía" },
  { category: "bienestar", color: "#F2D174", image: "/library-guardians/bienestar.svg", alt: "Guardián de Bienestar" },
];
