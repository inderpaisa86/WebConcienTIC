"""Landing /elartequeconecta/catalogo — catálogo de piezas terminadas hechas a mano (pedido por WhatsApp)."""
import pathlib, json, urllib.parse

ROOT = pathlib.Path(__file__).parent
OUT = ROOT.parent / "site"
CSS = (ROOT / "v3.css").read_text() + (ROOT / "v4.css").read_text()
SITE = "https://www.concientic.com/elartequeconecta"

# ====== CONFIGURA AQUÍ ======
WA = "57XXXXXXXXXX"          # número de WhatsApp con indicativo, sin + ni espacios
MONEDA = "COP"
# Precio por pieza: número (ej. 185000) o None para mostrar "Precio a consultar"
# =============================

CATS = [("bolsos", "Bolsos"), ("accesorios", "Accesorios"), ("hogar", "Hogar"), ("navidad", "Navidad")]

# (slug, nombre, categoría, imagen, encaje, descripción, detalles, precio)
P = [
 ("santorini", "Bolso Santorini", "bolsos", "catalogo/bol-santorini.webp", "cover;50% 55%",
  "Inspirado en el verano y los tonos naturales. Textura, movimiento y un acabado elegante para cualquier outfit.",
  ["Piola de brillos de 3 mm", "Colores: dorado, moca o crema", "Cargadera con mosquetones"], None),
 ("paris", "Bolso París", "bolsos", "catalogo/bol-paris.webp", "cover;45% 50%",
  "Asas de madera en media luna y franjas de color. El bolso que más preguntas recibe en la calle.",
  ["Cordón de algodón de 3 mm", "Asas de madera", "Combinación de colores a elección"], None),
 ("bahia", "Bolso Bahía", "bolsos", "catalogo/bol-bahia.webp", "cover;40% 50%",
  "Nudos planos que forman una textura firme, nudos mágicos en cordón de brillo y flecos boho. Incluye llavero tejido.",
  ["Algodón 3 mm verde pistacho + brillo dorado", "Asas redondas de madera", "Llavero decorativo incluido"], None),
 ("maxibolso", "Maxibolso", "bolsos", "catalogo/bol-maxibolso.webp", "cover;50% 50%",
  "Amplio, liviano y con flecos largos. Para el día a día, la playa o tus días más relajados.",
  ["Trapillo doble pluma, suave y elástico", "Tejido en nudo cuadrado", "Llavero decorativo"], None),
 ("maxitula", "Maxitula", "bolsos", "catalogo/bol-maxitula.webp", "cover;50% 60%",
  "Una tula grande y resistente que combina con todo y lleva todo lo que necesitas.",
  ["Tejido a mano en macramé", "Colores a elección"], None),
 ("verona", "Tula Verona", "bolsos", "catalogo/bol-tula-verona.webp", "cover;50% 50%",
  "Bolso tipo saquito ajustable con patrones geométricos y cargadera trenzada. Práctico, elegante y versátil.",
  ["Cordón de brillos de 2 mm", "Cierre ajustable", "Cargadera trenzada"], None),
 ("strap", "Strap para celular", "accesorios", "catalogo/strap.webp", "cover;50% 50%",
  "Tu celular siempre a mano, colgado al hombro o cruzado. Funcional, tendencia y personalizable.",
  ["Cordón de poliéster de 4 mm en dos colores", "2 mosquetones metálicos", "Largo a tu medida"], None),
 ("corazon", "Llavero Corazón", "accesorios", "catalogo/bol-corazon.webp", "cover;50% 50%",
  "Un detalle pequeño y lleno de cariño. Ideal para regalar o para acompañar tu bolso.",
  ["Varios colores disponibles", "Herraje metálico"], None),
 ("infinito", "Llavero Infinito", "accesorios", "catalogo/bol-infinito.webp", "cover;50% 45%",
  "Diseño de nudo infinito con detalles naturales. Perfecto para intercambios de regalos.",
  ["Varios colores disponibles", "Herraje metálico"], None),
 ("espejo", "Espejo bohemio", "hogar", "catalogo/espejo.webp", "cover;48% 50%",
  "Luz y textura para tu entrada o habitación. Una pieza tejida con precisión y simetría.",
  ["Espejo de 30 cm de diámetro", "Aro de madera de 32 cm", "Algodón retorcido crudo y cuentas de madera"], None),
 ("portamacetas", "Portamacetas", "hogar", "catalogo/portamacetas.webp", "cover;50% 30%",
  "Llena tu casa de vida colgando tus plantas en tonos tierra que conectan con la naturaleza.",
  ["Algodón retorcido de 4 mm", "Crudo, café y verde pistacho", "Aro de madera y cuentas"], None),
 ("prana", "Tapiz Prana", "hogar", "catalogo/v3_6.webp", "cover;50% 50%",
  "Pieza protagonista de gran formato: texturas, ritmo y acentos de color que transforman una pared.",
  ["Ancho: 150 cm", "Alto: 100 a 120 cm aprox.", "Algodón crudo 5 mm + 4 tonos de color"], None),
 ("hojas", "Tapiz de hojas", "hogar", "catalogo/tapiz-hojas.webp", "cover;50% 50%",
  "Hojas tejidas en tonos suaves para darle calidez a la sala o la habitación.",
  ["Tejido a mano en algodón", "Colores a elección"], None),
 ("chakras", "Mini tapiz de chakras", "hogar", "catalogo/tapiz-chakras.webp", "cover;50% 50%",
  "Un tapiz pequeño lleno de color y significado, ideal para rincones de calma.",
  ["Formato pequeño", "Siete colores de los chakras"], None),
 ("embarrilado", "Colgante embarrilado", "hogar", "catalogo/embarrilado.webp", "cover;50% 40%",
  "Formas alargadas, trenzados suaves y detalles naturales que transmiten calma y orden.",
  ["Hilaza y terlenka de colores", "Aro de madera", "Listo para colgar"], None),
 ("cojin", "Cojín tejido", "hogar", "catalogo/nav-cojin.webp", "cover;50% 50%",
  "Una pieza tejida con calma para vestir tu sofá con un detalle hecho a mano.",
  ["Algodón retorcido crudo de 4 mm", "Funda con cremallera", "Base en tela de algodón"], None),
 ("arbol3d", "Árbol de Navidad 3D", "navidad", "catalogo/nav-arbol-3d.webp", "cover;42% 50%",
  "El protagonista de tu sala esta Navidad: un árbol tejido con volumen y detalles a tu gusto.",
  ["Pieza de gran formato", "Colores y adornos a elección"], None),
 ("arbolpared", "Árbol de pared", "navidad", "catalogo/nav-arbol-pared.webp", "cover;50% 45%",
  "La Navidad sin ocupar espacio. Perfecto para apartamentos y ambientes cálidos.",
  ["Varilla de madera de 43 cm", "Estrellas doradas", "Algodón trenzado de 4 mm"], None),
 ("arbolito", "Arbolito de mesa", "navidad", "catalogo/nav-arbolito.webp", "cover;50% 50%",
  "Un árbol pequeño para la mesa, el escritorio o la repisa.", ["Formato de mesa", "Detalles dorados y rojos"], None),
 ("corona", "Corona navideña", "navidad", "catalogo/nav-corona.webp", "cover;50% 50%",
  "Bienvenida tejida para tu puerta esta temporada.", ["Tejida a mano", "Detalles navideños"], None),
 ("adorno", "Adornos navideños", "navidad", "catalogo/nav-adorno.webp", "cover;50% 50%",
  "Detalles para el árbol que se guardan y se vuelven a colgar cada diciembre.", ["Se venden por set", "Colores a elección"], None),
 ("angelito", "Angelito tejido", "navidad", "catalogo/nav-angelito.webp", "cover;50% 50%",
  "Delicado y lleno de significado: un regalo que cuenta una historia.", ["Tejido a mano", "Ideal para regalar"], None),
]

