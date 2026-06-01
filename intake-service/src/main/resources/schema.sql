CREATE SCHEMA IF NOT EXISTS intake_schema;

CREATE TABLE IF NOT EXISTS intake_schema.event_receipts (
    receipt_id UUID PRIMARY KEY,
    event_id UUID NOT NULL,
    producer_id VARCHAR NOT NULL,
    correlation_id VARCHAR,
    payload_hash VARCHAR NOT NULL,
    batch_id VARCHAR,
    trace_id VARCHAR,
    retry_count INT DEFAULT 0 NOT NULL,
    source VARCHAR,
    received_at TIMESTAMP NOT NULL
);

-- Ensure observability columns exist for existing tables
ALTER TABLE intake_schema.event_receipts ADD COLUMN IF NOT EXISTS batch_id VARCHAR;
ALTER TABLE intake_schema.event_receipts ALTER COLUMN batch_id TYPE VARCHAR;
ALTER TABLE intake_schema.event_receipts ADD COLUMN IF NOT EXISTS trace_id VARCHAR;
ALTER TABLE intake_schema.event_receipts ADD COLUMN IF NOT EXISTS retry_count INT DEFAULT 0;
UPDATE intake_schema.event_receipts SET retry_count = 0 WHERE retry_count IS NULL;
ALTER TABLE intake_schema.event_receipts ALTER COLUMN retry_count SET NOT NULL;
ALTER TABLE intake_schema.event_receipts ADD COLUMN IF NOT EXISTS source VARCHAR;
