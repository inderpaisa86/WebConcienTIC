// Agenda de talleres presenciales · El Arte que Conecta
// Fuente: formulario de inscripción (Google Forms). Agenda de diciembre 2026 (entrega V3.4).
// Para publicar nuevas fechas, edita solo este archivo.

export const FORMULARIO_INSCRIPCION =
  'https://docs.google.com/forms/d/e/1FAIpQLSfKp_aeGPwpa4fF2RzFAuGejDIPZDTm5svUGnof1QmB237SJw/viewform?usp=header';

export const CIUDAD = 'Ibagué';

/** Configuración de visibilidad por región (ver README). */
export const GEO = {
  /** Códigos de región ISO 3166-2 (parte de región) que ven la agenda completa. Tolima = TOL. */
  regionesPermitidas: ['TOL'],
  /** Ciudades (sin tildes, minúsculas) que ven la agenda completa. */
  ciudadesPermitidas: ['ibague'],
  /** Qué ve el resto: 'compacto' = aviso con botón "Ver agenda de Ibagué"; 'oculto' = no se muestra la sección. */
  modoFuera: 'compacto' as 'compacto' | 'oculto',
};

export type Sesion = { fecha: string; horario: string }; // fecha ISO yyyy-mm-dd (hora Colombia)

export type TallerPresencial = {
  id: string;
  nombre: string;
  descripcion: string;
  nivel: 'principiante' | 'intermedio';
  /** Requisito para inscribirse (se muestra en un recuadro bajo la descripción). */
  requisito?: string;
  color: string;
  imagen: string;
  precio: number; // COP
  horas: number;
  cerrado: boolean;
  sesiones: Sesion[];
};

export const TALLERES: TallerPresencial[] = [
  {
    id: 'paris',
    nombre: 'Bolso París',
    descripcion: 'Un bolso de líneas limpias y asas de madera, ideal para el día a día y para dar tus primeros nudos.',
    nivel: 'principiante',
    color: '#b9653f',
    imagen: '/elartequeconecta/assets/catalogo/paris-naranja-card.webp',
    precio: 200000,
    horas: 6,
    cerrado: false,
    sesiones: [
      { fecha: '2026-12-02', horario: '2:00 – 5:00 p. m.' },
      { fecha: '2026-12-09', horario: '2:00 – 5:00 p. m.' },
    ],
  },
  {
    id: 'santorini-dic',
    nombre: 'Bolso Santorini',
    descripcion: 'El bolso favorito de nuestras alumnas, ideal para empezar en el macramé.',
    nivel: 'principiante',
    color: '#63300e',
    imagen: '/elartequeconecta/assets/talleres/bolso-santorini.webp',
    precio: 220000,
    horas: 6,
    cerrado: false,
    sesiones: [
      { fecha: '2026-12-04', horario: '2:00 – 5:00 p. m.' },
      { fecha: '2026-12-11', horario: '2:00 – 5:00 p. m.' },
    ],
  },
  {
    id: 'verona',
    nombre: 'Tula Verona',
    descripcion: 'Una tula amplia con brillo y textura, nuestro taller más completo.',
    nivel: 'intermedio',
    requisito: 'Debes haber realizado por lo menos el Bolso Santorini o el Bolso París.',
    color: '#77784e',
    imagen: '/elartequeconecta/assets/talleres/tula-verona.webp',
    precio: 290000,
    horas: 9,
    cerrado: false,
    sesiones: [
      { fecha: '2026-12-05', horario: '9:00 a. m. – 12:00 m.' },
      { fecha: '2026-12-12', horario: '9:00 a. m. – 12:00 m.' },
      { fecha: '2026-12-19', horario: '9:00 a. m. – 12:00 m.' },
    ],
  },
];