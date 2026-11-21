# TradeFlow ETRM

Cloud-native, event-sourced **Energy Trading and Risk Management** platform for European power and gas markets: a six-microservice Java 17 / Spring Boot 3 monorepo modelling the trade lifecycle (capture, valuation, settlement, reporting). It is designed to mirror the enterprise trading stack - Apache Kafka, Kubernetes, DevSecOps and an Openlink Endur-style reporting layer.

> **Status: scaffold.** The Maven structure, dependencies, configuration, Docker/Kubernetes/Terraform skeletons, CI pipelines and documentation are in place. Business logic (controllers, entities, Kafka listeners, SQL schemas) is intentionally **not** implemented - each service has only a `@SpringBootApplication` entry point.

## Architecture

```
                 +------------------- bff-gateway -------------------+
                 |  JWT auth | Resilience4j | aggregation | WebSocket  |
                 +----+------------+-------------+-------------+------+
                      |            |             |             |
               trade-capture   reporting    market-data    valuation
                      |            |             |             |
              +-------+------------+-------------+-------------+-----+
              | Kafka: trades.events | market-data.prices |           |
              |        valuation.results | settlement.events         |
              +-------+-------------------------------------+--------+
                      |                                     |
              PostgreSQL 15 (per-service schemas)     Redis (price cache)
                                    settlement
```

Details: [docs/architecture.md](docs/architecture.md).

## Tech stack

Java 17 - Spring Boot 3.3 - Spring Cloud Gateway - Apache Kafka (Redpanda locally, MSK on AWS) - PostgreSQL 15 + Flyway - Redis 7 - Resilience4j - Micrometer / OpenTelemetry / Prometheus / Grafana - Docker - Kubernetes (Kind locally, EKS on AWS) - ArgoCD - GitHub Actions - Terraform - Testcontainers - SonarQube / OWASP Dependency-Check / Trivy / cosign.

## Services

| Service | Port | Responsibility |
|---|---|---|
| [trade-capture-service](trade-capture-service/README.md) | 8081 | Create/amend trades, emit `TRADE_BOOKED` |
| [market-data-service](market-data-service/README.md) | 8082 | Ingest ENTSO-E prices, publish to Kafka, cache in Redis |
| [valuation-service](valuation-service/README.md) | 8083 | Mark-to-market computation |
| [settlement-service](settlement-service/README.md) | 8084 | Settlement workflow, terminal trade state |
| [reporting-service](reporting-service/README.md) | 8085 | Position, P&L, blotter reports (Endur Report Builder mimic) |
| [bff-gateway](bff-gateway/README.md) | 8080 | Auth, routing, aggregation, WebSocket fan-out |

## Quick start

```bash
make build        # mvn clean package -DskipTests
make up           # docker compose up --build -d
make ps           # check container health
```

Full instructions, prerequisites and troubleshooting: **[SETUP.md](SETUP.md)**.

| URL | What |
|---|---|
| http://localhost:8080 | BFF gateway |
| http://localhost:9090 | Prometheus |
| http://localhost:3000 | Grafana (admin / admin) |
| http://localhost:18081 | Redpanda Schema Registry |

## Repository structure

```
tradeflow-etrm/
|-- pom.xml                    parent / aggregator POM (pins versions)
|-- docker-compose.yml         local stack
|-- Makefile                   build / run shortcuts
|-- README.md  SETUP.md
|-- trade-capture-service/     (each service: pom.xml, Dockerfile, README.md, src/)
|-- market-data-service/
|-- valuation-service/
|-- settlement-service/
|-- reporting-service/
|-- bff-gateway/
|-- infra/
|   |-- k8s/                   Kustomize manifests per service
|   |-- terraform/             AWS skeleton (VPC, EKS, RDS, MSK, ...)
|   |-- postgres/              schema bootstrap for local Postgres
|   |-- prometheus.yml
|   `-- grafana/dashboards/
|-- .github/workflows/         build-and-deploy, nightly-security-scan
|-- seed/                      reference data + trade generator (placeholders)
`-- docs/                      architecture, api, schemas, adr
```

## Documentation

- [SETUP.md](SETUP.md) - prerequisites, build, run, troubleshooting
- [docs/architecture.md](docs/architecture.md) - architecture, Kafka topics, trade lifecycle, data layer
- [docs/adr/](docs/adr/) - Architecture Decision Records
- Per-service READMEs (linked in the table above)

## Roadmap

1. Weekend 1 - trade capture CRUD, Flyway schema, first `TRADE_BOOKED` event on Kafka.
2. Weekend 2 - ENTSO-E market data ingestion, valuation consumer and MTM.
3. Weekend 3 - reporting endpoints (JSON/CSV), BFF gateway with JWT, WebSocket blotter.
4. Weekend 4 - Kubernetes manifests, CI/CD with ArgoCD, Prometheus/Grafana dashboards.
5. **Phase 2** - an MCP server exposing trade queries and report specifications to Claude Code (JSON-RPC).
