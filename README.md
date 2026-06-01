# Event Ingestion & Processing Platform

A microservices platform for ingesting, processing, routing, and delivering events at scale. Events flow through a synchronous validation/enrichment pipeline, then hand off to an asynchronous, queue-backed delivery stage with failure recovery and end-to-end auditing.

Built with **Java 21** and **Spring Boot 4.0**, packaged as a Maven multi-module project, observable via OpenTelemetry/Prometheus/Grafana, and deployable to **AWS ECS Fargate** with Terraform.

## Architecture

Events are received by the intake service and passed stage-to-stage over HTTP. The routing service is the seam between the synchronous and asynchronous halves: it persists the event to a PostgreSQL delivery queue and publishes it to RabbitMQ, from which the delivery service consumes and dispatches.

```
                  synchronous HTTP chain                          async
                                                                    
  intake ──▶ validation ──▶ normalization ──▶ enrichment ──▶ routing ──┐
  (8081)      (8082)          (8083)            (8084)        (8085)    │
                                                                        │ RabbitMQ
                                                                        │ + DB queue
                                                                        ▼
                                                                    delivery (8086)
                                                                        │
                                                              on failure ▼
                                                                    recovery (8087)

  every stage ───────────────────▶ audit (8088)  [PostgreSQL + Redis cache]
```

## Services

| Service | Port | Responsibility |
|---|---|---|
| **intake** | 8081 | Accepts single events (`POST /events`) and bulk uploads (CSV/XLSX/JSON, `POST /events/bulk`); persists receipts |
| **validation** | 8082 | Validates event schema and required fields |
| **normalization** | 8083 | Normalizes field formats |
| **enrichment** | 8084 | Enriches the payload with contextual data |
| **routing** | 8085 | Applies rule-based routing; enqueues to PostgreSQL and publishes to RabbitMQ with priority |
| **delivery** | 8086 | Consumes the queue; idempotent dispatch; hands failures to recovery |
| **recovery** | 8087 | Stores failed events; exposes a retry API |
| **audit** | 8088 | Records a per-stage audit trail; Redis-cached read API |
| **common** | — | Shared library: `EventEnvelope` contract, DTOs, and MDC/trace-propagation filters used by all services |

## Tech Stack

- **Java 21**, **Spring Boot 4.0.0**, **Spring Cloud 2024.0.0**
- **PostgreSQL** (per-service schemas), **RabbitMQ** (delivery queue), **Redis** (audit cache)
- **OpenTelemetry → Tempo** (traces), **Micrometer → Prometheus** (metrics), **Logback/Loki** (structured logs), **Grafana** (dashboards)
- **Maven** multi-module build; **Docker Compose** for local runs; **Terraform** for AWS ECS Fargate

## Getting Started

### Prerequisites

- JDK 21 and Maven
- Docker (for RabbitMQ, Redis, and the observability stack)
- A PostgreSQL instance on `localhost:5432` with a database named `eventdb` (the database must exist; each service creates its own schema/tables on startup)

### Build

```bash
mvn clean install                      # build and test all modules
mvn -pl <service> -am clean package    # build a single service (+ the common module)
```

### Run locally

```bash
# 1. Create the shared Docker network (once)
docker network create event-platform-network

# 2. Start infrastructure (RabbitMQ + Redis)
docker-compose -f infra/docker-compose.yml up -d

# 3. Start the observability stack (Prometheus, Grafana, Loki, Tempo)
docker-compose -f observability/docker-compose.yml up -d

# 4. Build and start all services
docker-compose up --build
```

Grafana is available at <http://localhost:3000> and the RabbitMQ management UI at <http://localhost:15672>.

### Run tests

```bash
mvn test                                              # all modules
mvn -pl <service> test                                # a single service
mvn -pl <service> test -Dtest=ClassName#methodName    # a single test
```

## Observability

Every service exports OTLP traces to Tempo, Prometheus metrics at `/actuator/prometheus`, and structured JSON logs collected into Loki. Correlation and trace IDs are propagated across HTTP calls, async tasks, and RabbitMQ messages via the shared MDC filters in the `common` module, so a single event can be traced end-to-end across all services.

## Deployment

The `infrastructure/` directory contains Terraform for AWS ECS Fargate. Services discover each other through ECS Service Connect; only the intake service is publicly exposed (behind an ALB). PostgreSQL (RDS), RabbitMQ (Amazon MQ), and Redis (ElastiCache) are provisioned as managed services, with credentials sourced from AWS Secrets Manager.

## Repository Layout

```
common/              Shared models, contracts, and MDC/tracing filters
intake-service/      …each service follows: controller → service → client/persistence
validation-service/
normalization-service/
enrichment-service/
routing-service/
delivery-service/
recovery-service/
audit-service/
infra/               Docker Compose for RabbitMQ + Redis
observability/       Docker Compose for Prometheus, Grafana, Loki, Tempo
infrastructure/      Terraform (AWS ECS Fargate)
```

See [CLAUDE.md](CLAUDE.md) for deeper architectural notes and common gotchas.