def money(v):
    return f"${v:,.0f}".replace(",", ".") + f" {MONEDA}"

def wa(text):
    return f"https://wa.me/{WA}?text=" + urllib.parse.quote(text)

def card(p):
    slug, name, cat, img, fit, desc, det, price = p
    pr = f'<span class="cprice">{money(price)}</span>' if price else '<span class="cprice ask">Precio a consultar</span>'
    msg = f"Hola, El Arte que Conecta 👋 Me interesa el {name} del catálogo. ¿Me cuentas precio, colores disponibles y tiempo de entrega?"
    dl = "".join(f"<li>{d}</li>" for d in det)
    return f'''<article class="pcard" data-cat="{cat}" id="{slug}">
<div class="pimg {fit.split(';')[0]}"><img src="assets/{img}" alt="{name} en macramé hecho a mano" style="object-position:{(fit.split(';')+['center'])[1]}"><span class="badge">Hecho a mano</span></div>
<div class="pbody"><div class="pcat">{dict(CATS)[cat]}</div><h3>{name}</h3><p>{desc}</p><ul>{dl}</ul>
<div class="pfoot">{pr}<a class="btn wa" href="{wa(msg)}" target="_blank" rel="noopener" data-pos="card-{slug}">Pedir por WhatsApp</a></div></div></article>'''

