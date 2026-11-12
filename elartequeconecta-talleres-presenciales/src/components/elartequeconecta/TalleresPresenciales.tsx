'use client';

/**
 * Sección "Talleres presenciales" de /elartequeconecta.
 * - Agenda solo para Ibagué: los visitantes de Tolima (o ?ciudad=ibague) ven la agenda completa;
 *   el resto ve un aviso compacto con botón "Ver agenda de Ibagué" (o nada, si GEO.modoFuera = 'oculto').
 * - La página sigue siendo estática: la región se consulta en el navegador a /api/elartequeconecta/region.
 * - Los talleres cuya última sesión ya pasó se ocultan solos.
 */

import { useEffect, useMemo, useState } from 'react';
import Image from 'next/image';
import s from './TalleresPresenciales.module.css';
import {
  CIUDAD,
  FORMULARIO_INSCRIPCION,
  GEO,
  TALLERES,
  type TallerPresencial,
} from '@/data/elartequeconecta/talleres-presenciales';

type Modo = 'cargando' | 'ibague' | 'fuera';

const MESES = ['enero', 'febrero', 'marzo', 'abril', 'mayo', 'junio', 'julio', 'agosto', 'septiembre', 'octubre', 'noviembre', 'diciembre'];
const DIAS_CORTOS = ['LUN', 'MAR', 'MIÉ', 'JUE', 'VIE', 'SÁB', 'DOM'];
const DIAS_LARGOS = ['Domingo', 'Lunes', 'Martes', 'Miércoles', 'Jueves', 'Viernes', 'Sábado'];
const STORAGE_KEY = 'aqc-talleres-region';

