/**
 * Layout del área nativa `/siginex/*` de concientic.
 *
 * Server component neutro: NO aplica centrado ni fondo, porque la home
 * dashboard (`/siginex`) debe ocupar todo el ancho con su propio shell navy
 * (Sidebar + Topbar). Las pantallas que necesitan centrado (p. ej. el login
 * en `/siginex/login`) lo aplican en su propia página. Tampoco monta el guard
 * de sesión aquí, para no bloquear la página de login; las rutas protegidas
 * envuelven su contenido con `SessionGuard`.
 *
 * Nota de routing (Next.js 16): esta ruta nativa de `app/` tiene prioridad
 * sobre el rewrite `/siginex/:path*` de `next.config.ts` (las rutas de
 * filesystem se resuelven antes que los `afterFiles` rewrites). El rewrite
 * sigue sirviendo cualquier `/siginex/<x>` que NO tenga página nativa.
 */
export default function SiginexLayout({ children }: LayoutProps<"/siginex">) {
  return <>{children}</>;
}
