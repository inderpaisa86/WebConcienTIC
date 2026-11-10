"""Catálogo V2 · /elartequeconecta/catalogo — piezas terminadas hechas a mano, pedido por WhatsApp.
Rediseño: video primero, fotos protagonistas, tarjetas limpias + vista rápida."""
import pathlib, json, urllib.parse, html as H
from catalogo import P, CATS, WA, MONEDA, SITE, FAQ

ROOT = pathlib.Path(__file__).parent
OUT = ROOT.parent / "site"
CSS = (ROOT / "v3.css").read_text() + (ROOT / "v4.css").read_text()

DESTACADAS = ["santorini", "paris", "prana", "arbol3d"]
CAT_IMG = {"bolsos": "catalogo/bol-santorini.webp", "accesorios": "catalogo/strap.webp",
           "hogar": "catalogo/espejo.webp", "navidad": "catalogo/nav-arbol-3d.webp"}
CAT_LINE = {"bolsos": "Para usar todos los días", "accesorios": "Detalles que acompañan",
            "hogar": "Textura para tus espacios", "navidad": "Tu diciembre, tejido"}

def money(v): return f"${v:,.0f}".replace(",", ".") + f" {MONEDA}"
def wa(t): return f"https://wa.me/{WA}?text=" + urllib.parse.quote(t)
def msg(name): return f"Hola, El Arte que Conecta 👋 Me interesa el {name} del catálogo. ¿Me cuentas precio, colores disponibles y tiempo de entrega?"
WA_SVG = '<svg width="18" height="18" viewBox="0 0 24 24" fill="currentColor"><path d="M12 2a10 10 0 0 0-8.6 15.1L2 22l5-1.3A10 10 0 1 0 12 2Zm5.3 14.1c-.2.6-1.3 1.2-1.8 1.2-.5.1-1 .2-3.3-.7-2.8-1.1-4.5-3.9-4.7-4.1-.1-.2-1.1-1.5-1.1-2.9s.7-2 1-2.3c.2-.3.5-.3.7-.3h.5c.2 0 .4 0 .6.5l.8 2c.1.2.1.4 0 .5l-.4.6-.4.4c-.1.2-.3.3-.1.6.2.3.8 1.3 1.7 2.1 1.2 1 2.1 1.4 2.4 1.5.3.1.5.1.6-.1l.9-1c.2-.3.4-.2.7-.1l1.9.9c.3.1.5.2.5.3.1.2.1.7-.1 1.3Z"/></svg>'

def card(p, big=False):
    slug, name, cat, img, fit, desc, det, price = p
    pos = (fit.split(";") + ["center"])[1]
    pr = money(price) if price else "Precio a consultar"
    short = desc.split(". ")[0].rstrip(".") + "."
    return f'''<article class="pc{' big' if big else ''}" data-cat="{cat}" data-id="{slug}">
<button class="pc-img" data-open="{slug}" aria-label="Ver detalles de {name}"><img src="assets/{img}" alt="{name} en macramé hecho a mano" style="object-position:{pos}" loading="lazy"><span class="pc-view">Ver detalles</span></button>
<div class="pc-body"><span class="pc-cat">{dict(CATS)[cat]}</span><h3>{name}</h3><p>{short}</p>
<div class="pc-foot"><span class="pc-price{'' if price else ' ask'}">{pr}</span><a class="pc-wa" href="{wa(msg(name))}" target="_blank" rel="noopener" aria-label="Pedir {name} por WhatsApp">{WA_SVG}<span>Pedir</span></a></div></div></article>'''

