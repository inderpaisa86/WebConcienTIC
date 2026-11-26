"use client";

import { useEffect, useRef, useState } from "react";

import styles from "./elartequeconecta.module.css";

const VIDEO = "/elartequeconecta/assets/catalogo/video/hero.mp4";
const POSTER = "/elartequeconecta/assets/catalogo/video/hero.jpg";

/** Formato m:ss para los segundos del video. */
function formatear(segundos: number) {
  const s = Math.floor(Number.isFinite(segundos) ? segundos : 0);
  return `${Math.floor(s / 60)}:${String(s % 60).padStart(2, "0")}`;
}

/**
 * Video de portada con controles propios: pausa/reproducir, tiempo, barra de
 * avance y pantalla completa. Se pausa al salir de la pantalla (ahorra datos
 * en celular) y, con "reducir movimiento", no se reproduce solo.
 */
export function HeroVideo() {
  const videoRef = useRef<HTMLVideoElement>(null);
  // true cuando el visitante pausó a mano: el observador no lo reanuda.
  const pausadoPorUsuario = useRef(false);
  const [pausado, setPausado] = useState(true);
  const [tiempo, setTiempo] = useState("0:00");
  const [progreso, setProgreso] = useState(0);

  useEffect(() => {
    const video = videoRef.current;
    if (!video) return;

    if (window.matchMedia("(prefers-reduced-motion: reduce)").matches) {
      video.removeAttribute("autoplay");
      video.pause();
      return;
    }

    if (!("IntersectionObserver" in window)) return;
    const observer = new IntersectionObserver(
      (entradas) => {
        entradas.forEach((e) => {
          if (!e.isIntersecting) {
            video.pause();
          } else if (!pausadoPorUsuario.current) {
            video.play().catch(() => {});
          }
        });
      },
      { threshold: 0.2 },
    );
    observer.observe(video);
    return () => observer.disconnect();
  }, []);

  const alternar = () => {
    const video = videoRef.current;
    if (!video) return;
    pausadoPorUsuario.current = !video.paused;
    if (video.paused) video.play().catch(() => {});
    else video.pause();
  };

  const pantallaCompleta = () => {
    const video = videoRef.current as
      | (HTMLVideoElement & { webkitEnterFullscreen?: () => void; webkitRequestFullscreen?: () => void })
      | null;
    if (!video) return;
    if (video.requestFullscreen) video.requestFullscreen();
    else if (video.webkitEnterFullscreen) video.webkitEnterFullscreen();
    else video.webkitRequestFullscreen?.();
  };

  const actualizarTiempo = () => {
    const video = videoRef.current;
    if (!video) return;
    const duracion = Number.isFinite(video.duration) ? video.duration : 0;
    setTiempo(`${formatear(video.currentTime)} / ${formatear(duracion)}`);
    setProgreso((video.currentTime / (duracion || 1)) * 100);
  };

  return (
    <div className={styles.hxFrame}>
      <video
        ref={videoRef}
        className={styles.hxVideo}
        src={VIDEO}
        poster={POSTER}
        autoPlay
        muted
        loop
        playsInline
        preload="metadata"
        aria-label="Extracto de video: piezas de macramé tejidas por nuestras alumnas"
        onPlay={() => setPausado(false)}
        onPause={() => setPausado(true)}
        onTimeUpdate={actualizarTiempo}
      />
      <div className={styles.hxBar}>
        <button
          type="button"
          className={styles.hxBoton}
          onClick={alternar}
          aria-label={pausado ? "Reproducir video" : "Pausar video"}
        >
          {pausado ? (
            <svg viewBox="0 0 24 24" width="20" height="20" aria-hidden="true">
              <path d="M8 5v14l11-7z" fill="currentColor" />
            </svg>
          ) : (
            <svg viewBox="0 0 24 24" width="20" height="20" aria-hidden="true">
              <path d="M7 5h3.5v14H7zM13.5 5H17v14h-3.5z" fill="currentColor" />
            </svg>
          )}
        </button>
        <span className={styles.hxTiempo}>{tiempo}</span>
        <div className={styles.hxProg} aria-hidden="true">
          <b style={{ width: `${progreso}%` }} />
        </div>
        <button
          type="button"
          className={styles.hxBoton}
          onClick={pantallaCompleta}
          aria-label="Ver en pantalla completa"
        >
          <svg viewBox="0 0 24 24" width="18" height="18" aria-hidden="true">
            <path d="M4 9V4h5M15 4h5v5M20 15v5h-5M9 20H4v-5" fill="none" stroke="currentColor" strokeWidth="2" />
          </svg>
        </button>
      </div>
    </div>
  );
}
