import Image from "next/image";
import { preload } from "react-dom";

import styles from "./elartequeconecta.module.css";
import { HeroVideo } from "./hero-video";
import { PromoNoviembre } from "./promo-noviembre";
import { Reveal } from "./reveal";
import TalleresPresenciales from "@/components/elartequeconecta/TalleresPresenciales";

const HOTMART_URL = "https://elartequeconecta.hotmart.host/";

const PAGE_URL = "https://www.concientic.com/elartequeconecta";

const WHATSAPP_URL =
  "https://wa.me/573133072355?text=Hola%2C%20El%20Arte%20que%20Conecta%20%F0%9F%91%8B%20Quiero%20una%20pieza%20tejida%20por%20las%20alumnas%20de%20la%20escuela.";

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
            <a href="#talleres">Talleres presenciales</a>
            <a href="#comunidad">Comunidad</a>
            <a className={styles.navBtn} href="/elartequeconecta/catalogo">
              Ver catálogo
            </a>
          </nav>
        </div>
      </header>

      <main id="contenido">
        {/* Portada */}
        <section className={styles.hx} aria-label="Portada">
          <Image
            className={styles.hxLeafL}
            src="/elartequeconecta/hoja-izq.webp"
            width={120}
            height={24}
            alt=""
            aria-hidden="true"
          />
          <Image
            className={styles.hxLeafT}
            src="/elartequeconecta/hoja-sup.webp"
            width={165}
            height={150}
            alt=""
            aria-hidden="true"
          />
          <div className={`${styles.wrap} ${styles.hxGrid}`}>
            <div className={`${styles.hxCopy} ${styles.rise}`}>
              <p className={styles.eyebrow}>El Arte que Conecta</p>
              <div className={styles.hxDiv} aria-hidden="true">
                <span className={styles.hxLine} />
                <svg viewBox="0 0 24 24" width="26" height="26">
                  <path
                    d="M12 20.5s-7.5-4.6-7.5-10A4.3 4.3 0 0 1 12 7.6a4.3 4.3 0 0 1 7.5 2.9c0 5.4-7.5 10-7.5 10Z"
                    fill="none"
                    stroke="currentColor"
                    strokeWidth="1.6"
                  />
                </svg>
                <span className={styles.hxLine} />
              </div>
              <h1>
                Teje momentos.
                <br />
                <em className={styles.hxHl}>Crea con el corazón.</em>
              </h1>
              <p>
                Aprende macramé, desarrolla tu creatividad y transforma hilos en piezas llenas de
                significado. A tu ritmo, desde donde estés.
              </p>
              <div className={styles.actions}>
                <a className={styles.btn} href="/elartequeconecta/escuela">
                  Conoce la escuela <span aria-hidden="true">→</span>
                </a>
                <a className={`${styles.btn} ${styles.btnOutline}`} href="#talleres">
                  Talleres presenciales
                </a>
              </div>
            </div>
            <div className={`${styles.hxMedia} ${styles.rise} ${styles.rise2}`}>
              <svg className={styles.hxSpark} viewBox="0 0 60 50" aria-hidden="true">
                <path
                  d="M8 30 4 10M24 22l2-20M38 26 50 8M44 40l14-8"
                  stroke="currentColor"
                  strokeWidth="4"
                  strokeLinecap="round"
                  fill="none"
                />
              </svg>
              <HeroVideo />
            </div>
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

        {/* Talleres presenciales (Ibagué) */}
        <TalleresPresenciales />

        {/* Catálogo (resumen con enlace al catálogo completo) */}
        <section id="catalogo" className={styles.cpromo}>
          <div className={`${styles.wrap} ${styles.cpGrid}`}>
            <div className={styles.cpMosaic}>
              <figure>
                <Image
                  src="/elartequeconecta/assets/catalogo/paris-naranja-card.webp"
                  alt="Bolsos París en macramé tejido a mano por alumnas de la escuela"
                  fill
                  sizes="(max-width: 820px) 90vw, 260px"
                  loading="lazy"
                />
                <figcaption>Bolsos París</figcaption>
              </figure>
              <figure>
                <Image
                  src="/elartequeconecta/assets/catalogo/bol-santorini.webp"
                  alt="Bolso Santorini en macramé tejido a mano por alumnas de la escuela"
                  fill
                  sizes="(max-width: 820px) 90vw, 200px"
                  loading="lazy"
                  className={styles.cpPosSantorini}
                />
                <figcaption>Bolso Santorini</figcaption>
              </figure>
              <figure>
                <Image
                  src="/elartequeconecta/assets/catalogo/portamacetas.webp"
                  alt="Portamacetas en macramé tejido a mano por alumnas de la escuela"
                  fill
                  sizes="(max-width: 820px) 90vw, 200px"
                  loading="lazy"
                  className={styles.cpPosPortamacetas}
                />
                <figcaption>Portamacetas</figcaption>
              </figure>
            </div>
            <div className={styles.cpText}>
              <p className={styles.eyebrow}>Catálogo · hecho a mano en Ibagué</p>
              <h2>
                Cada pieza la tejió <em>una alumna de nuestra escuela</em>
              </h2>
              <p>
                Bolsos y piezas tejidas a mano por la comunidad de la Escuela de Macramé en Ibagué.{" "}
                <b>Con tu compra apoyas el talento de tejedoras locales.</b>
              </p>
              <PromoNoviembre />
              <div className={styles.cpCta}>
                <a className={styles.btn} href="/elartequeconecta/catalogo">
                  Ver el catálogo
                </a>
                <a
                  className={`${styles.btn} ${styles.btnOutline}`}
                  href={WHATSAPP_URL}
                  target="_blank"
                  rel="noopener"
                >
                  Pedir por WhatsApp
                </a>
              </div>
            </div>
          </div>
        </section>

        {/* Comunidad */}
        <section className={styles.communitySection} id="comunidad">
          <div className={`${styles.wrap} ${styles.communityGrid}`}>
            <Reveal className={styles.communityVisual}>
              <Image
                src="/elartequeconecta/comunidad-circulo.webp"
                sizes="(max-width: 767px) 78vw, 460px"
                width={208}
                height={208}
                loading="lazy"
                alt="Ilustración de la comunidad El Arte que Conecta"
              />
            </Reveal>
            <Reveal className={styles.communityCopy} delay={120}>
              <p className={styles.eyebrow}>Hilos que nos unen</p>
              <h2>Tu creatividad merece un espacio.</h2>
              <p>Aprende, comparte y crea junto a nuestra comunidad.</p>
              <a className={styles.btn} href={HOTMART_URL} target="_blank" rel="noopener">
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
