// Agenda de talleres presenciales · El Arte que Conecta
// Fuente: formulario de inscripción (Google Forms), revisado el 11 nov 2026.
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
    descripcion: 'Un bolso de líneas limpias y asas de madera, ideal para el día a día.',
    color: '#b9653f',
    imagen: '/elartequeconecta/assets/talleres/bolso-paris.webp',
    precio: 210000,
    horas: 6,
    cerrado: false,
    sesiones: [
      { fecha: '2026-11-14', horario: '9:00 a. m. – 12:00 m.' },
      { fecha: '2026-11-20', horario: '2:00 – 5:00 p. m.' },
    ],
  },
  {
    id: 'verona',
    nombre: 'Tula Verona',
    descripcion: 'Una tula amplia con brillo y textura, nuestro taller más completo.',
    color: '#77784e',
    imagen: '/elartequeconecta/assets/talleres/tula-verona.webp',
    precio: 240000,
    horas: 8,
    cerrado: false,
    sesiones: [
      { fecha: '2026-12-04', horario: '2:00 – 6:00 p. m.' },
      { fecha: '2026-12-11', horario: '2:00 – 6:00 p. m.' },
    ],
  },
  {
    id: 'santorini-dic',
    nombre: 'Bolso Santorini',
    descripcion: 'El bolso favorito de nuestras alumnas, ahora en sesiones de sábado.',
    color: '#63300e',
    imagen: '/elartequeconecta/assets/talleres/bolso-santorini.webp',
    precio: 210000,
    horas: 6,
    cerrado: false,
    sesiones: [
      { fecha: '2026-12-05', horario: '9:00 a. m. – 12:00 m.' },
      { fecha: '2026-12-12', horario: '9:00 a. m. – 12:00 m.' },
    ],
  },
  {
    id: 'santorini-nov',
    nombre: 'Bolso Santorini',
    descripcion: 'Grupo de noviembre completo. Inscríbete en el grupo de diciembre.',
    color: '#8b7765',
    imagen: '/elartequeconecta/assets/talleres/bolso-santorini.webp',
    precio: 210000,
    horas: 6,
    cerrado: true,
    sesiones: [
      { fecha: '2026-11-06', horario: '2:00 – 5:00 p. m.' },
      { fecha: '2026-11-13', horario: '2:00 – 5:00 p. m.' },
    ],
  },
];
