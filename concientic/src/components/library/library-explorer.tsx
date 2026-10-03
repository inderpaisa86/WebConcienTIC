"use client";

import Image from "next/image";
import { useEffect, useMemo, useState } from "react";

import { guardians } from "@/content/guardians";
import { staticCourses } from "@/content/static-courses";
import {
  buildCatalogQuery,
  competencyOptions,
  providerOptions,
  type CatalogFilters,
  type CatalogPage,
  type CatalogResource,
} from "@/lib/catalog";

const initialFilters: CatalogFilters = {
  q: "",
  competency: "",
  provider: "",
  level: "",
  format: "",
  page: 0,
};

function competencyLabel(id: string | null) {
  return competencyOptions.find((option) => option.id === id)?.label ?? "Competencia pendiente";
}

function guardianFor(resource: CatalogResource) {
  return guardians.find((guardian) => guardian.name === resource.guardianPrimary);
}

function freeLabel(resource: CatalogResource) {
  if (resource.freeStatus === "FREE") return "Acceso gratuito";
  if (resource.freeStatus === "FREE_CONTENT_PAID_CERTIFICATE") return "Contenido gratuito";
  if (resource.freeStatus === "AUDIT_FREE") return "Auditoría gratuita";
  if (resource.freeStatus === "PARTIAL_FREE") return "Acceso parcial gratuito";
  return resource.freeExplanation || "Modalidad por confirmar";
}

const formatLabels: Record<string, string> = {
  article: "Artículo",
  course: "Curso",
  framework: "Marco",
  lesson: "Lección",
  library: "Biblioteca",
  module: "Módulo",
  other: "Recurso",
  route: "Ruta",
};

function formatLabel(format: string | null) {
  return format ? (formatLabels[format.toLowerCase()] ?? format) : null;
}

function normalizedUrl(url: string) {
  return url.replace(/\/$/, "").toLowerCase();
}

function matchesFilters(resource: CatalogResource, filters: CatalogFilters) {
  const searchText = [resource.title, resource.shortDescription, resource.provider, resource.primaryCompetency]
    .filter(Boolean)
    .join(" ")
    .toLowerCase();
  const resourceFormat = resource.format?.toLowerCase() ?? "";
  const queryMatches = !filters.q.trim() || searchText.includes(filters.q.trim().toLowerCase());
  const competencyMatches = !filters.competency || resource.primaryCompetency === filters.competency;
  const providerMatches = !filters.provider || resource.provider === filters.provider;
  const levelMatches = !filters.level || resource.level === filters.level;
  const formatMatches = !filters.format || resourceFormat === filters.format.toLowerCase();
  return queryMatches && competencyMatches && providerMatches && levelMatches && formatMatches;
}

function mergeCatalogResources(dynamicResources: CatalogResource[]) {
  const staticUrls = new Set(staticCourses.map((resource) => normalizedUrl(resource.sourceUrl)));
  const dynamicUrls = new Set<string>();
  const uniqueDynamic = dynamicResources.filter((resource) => {
    const url = normalizedUrl(resource.sourceUrl);
    if (staticUrls.has(url) || dynamicUrls.has(url)) return false;
    dynamicUrls.add(url);
    return true;
  });
  return [...staticCourses, ...uniqueDynamic];
}