const aFecha = (iso: string) => {
  const [y, m, d] = iso.split('-').map(Number);
  return new Date(y, m - 1, d);
};
const isoDe = (d: Date) =>
  `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
const cop = (v: number) => '$' + v.toLocaleString('es-CO');
const normalizar = (t: string) => t.normalize('NFD').replace(/[̀-ͯ]/g, '').toLowerCase().trim();

function track(taller: string) {
  const w = window as unknown as { gtag?: (...a: unknown[]) => void; fbq?: (...a: unknown[]) => void };
  try { w.gtag?.('event', 'generate_lead', { taller, canal: 'presencial', ciudad: CIUDAD }); } catch {}
  try { w.fbq?.('track', 'Lead', { content_name: taller }); } catch {}
}

function useModo(): [Modo, (m: Modo) => void] {
  const [modo, setModo] = useState<Modo>('cargando');
  useEffect(() => {
    // 1) Parámetro en la URL (útil para anuncios segmentados a Ibagué): ?ciudad=ibague
    const q = new URLSearchParams(window.location.search).get('ciudad');
    if (q) {
      const m: Modo = normalizar(q).startsWith('ibag') ? 'ibague' : 'fuera';
      try { sessionStorage.setItem(STORAGE_KEY, m); } catch {}
      setModo(m);
      return;
    }
    // 2) Decisión ya tomada en esta sesión
    try {
      const prev = sessionStorage.getItem(STORAGE_KEY) as Modo | null;
      if (prev === 'ibague' || prev === 'fuera') { setModo(prev); return; }
    } catch {}
    // 3) Geolocalización aproximada por IP (Vercel)
    const ctrl = new AbortController();
    const t = setTimeout(() => ctrl.abort(), 2500);
    fetch('/api/elartequeconecta/region', { signal: ctrl.signal, cache: 'no-store' })
      .then((r) => (r.ok ? r.json() : null))
      .then((g: { pais?: string; region?: string; ciudad?: string } | null) => {
        const dentro =
          !!g &&
          g.pais === 'CO' &&
          (GEO.regionesPermitidas.includes(String(g.region ?? '').toUpperCase()) ||
            GEO.ciudadesPermitidas.includes(normalizar(g.ciudad ?? '')));
        const m: Modo = dentro ? 'ibague' : 'fuera';
        try { sessionStorage.setItem(STORAGE_KEY, m); } catch {}
        setModo(m);
      })
      .catch(() => setModo('fuera'))
      .finally(() => clearTimeout(t));
    return () => { clearTimeout(t); ctrl.abort(); };
  }, []);
  return [modo, setModo];
}

function Calendario({ talleres, hoy }: { talleres: TallerPresencial[]; hoy: Date }) {
  const porDia = useMemo(() => {
    const map = new Map<string, { t: TallerPresencial; horario: string }>();
    talleres.forEach((t) => t.sesiones.forEach((x) => map.set(x.fecha, { t, horario: x.horario })));
    return map;
  }, [talleres]);

  const meses = useMemo(() => {
    const set = new Set<string>();
    talleres.forEach((t) => t.sesiones.forEach((x) => set.add(x.fecha.slice(0, 7))));
    return [...set].sort();
  }, [talleres]);

  const irA = (id: string) => {
    const card = document.getElementById(`taller-${id}`);
    if (!card) return;
    card.classList.remove(s.flash);
    void card.offsetWidth;
    card.classList.add(s.flash);
    setTimeout(() => card.classList.remove(s.flash), 1800);
  };

  return (
    <aside className={s.cal} aria-label={`Calendario de talleres presenciales en ${CIUDAD}`}>
      <div className={s.calHead}>
        <span className={s.eyebrow}>Calendario</span>
        <h3>{meses.map((m) => MESES[Number(m.slice(5)) - 1]).join(' y ').replace(/^./, (c) => c.toUpperCase())}</h3>
      </div>
      {meses.map((ym) => {
        const [y, m] = ym.split('-').map(Number);
        const primero = new Date(y, m - 1, 1);
        const offset = (primero.getDay() + 6) % 7; // lunes = 0
        const dias = new Date(y, m, 0).getDate();
        return (
          <div className={s.month} key={ym}>
            <div className={s.mname}>{MESES[m - 1].replace(/^./, (c) => c.toUpperCase())} {y}</div>
            <div className={s.grid}>
              {DIAS_CORTOS.map((d) => <span key={d} className={s.wd}>{d}</span>)}
              {Array.from({ length: offset }, (_, i) => <span key={`o${i}`} />)}
              {Array.from({ length: dias }, (_, i) => {
                const d = new Date(y, m - 1, i + 1);
                const iso = isoDe(d);
                const ev = porDia.get(iso);
                const pasado = d < hoy;
                if (!ev) return <span key={iso} className={`${s.d} ${pasado ? s.past : ''}`}>{i + 1}</span>;
                const tip = `${ev.t.nombre} · ${ev.horario}${ev.t.cerrado ? ' · cerrado' : ''}`;
                return (
                  <a
                    key={iso}
                    href={`#taller-${ev.t.id}`}
                    onClick={() => irA(ev.t.id)}
                    className={`${s.d} ${s.ev} ${ev.t.cerrado || pasado ? s.evOff : ''}`}
                    style={{ ['--c' as string]: ev.t.color }}
                    title={tip}
                    aria-label={`${i + 1} de ${MESES[m - 1]}: ${tip}`}
                  >
                    {i + 1}
                  </a>
                );
              })}
            </div>
          </div>
        );
      })}
      <ul className={s.legend}>
        {talleres.map((t) => (
          <li key={t.id}>
            <i style={{ background: t.color }} />
            {t.nombre}
            {talleres.filter((x) => x.nombre === t.nombre).length > 1
              ? ` (${MESES[aFecha(t.sesiones[0].fecha).getMonth()].slice(0, 3)}${t.cerrado ? ' · cerrado' : ''})`
              : ''}
          </li>
        ))}
      </ul>
    </aside>
  );
}

