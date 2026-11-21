# Trade Capture Service (`trade-capture-service`)

> Scaffold only: this module contains the Spring Boot entry point (`com.tradeflow.tradecapture.TradeCaptureApplication`), dependencies and configuration. The behaviour below is **planned**, as described in the engineering specification.

## Purpose and responsibility

System of record for trade creation. It is the **only** service that creates trade records. It validates incoming trades against the canonical trade schema (counterparty exists, portfolio exists, delivery window valid, volume and price positive), persists them, and emits lifecycle events.

## Planned REST endpoints

- `POST /api/v1/trades` - submit a trade (persisted as `DRAFT`, then `TRADE_BOOKED` is emitted)
- `GET /api/v1/trades/{id}` - current trade state, with valuation snapshot if available
- `PUT /api/v1/trades/{id}/amend` - amend a trade (emits `TRADE_AMENDED`, increments `version`)
- `GET /api/v1/trades?portfolio=...` - list trades by portfolio

## Kafka

**Produces**

- `trades.events` (key = `tradeId`): `TRADE_BOOKED`, `TRADE_AMENDED`

**Consumes**

- none

## Data owned

- `trades` (event-sourced; `version` increments on each amend)
- `counterparties` (reference data, seeded)
- `portfolios` (reference data, one per desk)

Flyway migrations: Yes - empty `db/migration/` folder.

## Dependencies

| Dependency | Why |
|---|---|
| spring-boot-starter-web | REST API |
| spring-boot-starter-data-jpa + postgresql | persistence |
| flyway-core | versioned schema migrations |
| spring-kafka | publishing lifecycle events |
| spring-boot-starter-validation | Bean Validation on request payloads |
| springdoc-openapi | OpenAPI documentation |
| actuator, micrometer-registry-prometheus, micrometer-tracing-bridge-otel, opentelemetry-exporter-otlp | metrics and distributed tracing |
| spring-boot-starter-test, Testcontainers | testing |

## Configuration

Port: **8081**. Settings live in `src/main/resources/application.yml` and can be overridden with environment variables:

| Variable | Default |
|---|---|
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://localhost:5432/tradeflow?currentSchema=trades` |
| `SPRING_DATASOURCE_USERNAME` / `SPRING_DATASOURCE_PASSWORD` | `tradeflow` / `tradeflow` |
| `SPRING_KAFKA_BOOTSTRAP_SERVERS` | `localhost:19092` (`redpanda:9092` inside Docker) |

Flyway migrations live in `src/main/resources/db/migration/` (empty for now).

## Run and test

```bash
# from the repository root
mvn -pl trade-capture-service -am clean package -DskipTests      # build
docker compose up -d postgres redpanda redis            # start the infrastructure this service needs
mvn -pl trade-capture-service spring-boot:run                     # run locally
mvn -pl trade-capture-service -am test                            # tests (Testcontainers needs Docker)
```

Health: `http://localhost:8081/actuator/health` - Prometheus: `http://localhost:8081/actuator/prometheus`

## Observability (planned)

- `tradeflow_trades_booked_total` (counter)
- `tradeflow_trade_validation_failures_total` (counter)
- booking latency timer

Traces are exported over OTLP (`opentelemetry-exporter-otlp`); metrics are exposed to Prometheus through Micrometer.
