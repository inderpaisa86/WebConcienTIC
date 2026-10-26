# `/laboratorios` page — native Next.js port of the LaboratoriosIA mock

The change adds a native `/laboratorios` route that reproduces the four `<main>` sections of `concientic_laboratoriosIA_v2.html` (hero, laboratorios/section-tint, enfoque/principle, final-cta) inside the global layout, inheriting the shared header/footer rather than the mock's own chrome. Content is modeled as a typed `as const` module (`src/content/laboratorios.ts`), the static markup is a server component with `metadata`, and the only interactive piece — the "pendiente de configurar" notice — is isolated in a `"use client"` grid component. All labs CSS is appended to `globals.css` namespaced under `.labs-page`, and the home's Ecosistema `lab-card` CTA is relinked from `#contacto` to `/laboratorios` as an internal same-tab `next/link`.

Watch for: the bundled diff also rewrites `header.tsx`, `footer.tsx`, and six `sections/*` components plus large swaths of `globals.css` — these belong to the earlier home-v13 menu iteration, not this labs task, and are out of scope here (confirmed). Within scope, copy, accent colors, logo placeholders, pending-link behavior, namespacing, and the home connection all match the mock and plan.

**Verdict**: APPROVED

## High-level view

The four sections are reproduced in order with the exact Spanish copy from the mock: the hero pill/h1/`<em>`/two buttons/note, the four lab cards (tags, h3s, paragraphs, card numbers, link labels), the two intro-band panels, the enfoque heading/paragraph/four principle cards, and the final-cta heading/paragraph/button. The `<em>` "a la práctica." renders cyan and non-italic via `.labs-page .hero h1 em`.

The four accent colors map correctly: SIGINEX uses the default `var(--ct-primary)` green, EmprendeCreativo (`violet`) uses `var(--ct-secondary)` purple, DQ-Nexus (`blue`) uses `#0785a0`, and TutorIA (`orange`) uses `#bd5b10` — the same literals as the mock. Each card carries a dashed `logo-slot` holding the mock's exact inline SVG per brand plus the "Logo oficial / por incorporar" caption; no real brand logos were introduced.

Pending behavior is preserved faithfully. `ARTIFACT_URLS` is all-empty, artifact links keep their `#configurar-*` href as a marker, and clicking calls `preventDefault` and surfaces a transient `role="status"` notice reading "El enlace del artefacto {name} está pendiente de configurar." that clears after 4.5s. No URLs were invented.

The home connection is correct: `ecosistema`'s `lab-card` item now has `cta.href: "/laboratorios"`, and `ecosistema.tsx` renders internal `/`-prefixed hrefs through `<Link>` (no `target`/`rel`), falling back to `<a>` for `#` anchors. The other two ecosistema CTAs (`#experiencia`) still render as anchors.

Scope and conventions hold: the page renders no header/footer of its own, the labs CSS lives entirely under `.labs-page` (including a renamed `labs-aura` keyframe so the mock's `aura` can't collide), the page is a server component with `metadata` while interactivity is isolated client-side, and the `nextjs-agent-rules` block in `AGENTS.md` is intact.

<details>
<summary>Issues (1)</summary>

1. **Bundled out-of-scope home changes** — the working tree carries modifications to `header.tsx`, `footer.tsx`, and six `sections/*` components plus a large `globals.css` rewrite from the prior home-v13 iteration. Not a labs-task defect, but be aware the labs commit will sweep them up unless staged selectively. No action required for this review.

</details>

<details>
<summary>Details</summary>

### Section fidelity and copy

All four sections render in order from `src/content/laboratorios.ts` through `page.tsx`. The hero emits the pill with the green `.dot`, the `h1` split into `titleLead` + `<em>{titleEmphasis}</em>` ("a la práctica."), the `hero-copy` paragraph, two buttons, and the `hero-note`. The primary button ("Explorar laboratorios ↘") anchors to `#laboratorios` in-page; the secondary ("Conoce ConcienTIC ↗") points to `/` via `<Link>`, which is the correct native-site target for the mock's external `concientic.com` link. The enfoque paragraph and final-cta copy match the mock verbatim, including the inline `color:#008f70` on the final-cta eyebrow.

The lab-card copy — tags (`01 · Gestión inteligente` … `04 · Aprendizaje inteligente`), h3s, descriptions, card numbers (`LABORATORIO 01`–`04`), and link labels (`Explorar SIGINEX`, `Entrar al laboratorio`, `Iniciar autodiagnóstico`, `Aprender con TutorIA`) — matches the mock string-for-string, and the trailing `↗` is appended in the component.