export function LibraryExplorer() {
  const [filters, setFilters] = useState(initialFilters);
  const [catalog, setCatalog] = useState<CatalogPage | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const allResources = useMemo(() => {
    const merged = mergeCatalogResources(catalog?.items ?? []);
    return merged.filter((resource) => matchesFilters(resource, filters));
  }, [catalog, filters]);
  const pageSize = 12;
  const totalPages = Math.max(1, Math.ceil(allResources.length / pageSize));
  const visibleResources = allResources.slice(filters.page * pageSize, (filters.page + 1) * pageSize);

  useEffect(() => {
    const controller = new AbortController();
    const timer = window.setTimeout(async () => {
      setLoading(true);
      setError(null);
      try {
        const response = await fetch(`/api/catalog/resources?${buildCatalogQuery(filters)}`, {
          signal: controller.signal,
        });
        if (!response.ok) throw new Error("CATALOG_REQUEST_FAILED");
        setCatalog((await response.json()) as CatalogPage);
      } catch (requestError) {
        if (requestError instanceof DOMException && requestError.name === "AbortError") return;
        setError("No pudimos conectar con la Biblioteca. Intenta nuevamente en unos minutos.");
      } finally {
        if (!controller.signal.aborted) setLoading(false);
      }
    }, 220);

    return () => {
      window.clearTimeout(timer);
      controller.abort();
    };
  }, [filters]);

  const availableProviders = useMemo(() => {
    const dynamicProviders = allResources.map((item) => item.provider);
    return [...new Set([...providerOptions, ...dynamicProviders])].sort();
  }, [allResources]);

  function updateFilter(key: keyof CatalogFilters, value: string | number) {
    setFilters((current) => {
      if (key === "page") return { ...current, page: Number(value) };
      return { ...current, [key]: String(value), page: 0 };
    });
  }

  function resetFilters() {
    setFilters(initialFilters);
  }

  return (
    <div className="library-page">
      <section className="library-hero">
        <div className="ct-container library-hero__inner">
          <div>
            <p className="ct-eyebrow">Servicios · Competencias Digitales · Biblioteca abierta</p>
            <h1>Aprender también es elegir con criterio.</h1>
            <p className="library-hero__lead">
              Una selección de recursos gratuitos o claramente identificados, verificados por el motor
              de inteligencia de contenidos de ConcienTIC.
            </p>
          </div>
          <div className="library-hero__statement" aria-label="Principio de la Biblioteca">
            <span>Menos ruido.</span>
            <strong>Más criterio.</strong>
            <span>Más autonomía.</span>
          </div>
        </div>
      </section>

      <main className="ct-section ct-section--soft" id="biblioteca">
        <div className="ct-container">
          <div className="library-toolbar" aria-label="Filtros de la Biblioteca">
            <div className="library-search">
              <label htmlFor="library-search">Buscar un recurso</label>
              <input
                id="library-search"
                type="search"
                value={filters.q}
                placeholder="Ej. inteligencia artificial, seguridad..."
                onChange={(event) => updateFilter("q", event.target.value)}
              />
            </div>
            <div className="library-selects">
              <label>
                Fuente
                <select value={filters.provider} onChange={(event) => updateFilter("provider", event.target.value)}>
                  <option value="">Todas las fuentes</option>
                  {availableProviders.map((provider) => (
                    <option key={provider} value={provider}>
                      {provider}
                    </option>
                  ))}
                </select>
              </label>
              <label>
                Nivel
                <select value={filters.level} onChange={(event) => updateFilter("level", event.target.value)}>
                  <option value="">Todos los niveles</option>
                  <option value="Inicial">Inicial</option>
                  <option value="Intermedio">Intermedio</option>
                  <option value="Avanzado">Avanzado</option>
                </select>
              </label>
              <label>
                Formato
                <select value={filters.format} onChange={(event) => updateFilter("format", event.target.value)}>
                  <option value="">Todos los formatos</option>
                  <option value="course">Curso</option>
                  <option value="lesson">Lección</option>
                  <option value="route">Ruta</option>
                  <option value="framework">Marco</option>
                  <option value="library">Biblioteca</option>
                  <option value="module">Módulo</option>
                  <option value="other">Recurso</option>
                </select>
              </label>
            </div>
          </div>

          <div className="library-competencies" aria-label="Filtrar por competencia digital">
            {competencyOptions.map((option) => (
              <button
                className={filters.competency === option.id ? "is-active" : ""}
                key={option.id || "all"}
                type="button"
                onClick={() => updateFilter("competency", option.id)}
              >
                {option.label}
              </button>
            ))}
            <button className="library-reset" type="button" onClick={resetFilters}>
              Limpiar filtros
            </button>
          </div>

          <div className="library-results-header">
            <div>
              <p className="ct-eyebrow">Selección curada</p>
              <h2>Recursos para comenzar</h2>
            </div>
            <p aria-live="polite">
              {loading ? "Buscando recursos..." : `${allResources.length} recursos disponibles`}
            </p>
          </div>

          {error ? (
            <div className="library-state library-state--error" role="alert">
              <h3>El motor no respondió, pero conservamos la Biblioteca.</h3>
              <p>{error} Los cursos existentes siguen disponibles.</p>
              <button type="button" onClick={() => setFilters((current) => ({ ...current }))}>
                Intentar de nuevo
              </button>
            </div>
          ) : null}

          {loading ? (
            <div className="library-grid" aria-label="Cargando recursos">
              {[0, 1, 2].map((item) => <div className="library-skeleton" key={item} />)}
            </div>
          ) : visibleResources.length ? (
            <div className="library-grid">
              {visibleResources.map((resource) => <ResourceCard key={resource.id} resource={resource} />)}
            </div>
          ) : (
            <div className="library-state">
              <h3>Todavía no encontramos una coincidencia.</h3>
              <p>Prueba otra competencia o elimina algún filtro. El catálogo crece con verificaciones, no con ruido.</p>
              <button type="button" onClick={resetFilters}>Ver todos los recursos</button>
            </div>
          )}

          {totalPages > 1 ? (
            <nav className="library-pagination" aria-label="Paginación de recursos">
              <button type="button" disabled={filters.page === 0} onClick={() => updateFilter("page", filters.page - 1)}>
                Anterior
              </button>
              <span>Página {filters.page + 1} de {totalPages}</span>
              <button type="button" disabled={filters.page + 1 >= totalPages} onClick={() => updateFilter("page", filters.page + 1)}>
                Siguiente
              </button>
            </nav>
          ) : null}
        </div>
      </main>

      <section className="ct-section library-academy">
        <div className="ct-container library-academy__inner">
          <div>
            <p className="ct-eyebrow">Academia ConcienTIC</p>
            <h2>Una experiencia propia para aprender con DQUILIBRIO.</h2>
          </div>
          <span className="library-academy__badge">Próximamente</span>
        </div>
      </section>
    </div>
  );
}

