# Landing "Escuela" V7: replica la estructura de elartequeconecta.hotmart.host con mejoras.
# Se importa desde build.py (usa btn, promo, testimonios, faq, FAQ_COMUN, SITE, temario).

def build_escuela(btn, promo, testimonios, faq, SITE, temario):
    def slide(img, name):
        return (f'<figure class="slide"><img src="assets/fotos/{img}.webp" alt="Proyecto {name} en macramé" '
                f'loading="lazy"><figcaption>{name}</figcaption></figure>')

    NIVELES = [
        ("1", "Principiante", "Aprende los nudos básicos",
         "Tus primeras piezas, mientras dominas los nudos base y el cálculo de hilos.",
         [("p-accesorio-corazon", "Llavero"), ("portamacetas", "Portamacetas"), ("tapiz-chakras", "Tapiz de chakras"), ("strap", "Strap")]),
        ("2", "Intermedio", "Crea proyectos más elaborados",
         "Combina técnicas y da forma a piezas para tu casa y tu día a día.",
         [("p-cojin", "Cojín"), ("p-bolso-paris", "Bolso París"), ("tapiz-hojas", "Tapiz de hojas"), ("p-bolso-bahia", "Bolso Bahía")]),
        ("3", "Avanzado", "Decora tu hogar con estilo",
         "Proyectos protagonistas, con estructura, volumen y acabados profesionales.",
         [("espejo", "Espejo"), ("embarrilado", "Embarrilado"), ("v3_6", "Tapiz Prana"), ("p-tula-verona", "Tula Verona"),
          ("p-maxibolso", "Maxibolso"), ("hombreras", "Hombreras")]),
    ]
    niveles = ""
    for n, nivel, titulo, desc, items in NIVELES:
        cid = f"nivel{n}"
        niveles += f'''<article class="level">
<div class="level-head"><span class="level-num">Nivel {n}</span><div><h3>{nivel} · <em>{titulo}</em></h3><p>{desc}</p></div>
<div class="arrows"><button type="button" class="arrow" data-scroll="{cid}" data-dir="-1" aria-label="Anterior">‹</button><button type="button" class="arrow" data-scroll="{cid}" data-dir="1" aria-label="Siguiente">›</button></div></div>
<div class="carousel" id="{cid}" tabindex="0" aria-label="Proyectos del nivel {nivel}">{"".join(slide(i, t) for i, t in items)}</div>
</article>'''

    INCLUYE = ["Video tutoriales paso a paso", "Acceso a la comunidad privada", "6 bonos exclusivos",
               "Un proyecto nuevo cada mes", "Certificados de participación", "Acceso desde cualquier país"]
    incluye = "".join(f"<li>{x}</li>" for x in INCLUYE)

    BONOS = [("📘", "E-book de materiales", "Conoce los hilos y cuerdas, y cuál usar en cada proyecto."),
             ("🧾", "Lista de materiales y proveedores", "Dónde conseguir lo que necesitas para empezar."),
             ("📏", "Masterclass de medidas", "Domina el cálculo de hilos para no quedarte corta ni desperdiciar."),
             ("🧮", "Calculadora de costos", "Ponle un precio justo a cada pieza si decides vender."),
             ("📓", "Bitácora de macramé", "Organiza tus proyectos, medidas e ideas."),
             ("🤝", "Acceso a la comunidad", "Un espacio de tejedoras para preguntar, compartir y celebrar.")]
    bonos = "".join(f'<article><div class="ic">{i}</div><h3>{t}</h3><p>{d}</p></article>' for i, t, d in BONOS)

    return f'''
<section class="trust"><div class="wrap trust-row">
<span>🛡️ Garantía de 7 días</span><span>🎥 Clases grabadas para ver y repetir</span><span>♾️ Acceso de por vida</span><span>💳 Pago único</span><span>🌎 Desde cualquier país</span>
</div></section>

<section id="aprende"><div class="wrap">
<div class="section-head"><div class="eyebrow">Todo lo que puedes aprender en la escuela</div><h2>Tres niveles, <em>un mismo camino</em></h2>
<p>Proyectos paso a paso, rutas de aprendizaje y herramientas para avanzar a tu propio ritmo.</p></div>
<div class="levels">{niveles}</div>
<details class="fundamentos"><summary>Ver el temario de fundamentos: hilos, nudos, medidas y costos</summary><div class="syllabus">{temario}</div></details>
</div></section>

{promo("escuela", "la Escuela de Macramé", False).replace('<div class="eyebrow promo-only">Noviembre Black</div>', '<div class="eyebrow promo-only">Único pago de inscripción · Noviembre Black</div>').replace('{{INCLUYE}}', '')}

<section id="incluye" class="offer" style="padding-top:0"><div class="wrap"><div class="include-card">
<div><div class="eyebrow">Tu inscripción incluye</div><h2>Acceso de por vida al contenido completo</h2>
<ul class="checks two-col">{incluye}</ul></div>
<div class="guarantee"><b>7</b><div><strong>Días de garantía</strong><p>Si sientes que la escuela no es para ti, solicitas la devolución de tu dinero dentro de los primeros 7 días.</p></div></div>
</div></div></section>

{testimonios()}

<section id="como-funciona" class="workshops"><div class="wrap">
<div class="section-head"><div class="eyebrow">¿Cómo funciona?</div><h2>Del “no sé por dónde empezar” a <em>tu primera pieza</em></h2></div>
<div class="journey">
<article><span class="step">Antes de entrar</span><h3>Quieres crear, pero no sabes por dónde empezar</h3>
<ul class="checks"><li>Ves piezas en redes y sientes que son difíciles.</li><li>No tienes un espacio creativo propio.</li><li>No sabes qué materiales comprar.</li></ul></article>
<article><span class="step">Dentro de la escuela</span><h3>Aprendes a tu ritmo y acompañada</h3>
<ul class="checks"><li>Paso a paso, sin presión ni horarios.</li><li>Un proyecto nuevo cada mes.</li><li>Una comunidad de más de 300 mujeres que tejen contigo.</li></ul></article>
<article><span class="step">Lo que lograrás</span><h3>Piezas hermosas, calma real, ingresos posibles</h3>
<ul class="checks"><li>Crear piezas únicas para tu hogar.</li><li>Un espacio de calma solo para ti.</li><li>Si lo deseas, convertir tu talento en ingresos.</li></ul></article>
</div>
<div style="text-align:center;margin-top:30px">{btn("escuela", "Quiero empezar ahora", "btn", "como-funciona")}</div>
</div></section>

<section id="bonos"><div class="wrap">
<div class="section-head"><div class="eyebrow">Bonos de regalo</div><h2>6 bonos para que <em>no empieces sola</em></h2>
<p>Herramientas que te facilitan todo el proceso, desde elegir materiales hasta ponerle precio a tus piezas.</p></div>
<div class="feat">{bonos}</div>
<div style="text-align:center;margin-top:28px">{btn("escuela", "Quiero mi 50% de descuento", "btn", "bonos")}</div>
</div></section>

{faq([
    ("¿Necesito experiencia previa?", "No. Todo está pensado para empezar desde cero: el nivel principiante parte de los nudos básicos, los materiales y el cálculo de hilos."),
    ("¿Las clases tienen horarios?", "No. Las clases están grabadas y disponibles 24/7, para que las veas y repitas cuando quieras."),
    ("¿Desde qué país puedo acceder?", "Desde cualquier país, con conexión a internet y un celular, tableta o computador."),
    ("¿Qué pasa si tengo dudas?", "Puedes escribir tus preguntas en la plataforma o en la comunidad privada de tejedoras."),
    ("¿Los materiales están incluidos?", "No. La escuela te comparte la lista de materiales y enlaces para comprarlos. En Colombia existe un kit de materiales con costo adicional."),
    ("¿Cómo accedo después de pagar?", "Recibes el acceso por correo electrónico y entras a la escuela en Hotmart Club. El acceso es de por vida."),
    ("¿Y si no es para mí?", "Tienes 7 días de garantía: si la escuela no es lo que esperabas, solicitas la devolución de tu dinero en Hotmart."),
    ("¿El Árbol de Navidad 3D está en la Escuela?", "No. El Árbol de Navidad 3D es un taller individual y hace parte del curso Navidad Mágica. Si ya eres alumna de la Escuela, puedes adquirirlo con 50% de descuento."),
    ("¿Quién es El Arte que Conecta?", "El Arte que Conecta es afiliada de la Escuela de Macramé: te asesoramos y te acompañamos a elegir tu curso, y recibimos una comisión por las compras hechas con estos enlaces, sin costo adicional para ti."),
])}

<section id="instructora"><div class="wrap duo">
<div class="duo-visual"><div class="photo-card"><img src="assets/fotos/instructora.webp" alt="Janeth Flórez, instructora de la Escuela de Macramé" loading="lazy"></div></div>
<div><div class="eyebrow">Conoce a tu instructora</div><h2>Janeth <em>Flórez</em></h2>
<p style="color:var(--text)">Artista textil con más de 7 años dedicada al macramé. Ha acompañado a cientos de mujeres a descubrir en este arte una forma de calma, creatividad y conexión.</p>
<p style="color:var(--text)">En la Escuela de Macramé enseña cada nudo con paciencia y claridad, para que cada proyecto que empieces llegue a su final.</p>
<div style="margin-top:18px">{btn("escuela", "Quiero aprender con Janeth", "btn", "instructora")}</div></div>
</div></section>

<section id="comunidad" style="padding-top:0"><div class="wrap"><div style="max-width:760px;margin:0 auto"><div class="cta">
<div class="eyebrow">Hilos que nos unen</div><h2>Tu creatividad merece un espacio.</h2><p>Aprende, comparte y crea junto a una comunidad de tejedoras.</p>
{btn("escuela", "Quiero empezar ahora", "btn", "final")}<p class="fine" style="font-size:.85rem;color:var(--taupe);margin-top:12px">Pago único · Acceso de por vida · Garantía de 7 días</p>
</div></div></div></section>

<section style="padding-top:0"><div class="wrap"><div class="section-head"><div class="eyebrow">¿Prefieres empezar por un curso?</div></div>
<div class="tiles two">
<article class="tile"><img src="assets/fotos/v3_2.webp" alt="Curso de Bolsos" loading="lazy"><h3>Curso de Bolsos</h3><p>Aprende a crear bolsos artesanales con técnicas y detalles únicos.</p><a href="{SITE}/bolsos">Descubre el curso →</a></article>
<article class="tile"><img src="assets/fotos/v3_3.webp" alt="Navidad Mágica" loading="lazy"><h3>Navidad Mágica</h3><p>Teje decoraciones cálidas y especiales para la temporada.</p><a href="{SITE}/navidad">Descubre el curso →</a></article>
</div></div></section>'''

