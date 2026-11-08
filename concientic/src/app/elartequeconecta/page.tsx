import Image from "next/image";

import styles from "./elartequeconecta.module.css";
import { Reveal } from "./reveal";

const HOTMART_URL = "https://elartequeconecta.hotmart.host/";

const ARBOL_NAVIDAD_3D_URL =
  "https://hotmart.com/es/marketplace/productos/arbol-de-navidad-3d/D108220799V?ref=T108269822F";

const PAGE_URL = "https://www.concientic.com/elartequeconecta";

const jsonLd = {
  "@context": "https://schema.org",
  "@type": "EducationalOrganization",
  name: "El Arte que Conecta",
  description:
    "Escuela de macramé online: programa principal, cursos de bolsos y Navidad, talleres individuales y una comunidad artesanal.",
  url: PAGE_URL,
  image: "https://www.concientic.com/elartequeconecta/logomacrame.jpg",
  sameAs: [HOTMART_URL],
};

export default function ElArteQueConectaPage() {
  return (
    <div className={styles.page} id="inicio">
      <script
        type="application/ld+json"
        dangerouslySetInnerHTML={{ __html: JSON.stringify(jsonLd) }}
      />
      <a className={styles.skip} href="#contenido">
        Saltar al contenido
      </a>

      <header className={styles.siteHeader}>
        <div className={`${styles.wrap} ${styles.nav}`}>
          <a className={styles.brand} href="#inicio">
            <span aria-hidden="true">♥</span> El Arte que Conecta
          </a>
          <nav aria-label="Principal">
            <a href="#escuela">La escuela</a>
            <a href="#talleres">Talleres</a>
            <a href="#comunidad">Comunidad</a>
          </nav>
        </div>
      </header>

      <main id="contenido">
        {/* Portada */}
        <section className={styles.hero}>
          <div className={styles.wrap}>
            <div className={styles.rise}>
              <p className={styles.eyebrow}>Macramé · creatividad · conexión</p>
              <h1>
                Teje momentos.
                <br />
                <em>Crea con el corazón.</em>
              </h1>
              <p className={styles.lead}>
                Un espacio para aprender macramé, descubrir tu creatividad y transformar hilos en
                piezas llenas de significado. A tu ritmo, desde donde estés.
              </p>
              <div className={styles.actions}>
                <a
                  className={styles.btn}
                  href={HOTMART_URL}
                  target="_blank"
                  rel="noopener noreferrer"
                >
                  Conoce la escuela
                </a>
                <a className={`${styles.btn} ${styles.btnOutline}`} href="#talleres">
                  Explora los talleres
                </a>
              </div>
            </div>
            <Image
              className={`${styles.heroImg} ${styles.rise} ${styles.rise2}`}
              src="/elartequeconecta/logomacrame.jpg"
              width={800}
              height={800}
              priority
              alt="Logo de El Arte que Conecta — escuela de macramé"
            />
          </div>
        </section>

        {/* Programas */}
        <section className={styles.section} id="escuela">
          <div className={`${styles.wrap} ${styles.escuelaWrap}`}>
            <div className={styles.sectionHead}>
              <p className={styles.eyebrow}>Aprende y crea</p>
              <h2>Un universo de hilos y posibilidades</h2>
              <p>Encuentra inspiración, formación y proyectos para acompañar tu camino creativo.</p>
            </div>
            <div className={styles.tiles}>
              <Reveal as="article" className={styles.tile}>
                <div className={styles.media}>
                  <Image
                    src="/elartequeconecta/programa.webp"
                    width={1000}
                    height={723}
                    loading="lazy"
                    alt="Instructora sentada junto a piezas de macramé en su taller"
                  />
                </div>
                <div className={styles.tileBody}>
                  <h3>Programa principal</h3>
                  <p>Todos los proyectos que quieres aprender, en un solo lugar.</p>
                  <a className={styles.tileLink} href="/elartequeconecta/escuela">
                    Explora el programa <span aria-hidden="true">→</span>
                  </a>
                </div>
              </Reveal>
              <Reveal as="article" className={styles.tile} delay={120}>
                <div className={styles.media}>
                  <Image
                    src="/elartequeconecta/bolsos.webp"
                    width={1000}
                    height={667}
                    loading="lazy"
                    alt="Tres bolsos de macramé exhibidos en maniquí y soportes"
                  />
                </div>
                <div className={styles.tileBody}>
                  <h3>Curso de Bolsos</h3>
                  <p>Aprende a crear bolsos artesanales con técnicas y detalles únicos.</p>
                  <a className={styles.tileLink} href="/elartequeconecta/bolsos">
                    Descubre el curso <span aria-hidden="true">→</span>
                  </a>
                </div>
              </Reveal>
              <Reveal as="article" className={styles.tile} delay={240}>
                <div className={styles.media}>
                  <Image
                    src="/elartequeconecta/navidad.webp"
                    width={1000}
                    height={626}
                    loading="lazy"
                    alt="Instructora junto a árboles de Navidad tejidos en macramé"
                  />
                </div>
                <div className={styles.tileBody}>
                  <h3>Curso de Navidad</h3>
                  <p>Teje decoraciones cálidas y especiales para la temporada.</p>
                  <a className={styles.tileLink} href="/elartequeconecta/navidad">
                    Descubre el curso <span aria-hidden="true">→</span>
                  </a>
                </div>
              </Reveal>
            </div>
          </div>
        </section>

        {/* Talleres */}
        <section className={styles.workshops} id="talleres">
          <div className={styles.wrap}>
            <div className={styles.sectionHead}>
              <p className={styles.eyebrow}>Talleres individuales</p>
              <h2>Un proyecto. Un nuevo comienzo.</h2>
              <p>Elige tu próxima creación y disfruta el proceso, nudo a nudo.</p>
            </div>
            <div className={styles.workshopGrid}>
              <Reveal as="article" className={styles.workshop}>
                <div className={styles.workshopArt}>
                  <Image
                    src="/elartequeconecta/maxibolso.webp"
                    width={619}
                    height={1100}
                    loading="lazy"
                    alt="Maxibolsos de macramé con flecos sobre una mesa de madera"
                  />
                </div>
                <div>
                  <span className={`${styles.eyebrow} ${styles.pill}`}>Taller individual</span>
                  <h3>Maxibolso</h3>
                  <p>Un accesorio con personalidad, movimiento y detalles artesanales.</p>
                  <p className={styles.coupon}>
                    🎟️ Cupón: <strong>BLACKNOV50</strong> (por todo noviembre)
                  </p>
                  <a
                    className={`${styles.btn} ${styles.btnSm}`}
                    href={HOTMART_URL}
                    target="_blank"
                    rel="noopener noreferrer"
                    aria-label="Ver taller Maxibolso"
                  >
                    Ver taller
                  </a>
                </div>
              </Reveal>
              <Reveal as="article" className={styles.workshop} delay={100}>
                <div className={styles.workshopArt}>
                  <Image
                    src="/elartequeconecta/arbol-navidad.webp"
                    width={619}
                    height={1100}
                    loading="lazy"
                    alt="Instructora sosteniendo un árbol de Navidad 3D tejido en verde"
                  />
                </div>
                <div>
                  <span className={`${styles.eyebrow} ${styles.pill}`}>Taller individual</span>
                  <h3>Árbol de Navidad 3D</h3>
                  <p>Una pieza protagonista para vestir tus espacios de magia tejida.</p>
                  <p className={styles.coupon}>
                    🎟️ Cupón: <strong>BLACKNOV50</strong> (por todo noviembre)
                  </p>
                  <a
                    className={`${styles.btn} ${styles.btnSm}`}
                    href={ARBOL_NAVIDAD_3D_URL}
                    target="_blank"
                    rel="noopener noreferrer"
                    aria-label="Ver taller Árbol de Navidad 3D"
                  >
                    Ver taller
                  </a>
                </div>
              </Reveal>
              <Reveal as="article" className={styles.workshop} delay={200}>
                <div className={styles.workshopArt}>
                  <Image
                    src="/elartequeconecta/tapiz-prana.webp"
                    width={974}
                    height={808}
                    loading="lazy"
                    alt="Tapiz de macramé colgado sobre un sofá en una sala cálida"
                  />
                </div>
                <div>
                  <span className={`${styles.eyebrow} ${styles.pill}`}>Taller individual</span>
                  <h3>Tapiz Prana</h3>
                  <p>Un tejido decorativo que suma calidez, textura y armonía a tu hogar.</p>
                  <p className={styles.coupon}>
                    🎟️ Cupón: <strong>BLACKNOV50</strong> (por todo noviembre)
                  </p>
                  <a
                    className={`${styles.btn} ${styles.btnSm}`}
                    href={HOTMART_URL}
                    target="_blank"
                    rel="noopener noreferrer"
                    aria-label="Ver taller Tapiz Prana"
                  >
                    Ver taller
                  </a>
                </div>
              </Reveal>
              <Reveal as="article" className={styles.workshop} delay={300}>
                <div className={styles.workshopArt}>
                  <Image
                    src="/elartequeconecta/arbol-pared.webp"
                    width={656}
                    height={1100}
                    loading="lazy"
                    alt="Instructora mostrando un árbol de pared tejido en macramé"
                  />
                </div>
                <div>
                  <span className={`${styles.eyebrow} ${styles.pill}`}>Taller individual</span>
                  <h3>Árbol de pared</h3>
                  <p>
                    Una pieza artesanal inspirada en la naturaleza, ideal para decorar con textura,
                    calidez y encanto bohemio.
                  </p>
                  <p className={styles.coupon}>
                    🎟️ Cupón: <strong>BLACKNOV50</strong> (por todo noviembre)
                  </p>
                  <a
                    className={`${styles.btn} ${styles.btnSm}`}
                    href={HOTMART_URL}
                    target="_blank"
                    rel="noopener noreferrer"
                    aria-label="Ver taller Árbol de pared"
                  >
                    Ver taller
                  </a>
                </div>
              </Reveal>
            </div>
          </div>
        </section>

        {/* Comunidad */}
        <section className={styles.communitySection} id="comunidad">
          <div className={`${styles.wrap} ${styles.communityGrid}`}>
            <Reveal className={styles.communityVisual}>
              <Image
                src="/elartequeconecta/comunidad.webp"
                width={1066}
                height={1018}
                loading="lazy"
                alt="Ilustración de la comunidad El Arte que Conecta"
              />
            </Reveal>
            <Reveal className={styles.communityCopy} delay={120}>
              <p className={styles.eyebrow}>Hilos que nos unen</p>
              <h2>Tu creatividad merece un espacio.</h2>
              <p>Aprende, comparte y crea junto a nuestra comunidad.</p>
              <a
                className={styles.btn}
                href={HOTMART_URL}
                target="_blank"
                rel="noopener noreferrer"
              >
                Únete a El Arte que Conecta
              </a>
            </Reveal>
          </div>
        </section>
      </main>

      <footer className={styles.footer}>
        <div className={styles.wrap}>
          <p>
            El Arte que Conecta · Tejemos creatividad, calma y comunidad{" "}
            <span className={styles.heart} aria-hidden="true">
              ♥
            </span>
          </p>
          <p>© {new Date().getFullYear()} El Arte que Conecta</p>
        </div>
      </footer>
    </div>
  );
}