EXTRA = """
:root{--hand:'Caveat',cursive}
.topbar{background:var(--title);color:#fef8f1;text-align:center;font-size:.85rem;padding:9px 12px;letter-spacing:.02em}.topbar b{color:var(--rose)}
header{position:sticky;top:0;z-index:30}
.c-hero{padding:46px 0 30px}.c-hero .wrap{display:grid;grid-template-columns:1.05fr .95fr;gap:48px;align-items:center}
.c-hero h1{font-size:clamp(2.6rem,5.4vw,4.6rem);line-height:1.02;margin:14px 0 18px}
.c-hero p.lead{font-size:1.12rem;color:var(--text);max-width:520px;margin:0}
.c-hero .hand{font-family:var(--hand);font-size:1.7rem;color:var(--terra);transform:rotate(-3deg);display:inline-block;margin-top:18px}
.trust{display:flex;flex-wrap:wrap;gap:8px;margin-top:22px}.trust span{background:var(--card);border:1px solid var(--line);border-radius:999px;padding:7px 14px;font-size:.86rem;color:var(--title);font-weight:700}
.hero-media{position:relative;justify-self:center;width:min(100%,460px)}
.hero-media video{width:100%;aspect-ratio:4/5;object-fit:cover;border-radius:32px;display:block;box-shadow:0 26px 60px rgba(92,51,22,.22);background:var(--blush)}
.hero-media .tagv{position:absolute;left:-18px;bottom:34px;background:var(--card);border:1px solid var(--line);border-radius:18px;padding:12px 16px;box-shadow:var(--shadow);font-size:.9rem;color:var(--title);font-weight:700;display:flex;gap:10px;align-items:center}
.hero-media .tagv i{width:10px;height:10px;border-radius:50%;background:var(--terra);box-shadow:0 0 0 5px rgba(185,101,63,.18)}
.hero-media .stamp{position:absolute;right:-16px;top:26px;width:112px;height:112px;border-radius:50%;background:var(--rose);color:var(--title);display:grid;place-items:center;text-align:center;font-family:var(--hand);font-size:1.35rem;line-height:1.05;transform:rotate(10deg);box-shadow:var(--shadow)}
.cats{display:grid;grid-template-columns:repeat(4,1fr);gap:16px}
.cat{position:relative;border-radius:24px;overflow:hidden;aspect-ratio:3/4;border:0;padding:0;cursor:pointer;background:var(--blush);text-align:left}
.cat img{width:100%;height:100%;object-fit:cover;transition:transform .5s}.cat:hover img{transform:scale(1.05)}
.cat::after{content:"";position:absolute;inset:45% 0 0;background:linear-gradient(transparent,rgba(56,41,30,.78))}
.cat span{position:absolute;left:18px;right:18px;bottom:16px;z-index:2;color:#fef8f1}.cat b{display:block;font-family:var(--serif);font-size:1.6rem;font-weight:500;line-height:1.1}.cat small{font-size:.85rem;opacity:.9}
.cat em{position:absolute;right:14px;top:14px;z-index:2;background:var(--card);color:var(--title);font-style:normal;font-weight:700;font-size:.8rem;border-radius:999px;padding:4px 10px}
.feat-row{display:grid;grid-template-columns:repeat(4,1fr);gap:20px}
.filters{position:sticky;top:64px;z-index:20;background:rgba(254,248,241,.94);backdrop-filter:blur(8px);padding:12px 0;border-bottom:1px solid var(--line);margin-bottom:28px}
.filters .wrap{display:flex;gap:8px;flex-wrap:wrap;align-items:center}
.filters button{font:700 .9rem var(--sans);padding:9px 18px;border-radius:999px;border:1px solid var(--line);background:var(--card);color:var(--title);cursor:pointer}
.filters button.on{background:var(--title);border-color:var(--title);color:#fef8f1}
.filters .count{margin-left:auto;color:var(--taupe);font-size:.88rem}
.grid{display:grid;grid-template-columns:repeat(4,minmax(0,1fr));gap:26px 20px}
.pc{display:flex;flex-direction:column}
.pc-img{position:relative;display:block;width:100%;aspect-ratio:4/5;border-radius:22px;overflow:hidden;border:0;padding:0;background:var(--blush);cursor:pointer}
.pc-img img{width:100%;height:100%;object-fit:cover;display:block;transition:transform .5s}.pc:hover .pc-img img{transform:scale(1.05)}
.pc-view{position:absolute;left:50%;bottom:14px;transform:translate(-50%,8px);opacity:0;background:var(--card);color:var(--title);font-weight:700;font-size:.85rem;border-radius:999px;padding:8px 16px;transition:.25s;white-space:nowrap}
.pc:hover .pc-view{opacity:1;transform:translate(-50%,0)}
.pc-body{padding:12px 4px 0;display:flex;flex-direction:column;flex:1}
.pc-cat{font-size:.7rem;letter-spacing:.14em;text-transform:uppercase;color:var(--terra);font-weight:700}
.pc-body h3{font-size:1.3rem;margin:4px 0 4px}.pc-body p{margin:0;color:var(--text);font-size:.9rem;line-height:1.45}
.pc-foot{margin-top:auto;padding-top:12px;display:flex;align-items:center;justify-content:space-between;gap:8px}
.pc-price{font-family:var(--serif);font-size:1.15rem;color:var(--title)}.pc-price.ask{font-family:var(--sans);font-size:.85rem;font-weight:700;color:var(--taupe)}
.pc-wa{display:inline-flex;align-items:center;gap:6px;background:var(--terra);color:#fff;text-decoration:none;font-weight:700;font-size:.88rem;border-radius:999px;padding:8px 14px}
.pc.big .pc-img{aspect-ratio:4/5}
.process{background:var(--title);color:#fef8f1;border-radius:36px;padding:48px}
.process h2{color:#fef8f1!important}.process h2 em{color:var(--rose)!important}.process .eyebrow{color:var(--rose)}.process p{color:#f3e3d3}
.process-grid{display:grid;grid-template-columns:repeat(3,1fr);gap:18px;margin-top:26px}
.process figure{margin:0}.process video{width:100%;aspect-ratio:4/5;object-fit:cover;border-radius:22px;display:block;background:#4a2a14}
.process figcaption{margin-top:10px;font-family:var(--hand);font-size:1.45rem;color:var(--rose)}
.custom{display:grid;grid-template-columns:1fr 1fr;gap:40px;align-items:center}.custom img{width:100%;border-radius:28px;box-shadow:0 18px 40px rgba(92,51,22,.16)}
.custom ul{list-style:none;padding:0;margin:18px 0;display:grid;gap:8px}.custom li{padding-left:28px;position:relative;color:var(--text)}.custom li::before{content:"";position:absolute;left:0;top:.45em;width:14px;height:14px;border-radius:50%;background:var(--sage)}
.steps{display:grid;grid-template-columns:repeat(4,1fr);gap:16px;counter-reset:s}
.steps article{background:var(--card);border:1px solid var(--line);border-radius:22px;padding:22px}
.steps article::before{counter-increment:s;content:counter(s);display:grid;place-items:center;width:42px;height:42px;border-radius:50%;background:var(--rose);color:var(--title);font:600 1.2rem var(--serif);margin-bottom:12px}
.steps h3{font-size:1.15rem;margin:0 0 6px}.steps p{margin:0;color:var(--text);font-size:.92rem}
.learn{display:grid;grid-template-columns:.8fr 1.2fr;gap:36px;align-items:center;background:var(--blush);border-radius:32px;padding:36px}
.learn img{width:100%;border-radius:24px}
.faq details{background:var(--card);border:1px solid var(--line);border-radius:18px;padding:16px 20px;margin-bottom:10px}.faq summary{cursor:pointer;font-weight:700;color:var(--title)}.faq p{color:var(--text);margin:10px 0 0}
.modal{position:fixed;inset:0;background:rgba(56,41,30,.6);display:none;align-items:center;justify-content:center;z-index:60;padding:18px}
.modal.on{display:flex}
.mbox{background:var(--bg);border-radius:28px;max-width:920px;width:100%;max-height:92vh;overflow:auto;display:grid;grid-template-columns:1fr 1fr;position:relative}
.mbox img{width:100%;height:100%;min-height:360px;object-fit:cover;display:block}
.minfo{padding:30px 30px 26px}.minfo h3{font-size:2rem;margin:6px 0 10px}.minfo p{color:var(--text)}
.minfo ul{list-style:none;padding:0;margin:16px 0;display:grid;gap:6px}.minfo li{padding-left:22px;position:relative;color:var(--ink);font-size:.95rem}.minfo li::before{content:"";position:absolute;left:0;top:.5em;width:9px;height:9px;border-radius:50%;background:var(--sage)}
.minfo .note{font-family:var(--hand);font-size:1.4rem;color:var(--terra)}
.mclose{position:absolute;right:14px;top:12px;width:40px;height:40px;border-radius:50%;border:0;background:var(--card);font-size:1.4rem;cursor:pointer;color:var(--title);z-index:2}
.sticky-wa{position:fixed;right:18px;bottom:18px;z-index:40;background:var(--terra);color:#fff;border-radius:999px;padding:14px 20px;font-weight:700;text-decoration:none;box-shadow:0 10px 26px rgba(92,51,22,.3);display:flex;gap:8px;align-items:center}
@media(max-width:980px){.grid,.feat-row{grid-template-columns:repeat(3,minmax(0,1fr))}}
@media(max-width:860px){.c-hero .wrap,.custom,.learn,.mbox{grid-template-columns:1fr}.cats{grid-template-columns:repeat(2,1fr)}.steps{grid-template-columns:repeat(2,1fr)}.process-grid{grid-template-columns:repeat(3,72%);overflow-x:auto;scroll-snap-type:x mandatory}.process-grid figure{scroll-snap-align:start}.process{padding:30px 22px}.hero-media .tagv{left:8px}.hero-media .stamp{right:6px}.filters{top:0}}
@media(max-width:600px){header nav{display:none}.topbar{font-size:.75rem}.grid,.feat-row{grid-template-columns:repeat(2,minmax(0,1fr));gap:20px 12px}.pc-body h3{font-size:1.08rem}.pc-body p{display:none}.pc-wa span{display:none}.pc-wa{padding:9px}.steps{grid-template-columns:1fr}.filters .count{display:none}}
"""

