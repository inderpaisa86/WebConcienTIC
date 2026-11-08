"""Genera las landings V4 de El Arte que Conecta sobre el CSS del baseline V3."""
import pathlib, json

ROOT = pathlib.Path(__file__).parent
OUT = ROOT / "build"
CSS = (ROOT / "v3.css").read_text() + (ROOT / "v4.css").read_text()
SITE = "https://www.concientic.com/elartequeconecta"
END = "2026-11-30T23:59:59-05:00"
TRM = 3228  # TRM 6 nov 2026 (referencia)

PAY = {
    "escuela": "https://go.hotmart.com/Q105909603A?ap=7bed",
    "bolsos": "https://go.hotmart.com/G105909650H?ap=ba72",
    "navidad": "https://go.hotmart.com/S108269802C?ap=1a28",
}
PRICE = {"escuela": (97, 48.50), "bolsos": (58, 29.00), "navidad": (58, 29.00)}

def usd(v):
    return f"USD {int(v)}" if v == int(v) else f"USD {v:.2f}".replace(".", ",")

def cop(v):
    return "$" + f"{round(v * TRM, -2):,.0f}".replace(",", ".")

def btn(page, label, cls="btn", pos=""):
    return f'<a class="{cls}" href="{PAY[page]}" target="_blank" rel="noopener sponsored" data-pos="{pos}">{label}</a>'

def project(slug, name):
    return f'<figure class="project"><img src="assets/proyectos/{slug}.webp" alt="Proyecto {name} en macramé" loading="lazy" width="504" height="630"><figcaption>{name}</figcaption></figure>'

def promo(page, product, coupon):
    old, new = PRICE[page]
    if coupon:
        extra = '''<div class="coupon promo-only"><div><small>Usa este cupón al pagar en Hotmart</small><span class="code" id="cupon">BLACKNOV50</span></div><button class="copy" type="button" data-copy="BLACKNOV50">Copiar cupón</button></div>'''
        fine = "Pago único · El cupón BLACKNOV50 aplica del 1 al 30 de noviembre."
    else:
        extra = '''<div class="applied promo-only"><b>✓</b><span>El 50% ya está aplicado en el botón de pago. No necesitas cupón.</span></div>'''
        fine = "Pago único · Oferta válida hasta el 30 de noviembre."
    return f'''<section class="offer" id="precio"><div class="wrap">
<div class="offer-card">
<div class="eyebrow promo-only">Noviembre Black</div><div class="eyebrow after-promo">Inscripciones abiertas</div>
<h2 class="promo-only">50% de descuento en <em>{product}</em></h2>
<h2 class="after-promo">Inscríbete en <em>{product}</em></h2>
<div class="when promo-only">Del 1 al 30 de noviembre</div>
<div class="price"><span class="old promo-only">{usd(old)}</span><span class="new"><span class="promo-only">{usd(new)}</span><span class="after-promo">{usd(old)}</span></span><span class="tag promo-only">-50%</span></div>
<p class="price-cop promo-only">Aprox. <strong>{cop(new)} COP</strong> · antes {cop(old)}</p>
<p class="price-cop after-promo">Aprox. <strong>{cop(old)} COP</strong></p>
<p class="price-note">Valor de referencia en pesos colombianos (TRM del 6 de nov. de 2026: $3.228). Hotmart muestra el valor final según tu país y medio de pago.</p>
{extra}
<div class="countdown promo-only" aria-label="Tiempo restante de la oferta">Termina en <span class="u"><b data-cd="d">--</b>días</span><span class="u"><b data-cd="h">--</b>h</span><span class="u"><b data-cd="m">--</b>min</span><span class="u"><b data-cd="s">--</b>s</span></div>
{btn(page, '<span class="promo-only">Quiero mi 50% de descuento</span><span class="after-promo">Quiero inscribirme</span>', "btn", "precio")}
<p class="fine promo-only">{fine}</p>
</div></div></section>'''

