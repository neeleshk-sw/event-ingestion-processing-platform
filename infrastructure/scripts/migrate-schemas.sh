#!/bin/bash
# =============================================================================
# Schema Migration Script for RDS PostgreSQL
# =============================================================================
#
# Consolidates all service schema.sql files into a single migration.
# Run this against the RDS instance via SSM Session Manager or bastion host.
#
# Usage:
#   export PGHOST=<rds-endpoint>
#   export PGPORT=5432
#   export PGUSER=eventadmin
#   export PGPASSWORD=<password>
#   export PGDATABASE=eventdb
#   ./migrate-schemas.sh
#
# Or directly:
#   psql -h <rds-endpoint> -U eventadmin -d eventdb -f migrate-schemas.sh
# =============================================================================

set -euo pipefail

echo "========================================="
echo "Event Platform — Schema Migration"
echo "========================================="
echo "Host: ${PGHOST:-localhost}"
echo "Database: ${PGDATABASE:-eventdb}"
echo ""

PSQL="psql -h ${PGHOST:-localhost} -p ${PGPORT:-5432} -U ${PGUSER:-eventadmin} -d ${PGDATABASE:-eventdb} -v ON_ERROR_STOP=1"

# ----- intake_schema -----
echo "[1/4] Migrating intake_schema..."
$PSQL <<'SQL'
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

ALTER TABLE intake_schema.event_receipts ADD COLUMN IF NOT EXISTS batch_id VARCHAR;
ALTER TABLE intake_schema.event_receipts ALTER COLUMN batch_id TYPE VARCHAR;
ALTER TABLE intake_schema.event_receipts ADD COLUMN IF NOT EXISTS trace_id VARCHAR;
ALTER TABLE intake_schema.event_receipts ADD COLUMN IF NOT EXISTS retry_count INT DEFAULT 0;
UPDATE intake_schema.event_receipts SET retry_count = 0 WHERE retry_count IS NULL;
ALTER TABLE intake_schema.event_receipts ALTER COLUMN retry_count SET NOT NULL;
ALTER TABLE intake_schema.event_receipts ADD COLUMN IF NOT EXISTS source VARCHAR;
SQL
echo "  ✓ intake_schema ready"

# ----- delivery_schema -----
echo "[2/4] Migrating delivery_schema..."
$PSQL <<'SQL'
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
SQL
echo "  ✓ delivery_schema ready"

# ----- recovery_schema -----
echo "[3/4] Migrating recovery_schema..."
$PSQL <<'SQL'
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

ALTER TABLE recovery_schema.failed_events ADD COLUMN IF NOT EXISTS batch_id VARCHAR;
ALTER TABLE recovery_schema.failed_events ALTER COLUMN batch_id TYPE VARCHAR;
ALTER TABLE recovery_schema.failed_events ADD COLUMN IF NOT EXISTS trace_id VARCHAR;
ALTER TABLE recovery_schema.failed_events ADD COLUMN IF NOT EXISTS retry_count INT DEFAULT 0;
UPDATE recovery_schema.failed_events SET retry_count = 0 WHERE retry_count IS NULL;
ALTER TABLE recovery_schema.failed_events ALTER COLUMN retry_count SET NOT NULL;
ALTER TABLE recovery_schema.failed_events ADD COLUMN IF NOT EXISTS source VARCHAR;
SQL
echo "  ✓ recovery_schema ready"

# ----- audit_schema -----
echo "[4/4] Migrating audit_schema..."
$PSQL <<'SQL'
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

ALTER TABLE audit_schema.audit_log ADD COLUMN IF NOT EXISTS batch_id VARCHAR;
ALTER TABLE audit_schema.audit_log ALTER COLUMN batch_id TYPE VARCHAR;
ALTER TABLE audit_schema.audit_log ADD COLUMN IF NOT EXISTS trace_id VARCHAR;
ALTER TABLE audit_schema.audit_log ADD COLUMN IF NOT EXISTS retry_count INT DEFAULT 0;
UPDATE audit_schema.audit_log SET retry_count = 0 WHERE retry_count IS NULL;
ALTER TABLE audit_schema.audit_log ALTER COLUMN retry_count SET NOT NULL;
ALTER TABLE audit_schema.audit_log ADD COLUMN IF NOT EXISTS source VARCHAR;
SQL
echo "  ✓ audit_schema ready"

echo ""
echo "========================================="
echo "All 4 schemas migrated successfully!"
echo "========================================="