def build():
    byid = {p[0]: p for p in P}
    counts = {k: sum(1 for p in P if p[2] == k) for k, _ in CATS}
    cats = "".join(f'<button class="cat" data-go="{k}"><img src="assets/{CAT_IMG[k]}" alt="{v}"><em>{counts[k]} piezas</em><span><b>{v}</b><small>{CAT_LINE[k]}</small></span></button>' for k, v in CATS)
    feat = "".join(card(byid[s], True) for s in DESTACADAS)
    grid = "\n".join(card(p) for p in P)
    filt = f'<button class="on" data-f="all">Todo ({len(P)})</button>' + "".join(f'<button data-f="{k}">{v}</button>' for k, v in CATS)
    faq = "".join(f"<details><summary>{q}</summary><p>{a}</p></details>" for q, a in FAQ)
    data = {p[0]: {"n": p[1], "c": dict(CATS)[p[2]], "i": "assets/" + p[3], "d": p[5], "l": p[6],
                   "p": money(p[7]) if p[7] else "Precio a consultar", "w": wa(msg(p[1]))} for p in P}
    general = wa("Hola, El Arte que Conecta 👋 Vi el catálogo y quiero hacer un pedido.")
    custom = wa("Hola, El Arte que Conecta 👋 Quiero una pieza personalizada en macramé. Te cuento la idea:")
    ld = {"@context": "https://schema.org", "@type": "ItemList", "name": "Catálogo de piezas en macramé hechas a mano",
          "itemListElement": [{"@type": "ListItem", "position": i + 1, "item": {"@type": "Product", "name": p[1], "description": p[5],
              "image": f"{SITE}/assets/{p[3]}", "brand": {"@type": "Brand", "name": "El Arte que Conecta"}}} for i, p in enumerate(P)]}
    title = "Catálogo de macramé hecho a mano | El Arte que Conecta"
    desc = "Bolsos, accesorios, decoración y piezas navideñas en macramé, tejidas a mano. Elige tu pieza y pídela por WhatsApp."
    nav = "".join(f'<a href="{h}">{l}</a>' for l, h in [("Piezas", "#piezas"), ("Personalizados", "#personalizados"), ("Cómo pedir", "#como-pedir"), ("Aprende", f"{SITE}/escuela"), ("Preguntas", "#preguntas")])
    page = f'''<!DOCTYPE html>
<html lang="es"><head><meta charset="utf-8"/><meta name="viewport" content="width=device-width,initial-scale=1"/>
<title>{title}</title><meta name="description" content="{desc}"/><link rel="canonical" href="{SITE}/catalogo"/>
<meta property="og:title" content="{title}"/><meta property="og:description" content="{desc}"/><meta property="og:url" content="{SITE}/catalogo"/><meta property="og:image" content="{SITE}/assets/catalogo/bol-santorini.webp"/>
<link rel="preconnect" href="https://fonts.googleapis.com"/><link rel="preconnect" href="https://fonts.gstatic.com" crossorigin/>
<link href="https://fonts.googleapis.com/css2?family=Caveat:wght@600;700&family=DM+Sans:wght@400;500;700&family=Fraunces:ital,opsz,wght@0,9..144,400;0,9..144,500;0,9..144,600;1,9..144,400;1,9..144,500&display=swap" rel="stylesheet"/>
<script type="application/ld+json">{json.dumps(ld, ensure_ascii=False)}</script>
<style>{CSS}{EXTRA}</style></head><body>
<div class="topbar">🧶 Cada pieza se teje a mano · <b>Encargos de Navidad abiertos</b> · Pedidos por WhatsApp</div>
<header><div class="wrap nav"><a class="brand" href="{SITE}" style="text-decoration:none">♥ El Arte que Conecta</a><nav>{nav}</nav></div></header>
<main>
<section class="c-hero"><div class="wrap">
<div><div class="eyebrow">Catálogo · Macramé hecho a mano</div>
<h1>Piezas tejidas <em>nudo a nudo,</em> para ti.</h1>
<p class="lead">Bolsos, accesorios, decoración y detalles de Navidad. Cada pieza es única y se teje en los colores que elijas.</p>
<a class="btn" href="#piezas">Ver las piezas</a><a class="btn secondary" href="#personalizados">Quiero algo personalizado</a>
<div class="trust"><span>🧶 100% a mano</span><span>🎨 Colores a tu gusto</span><span>📦 Envíos coordinados</span><span>🎁 Listas para regalar</span></div>
<div class="hand">mírala de cerca → </div></div>
<div class="hero-media"><video src="assets/catalogo/video/hero.mp4" poster="assets/catalogo/video/hero.jpg" autoplay muted loop playsinline></video>
<div class="stamp">hecho<br>con calma</div><div class="tagv"><i></i>Así se ve de cerca</div></div>
</div></section>

<section style="padding-top:20px"><div class="wrap"><div class="cats">{cats}</div></div></section>


<div class="filters" id="piezas"><div class="wrap">{filt}<span class="count" id="count">{len(P)} piezas</span></div></div>
<section style="padding-top:0"><div class="wrap"><div class="grid" id="grid">{grid}</div></div></section>

<section><div class="wrap"><div class="process">
<div class="eyebrow">Detrás de cada pieza</div><h2 style="font-size:clamp(2rem,4vw,3rem)">Horas de trabajo, <em>nudo a nudo.</em></h2>
<p style="max-width:620px">Nada de esto sale de una fábrica. Cada pieza pasa por las manos de quien la teje, con hilos elegidos para durar.</p>
<div class="process-grid">
<figure><video src="assets/catalogo/video/prana.mp4" poster="assets/catalogo/video/prana.jpg" autoplay muted loop playsinline></video><figcaption>espirales de color</figcaption></figure>
<figure><video src="assets/catalogo/video/arbolito.mp4" poster="assets/catalogo/video/arbolito.jpg" autoplay muted loop playsinline></video><figcaption>detalles de temporada</figcaption></figure>
<figure><video src="assets/catalogo/video/paris.mp4" poster="assets/catalogo/video/paris.jpg" autoplay muted loop playsinline></video><figcaption>acabados que se notan</figcaption></figure>
</div></div></div></section>

<section id="personalizados" style="padding-top:20px"><div class="wrap custom">
<div><div class="eyebrow">Por encargo</div><h2 style="font-size:clamp(2rem,4vw,3rem)">¿Tienes una idea? <em>La tejemos.</em></h2>
<p style="color:var(--text)">Cuéntanos qué imaginas y lo hacemos realidad, en tus colores y a tu medida.</p>
<ul><li>Tapices a la medida de tu pared</li><li>Bolsos en los colores de tu marca</li><li>Recordatorios para eventos y regalos corporativos</li></ul>
<a class="btn" href="{custom}" target="_blank" rel="noopener">Contar mi idea por WhatsApp</a></div>
<img src="assets/fotos/v3_1.webp" alt="Piezas de macramé tejidas a mano por encargo" loading="lazy"></div></section>

<section id="como-pedir" class="workshops"><div class="wrap">
<div class="section-head"><div class="eyebrow">Cómo pedir</div><h2>Tu pieza en <em>4 pasos</em></h2></div>
<div class="steps"><article><h3>Elige</h3><p>Encuentra la pieza que te enamora en el catálogo.</p></article>
<article><h3>Escríbenos</h3><p>Toca “Pedir” y te llega un mensaje listo con el nombre de la pieza.</p></article>
<article><h3>Personaliza</h3><p>Confirmamos colores, medidas, precio y tiempo de entrega.</p></article>
<article><h3>Recíbela</h3><p>Pagas, coordinamos el envío y tu pieza llega a casa.</p></article></div></div></section>

<section><div class="wrap learn">
<img src="assets/fotos/instructora.webp" alt="Instructora de la Escuela de Macramé" loading="lazy">
<div><div class="eyebrow">¿Prefieres tejerla tú?</div><h2 style="font-size:clamp(1.9rem,3.6vw,2.8rem)">Aprende a hacer estas piezas <em>desde cero</em></h2>
<p style="color:var(--text)">Casi todo lo que ves aquí se enseña paso a paso en la Escuela de Macramé y en los cursos de Bolsos y Navidad Mágica.</p>
<a class="btn" href="{SITE}/escuela">Conocer la Escuela</a></div></div></section>

<section id="preguntas" style="padding-top:10px"><div class="wrap" style="max-width:780px">
<div class="section-head"><div class="eyebrow">Preguntas frecuentes</div><h2>Antes de <em>pedir</em></h2></div><div class="faq">{faq}</div></div></section>
</main>
<div class="modal" id="modal"><div class="mbox"><button class="mclose" aria-label="Cerrar">×</button><img id="mimg" alt="">
<div class="minfo"><span class="pc-cat" id="mcat"></span><h3 id="mname"></h3><p id="mdesc"></p><ul id="mlist"></ul>
<div class="pc-price" id="mprice" style="margin:6px 0 14px"></div><a class="btn" id="mwa" target="_blank" rel="noopener" style="margin:0">Pedir por WhatsApp</a>
<p class="note" style="margin-top:14px">colores a tu gusto ♥</p></div></div></div>
<a class="sticky-wa" href="{general}" target="_blank" rel="noopener">{WA_SVG}Hacer un pedido</a>
<footer><div class="wrap">El Arte que Conecta · Tejemos creatividad, calma y comunidad ♥<br><small>Piezas hechas a mano · Pedidos por WhatsApp</small></div></footer>
<script>
var D={json.dumps(data, ensure_ascii=False)};
function filt(f){{var n=0;document.querySelectorAll(".filters button").forEach(function(x){{x.classList.toggle("on",x.dataset.f===f)}});
document.querySelectorAll("#grid .pc").forEach(function(c){{var s=f==="all"||c.dataset.cat===f;c.style.display=s?"":"none";if(s)n++}});
document.getElementById("count").textContent=n+" piezas";}}
document.querySelectorAll(".filters button").forEach(function(b){{b.onclick=function(){{filt(b.dataset.f)}}}});
document.querySelectorAll(".cat").forEach(function(b){{b.onclick=function(){{filt(b.dataset.go);document.getElementById("piezas").scrollIntoView({{behavior:"smooth"}})}}}});
var M=document.getElementById("modal");
document.querySelectorAll("[data-open]").forEach(function(b){{b.onclick=function(){{var d=D[b.dataset.open];
mimg.src=d.i;mimg.alt=d.n;mcat.textContent=d.c;mname.textContent=d.n;mdesc.textContent=d.d;mprice.textContent=d.p;mwa.href=d.w;
mlist.innerHTML=d.l.map(function(x){{return"<li>"+x+"</li>"}}).join("");M.classList.add("on")}}}});
M.onclick=function(e){{if(e.target===M||e.target.classList.contains("mclose"))M.classList.remove("on")}};
document.addEventListener("keydown",function(e){{if(e.key==="Escape")M.classList.remove("on")}});
</script></body></html>'''
    (OUT / "catalogo.html").write_text(page)
    print("ok", len(P))

if __name__ == "__main__":
    build()
