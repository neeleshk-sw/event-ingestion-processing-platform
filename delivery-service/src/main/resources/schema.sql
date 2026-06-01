CREATE SCHEMA IF NOT EXISTS delivery_schema;

CREATE TABLE IF NOT EXISTS delivery_schema.delivery_state (
    idempotency_key VARCHAR PRIMARY KEY,
    event_id UUID NOT NULL,
    destination VARCHAR NOT NULL,
    status VARCHAR NOT NULL,
    attempts INT NOT NULL,
    last_error TEXT,
    batch_id VARCHAR,
    trace_id VARCHAR,
    retry_count INT DEFAULT 0 NOT NULL,
    source VARCHAR,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS delivery_schema.delivery_queue (
    id UUID PRIMARY KEY,
    event_id UUID NOT NULL,
    payload TEXT NOT NULL,
    destination VARCHAR NOT NULL,
    routing_key VARCHAR NOT NULL,
    priority INT NOT NULL DEFAULT 1,
    status VARCHAR NOT NULL DEFAULT 'NEW',
    batch_id VARCHAR,
    trace_id VARCHAR,
    retry_count INT DEFAULT 0 NOT NULL,
    source VARCHAR,
    queued_at TIMESTAMP NOT NULL
);

-- Ensure observability columns exist for existing tables
ALTER TABLE delivery_schema.delivery_state ADD COLUMN IF NOT EXISTS batch_id VARCHAR;
ALTER TABLE delivery_schema.delivery_state ALTER COLUMN batch_id TYPE VARCHAR;
ALTER TABLE delivery_schema.delivery_state ADD COLUMN IF NOT EXISTS trace_id VARCHAR;
ALTER TABLE delivery_schema.delivery_state ADD COLUMN IF NOT EXISTS retry_count INT DEFAULT 0;
UPDATE delivery_schema.delivery_state SET retry_count = 0 WHERE retry_count IS NULL;
ALTER TABLE delivery_schema.delivery_state ALTER COLUMN retry_count SET NOT NULL;
ALTER TABLE delivery_schema.delivery_state ADD COLUMN IF NOT EXISTS source VARCHAR;

ALTER TABLE delivery_schema.delivery_queue ADD COLUMN IF NOT EXISTS batch_id VARCHAR;
ALTER TABLE delivery_schema.delivery_queue ALTER COLUMN batch_id TYPE VARCHAR;
ALTER TABLE delivery_schema.delivery_queue ADD COLUMN IF NOT EXISTS trace_id VARCHAR;
ALTER TABLE delivery_schema.delivery_queue ADD COLUMN IF NOT EXISTS retry_count INT DEFAULT 0;
UPDATE delivery_schema.delivery_queue SET retry_count = 0 WHERE retry_count IS NULL;
ALTER TABLE delivery_schema.delivery_queue ALTER COLUMN retry_count SET NOT NULL;
ALTER TABLE delivery_schema.delivery_queue ADD COLUMN IF NOT EXISTS source VARCHAR;

