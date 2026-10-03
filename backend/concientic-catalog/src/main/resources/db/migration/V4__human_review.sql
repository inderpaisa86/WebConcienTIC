ALTER TABLE review_queue
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ADD COLUMN IF NOT EXISTS version INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS reviewer_id VARCHAR(160),
    ADD COLUMN IF NOT EXISTS human_decision VARCHAR(30),
    ADD COLUMN IF NOT EXISTS decision_notes TEXT;

CREATE TABLE resource_human_decisions (
    resource_id UUID PRIMARY KEY REFERENCES resources(id) ON DELETE CASCADE,
    decision VARCHAR(30) NOT NULL,
    reviewer_id VARCHAR(160) NOT NULL,
    decision_notes TEXT,
    approved_title TEXT,
    approved_description TEXT,
    approved_free_status VARCHAR(50),
    approved_resource_status VARCHAR(40),
    decided_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT resource_human_decision_type_check CHECK (decision IN ('APPROVED', 'CORRECTED', 'REJECTED'))
);

CREATE TABLE review_decision_audit (
    id UUID PRIMARY KEY,
    review_queue_id UUID REFERENCES review_queue(id) ON DELETE SET NULL,
    resource_id UUID REFERENCES resources(id) ON DELETE SET NULL,
    action VARCHAR(30) NOT NULL,
    reviewer_id VARCHAR(160) NOT NULL,
    decision_notes TEXT,
    payload JSONB,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX resource_human_decisions_decision_idx ON resource_human_decisions (decision, decided_at DESC);
CREATE INDEX review_decision_audit_resource_idx ON review_decision_audit (resource_id, created_at DESC);
CREATE INDEX review_queue_open_resource_idx ON review_queue (resource_id, status, created_at DESC);
