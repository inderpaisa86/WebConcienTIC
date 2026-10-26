# Verification Report — `/laboratorios` route + home Ecosistema link

Scope: visual/DOM verification of the new `/laboratorios` route and the home-link
change against the reference mock `concientic_laboratoriosIA_v2.html`.
Project: `c:\Jose\RepoSiginex\WebConcienTIC\concientic` (Next.js 16.3.4 + Turbopack).
No git commits were created (per instructions).

## What was created / changed (confirmed in the working tree)

Created:
- `src/app/laboratorios/page.tsx` — server component. Exports `metadata`
  (`title: "LaboratoriosIA — ConcienTIC"`, description = mock meta) and renders
  `<div className="labs-page">` with hero / laboratorios+intro-band / enfoque /
  final-cta. Inherits the global `<Header/>` + `<Footer/>` from the root layout;
  renders no header/footer of its own.
- `src/components/laboratorios/lab-card-grid.tsx` — `"use client"`. Renders the 4
  `lab-card`s with exact inline SVG logo placeholders and the accessible
  "pendiente de configurar" notice (`role="status"`). `ARTIFACT_URLS` are all
  empty; click handler calls `preventDefault()`, shows the notice, auto-hides at
  ~4500 ms, clears the prior timeout.
- `src/content/laboratorios.ts` — typed (`as const`) content for all sections.

Edited:
- `src/app/globals.css` — appended CSS, all namespaced under `.labs-page`
  (including the `@media (prefers-reduced-motion: reduce)` block). No existing
  home rules modified.
- `src/content/site.ts` — Ecosistema `lab-card` item `cta.href` changed from
  `#contacto` to `/laboratorios`.
- `src/components/sections/ecosistema.tsx` — CTA hrefs starting with `/` render
  via `next/link` `<Link>` (same tab, no `target`/`rel`); `#` anchors keep `<a>`.

A temporary `next.config.ts` dist-dir hook was added only to run a second dev
server alongside the orchestrator's (see "Dev server note"); it was **reverted**
after verification — `next.config.ts` is back to its original content.

## Build / lint / typecheck

Run from `concientic` (Next 16.3.4 + Turbopack):

- `npm run typecheck` (`tsc --noEmit`) → Exit 0, no errors.
- `npm run lint` (`eslint`) → Exit 0, no warnings/errors.
- `npm run build` (`next build`) → Exit 0. Compiled successfully; TypeScript OK;
  11/11 static pages generated. `/laboratorios` listed as `○ (Static)` alongside
  `/` and the other existing routes. Re-run after reverting `next.config.ts` was
  also clean.

No fixes were required — the build was already green and stayed green.

## Dev server note (why port 3100 needed a workaround)

Next 16's `next dev` holds an exclusive per-project lockfile
(`<distDir>/lock`, see `node_modules/next/dist/build/lockfile.js`). With the
orchestrator's server already holding the lock for `.next` in this directory, a
second `npm run dev -- -p 3100` in the same directory detected the running server
and exited (`process.exit(1)`) — it never bound the port. There is no env flag to
bypass the lock. To verify on a **different port without disturbing the
orchestrator's 3000 server**, the second dev server was started on port 3100 with
a separate dist dir (`.next-verify`, via a temporary `next.config.ts` env hook),
which gives it its own lockfile. The 3100 server bound and served correctly; the
orchestrator's 3000 server was never touched. Afterwards the 3100 server was shut
down (whole process tree), the `.next-verify` dir and all temp files were removed,
and the config hook was reverted. Confirmed post-shutdown: port 3100 free, port
3000 still listening.

## Visual / DOM verification — `/laboratorios` (served on :3100, 200 OK)

Note on method: no headless browser (Playwright/Puppeteer) is installed in this
project, so verification used the server-rendered DOM plus the compiled CSS
bundle (checking the actual namespaced rules and resolved color tokens). This
confirms structure, copy, class wiring, and the style rules that apply; it does
not substitute for a pixel screenshot. Points that depend on runtime layout are
noted as such.

Per section (DOM + CSS confirmed):

- **Hero** — `id="inicio"`; `pill` with green `dot` + "Experimenta con intención.
  Aplica con criterio."; `h1` with `<em>a la práctica.</em>`; the CSS rule
  `.labs-page .hero h1 em { font-style: normal; color: var(--ct-cyan) }` is
  present → `<em>` is cyan and non-italic. Two buttons: primary cyan
  (`#laboratorios`, "Explorar laboratorios ↘") and secondary bordered (`/`,
  "Conoce ConcienTIC ↗"). `hero-note` present with exact copy. Decorative
  aura/grid are CSS-only and disabled under reduced-motion. ✓