def testimonios(extra_first=None, intro="Capturas reales del chat de la Comunidad de Tejedoras de la Escuela de Macramé."):
    items = [("velisa-maestra", "Velisa comparte su tapiz terminado"),
             ("paris-vendido", "Terminó su Bolso París y lo vendió"),
             ("velisa-calculadora", "Sobre la calculadora de costos"),
             ("angelica-termine", "Angélica terminó su bolso"),
             ("adaly-straps", "Adaly ya vende sus straps"),
             ("mocaronico-enamoro", "“Cada día me enamoro más de los proyectos”"),
             ("alexa-proceso", "“Me encanta el proceso”"),
             ("luz-tapiz", "Luz Ángela avanza en su tapiz")]
    figs = "\n".join(
        f'<figure><img src="assets/testimonios/chat-{f}.webp" alt="Captura del chat de la comunidad: {c}" loading="lazy"><figcaption>{c}</figcaption></figure>'
        for f, c in items)
    return f'''<section id="testimonios"><div class="wrap">
<div class="section-head"><div class="eyebrow">Testimonios</div><h2>Lo que dicen las alumnas</h2><p>{intro}</p></div>
<div class="testi chat">{figs}</div></div></section>'''

def faq(items):
    qs = "\n".join(f"<details><summary>{q}</summary><p>{a}</p></details>" for q, a in items)
    return f'''<section id="preguntas" class="workshops"><div class="wrap">
<div class="section-head"><div class="eyebrow">Preguntas frecuentes</div><h2>Antes de empezar</h2></div><div class="faq">{qs}</div>
</div></section>'''

DISCLOSURE = ("El Arte que Conecta es afiliada de la Escuela de Macramé: te asesoramos y te acompañamos a elegir tu curso, "
              "y recibimos una comisión por las compras hechas con estos enlaces, sin costo adicional para ti. "
              "El pago y el acceso a las clases se gestionan en Hotmart.")

FAQ_COMUN = [
    ("¿Cómo accedo a las clases?", "Compras en Hotmart y recibes en tu correo el acceso a la plataforma. Las clases son grabadas: las ves a tu ritmo, sin horarios, desde el celular o el computador."),
    ("¿El pago es mensual?", "No. Es un pago único."),
    ("¿Incluye un kit de materiales?", "No incluye kits ni materiales físicos. Sí encuentras la información de materiales y proveedores para conseguirlos."),
    ("¿Hay mentorías uno a uno?", "No. El acompañamiento es a través de las clases paso a paso y la comunidad de tejedoras."),
    ("¿Quién es El Arte que Conecta?", DISCLOSURE),
]

