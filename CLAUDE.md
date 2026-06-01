# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Test

Java 21, Spring Boot 4.0.0, Maven multi-module project (root `pom.xml` aggregates all modules).

```bash
mvn clean install                       # build + test all modules
mvn -pl <service> -am clean package     # build one service (+ its `common` dependency)
mvn test                                # run all tests
mvn -pl <service> test                  # test one service
mvn -pl <service> test -Dtest=ClassName#methodName   # run a single test
```

`common` must be built before any service — always include `-am` when building a single module.

## Local Run

PostgreSQL must run separately on `localhost:5432` with database `eventdb` (the database itself must pre-exist). Hibernate `ddl-auto` is `none`; instead, each stateful service ships a `schema.sql` (`CREATE SCHEMA IF NOT EXISTS …` + tables) that runs on every startup because `spring.sql.init.mode: always`. So schemas/tables auto-create, but the `eventdb` database does not.

```bash
docker network create event-platform-network          # first time only
docker-compose -f infra/docker-compose.yml up -d       # RabbitMQ + Redis
docker-compose -f observability/docker-compose.yml up -d   # Prometheus, Grafana, Loki, Tempo
docker-compose up --build                              # all 8 services
```

Grafana: `localhost:3000`. RabbitMQ management: `localhost:15672`.

## Architecture

### Synchronous HTTP pipeline

Events flow through services via blocking `RestClient` calls, each stage calling the next:

```
intake (8081) → validation (8082) → normalization (8083) → enrichment (8084) → routing (8085)
```

`routing-service` is the seam between sync and async: it writes the event to a PostgreSQL **delivery queue table** AND publishes to **RabbitMQ** with a priority-bearing routing key. `delivery-service` (8086) consumes from RabbitMQ asynchronously, enforces idempotency via DB state, and on failure calls `recovery-service` (8087). Every stage independently calls `audit-service` (8088) at start/transition/completion.

| Service | Port | Endpoint | Role |
|---|---|---|---|
| intake | 8081 | `/events`, `/events/bulk` | Single + bulk (CSV/XLSX/JSON) ingestion; persists receipts |
| validation | 8082 | `/validate` | Schema/required-field checks |
| normalization | 8083 | `/normalize` | Field normalization |
| enrichment | 8084 | `/enrich` | Payload enrichment |
| routing | 8085 | `/route` | Rule-based destination + enqueue to DB & RabbitMQ |
| delivery | 8086 | `/deliver` | RabbitMQ consumer; idempotent delivery; failure handoff |
| recovery | 8087 | `/failures` | Stores `FailedEventRecord`; retry API |
| audit | 8088 | `/audit` | Per-stage audit log; Redis-cached; read API |

### `common` module — the shared contract

All services depend on `common`. `EventEnvelope` (in `common/model`) is the canonical object passed through the entire pipeline — `eventId`, `producerId`, `eventType`, `EventMetadata` (correlation/batch/trace IDs, `attributes` map, retry count), and `EventPayload`. Inter-service responses use `common/contract` DTOs (`ValidationResult`, `NormalizationResult`, `DeliveryResult`).

