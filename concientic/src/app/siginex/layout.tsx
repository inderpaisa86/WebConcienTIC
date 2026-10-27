/**
 * Layout del área nativa `/siginex/*` de concientic.
 *
 * Server component: solo aporta el contenedor centrado y el fondo suave de la
 * marca. NO monta el guard de sesión aquí, para no bloquear la propia página
 * de login; las rutas protegidas (p. ej. `/siginex/panel`) envuelven su
 * contenido con `SessionGuard`.
 *
 * Nota de routing (Next.js 16): esta ruta nativa de `app/` tiene prioridad
 * sobre el rewrite `/siginex/:path*` de `next.config.ts` (las rutas de
 * filesystem se resuelven antes que los `afterFiles` rewrites). El rewrite
 * sigue sirviendo cualquier `/siginex/<x>` que NO tenga página nativa.
 */
export default function SiginexLayout({ children }: LayoutProps<"/siginex">) {
  return (
    <div className="flex min-h-[70vh] items-center justify-center bg-muted px-4 py-16">
      {children}
    </div>
  );
}