def page(slug, title, desc, nav_active, hero, body, cta_label):
    nav = [("Escuela", f"{SITE}/escuela", "escuela"), ("Bolsos", f"{SITE}/bolsos", "bolsos"),
           ("Navidad Mágica", f"{SITE}/navidad", "navidad"), ("Talleres", f"{SITE}#talleres", ""), ("Preguntas", "#preguntas", "")]
    navhtml = "".join(f'<a href="{h}"{" class=\"active\" aria-current=\"page\"" if k == nav_active else ""}>{l}</a>' for l, h, k in nav)
    ld = {"@context": "https://schema.org", "@type": "Course", "name": title.split("|")[0].strip(), "description": desc,
          "inLanguage": "es", "provider": {"@type": "Organization", "name": "Escuela de Macramé"}}
    return f'''<!DOCTYPE html>
<html lang="es"><head><meta charset="utf-8"/><meta content="width=device-width,initial-scale=1" name="viewport"/>
<title>{title}</title>
<meta name="description" content="{desc}"/>
<link rel="canonical" href="{SITE}/{slug}"/>
<meta property="og:title" content="{title}"/><meta property="og:description" content="{desc}"/>
<meta property="og:url" content="{SITE}/{slug}"/><meta property="og:image" content="{SITE}/assets/fotos/{hero['img']}.webp"/>
<link rel="preconnect" href="https://fonts.googleapis.com"/><link rel="preconnect" href="https://fonts.gstatic.com" crossorigin/>
<link href="https://fonts.googleapis.com/css2?family=DM+Sans:wght@400;500;700&family=Fraunces:ital,opsz,wght@0,9..144,400;0,9..144,500;0,9..144,600;1,9..144,400;1,9..144,500&display=swap" rel="stylesheet"/>
<script type="application/ld+json">{json.dumps(ld, ensure_ascii=False)}</script>
<style>{CSS}</style></head><body>
<div class="ribbon promo-only"><span class="heart">♥</span> Noviembre Black: 50% de descuento hasta el 30 de noviembre <span class="heart">♥</span></div>
<header><div class="wrap nav"><a class="brand" href="{SITE}" style="text-decoration:none">♥ El Arte que Conecta</a><nav>{navhtml}</nav></div></header>
<main id="inicio"><div class="wrap hero"><div>
<div class="chip promo-only">♥ Noviembre Black · 50% de descuento</div>
<div class="eyebrow">{hero['eyebrow']}</div><h1>{hero['h1']}</h1><p>{hero['p']}</p>
{btn(slug, '<span class="promo-only">Quiero mi 50% de descuento</span><span class="after-promo">Quiero inscribirme</span>', "btn", "hero")}<a class="btn secondary" href="{hero['sec_href']}">{hero['sec']}</a>
<p class="lead-note">{hero['note']}</p>
</div><div class="photo-card"><img alt="{hero['alt']}" src="assets/fotos/{hero['img']}.webp" width="1000" height="750"/></div></div>
{body}
<section style="padding-top:20px"><div class="wrap"><p class="disclosure" style="text-align:center">{DISCLOSURE}</p></div></section>
</main>
{btn(slug, cta_label, "btn sticky-cta", "sticky")}
<footer><div class="wrap">El Arte que Conecta · Tejemos creatividad, calma y comunidad ♥<br><small>Afiliada de la Escuela de Macramé · Pagos procesados por Hotmart</small></div></footer>
<script>
(function(){{
  var end=new Date("{END}").getTime();
  function tick(){{
    var d=end-Date.now();
    if(d<=0){{document.documentElement.classList.add("is-ended");return;}}
    var s=Math.floor(d/1000),v={{d:Math.floor(s/86400),h:Math.floor(s%86400/3600),m:Math.floor(s%3600/60),s:s%60}};
    document.querySelectorAll("[data-cd]").forEach(function(el){{var n=v[el.dataset.cd];el.textContent=(n<10?"0":"")+n;}});
    setTimeout(tick,1000);
  }}
  tick();
  document.querySelectorAll("[data-copy]").forEach(function(b){{
    b.addEventListener("click",function(){{
      var t=b.dataset.copy,done=function(){{b.textContent="¡Copiado!";setTimeout(function(){{b.textContent="Copiar cupón";}},2000);}};
      if(navigator.clipboard){{navigator.clipboard.writeText(t).then(done,function(){{fallback();}});}}else fallback();
      function fallback(){{var r=document.createRange();r.selectNodeContents(document.getElementById("cupon"));var sel=getSelection();sel.removeAllRanges();sel.addRange(r);try{{document.execCommand("copy");done();}}catch(e){{}}}}
    }});
  }});
}})();
</script>
</body></html>
'''

# ---------------- ESCUELA ----------------
TEMARIO = [
    ("Bienvenida", ["Bienvenida a la Escuela de Macramé"]),
    ("Tipos de hilos y cuerdas", ["Clase: ¿Con qué se teje? Descubre los hilos", "Ebook de materiales de macramé", "Ebook con lista de proveedores"]),
    ("Nudos básicos", ["Nudo alondra y sus variaciones (invertido, horizontal y vertical)", "Nudo festón: básico, en diagonal y con guía externa",
                       "Nudo plano o cuadrado, y plano deslizado", "Nudo espiral o tornillo", "Nudos decorativos 1 y 2"]),
    ("Cálculo de hilos", ["Clase: domina el cálculo de hilos en macramé", "Planilla de medidas", "Bitácora de macramé"]),
    ("Costos y finanzas", ["Clase de costos para macramé", "Calculadora de costos para macramé"]),
    ("Clases prácticas", ["Cada proyecto incluye su clase en video y su ebook o ficha técnica descargable"]),
]
temario = "\n".join(f"<details{' open' if i==0 else ''}><summary>{t}</summary><ul>{''.join(f'<li>{x}</li>' for x in xs)}</ul></details>" for i,(t,xs) in enumerate(TEMARIO))

ESC_PROJ = [("maxibolso","Maxibolso"),("tula-verona","Tula Verona"),("maxitula","Maxitula"),("bolso-paris","Bolso París"),
            ("bolso-bahia","Bolso Bahía"),("bolso-santorini","Bolso Santorini"),("accesorio-corazon","Accesorio Corazón"),("accesorio-infinito","Accesorio Infinito"),
            ("cojin","Cojín"),("arbol-de-pared","Árbol de pared"),("corona-navidena","Corona navideña"),("arbolito-de-mesa","Arbolito de mesa")]