- **4 lab cards** — correct order and variants/accents:
  - 01 SIGINEX → `--accent: var(--ct-primary)` = **#00c49a (green)**
  - 02 EmprendeCreativo → `.violet` → `var(--ct-secondary)` = **#6c63ff (violet)**
  - 03 DQ-Nexus → `.blue` → **#0785a0**
  - 04 TutorIA → `.orange` → **#bd5b10**
  Token values and the two literals were read back from the compiled CSS and
  match the mock exactly. Each card has the dashed `logo-slot` with the exact
  inline brand SVG, the "Logo oficial / por incorporar" caption, the correct
  Spanish `tag`/`h3`/`p`, `card-number` "LABORATORIO 0N", and the
  `card-link artifact-link` with `data-lab` + `#configurar-*` href. ✓
- **intro-band** — two panels: normal + `.accent` (gradient), exact copy. ✓
- **enfoque** — `.principle` layout; eyebrow "Tecnología con intención"; h2 and
  paragraph match the user's requested wording verbatim; secondary CTA → `/`
  "Conoce nuestra visión ↗"; 4 `principle-card`s with exact strings. ✓
- **final-cta** — gradient section; eyebrow (inline `color:#008f70`); h2; copy;
  primary CTA → `#laboratorios` "Elegir un laboratorio ↗". ✓

78 `.labs-page`-scoped rules are present in the compiled bundle, including all
collision-prone selectors (`.labs-page .final-cta`, `.labs-page .principle`,
`.labs-page .lab-card.violet/.blue/.orange`, `.labs-page .pill`, etc.).

## Global header / footer on `/laboratorios`

- The page renders the **site** `<header class="site-header">` (not the mock's
  header). Nav items unchanged: Ecosistema / Guardianes / Aprende / Metodología /
  DQUILIBRIO, plus the CTA "Encuentra tu ruta ↗" (`ct-button--dark`,
  `href="#experiencia"`). ✓
- The page renders the **site** `<footer class="site-footer">` (not the mock's
  footer). ✓
- The mock's own `<header>`/`<footer>`/`<nav>` and its scroll/menu JS were
  correctly not reproduced. ✓

## Home verification — `/` (served on :3100, 200 OK)

- **No CSS leak / home unchanged**: the home HTML contains no `.labs-page`
  wrapper, so none of the `.labs-page …` descendant rules can match on the home.
  Collision analysis against the compiled CSS:
  - Home uses `hero-section` (not bare `.hero`) and does not use `.principle` /
    `.principle-card` at all → all `.hero`/`.principle*` rules are labs-scoped
    only; no effect on home.
  - `.final-cta`: home's unscoped rules remain intact and coexist with the
    labs-scoped ones (which don't apply on home).
  - Shared names (`feature-card`, `section-head`, `pill`, `hero-copy`,
    `hero-actions`): home's global rules are intact; the lab variants are
    `.labs-page`-scoped and inert on the home.
  Conclusion: the home's `.hero`(-section), `.final-cta`, and principle styling
  render from their own unchanged rules. ✓
- **Ecosistema link**: the card "03 · Experimentación y aplicación — Laboratorios
  IA" now renders `<a href="/laboratorios">Explorar laboratorios ↗</a>` — a plain
  internal link with **no `target="_blank"` and no `rel`**, so it navigates in the
  same tab (Next `<Link>` → `<a href="/laboratorios">`). The sibling Ecosistema
  card still uses `href="#experiencia"`. ✓

## Artifact-link "pendiente de configurar" behavior

Verified at source + DOM + CSS level (no headless browser available to script a
real click):
- The toast element `<div class="lab-notice" role="status" data-visible="false">`
  is server-rendered. ✓
- `LabCardGrid.handleArtifactClick` calls `event.preventDefault()` (all
  `ARTIFACT_URLS` are empty), sets the message
  "El enlace del artefacto {name} está pendiente de configurar.", toggles
  `data-visible="true"`, and auto-hides after 4500 ms, clearing any prior timer. ✓
- The CSS rule `.labs-page .lab-notice[data-visible="true"] { display: block }`
  exists (hidden by default). So on click the notice shows and no navigation
  occurs. ✓

## Cleanup

- Dev server on 3100 shut down (full process tree); port 3100 free.
- Orchestrator's server on port 3000 left running and untouched.
- Temp files removed: `.devserver-3100.*`, `.verify-labs.html`, and the
  throwaway `.next-verify` dist dir.
- `next.config.ts` reverted to original. Final `npm run build` after revert: clean.

## Unresolved differences / caveats

- None functional. The only caveat is methodological: visual checks relied on the
  rendered DOM and the compiled CSS (resolved tokens, applicable rules) rather
  than pixel screenshots, because no headless-browser tooling is installed in the
  project. Structure, copy, accent mapping, namespacing/no-leak, header/footer
  inheritance, the home link, and the artifact-link behavior are all confirmed;
  exact rendered pixel fidelity was not captured via screenshot.
