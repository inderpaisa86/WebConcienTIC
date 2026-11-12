import Image from "next/image";
import { preload } from "react-dom";

import styles from "./elartequeconecta.module.css";
import { Reveal } from "./reveal";

const HOTMART_URL = "https://elartequeconecta.hotmart.host/";

const PAGE_URL = "https://www.concientic.com/elartequeconecta";

const jsonLd = {
  "@context": "https://schema.org",
  "@type": "EducationalOrganization",
  name: "El Arte que Conecta",
  description:
    "Escuela de macramé online: programa principal, cursos de bolsos y Navidad, catálogo de piezas y una comunidad artesanal.",
  url: PAGE_URL,
  image: "https://www.concientic.com/elartequeconecta/logomacrame.jpg",
  sameAs: [HOTMART_URL],
};

export default function ElArteQueConectaPage() {
  // El póster del video de portada suele ser el LCP: se pide cuanto antes y con prioridad alta.
  preload("/elartequeconecta/assets/catalogo/video/hero.jpg", {
    as: "image",
    fetchPriority: "high",
  });

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
            <a href="/elartequeconecta/catalogo">Catálogo</a>
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
                <a className={styles.btn} href="/elartequeconecta/escuela">
                  Conoce la escuela
                </a>
                <a className={`${styles.btn} ${styles.btnOutline}`} href="#escuela">
                  Explora los cursos
                </a>
              </div>
            </div>
            <video
              className={`${styles.heroVideo} ${styles.rise} ${styles.rise2}`}
              src="/elartequeconecta/assets/catalogo/video/hero.mp4"
              poster="/elartequeconecta/assets/catalogo/video/hero.jpg"
              width={720}
              height={900}
              autoPlay
              muted
              loop
              playsInline
              preload="metadata"
              aria-label="Piezas de macramé tejidas a mano, vistas de cerca"
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
                    sizes="(max-width: 639px) 92vw, (max-width: 1023px) 46vw, 380px"
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
                    sizes="(max-width: 639px) 92vw, (max-width: 1023px) 46vw, 380px"
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
                    sizes="(max-width: 639px) 92vw, (max-width: 1023px) 46vw, 380px"
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

        {/* Comunidad */}
        <section className={styles.communitySection} id="comunidad">
          <div className={`${styles.wrap} ${styles.communityGrid}`}>
            <Reveal className={styles.communityVisual}>
              <Image
                src="/elartequeconecta/comunidad.webp"
                sizes="(max-width: 767px) 78vw, 420px"
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
              <a className={styles.btn} href="/elartequeconecta/escuela">
                Únete a la Escuela de macramé
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