ESC_MORE = ["Espejo","Tapiz de chakras","Portamacetas","Tapiz de hojas","Embarrilado","Tapiz Prana","Pareo","Individual de mesa",
            "Hombreras","Strap","Angelito","Reno","Mini árbol colgante navideño"]

escuela_body = f'''{promo("escuela", "la Escuela de Macramé", False)}
<section id="incluye"><div class="wrap"><div class="section-head"><div class="eyebrow">Qué incluye</div><h2>Una guía paso a paso para crear con tus manos</h2>
<p>Para tu casa, para regalar o incluso para vender.</p></div>
<div class="feat">
<article><div class="ic">🎥</div><h3>Proyectos en video</h3><p>Cada proyecto explicado paso a paso en clases pregrabadas.</p></article>
<article><div class="ic">🧶</div><h3>Desde cero</h3><p>Niveles principiante, intermedio y avanzado. No necesitas experiencia previa.</p></article>
<article><div class="ic">🌙</div><h3>A tu ritmo</h3><p>Sin horarios. Pago único y acceso de por vida para volver a las clases cuando quieras.</p></article>
<article><div class="ic">📘</div><h3>Ebooks y fichas técnicas</h3><p>Recursos descargables y materiales de apoyo para cada proyecto.</p></article>
<article><div class="ic">🧮</div><h3>Herramientas para vender</h3><p>Proveedores, guía de materiales, cálculo de hilos, calculadora de costos y bitácora.</p></article>
<article><div class="ic">🤝</div><h3>Comunidad privada</h3><p>Un espacio de tejedoras para compartir dudas, avances y celebrar juntas.</p></article>
</div></div></section>
<section id="temario" class="workshops"><div class="wrap duo">
<div class="duo-visual"><div class="photo-card"><img src="assets/fotos/v3_6.webp" alt="Maestra de la Escuela bajo el Tapiz Prana" loading="lazy"></div></div>
<div><div class="eyebrow">Temario</div><h2>Fundamentos para empezar con confianza</h2><p style="color:var(--text)">Aquí empiezan todas, incluso sin experiencia.</p>
<div class="syllabus">{temario}</div></div></div></section>
<section id="proyectos"><div class="wrap"><div class="section-head"><div class="eyebrow">Clases prácticas</div><h2>Proyectos que vas a tejer</h2>
<p>Bolsos, accesorios, decoración para el hogar y piezas navideñas, cada uno con su clase y su ebook o ficha técnica.</p></div>
<div class="projects">{"".join(project(s,n) for s,n in ESC_PROJ)}</div>
<div class="more" aria-label="Más proyectos incluidos">{"".join(f"<span>{m}</span>" for m in ESC_MORE)}</div>
</div></section>
<section class="workshops"><div class="wrap duo rev">
<div class="duo-visual"><div class="photo-card"><img src="assets/fotos/v3_4.webp" alt="Maxibolsos tejidos en macramé" loading="lazy"></div></div>
<div><div class="eyebrow">Para quién es</div><h2>Para ti, si quieres crear con tus manos</h2>
<ul class="checks"><li>Nunca has hecho un nudo y quieres empezar desde cero, con guía.</li>
<li>Ya tejes y buscas proyectos reales y bien explicados.</li>
<li>Quieres un espacio de calma, creatividad y reconexión.</li>
<li>Sueñas con vender tus piezas y necesitas calcular costos y precios.</li></ul>
<div style="margin-top:18px">{btn("escuela","Quiero entrar a la Escuela","btn","para-quien")}</div></div></div></section>
{testimonios()}
{faq([("¿Necesito experiencia previa?", "No. La Escuela empieza por los fundamentos: tipos de hilos y cuerdas, proveedores y nudos básicos, y avanza por niveles principiante, intermedio y avanzado."),
      ("¿Por cuánto tiempo tengo acceso?", "Es un pago único con acceso de por vida: aprendes a tu ritmo y vuelves a las clases cuando lo necesites.")]
     + FAQ_COMUN[:1] + FAQ_COMUN[2:4] +
     [("¿El Árbol de Navidad 3D está en la Escuela?", "No. El Árbol de Navidad 3D es un taller individual y también hace parte del curso Navidad Mágica. Si ya eres alumna de la Escuela, puedes adquirirlo con 50% de descuento."),
      FAQ_COMUN[4]])}
<section id="comunidad"><div class="wrap"><div style="max-width:760px;margin:0 auto">
<div class="cta"><div class="eyebrow">Hilos que nos unen</div><h2>Tu creatividad merece un espacio.</h2><p>Aprende, comparte y crea junto a una comunidad de tejedoras.</p>
{btn("escuela","Quiero mi 50% de descuento","btn","comunidad")}</div></div></div></section>
<section style="padding-top:0"><div class="wrap"><div class="section-head"><div class="eyebrow">¿Prefieres empezar por un curso?</div></div>
<div class="tiles two"><article class="tile"><img src="assets/fotos/v3_2.webp" alt="Curso de Bolsos" loading="lazy"><h3>Curso de Bolsos</h3><p>Aprende a crear bolsos artesanales con técnicas y detalles únicos.</p><a href="{SITE}/bolsos">Descubre el curso →</a></article><article class="tile"><img src="assets/fotos/v3_3.webp" alt="Navidad Mágica" loading="lazy"><h3>Navidad Mágica</h3><p>Teje decoraciones cálidas y especiales para la temporada.</p><a href="{SITE}/navidad">Descubre el curso →</a></article></div></div></section>'''

