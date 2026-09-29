CREATE TABLE sources (
    id UUID PRIMARY KEY,
    name VARCHAR(200) NOT NULL UNIQUE,
    category VARCHAR(80) NOT NULL,
    base_url TEXT,
    trust_level VARCHAR(1),
    status VARCHAR(40) NOT NULL DEFAULT 'UNVERIFIED_SEED',
    is_active BOOLEAN NOT NULL DEFAULT FALSE,
    last_checked_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE resources (
    id UUID PRIMARY KEY,
    title TEXT NOT NULL,
    short_description TEXT NOT NULL,
    provider VARCHAR(240) NOT NULL,
    provider_type VARCHAR(80) NOT NULL,
    country VARCHAR(120),
    source_url TEXT NOT NULL,
    canonical_url TEXT,
    verified_url TEXT,
    url_status VARCHAR(40) NOT NULL DEFAULT 'TEMPORARILY_UNAVAILABLE',
    http_status INTEGER,
    last_verified_at TIMESTAMPTZ,
    first_discovered_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    free_status VARCHAR(50) NOT NULL DEFAULT 'UNKNOWN',
    free_explanation TEXT NOT NULL DEFAULT 'Sin verificar',
    languages TEXT[] NOT NULL DEFAULT '{}',
    level VARCHAR(100),
    duration VARCHAR(100),
    format VARCHAR(80) NOT NULL,
    certificate TEXT,
    primary_competency VARCHAR(40),
    guardian_primary VARCHAR(80),
    topics TEXT[] NOT NULL DEFAULT '{}',
    audience TEXT[] NOT NULL DEFAULT '{}',
    trust_level VARCHAR(1),
    trust_score NUMERIC(5,4),
    relevance_score NUMERIC(5,4),
    quality_score NUMERIC(5,4),
    freshness_score NUMERIC(5,4),
    verification_score NUMERIC(5,4),
    overall_score NUMERIC(5,4),
    reason_for_inclusion TEXT,
    status VARCHAR(40) NOT NULL DEFAULT 'DISCOVERED',
    duplicate_of UUID REFERENCES resources(id),
    source_last_checked TIMESTAMPTZ,
    notes TEXT,
    search_document TSVECTOR GENERATED ALWAYS AS (
        to_tsvector('simple', coalesce(title, '') || ' ' || coalesce(short_description, '') || ' ' || coalesce(provider, ''))
    ) STORED,
    CONSTRAINT resources_source_url_not_blank CHECK (length(trim(source_url)) > 0)
);

CREATE TABLE resource_competencies (
    resource_id UUID NOT NULL REFERENCES resources(id) ON DELETE CASCADE,
    competency_id VARCHAR(40) NOT NULL,
    is_primary BOOLEAN NOT NULL DEFAULT FALSE,
    assignment_source VARCHAR(80),
    confidence_score NUMERIC(5,4),
    explanation TEXT,
    PRIMARY KEY (resource_id, competency_id)
);

CREATE TABLE resource_guardians (
    resource_id UUID NOT NULL REFERENCES resources(id) ON DELETE CASCADE,
    guardian_name VARCHAR(80) NOT NULL,
    is_primary BOOLEAN NOT NULL DEFAULT FALSE,
    assignment_source VARCHAR(80),
    confidence_score NUMERIC(5,4),
    explanation TEXT,
    PRIMARY KEY (resource_id, guardian_name)
);

CREATE TABLE ingestion_runs (
    id UUID PRIMARY KEY,
    status VARCHAR(40) NOT NULL,
    started_at TIMESTAMPTZ NOT NULL,
    finished_at TIMESTAMPTZ,
    timezone VARCHAR(80) NOT NULL,
    sources_investigated INTEGER NOT NULL DEFAULT 0,
    resources_discovered INTEGER NOT NULL DEFAULT 0,
    resources_updated INTEGER NOT NULL DEFAULT 0,
    resources_removed INTEGER NOT NULL DEFAULT 0,
    review_required INTEGER NOT NULL DEFAULT 0,
    error_message TEXT
);

CREATE TABLE resource_checks (
    id UUID PRIMARY KEY,
    resource_id UUID NOT NULL REFERENCES resources(id) ON DELETE CASCADE,
    ingestion_run_id UUID NOT NULL REFERENCES ingestion_runs(id) ON DELETE CASCADE,
    requested_url TEXT NOT NULL,
    final_url TEXT,
    canonical_url TEXT,
    http_status INTEGER,
    response_time_ms INTEGER,
    access_status VARCHAR(50) NOT NULL,
    requires_account BOOLEAN,
    requires_payment BOOLEAN,
    language_detected TEXT,
    checked_at TIMESTAMPTZ NOT NULL,
    evidence_hash VARCHAR(128),
    error_code VARCHAR(80),
    error_message TEXT
);

CREATE TABLE resource_changes (
    id UUID PRIMARY KEY,
    resource_id UUID NOT NULL REFERENCES resources(id) ON DELETE CASCADE,
    ingestion_run_id UUID NOT NULL REFERENCES ingestion_runs(id) ON DELETE CASCADE,
    field_name VARCHAR(100) NOT NULL,
    previous_value TEXT,
    current_value TEXT,
    change_type VARCHAR(80) NOT NULL,
    evidence_reference TEXT,
    detected_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX resources_search_document_idx ON resources USING GIN (search_document);
CREATE INDEX resources_status_score_idx ON resources (status, overall_score DESC NULLS LAST);
CREATE INDEX resources_competency_idx ON resource_competencies (competency_id, is_primary);
CREATE INDEX resources_guardian_idx ON resource_guardians (guardian_name, is_primary);
CREATE INDEX resource_checks_resource_time_idx ON resource_checks (resource_id, checked_at DESC);
CREATE INDEX resource_changes_resource_time_idx ON resource_changes (resource_id, detected_at DESC);
