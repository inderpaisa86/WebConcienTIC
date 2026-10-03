export const competencyOptions = [
  { id: "", label: "Todos" },
  { id: "informacion", label: "01 · Información" },
  { id: "comunicacion", label: "02 · Comunicación" },
  { id: "creacion", label: "03 · Creación" },
  { id: "seguridad", label: "04 · Seguridad" },
  { id: "discernimiento", label: "05 · Discernimiento" },
  { id: "ia", label: "06 · IA" },
  { id: "bienestar", label: "07 · Bienestar" },
  { id: "ciudadania", label: "08 · Ciudadanía" },
] as const;

export type CatalogResource = {
  id: string;
  title: string;
  shortDescription: string;
  provider: string;
  providerType: string;
  sourceUrl: string;
  canonicalUrl: string | null;
  verifiedUrl: string | null;
  urlStatus: string;
  httpStatus: number | null;
  lastVerifiedAt: string | null;
  freeStatus: string;
  freeExplanation: string;
  language: string[];
  level: string | null;
  duration: string | null;
  format: string | null;
  certificate: string | null;
  primaryCompetency: string | null;
  guardianPrimary: string | null;
  topics: string[];
  audience: string[];
  overallScore: number | null;
  reasonForInclusion: string | null;
  status: string;
  notes: string | null;
};

export type CatalogPage = {
  items: CatalogResource[];
  page: number;
  size: number;
  total: number;
  totalPages: number;
};

export type CatalogFilters = {
  q: string;
  competency: string;
  provider: string;
  level: string;
  format: string;
  page: number;
};

export const providerOptions = [
  "Microsoft Learn",
  "freeCodeCamp",
  "Khan Academy",
  "Google Applied Digital Skills",
  "IBM SkillsBuild",
  "OpenLearn",
] as const;

export function buildCatalogQuery(filters: CatalogFilters): string {
  const query = new URLSearchParams({ page: String(filters.page), size: "12" });
  const values: Array<[string, string]> = [
    ["q", filters.q],
    ["competency", filters.competency],
    ["provider", filters.provider],
    ["level", filters.level],
    ["format", filters.format],
  ];

  for (const [key, value] of values) {
    if (value.trim()) query.set(key, value.trim());
  }

  return query.toString();
}