# ---------------- BOLSOS ----------------
BOL = [("maxibolso","Maxibolso"),("tula-verona","Tula Verona"),("maxitula","Maxitula"),("bolso-paris","Bolso París"),
       ("bolso-bahia","Bolso Bahía"),("bolso-santorini","Bolso Santorini"),("accesorio-corazon","Accesorio Corazón"),("accesorio-infinito","Accesorio Infinito")]
bolsos_body = f'''{promo("bolsos", "el Curso de Bolsos", True)}
<section id="proyectos"><div class="wrap"><div class="section-head"><div class="eyebrow">8 proyectos</div><h2>Bolsos y accesorios que vas a tejer</h2>
<p>Cada proyecto con su clase en video y su ebook. La Tula Verona viene en tres partes.</p></div>
<div class="projects">{"".join(project(s,n) for s,n in BOL)}</div>
<div style="text-align:center;margin-top:28px">{btn("bolsos","Quiero tejer estos bolsos","btn","proyectos")}</div></div></section>
<section class="workshops"><div class="wrap"><div class="section-head"><div class="eyebrow">Qué incluye</div><h2>Todo lo que necesitas para tu primer bolso</h2></div>
<div class="feat">
<article><div class="ic">🎥</div><h3>Clases paso a paso</h3><p>Cada bolso y accesorio explicado en video, para pausar y repetir cuando lo necesites.</p></article>
<article><div class="ic">📘</div><h3>Ebook de cada proyecto</h3><p>El material descargable de cada modelo para tejer con la guía a mano.</p></article>
<article><div class="ic">💼</div><h3>Para usar o vender</h3><p>Modelos con detalles únicos, pensados para lucirlos o convertirlos en ingresos.</p></article>
</div></div></section>
<section><div class="wrap duo rev">
<div class="duo-visual"><div class="photo-card"><img src="assets/fotos/v3_4.webp" alt="Maxibolsos tejidos en macramé" loading="lazy"></div></div>
<div><div class="eyebrow">Para quién es</div><h2>Bolsos que parecen de tienda, hechos por ti</h2>
<ul class="checks"><li>Te encantan los bolsos artesanales y quieres tejer los tuyos.</li>
<li>Quieres una colección de modelos para tu emprendimiento.</li>
<li>Buscas proyectos con acabados profesionales: asas, flecos y detalles.</li></ul>
<div style="margin-top:18px">{btn("bolsos","Quiero mi 50% de descuento","btn","para-quien")}</div></div></div></section>
{testimonios()}
{faq([("¿Necesito saber macramé?", "Te ayuda conocer los nudos básicos. Si empiezas desde cero, la Escuela de Macramé incluye los fundamentos y también estos 8 proyectos."),
      ("¿Cómo uso el cupón BLACKNOV50?", "Copia el cupón en el bloque de precio y escríbelo en el campo de cupón al pagar en Hotmart. Es válido del 1 al 30 de noviembre.")]
     + FAQ_COMUN[:1] + [FAQ_COMUN[1], FAQ_COMUN[2], FAQ_COMUN[4]])}
<section style="padding-top:20px"><div class="wrap"><div class="section-head"><div class="eyebrow">¿Quieres aprender todo?</div><h2>La Escuela incluye estos 8 proyectos y muchos más</h2></div>
<div class="tiles two"><article class="tile"><img src="assets/fotos/v3_1.webp" alt="Programa principal" loading="lazy"><h3>Programa principal</h3><p>Todos los proyectos que quieres aprender, en un solo lugar.</p><a href="{SITE}/escuela">Explora el programa →</a></article><article class="tile"><img src="assets/fotos/v3_3.webp" alt="Navidad Mágica" loading="lazy"><h3>Navidad Mágica</h3><p>Teje decoraciones cálidas y especiales para la temporada.</p><a href="{SITE}/navidad">Descubre el curso →</a></article></div></div></section>'''