function ResourceCard({ resource }: { resource: CatalogResource }) {
  const guardian = guardianFor(resource);
  const guardianColor = guardian?.color ?? "var(--ct-primary)";
  const metadata = [resource.level, resource.duration, resource.language[0], formatLabel(resource.format)].filter(Boolean);
  const category = competencyLabel(resource.primaryCompetency);
  const providerMark = resource.provider.slice(0, 2).toUpperCase();

  return (
    <article className="library-card" style={{ "--guardian-color": guardianColor } as React.CSSProperties}>
      <div className="library-card__visual">
        <div className="library-card__topline">
          <span>{category}</span>
        </div>
        <div className="library-card__provider">
          <span className="library-card__provider-mark" aria-hidden="true">{providerMark}</span>
          <span>{resource.provider}</span>
        </div>
        {guardian ? (
          <Image className="library-card__guardian-image" src={guardian.image} alt="" width={132} height={122} />
        ) : null}
      </div>
      <div className="library-card__body">
        <h3>{resource.title}</h3>
        <p>{resource.shortDescription}</p>
        {metadata.length ? <div className="library-card__metadata">{metadata.map((item) => <span key={item}>{item}</span>)}</div> : null}
        <div className="library-card__free">
          <strong>{freeLabel(resource)}</strong>
          {resource.freeExplanation && resource.freeStatus !== "FREE" ? <span>{resource.freeExplanation}</span> : null}
        </div>
        <a className="ct-button library-card__cta" href={resource.verifiedUrl ?? resource.sourceUrl} target="_blank" rel="noreferrer">
          Explorar recurso <span aria-hidden="true">→</span>
        </a>
      </div>
    </article>
  );
}