### Accent colors and logo placeholders

`.labs-page .lab-card` defaults `--accent` to `var(--ct-primary)` (green for SIGINEX), with `.violet` → `var(--ct-secondary)` (purple), `.blue` → `#0785a0`, `.orange` → `#bd5b10`. These are the same values the mock uses (`--purple` for violet, literals for the other two). Variant classes come straight from the typed data (`"" | violet | blue | orange`).

Each `logo-slot` is the dashed placeholder box with the mock's exact per-brand inline SVG (`LAB_ICONS` keyed by card key, SVG attributes transcribed to JSX camelCase) and the "Logo oficial<br>por incorporar" caption. No real brand assets were added, matching the "placeholder only" requirement.

### Pending-artifact behavior

```tsx
if (ARTIFACT_URLS[name]) { return; }
event.preventDefault();
setNotice(`El enlace del artefacto ${name} está pendiente de configurar.`);
if (timeoutRef.current) { clearTimeout(timeoutRef.current); }
timeoutRef.current = setTimeout(() => setNotice(null), 4500);
```

`ARTIFACT_URLS` holds four empty strings, so every click hits the `preventDefault` path — no navigation occurs and no URL is invented. The notice renders in a persistent `role="status"` container toggled by `data-visible`, which keeps it in the accessibility tree for screen readers rather than swapping the node in and out. The 4.5s auto-dismiss and the clearing of any prior timeout mirror the mock's `clearTimeout(window.noticeTimeout)` logic. The `#configurar-*` href remains as an inert marker, as intended.

### Home connection

The Ecosistema `lab-card` item's `cta.href` is `/laboratorios`, label unchanged. `ecosistema.tsx` branches on `item.cta.href.startsWith("/")`: internal routes render as `<Link href=...>` with no `target`/`rel` (same-tab), while the other two items (`#experiencia`) keep `<a href>`. The home is not broken by the relink.

### Scope and Next conventions

The page renders only its `<div className="labs-page">` content — no header/footer — so it inherits the global layout. `header.tsx`/`footer.tsx`/`layout.tsx` mounting is not touched by the labs work. All new CSS sits under `.labs-page` two-level selectors, and the hero aura keyframe was renamed to `labs-aura` so it cannot shadow or be shadowed by the home. A `@media (prefers-reduced-motion: reduce)` block disables the aura animation. The route is a server component exporting `metadata` (title/description from the mock's `<title>`/`<meta>`), with interactivity confined to the `"use client"` grid — consistent with the Next 16.3.4 metadata/client-split rules. The `nextjs-agent-rules` block in `AGENTS.md` is present and unmodified.

### Verification evidence

The plan records the coder's run in `c:\Jose\RepoSiginex\WebConcienTIC\concientic`: `npm run typecheck` (tsc --noEmit) exit 0, `npm run lint` exit 0, `npm run build` (next build) exit 0 with `/laboratorios` prerendered as Static alongside `/` and 11/11 pages generated. The `nextjs-agent-rules` block was grep-verified intact post-change. This matches the current tree state spot-checked here (AGENTS.md block present, CSS namespaced, home connection wired). Suites were not re-run per instructions.

</details>

<details>
<summary>File map</summary>

- `src/content/laboratorios.ts` (new) — typed `as const` content for all four sections; no SVG.
- `src/components/laboratorios/lab-card-grid.tsx` (new) — `"use client"` 4-card grid + exact inline SVGs + `role="status"` pending notice, empty `ARTIFACT_URLS`.
- `src/app/laboratorios/page.tsx` (new) — server component, `metadata`, `.labs-page` wrapper, inherits global layout.
- `src/app/globals.css` (edited) — labs CSS appended under `.labs-page`, renamed `labs-aura` keyframe, reduced-motion block. (Also carries unrelated home-v13 CSS.)
- `src/content/site.ts` (edited) — `ecosistema` lab-card `cta.href` → `/laboratorios`.
- `src/components/sections/ecosistema.tsx` (edited) — internal `/` hrefs via `next/link`; `#` anchors via `<a>`. (Also refactored under home-v13.)
- Out of scope (home-v13 iteration, not labs): `header.tsx`, `footer.tsx`, `contact.tsx`, `dquilibrio.tsx`, `experiencia.tsx`, `hero.tsx`, `methodology.tsx`, `recursos.tsx`.

Full diff: `git -C concientic diff`.

</details>