# ---------------- NAVIDAD ----------------
NAV = [("arbolito-de-mesa","Arbolito de mesa"),("adorno-navideno","Adorno navideño"),("corona-navidena","Corona navideña"),("arbol-de-pared","Árbol de pared"),
       ("arbol-3d","Árbol 3D"),("angelito","Angelito"),("cojin","Cojín")]
VIDEO = "https://drive.google.com/file/d/15ejaKDYYR-dFUSaNKNe0Zhk18zYfhytU/preview"
navidad_body = f'''{promo("navidad", "Navidad Mágica", False)}
<section id="proyectos"><div class="wrap"><div class="section-head"><div class="eyebrow">7 proyectos</div><h2>Lo que vas a tejer esta temporada</h2>
<p>Piezas navideñas en macramé explicadas paso a paso, para decorar tu casa, regalar o vender en diciembre.</p></div>
<div class="projects center">{"".join(project(s,n) for s,n in NAV)}</div></div></section>
<section id="video" class="workshops"><div class="wrap"><div class="section-head"><div class="eyebrow">Incluido en Navidad Mágica</div><h2>Mira el Árbol de Navidad 3D</h2>
<p>La pieza protagonista de la temporada, tejida en macramé.</p></div>
<div class="video"><iframe src="{VIDEO}" title="Video del Árbol de Navidad 3D en macramé" allow="autoplay; encrypted-media; fullscreen" allowfullscreen loading="lazy"></iframe></div>
<p class="video-fallback">¿No se reproduce? <a href="{VIDEO.replace('/preview','/view')}" target="_blank" rel="noopener">Ábrelo aquí</a>.</p>
<div style="text-align:center;margin-top:20px">{btn("navidad","Quiero tejer mi Árbol 3D","btn","video")}</div></div></section>
<section><div class="wrap"><div class="section-head"><div class="eyebrow">Una ruta, tres formas de vivir la Navidad</div><h2>Decora, regala o vende</h2></div>
<div class="feat">
<article><div class="ic">🏡</div><h3>Decora tu casa</h3><p>Árboles, corona, angelitos y cojín para llenar tus espacios de calidez tejida.</p></article>
<article><div class="ic">🎁</div><h3>Regala algo hecho a mano</h3><p>Detalles únicos para tu familia y amigas, con el valor de tu tiempo.</p></article>
<article><div class="ic">💸</div><h3>Vende en temporada</h3><p>Diciembre es la temporada de los regalos hechos a mano: llega con tus piezas listas.</p></article>
</div><div style="text-align:center;margin-top:26px">{btn("navidad","Quiero mi 50% de descuento","btn","usos")}</div></div></section>
{testimonios()}
{faq([("¿Alcanzo a tejer antes de diciembre?", "Sí: las clases están grabadas y empiezas apenas compras, así que puedes avanzar a tu ritmo durante noviembre y llegar a diciembre con tus piezas listas."),
      ("¿Necesito experiencia previa?", "Te ayuda conocer los nudos básicos. Si empiezas desde cero, la Escuela de Macramé incluye los fundamentos."),
      ("¿El Árbol de Navidad 3D está incluido?", "Sí, el Árbol de Navidad 3D es uno de los 7 proyectos de Navidad Mágica.")]
     + FAQ_COMUN[:1] + [FAQ_COMUN[1], FAQ_COMUN[2], FAQ_COMUN[4]])}
<section style="padding-top:20px"><div class="wrap"><div class="section-head"><div class="eyebrow">Sigue tejiendo</div></div>
<div class="tiles two"><article class="tile"><img src="assets/fotos/v3_1.webp" alt="Programa principal" loading="lazy"><h3>Programa principal</h3><p>Todos los proyectos que quieres aprender, en un solo lugar.</p><a href="{SITE}/escuela">Explora el programa →</a></article><article class="tile"><img src="assets/fotos/v3_2.webp" alt="Curso de Bolsos" loading="lazy"><h3>Curso de Bolsos</h3><p>Aprende a crear bolsos artesanales con técnicas y detalles únicos.</p><a href="{SITE}/bolsos">Descubre el curso →</a></article></div></div></section>'''

