# Static-first initial content for Biblioteca abierta

The public `Biblioteca abierta` now keeps the curated static collection visible while the catalog request is pending, instead of replacing it with skeletons. The change moves the `visibleResources` check ahead of `loading`, so the existing `staticCourses` base renders on the first pass and remains available while dynamic resources are fetched or unavailable. Once the request completes, the existing merge still appends deduplicated dynamic results after the static collection, while the existing filters and pagination continue to operate on the merged set. The recorded lint, typecheck, build, and diff checks all passed.

**Watch for:** **possible** — no browser/component regression scenario was recorded for the initial, slow, failed, and empty-response states; add one when the test harness is available, although the reviewed code path satisfies those cases.

**Verdict**: APPROVED

## High-level view

The render gate now prioritizes an available filtered collection over request status, allowing the pre-query static base to appear immediately. Loading remains a fallback only when the current filters produce no visible resources.

The catalog topology and ordering remain unchanged: static resources are the base, dynamic results are appended, and URL-normalized duplicates are removed before filtering and client-side pagination. The API query builder, debounce, abort behavior, filters, search, pagination, official Guardianes, and CTA remain outside the change.

A failed or empty backend response cannot erase the static base: the error notice is rendered alongside the collection, and the no-match state requires loading to have ended with no visible resources. The only identified gap is **possible** automated coverage of those temporal states.

<details>
<summary>Issues (1)</summary>

1. **Transient-state regression coverage** — **possible**: lint, typecheck, build, and code-flow review do not exercise the browser states for initial loading, slow responses, failures, and empty responses; add a focused component or browser regression test when the project test harness supports it.

</details>

<details>
<summary>Details</summary>

### Static-first render gate

The changed conditional in `concientic/src/components/library/library-explorer.tsx` makes `visibleResources` win over `loading`. On the initial render, `catalog` is `null`, but `mergeCatalogResources(catalog?.items ?? [])` still returns the static base, so page zero is populated before the 220 ms debounce and fetch resolve. A slow request therefore leaves the cards in place instead of showing only skeletons; a response that fails or returns no dynamic items also cannot remove them.

The no-match message remains behind both gates: there must be no visible filtered resources, and loading must already be false. That preserves an empty state for genuinely unmatched filters while preventing “Todavía no encontramos una coincidencia.” from flashing over an available static collection.

### Static/dynamic catalog contract

`mergeCatalogResources` is unchanged and continues to return `staticCourses` first, followed by dynamic items whose normalized source URLs are not already present in the static or dynamic sets. Filtering and client-side pagination still run after that merge, so dynamic additions do not replace the burned-in collection or reorder it. `buildCatalogQuery` continues requesting the dynamic page independently of the local page control, preserving the existing catalog contract.

### Failure behavior, preserved surface, and validation boundary

The error alert remains above the resource grid and explicitly communicates that the existing courses are retained; it does not take the place of the grid. The diff does not touch `static-courses.ts`, `catalog.ts`, the API route, `page.tsx`, `guardians.ts`, or any `library-guardians` source reference, and it leaves the official Guardian data, Guardian rendering, filters, search, pagination, and resource CTA intact.

The supplied evidence records successful `npm run lint`, `npm run typecheck`, `npm run build`, and `git diff --check` runs, plus checks that official Guardianes were unchanged and `library-guardians` was not reintroduced. **possible** coverage gap: it also records no browser visual or runtime scenario check, so the temporal behavior is verified from the code path rather than by an end-to-end assertion. This does not expose a blocking issue in the reviewed diff.

</details>

<details>
<summary>Changed files</summary>

- `concientic/src/components/library/library-explorer.tsx` — prioritizes visible merged resources over the loading skeleton branch.

Full diff: `git diff -- concientic/src/components/library/library-explorer.tsx`

</details>
