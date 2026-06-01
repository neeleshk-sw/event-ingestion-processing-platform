CREATE SCHEMA IF NOT EXISTS audit_schema;

CREATE TABLE IF NOT EXISTS audit_schema.audit_log (
    audit_id UUID PRIMARY KEY,
    event_id UUID NOT NULL,
    correlation_id VARCHAR,
    stage VARCHAR NOT NULL,
    action VARCHAR NOT NULL,
    details TEXT,
    batch_id VARCHAR,
    trace_id VARCHAR,
    retry_count INT DEFAULT 0 NOT NULL,
    source VARCHAR,
    recorded_at TIMESTAMP NOT NULL
);

-- Ensure observability columns exist for existing tables
ALTER TABLE audit_schema.audit_log ADD COLUMN IF NOT EXISTS batch_id VARCHAR;
ALTER TABLE audit_schema.audit_log ALTER COLUMN batch_id TYPE VARCHAR;
ALTER TABLE audit_schema.audit_log ADD COLUMN IF NOT EXISTS trace_id VARCHAR;
ALTER TABLE audit_schema.audit_log ADD COLUMN IF NOT EXISTS retry_count INT DEFAULT 0;
UPDATE audit_schema.audit_log SET retry_count = 0 WHERE retry_count IS NULL;
ALTER TABLE audit_schema.audit_log ALTER COLUMN retry_count SET NOT NULL;
ALTER TABLE audit_schema.audit_log ADD COLUMN IF NOT EXISTS source VARCHAR;
