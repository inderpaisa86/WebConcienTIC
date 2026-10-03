CREATE TABLE resource_versions (
    id UUID PRIMARY KEY,
    resource_id UUID NOT NULL REFERENCES resources(id) ON DELETE CASCADE,
    ingestion_run_id UUID NOT NULL REFERENCES ingestion_runs(id) ON DELETE CASCADE,
    version_number INTEGER NOT NULL,
    title TEXT NOT NULL,
    canonical_url TEXT,
    access_status VARCHAR(50) NOT NULL,
    free_status VARCHAR(50) NOT NULL,
    resource_status VARCHAR(40) NOT NULL,
    snapshot_hash VARCHAR(128) NOT NULL,
    captured_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (resource_id, ingestion_run_id),
    UNIQUE (resource_id, version_number)
);

CREATE TABLE daily_reports (
    run_id UUID PRIMARY KEY REFERENCES ingestion_runs(id) ON DELETE CASCADE,
    status VARCHAR(40) NOT NULL,
    generated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    report_json JSONB NOT NULL
);

CREATE INDEX resource_versions_resource_time_idx ON resource_versions (resource_id, captured_at DESC);
CREATE INDEX resource_changes_run_type_idx ON resource_changes (ingestion_run_id, change_type);