CSS_V7 = '''
/* ===== V7 · Escuela (estructura Hotmart mejorada) ===== */
.trust{padding:0;background:var(--card);border-top:1px solid var(--line);border-bottom:1px solid var(--line)}
.trust-row{display:flex;flex-wrap:wrap;justify-content:center;gap:10px 28px;padding:16px 0;font-size:.92rem;color:var(--title);font-weight:500}
.levels{display:grid;grid-template-columns:minmax(0,1fr);gap:22px}.level{min-width:0}
.level{background:var(--card);border:1px solid var(--line);border-radius:24px;padding:22px;box-shadow:var(--shadow)}
.level-head{display:grid;grid-template-columns:auto 1fr auto;gap:18px;align-items:center;margin-bottom:16px}
.level-num{justify-self:start;background:var(--rose);color:var(--title);border-radius:999px;padding:6px 14px;font-weight:700;font-size:.82rem;letter-spacing:.06em;text-transform:uppercase;white-space:nowrap}
.level h3{font-size:1.45rem;font-weight:500;margin:0}.level h3 em{font-size:1.2rem}
.level-head p{margin:2px 0 0;color:var(--text);font-size:.95rem}
.arrows{display:flex;gap:8px}
.arrow{width:40px;height:40px;border-radius:50%;border:1px solid var(--line);background:var(--bg);color:var(--title);font-size:1.4rem;line-height:1;cursor:pointer}
.arrow:hover{background:var(--terra);color:var(--card);border-color:var(--terra)}
.carousel{display:grid;grid-auto-flow:column;grid-auto-columns:calc(25% - 12px);gap:16px;overflow-x:auto;scroll-snap-type:x mandatory;scrollbar-width:thin;padding-bottom:6px}
.slide{margin:0;scroll-snap-align:start}
.slide img{width:100%;height:auto;aspect-ratio:1/1;object-fit:cover;border-radius:16px;display:block}
.slide figcaption{font:500 1.05rem var(--serif);color:var(--title);text-align:center;padding:8px 0 0}
.fundamentos{margin-top:22px;background:var(--card);border:1px solid var(--line);border-radius:20px;padding:6px 20px}
.fundamentos>summary{cursor:pointer;list-style:none;padding:14px 0;font-weight:700;color:var(--terra);text-align:center}
.fundamentos>summary::-webkit-details-marker{display:none}
.fundamentos[open]>summary{border-bottom:1px solid var(--line);margin-bottom:6px}
.fundamentos .syllabus{padding-bottom:16px}
.include-card{max-width:760px;margin:0 auto;background:var(--card);border:1px solid var(--line);border-radius:28px;padding:30px clamp(22px,5vw,48px);box-shadow:var(--shadow);display:grid;gap:22px}
.include-card h2{font-size:clamp(1.5rem,3vw,2rem);font-weight:500;margin:6px 0 0}
.checks.two-col{grid-template-columns:repeat(2,minmax(0,1fr))}
.guarantee{display:flex;gap:16px;align-items:center;background:var(--bg);border:1px dashed var(--sand);border-radius:18px;padding:16px 18px}
.guarantee b{display:grid;place-items:center;min-width:64px;height:64px;border-radius:50%;background:var(--rose);color:var(--title);font:600 1.9rem var(--serif)}
.guarantee strong{color:var(--title);font-family:var(--serif);font-size:1.15rem}.guarantee p{margin:2px 0 0;color:var(--text);font-size:.93rem}
.journey{display:grid;grid-template-columns:repeat(3,minmax(0,1fr));gap:20px}
.journey article{background:var(--card);border:1px solid var(--line);border-radius:22px;padding:24px;box-shadow:var(--shadow)}
.journey .step{display:inline-block;background:var(--blush);color:var(--terra);border-radius:999px;padding:5px 12px;font-size:.78rem;font-weight:700;letter-spacing:.08em;text-transform:uppercase}
.journey h3{font-size:1.3rem;font-weight:500;margin:12px 0 4px}
@media(max-width:900px){.carousel{grid-auto-columns:calc(42% - 10px)}.journey{grid-template-columns:1fr}}
@media(max-width:620px){.level-head{grid-template-columns:1fr}.arrows{display:none}.carousel{grid-auto-columns:72%}.checks.two-col{grid-template-columns:1fr}.trust-row{justify-content:flex-start;font-size:.86rem}}
'''

SCRIPT_V7 = '''<script>document.querySelectorAll("[data-scroll]").forEach(function(b){b.addEventListener("click",function(){var c=document.getElementById(b.dataset.scroll);c.scrollBy({left:c.clientWidth*0.8*Number(b.dataset.dir),behavior:"smooth"});});});</script>'''