function Tarjeta({ t, hoy }: { t: TallerPresencial; hoy: Date }) {
  return (
    <article id={`taller-${t.id}`} className={`${s.card} ${t.cerrado ? s.closed : ''}`} style={{ ['--c' as string]: t.color }}>
      <div className={s.art}>
        <Image src={t.imagen} alt={`${t.nombre} tejido en macramé`} fill sizes="(max-width: 600px) 100vw, 190px" />
        <span className={`${s.badge} ${t.cerrado ? '' : s.badgeOpen}`}>{t.cerrado ? 'Cupos cerrados' : 'Cupos limitados'}</span>
      </div>
      <div className={s.body}>
        <span className={s.pill}>{t.sesiones.length} sesiones · {t.horas} horas</span>
        <h3>{t.nombre}</h3>
        <p>{t.descripcion}</p>
        <ul className={s.ses}>
          {t.sesiones.map((x) => {
            const d = aFecha(x.fecha);
            return (
              <li key={x.fecha} className={d < hoy ? s.sesPast : ''}>
                <span className={s.date}><b>{d.getDate()}</b>{MESES[d.getMonth()].slice(0, 3)}</span>
                <span><strong>{DIAS_LARGOS[d.getDay()]} {d.getDate()} de {MESES[d.getMonth()]}</strong><br />{x.horario}</span>
              </li>
            );
          })}
        </ul>
        <div className={s.foot}>
          <div className={s.price}><b>{cop(t.precio)}</b><small>por persona · kit de materiales incluido</small></div>
          {t.cerrado ? (
            <span className={`${s.btn} ${s.btnOff}`} aria-disabled="true">Cupos cerrados</span>
          ) : (
            <a className={s.btn} href={FORMULARIO_INSCRIPCION} target="_blank" rel="noopener noreferrer" onClick={() => track(t.id)}>
              Inscribirme
            </a>
          )}
        </div>
      </div>
    </article>
  );
}

export default function TalleresPresenciales() {
  const [modo, setModo] = useModo();
  const hoy = useMemo(() => { const d = new Date(); d.setHours(0, 0, 0, 0); return d; }, []);
  const vigentes = useMemo(
    () => TALLERES.filter((t) => aFecha(t.sesiones[t.sesiones.length - 1].fecha) >= hoy)
      .sort((a, b) => Number(a.cerrado) - Number(b.cerrado) || a.sesiones[0].fecha.localeCompare(b.sesiones[0].fecha)),
    [hoy],
  );

  if (vigentes.length === 0) return null; // sin agenda vigente, la sección desaparece
  if (modo === 'fuera' && GEO.modoFuera === 'oculto') return null;

  return (
    <section id="talleres" className={s.section} data-mode={modo}>
      <div className={s.wrap}>
        <div className={s.head}>
          <div className={s.eyebrow}>Talleres presenciales · {CIUDAD}</div>
          <h2>Aprende en persona, nudo a nudo.</h2>
          <p>Grupos pequeños, guía paso a paso y tu proyecto terminado al final. Elige la fecha que mejor te quede y aparta tu cupo.</p>
        </div>

        {modo !== 'ibague' ? (
          <div className={s.teaser}>
            <p>
              <strong>Talleres presenciales en {CIUDAD}.</strong> Si vives en {CIUDAD} o vas a estar allí en noviembre o
              diciembre, mira la agenda y aparta tu cupo.
            </p>
            <button type="button" className={`${s.btn} ${s.btnSecondary}`} onClick={() => setModo('ibague')}>
              Ver agenda de {CIUDAD}
            </button>
          </div>
        ) : (
          <>
            <p className={s.city} role="note">
              <span aria-hidden="true">📍</span>
              <span>
                <strong>Esta agenda aplica solo para la ciudad de {CIUDAD}.</strong> Las sesiones son presenciales; el
                lugar exacto se confirma al inscribirte.
              </span>
            </p>
            <ul className={s.perks}>
              <li>Kit de materiales incluido</li>
              <li>Merienda para compartir</li>
              <li>Acompañamiento paso a paso</li>
              <li>10% con cupón de las ferias Foret y Samaria</li>
            </ul>
            <div className={s.layout}>
              <Calendario talleres={vigentes} hoy={hoy} />
              <div className={s.list}>{vigentes.map((t) => <Tarjeta key={t.id} t={t} hoy={hoy} />)}</div>
            </div>
            <ol className={s.steps}>
              <li><b>1</b><span><strong>Llena el formulario</strong> con tus datos y la fecha que elegiste.</span></li>
              <li><b>2</b><span><strong>Te contactamos</strong> para confirmar tu cupo y darte la dirección del taller en {CIUDAD}.</span></li>
              <li><b>3</b><span><strong>Paga con el código QR</strong> y envía tu comprobante por WhatsApp.</span></li>
            </ol>
            <div className={s.cta}>
              <a className={s.btn} href={FORMULARIO_INSCRIPCION} target="_blank" rel="noopener noreferrer" onClick={() => track('general')}>
                Ir al formulario de inscripción
              </a>
            </div>
          </>
        )}
      </div>
    </section>
  );
}