EXTRA_CSS = """
.cat-hero{padding:56px 0 30px;text-align:center}.cat-hero h1{max-width:820px;margin:12px auto}.cat-hero p{max-width:640px;margin:0 auto;color:#725b49;font-size:1.08rem}
.trust{display:flex;flex-wrap:wrap;justify-content:center;gap:10px;margin-top:22px}.trust span{background:#fffaf5;border:1px solid #ead4c0;border-radius:999px;padding:8px 16px;font-size:.9rem;color:var(--brown);font-weight:700}
.collage{display:grid;grid-template-columns:repeat(4,1fr);gap:14px;margin-top:34px}.collage img{width:100%;aspect-ratio:4/5;object-fit:cover;border-radius:20px;box-shadow:0 10px 30px #5c2d1118}
.collage img:nth-child(even){transform:translateY(22px)}
.filters{position:sticky;top:0;z-index:20;background:#fff8f0ee;backdrop-filter:blur(6px);padding:14px 0;border-bottom:1px solid #ead8c7}
.filters .wrap{display:flex;gap:10px;flex-wrap:wrap;justify-content:center}
.filters button{font:700 .92rem inherit;font-family:inherit;padding:10px 20px;border-radius:999px;border:1px solid #ead4c0;background:#fffaf5;color:var(--brown);cursor:pointer}
.filters button.on{background:var(--terra);border-color:var(--terra);color:#fff}
.grid{display:grid;grid-template-columns:repeat(3,minmax(0,1fr));gap:26px}
.pcard{background:#fff;border:1px solid #ead9ca;border-radius:24px;overflow:hidden;box-shadow:0 10px 28px #5c2d1110;display:flex;flex-direction:column;transition:transform .25s,box-shadow .25s}
.pcard:hover{transform:translateY(-4px);box-shadow:0 18px 38px #5c2d111c}
.pimg{position:relative;aspect-ratio:1/1;background:#f7e8dd;overflow:hidden}.pimg img{width:100%;height:100%;object-fit:cover;display:block}
.pimg.contain img{object-fit:contain;padding:14px}
.badge{position:absolute;left:14px;top:14px;background:#fffaf5;color:var(--brown);font-size:.75rem;font-weight:700;border-radius:999px;padding:5px 12px;letter-spacing:.04em}
.pbody{padding:18px 20px 20px;display:flex;flex-direction:column;flex:1}.pcat{font-size:.72rem;letter-spacing:.14em;text-transform:uppercase;color:var(--terra);font-weight:700}
.pbody h3{font-size:1.5rem;margin:6px 0 6px}.pbody p{margin:0;color:#7a6250;font-size:.95rem}
.pbody ul{list-style:none;padding:0;margin:12px 0 16px;display:grid;gap:4px}.pbody li{font-size:.88rem;color:#5e4a3b;padding-left:18px;position:relative}.pbody li::before{content:"";position:absolute;left:0;top:.6em;width:8px;height:8px;border-radius:50%;background:var(--sage)}
.pfoot{margin-top:auto;display:flex;align-items:center;justify-content:space-between;gap:10px;flex-wrap:wrap;border-top:1px dashed #ead4c0;padding-top:14px}
.cprice{font:600 1.25rem Georgia,serif;color:var(--brown)}.cprice.ask{font:700 .9rem inherit;color:var(--taupe)}
.btn.wa{margin:0;padding:11px 18px;font-size:.92rem}
.steps{display:grid;grid-template-columns:repeat(4,1fr);gap:18px;counter-reset:s}.steps article{background:#fffaf5;border:1px solid #ead4c0;border-radius:22px;padding:22px}
.steps article::before{counter-increment:s;content:counter(s);display:grid;place-items:center;width:44px;height:44px;border-radius:50%;background:var(--terra);color:#fff;font:600 1.3rem Georgia,serif;margin-bottom:12px}
.steps h3{font-size:1.2rem;margin:0 0 6px}.steps p{margin:0;color:#7a6250;font-size:.93rem}
.custom{display:grid;grid-template-columns:1fr 1fr;gap:36px;align-items:center}.custom img{width:100%;border-radius:26px;box-shadow:0 14px 36px #5c2d1120}
.learn{background:var(--brown);color:#fef8f1;border-radius:30px;padding:40px;display:grid;grid-template-columns:1.3fr .7fr;gap:30px;align-items:center}
.learn h2{color:#fef8f1}.learn h2 em{color:var(--rose)}.learn p{color:#f3e3d3}.learn img{width:100%;border-radius:22px}
.learn .btn{background:var(--rose);color:var(--brown)}
.sticky-wa{position:fixed;right:18px;bottom:18px;z-index:40;background:#25d366;color:#fff;border-radius:999px;padding:14px 20px;font-weight:700;text-decoration:none;box-shadow:0 10px 26px #0003;display:flex;gap:8px;align-items:center}
@media(max-width:900px){.grid{grid-template-columns:repeat(2,minmax(0,1fr))}.steps{grid-template-columns:repeat(2,1fr)}.custom,.learn{grid-template-columns:1fr}.collage{grid-template-columns:repeat(2,1fr)}.collage img:nth-child(n+3){display:none}}
@media(max-width:560px){.trust span{font-size:.78rem;padding:6px 11px}.grid{grid-template-columns:1fr}.steps{grid-template-columns:1fr}.learn{padding:26px}}
"""