# ---- V7: Escuela replica la estructura de la landing actual en Hotmart ----
import escuela_v7
CSS += escuela_v7.CSS_V7
escuela_body = escuela_v7.build_escuela(btn, promo, testimonios, faq, SITE, temario) + escuela_v7.SCRIPT_V7

PAGES = [
    ("escuela", "Escuela de Macramé: aprende y emprende · 50% en Noviembre Black | El Arte que Conecta",
     "Aprende macramé desde cero con la Escuela de Macramé: proyectos paso a paso en video, ebooks, comunidad y acceso de por vida. 50% de descuento hasta el 30 de noviembre.",
     {"eyebrow": "Escuela de Macramé · Aprende y emprende", "h1": "Aprende macramé paso a paso para<br/><em>decorar, vender o emprender.</em>",
      "p": "A tu ritmo, con acompañamiento y proyectos que sí se terminan. Clases grabadas, comunidad de tejedoras y acceso de por vida con un pago único.",
      "sec": "Ver qué vas a aprender", "sec_href": "#aprende", "note": "USD 48,50 este noviembre (antes USD 97) · Garantía de 7 días.",
      "img": "v3_1", "alt": "Janeth Flórez, instructora de la Escuela de Macramé, con sus piezas"}, escuela_body, "Quiero mi 50% · USD 48,50"),
    ("bolsos", "Curso de Bolsos en macramé · 50% en Noviembre Black | El Arte que Conecta",
     "8 proyectos de bolsos y accesorios en macramé: Maxibolso, Tula Verona, Maxitula, Bolso París, Bahía, Santorini, Accesorio Corazón e Infinito. 50% con el cupón BLACKNOV50.",
     {"eyebrow": "Escuela de Macramé · Curso de Bolsos", "h1": "Bolsos que enamoran.<br/><em>Tejidos por ti.</em>",
      "p": "Aprende a crear bolsos artesanales con técnicas y detalles únicos: 8 proyectos con su clase en video y su ebook, para lucirlos o venderlos.",
      "sec": "Ver los 8 proyectos", "sec_href": "#proyectos", "note": "USD 29 con el cupón BLACKNOV50 (antes USD 58).",
      "img": "v3_2", "alt": "Tres bolsos de macramé en maniquíes"}, bolsos_body, "Quiero mi 50% · USD 29"),
    ("navidad", "Navidad Mágica · Curso de Navidad en macramé · 50% | El Arte que Conecta",
     "Navidad Mágica: 7 proyectos navideños en macramé, incluido el Árbol de Navidad 3D. Decora, regala o vende esta temporada. 50% de descuento hasta el 30 de noviembre.",
     {"eyebrow": "Escuela de Macramé · Navidad Mágica", "h1": "Teje decoraciones cálidas<br/><em>para esta Navidad.</em>",
      "p": "7 proyectos navideños en macramé, incluido el Árbol de Navidad 3D, para decorar tu casa, regalar con sentido o vender en temporada.",
      "sec": "Ver los 7 proyectos", "sec_href": "#proyectos", "note": "USD 29 este noviembre (antes USD 58). Descuento ya aplicado.",
      "img": "v3_3", "alt": "Maestra con árboles navideños de macramé"}, navidad_body, "Quiero mi 50% · USD 29"),
]

for slug, title, desc, hero, body, cta in PAGES:
    (OUT / f"{slug}.html").write_text(page(slug, title, desc, slug, hero, body, cta))
    print("ok", slug)
