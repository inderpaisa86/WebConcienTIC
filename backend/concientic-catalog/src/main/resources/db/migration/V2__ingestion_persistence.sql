CREATE UNIQUE INDEX resources_source_url_unique_idx ON resources (source_url);

CREATE TABLE review_queue (
    id UUID PRIMARY KEY,
    resource_id UUID REFERENCES resources(id) ON DELETE SET NULL,
    candidate_url TEXT NOT NULL,
    issue VARCHAR(120) NOT NULL,
    evidence TEXT,
    agent_decision TEXT,
    confidence_score NUMERIC(5,4),
    recommended_action TEXT,
    status VARCHAR(40) NOT NULL DEFAULT 'OPEN',
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    resolved_at TIMESTAMPTZ
);

CREATE INDEX review_queue_status_idx ON review_queue (status, created_at DESC);