FAQ = [
 ("¿Las piezas están listas para entrega?", "Cada pieza se teje a mano. Algunas están disponibles para entrega inmediata y otras se hacen por encargo. Al escribirnos te confirmamos disponibilidad y tiempo de entrega."),
 ("¿Puedo elegir los colores?", "Sí. La mayoría de piezas se pueden tejer en los colores que prefieras. Cuéntanos qué tienes en mente y te mostramos opciones de hilos."),
 ("¿Hacen envíos?", "Sí, coordinamos el envío contigo por WhatsApp. El costo depende de tu ciudad y se confirma antes del pago."),
 ("¿Cómo pago?", "Te compartimos los medios de pago disponibles por WhatsApp al confirmar tu pedido."),
 ("¿Cómo cuido mi pieza de macramé?", "Mantenla lejos de la humedad y del sol directo. Para limpiarla, sacude el polvo o usa un paño húmedo suave; los flecos se peinan con un peine de dientes anchos."),
 ("¿Puedo aprender a tejerlas yo?", "¡Claro! Casi todas estas piezas se enseñan paso a paso en la Escuela de Macramé y en los cursos de Bolsos y Navidad Mágica."),
]

def build():
    filt = '<button class="on" data-f="all">Todo</button>' + "".join(f'<button data-f="{k}">{v}</button>' for k, v in CATS)
    grid = "\n".join(card(p) for p in P)
    collage = "".join(f'<img src="assets/{i}" alt="" loading="eager">' for i in
                      ["catalogo/bol-santorini.webp", "catalogo/nav-corona.webp", "catalogo/bol-maxibolso.webp", "catalogo/nav-cojin.webp"])
    faq = "".join(f"<details><summary>{q}</summary><p>{a}</p></details>" for q, a in FAQ)
    general = wa("Hola, El Arte que Conecta 👋 Vi el catálogo y quiero hacer un pedido.")
    custom = wa("Hola, El Arte que Conecta 👋 Quiero una pieza personalizada en macramé. Te cuento la idea:")
    nav = [("Catálogo", "#piezas"), ("Cómo pedir", "#como-pedir"), ("Personalizados", "#personalizados"), ("Aprende", f"{SITE}/escuela"), ("Preguntas", "#preguntas")]
    navhtml = "".join(f'<a href="{h}">{l}</a>' for l, h in nav)
    ld = {"@context": "https://schema.org", "@type": "ItemList", "name": "Catálogo de piezas en macramé hechas a mano",
          "itemListElement": [{"@type": "ListItem", "position": i + 1, "item": {"@type": "Product", "name": p[1], "description": p[5],
                               "image": f"{SITE}/assets/{p[3]}", "brand": {"@type": "Brand", "name": "El Arte que Conecta"}}} for i, p in enumerate(P)]}
    title = "Catálogo de macramé hecho a mano | El Arte que Conecta"
    desc = "Bolsos, accesorios, decoración y piezas navideñas en macramé, tejidas a mano. Elige tu pieza y pídela por WhatsApp."
    html = f'''<!DOCTYPE html>
<html lang="es"><head><meta charset="utf-8"/><meta content="width=device-width,initial-scale=1" name="viewport"/>
<title>{title}</title><meta name="description" content="{desc}"/><link rel="canonical" href="{SITE}/catalogo"/>
<meta property="og:title" content="{title}"/><meta property="og:description" content="{desc}"/><meta property="og:url" content="{SITE}/catalogo"/>
<meta property="og:image" content="{SITE}/assets/catalogo/bol-santorini.webp"/>
<link rel="preconnect" href="https://fonts.googleapis.com"/><link rel="preconnect" href="https://fonts.gstatic.com" crossorigin/>
<link href="https://fonts.googleapis.com/css2?family=DM+Sans:wght@400;500;700&family=Fraunces:ital,opsz,wght@0,9..144,400;0,9..144,500;0,9..144,600;1,9..144,400;1,9..144,500&display=swap" rel="stylesheet"/>
<script type="application/ld+json">{json.dumps(ld, ensure_ascii=False)}</script>
<style>{CSS}{EXTRA_CSS}</style></head><body>
<header><div class="wrap nav"><a class="brand" href="{SITE}" style="text-decoration:none">♥ El Arte que Conecta</a><nav>{navhtml}</nav></div></header>
<main>
<section class="cat-hero"><div class="wrap">
<div class="eyebrow">Catálogo · Hecho a mano</div>
<h1>Piezas tejidas <em>nudo a nudo</em> para ti</h1>
<p>Bolsos, accesorios, decoración para tu hogar y detalles de Navidad en macramé. Cada pieza es única: elige la tuya y pídela por WhatsApp.</p>
<a class="btn" href="#piezas">Ver el catálogo</a><a class="btn secondary" href="#personalizados">Quiero algo personalizado</a>
<div class="trust"><span>🧶 Tejido 100% a mano</span><span>🎨 Colores a tu gusto</span><span>📦 Envíos coordinados</span><span>🎁 Ideal para regalar</span></div>
<div class="collage">{collage}</div>
</div></section>
<div class="filters" id="piezas"><div class="wrap">{filt}</div></div>
<section><div class="wrap"><div class="grid" id="grid">{grid}</div></div></section>
<section id="como-pedir" class="workshops"><div class="wrap">
<div class="section-head"><div class="eyebrow">Cómo pedir</div><h2>Tu pieza en 4 pasos</h2></div>
<div class="steps">
<article><h3>Elige</h3><p>Encuentra en el catálogo la pieza que te enamora.</p></article>
<article><h3>Escríbenos</h3><p>Toca “Pedir por WhatsApp” y te llega un mensaje listo con el nombre de la pieza.</p></article>
<article><h3>Personaliza</h3><p>Confirmamos colores, medidas, precio y tiempo de entrega.</p></article>
<article><h3>Recíbela</h3><p>Pagas, coordinamos el envío y tu pieza llega a casa.</p></article>
</div></div></section>
<section id="personalizados"><div class="wrap custom">
<div><div class="eyebrow">Por encargo</div><h2 style="font:500 clamp(2rem,4vw,3rem) Georgia,serif">¿Tienes una idea? <em>La tejemos.</em></h2>
<p style="color:#725b49">Un tapiz a la medida de tu pared, un bolso en los colores de tu marca, recordatorios para un evento o un regalo corporativo. Cuéntanos tu idea y la hacemos realidad, nudo a nudo.</p>
<a class="btn" href="{custom}" target="_blank" rel="noopener">Contar mi idea</a></div>
<img src="assets/fotos/v3_6.webp" alt="Tapiz Prana tejido a mano" ></div></section>
<section><div class="wrap"><div class="learn">
<div><div class="eyebrow" style="color:var(--rose)">¿Prefieres tejerlo tú?</div><h2>Aprende a hacer estas piezas <em>desde cero</em></h2>
<p>Casi todo lo que ves en este catálogo se enseña paso a paso en la Escuela de Macramé y en los cursos de Bolsos y Navidad Mágica.</p>
<a class="btn" href="{SITE}/escuela">Conocer la Escuela</a></div>
<img src="assets/fotos/instructora.webp" alt="Instructora de la Escuela de Macramé" ></div></div></section>
<section id="preguntas" class="workshops"><div class="wrap">
<div class="section-head"><div class="eyebrow">Preguntas frecuentes</div><h2>Antes de pedir</h2></div><div class="faq">{faq}</div></div></section>
</main>
<a class="sticky-wa" href="{general}" target="_blank" rel="noopener" aria-label="Escríbenos por WhatsApp"><svg width="22" height="22" viewBox="0 0 24 24" fill="#fff"><path d="M12 2a10 10 0 0 0-8.6 15.1L2 22l5-1.3A10 10 0 1 0 12 2Zm5.3 14.1c-.2.6-1.3 1.2-1.8 1.2-.5.1-1 .2-3.3-.7-2.8-1.1-4.5-3.9-4.7-4.1-.1-.2-1.1-1.5-1.1-2.9s.7-2 1-2.3c.2-.3.5-.3.7-.3h.5c.2 0 .4 0 .6.5l.8 2c.1.2.1.4 0 .5l-.4.6-.4.4c-.1.2-.3.3-.1.6.2.3.8 1.3 1.7 2.1 1.2 1 2.1 1.4 2.4 1.5.3.1.5.1.6-.1l.9-1c.2-.3.4-.2.7-.1l1.9.9c.3.1.5.2.5.3.1.2.1.7-.1 1.3Z"/></svg>Hacer un pedido</a>
<footer><div class="wrap">El Arte que Conecta · Tejemos creatividad, calma y comunidad ♥<br><small>Piezas hechas a mano · Pedidos por WhatsApp</small></div></footer>
<script>
document.querySelectorAll(".filters button").forEach(function(b){{b.addEventListener("click",function(){{
document.querySelectorAll(".filters button").forEach(function(x){{x.classList.toggle("on",x===b)}});
var f=b.dataset.f;document.querySelectorAll(".pcard").forEach(function(c){{c.style.display=(f==="all"||c.dataset.cat===f)?"":"none"}});
document.getElementById("piezas").scrollIntoView({{behavior:"smooth"}});}})}});
</script>
</body></html>'''
    (OUT / "catalogo.html").write_text(html)
    print("ok", len(P), "piezas")

if __name__ == "__main__":
    build()