**MDC / trace propagation is centralized in `common/filter`** and must not be bypassed when adding new call paths:
- `MdcLoggingFilter` — populates MDC from inbound HTTP headers
- `MdcClientHttpRequestInterceptor` — propagates MDC onto outbound `RestClient` calls (wired in each service's `config/RestClientConfig`)
- `MdcTaskDecorator` — propagates MDC across async/thread boundaries
- `MdcMessagePostProcessor` — propagates MDC onto RabbitMQ messages

When adding a new downstream client or async task, reuse these so correlation/trace IDs survive the hop.

### Per-service layering (consistent across all modules)

`controller/` (REST) → `service/` (logic + audit calls) → `client/` (RestClient to downstream + audit) and `persistence/` (wrapper over JPA `repository/`). `config/RestClientConfig` wires the MDC interceptor; `exception/GlobalExceptionHandler` maps `InvalidEventException`/`ProcessingException` to HTTP responses; `model/` holds service-local JPA entities.

### Routing rules

Declared in `routing-service/src/main/resources/application.yml` under `routing.rules` — each rule has an optional `when` map (matched against `producerId` or `metadata.attributes.priority`) and a `destination`. Unmatched events fall through to `DEFAULT_DESTINATION`. Separately, the RabbitMQ routing key + message priority is chosen from `producerId` substring (`mobile`=10, `api`=5, else default=1) in `EventRoutingService.enqueueForDelivery`.

### Bulk upload

`intake-service` accepts bulk files (`POST /events/bulk`) and also auto-processes files dropped into `bulk/pending/`. `BulkFileScheduler` polls that directory every `bulk.upload.poll-interval-ms`; each row becomes an `EventEnvelope` fed through the normal ingestion path, then the file moves to `bulk/processed/` or `bulk/failed/`.

## Observability

Every service exports OTLP traces to Tempo (`MANAGEMENT_OTLP_TRACING_ENDPOINT`, default `localhost:4318`), Prometheus metrics at `/actuator/prometheus`, and structured JSON logs (logstash-logback-encoder) collected by Promtail into Loki. Sampling is 100% (`management.tracing.sampling.probability: 1.0`).

## AWS deployment

`infrastructure/` is Terraform for ECS Fargate. Services discover each other via ECS Service Connect under the `<project>.local` namespace (URLs built in `main.tf` `local.service_urls`). Only `intake-service` is public (behind an ALB); all others are internal. DB/RabbitMQ/Redis credentials come from AWS Secrets Manager, injected as ECS secret env vars. The per-service flags `has_db`/`has_rabbitmq`/`has_redis`/`has_efs`/`is_public` in `var.services` drive which resources each task wires up.

## Common Gotchas

- **`event-platform-network` is an external Docker network.** All three compose files attach to it but none create it. Run `docker network create event-platform-network` once first, or every `docker-compose up` fails immediately.
- **Postgres is not in any compose file.** Services point at `host.docker.internal:5432` and expect a host-run Postgres with the `eventdb` database already created. Schemas and tables are created by each service's `schema.sql` at startup, but the database is not.
- **Only 4 services touch the database.** `intake`, `audit`, `delivery`, and `recovery` have `schema.sql` and a datasource. `validation`, `normalization`, and `enrichment` are stateless — they configure no datasource, so don't add JPA entities/repositories to them expecting a DB to exist.
- **`routing` and `delivery` share `delivery_schema` and the delivery-queue table.** routing-service writes rows that delivery-service polls/reads — they are coupled through that table, not just RabbitMQ. A schema change to the queue table affects both services.
- **`routing-service` ships its own `application-db.yml` that shadows `common`'s.** Every other DB service inherits `common/src/main/resources/application-db.yml` (pool size 10, plain `DB_HOST`/`DB_PORT`). routing-service overrides it with a local copy (pool size 5, a `DB_URL` placeholder, `delivery_schema`). Editing the shared `common` file will not change routing-service.
- **RabbitMQ host has two accepted env-var names.** The YAML default placeholder is `RABBITMQ_HOST`, but compose sets `SPRING_RABBITMQ_HOST` (Spring relaxed binding). Both resolve, but grep for the wrong one and you'll think it's unset.
- **`schema.sql` runs on every startup and must stay idempotent.** Use `IF NOT EXISTS` / `ADD COLUMN IF NOT EXISTS` for any DDL you add, since `spring.sql.init.mode: always` re-executes the whole file each boot.
- **Delivery failure is simulated by an attribute.** `EventDeliveryService.simulateDelivery` throws if `metadata.attributes.forceFailure == "FAIL"`. Useful for exercising the recovery path locally; there is no real external delivery yet.
- **The pipeline is synchronous and unguarded up to routing.** Each stage blocks on a `RestClient` call to the next; there is no retry library or circuit breaker anywhere (`grep` finds no resilience4j/spring-retry), so one slow/down service stalls the whole intake→validation→…→routing chain. Async decoupling and the failure-recovery path (RabbitMQ + DB queue + recovery-service) only begin at the RabbitMQ hop into delivery-service.
