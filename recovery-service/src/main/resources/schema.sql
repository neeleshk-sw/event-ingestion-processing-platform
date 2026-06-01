CREATE SCHEMA IF NOT EXISTS recovery_schema;

CREATE TABLE IF NOT EXISTS recovery_schema.failed_events (
    failure_id UUID PRIMARY KEY,
    event_id UUID NOT NULL,
    stage VARCHAR NOT NULL,
    reason TEXT NOT NULL,
    payload JSONB NOT NULL,
    correlation_id VARCHAR,
    batch_id VARCHAR,
    trace_id VARCHAR,
    retry_count INT DEFAULT 0 NOT NULL,
    source VARCHAR,
    failed_at TIMESTAMP NOT NULL
);

-- Ensure observability columns exist for existing tables
ALTER TABLE recovery_schema.failed_events ADD COLUMN IF NOT EXISTS batch_id VARCHAR;
ALTER TABLE recovery_schema.failed_events ALTER COLUMN batch_id TYPE VARCHAR;
ALTER TABLE recovery_schema.failed_events ADD COLUMN IF NOT EXISTS trace_id VARCHAR;
ALTER TABLE recovery_schema.failed_events ADD COLUMN IF NOT EXISTS retry_count INT DEFAULT 0;
UPDATE recovery_schema.failed_events SET retry_count = 0 WHERE retry_count IS NULL;
ALTER TABLE recovery_schema.failed_events ALTER COLUMN retry_count SET NOT NULL;
ALTER TABLE recovery_schema.failed_events ADD COLUMN IF NOT EXISTS source VARCHAR;
