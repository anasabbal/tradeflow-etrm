# Settlement Service (`settlement-service`)

> Scaffold only: this module contains the Spring Boot entry point (`com.tradeflow.settlement.SettlementApplication`), dependencies and configuration. The behaviour below is **planned**, as described in the engineering specification.

## Purpose and responsibility

Moves a trade from a valued state to a terminal settled state and is the only service that does so. A nightly job finds trades whose delivery end is in the past and emits `SETTLEMENT_PENDING`; once an operator (or test stub) confirms, it computes the final amount from realised vs contracted price, emits `TRADE_SETTLED` and writes the settlement row. Mirrors the Openlink Endur settlement workflow.

## Planned REST endpoints

- `GET /api/v1/settlements/{id}`
- `POST /api/v1/settlements/{id}/confirm`

## Kafka

**Produces**

- `settlement.events` (key = `tradeId`)
- `trades.events` (`TRADE_SETTLED`)

**Consumes**

- `valuation.results` (`VALUATION_COMPUTED`)

## Data owned

- `settlements` - one row per settled trade

Flyway migrations: Yes - empty `db/migration/` folder.

## Dependencies

| Dependency | Why |
|---|---|
| spring-kafka | consume valuations, publish settlement events |
| spring-boot-starter-data-jpa + flyway + postgresql | persist settlements |
| spring-boot-starter-quartz | nightly settlement-pending job |
| spring-boot-starter-web | REST API and actuator HTTP server |
| actuator, micrometer-registry-prometheus, micrometer-tracing-bridge-otel, opentelemetry-exporter-otlp | metrics and distributed tracing |
| spring-boot-starter-test, Testcontainers | testing |

## Configuration

Port: **8084**. Settings live in `src/main/resources/application.yml` and can be overridden with environment variables:

| Variable | Default |
|---|---|
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://localhost:5432/tradeflow?currentSchema=settlements` |
| `SPRING_DATASOURCE_USERNAME` / `SPRING_DATASOURCE_PASSWORD` | `tradeflow` / `tradeflow` |
| `SPRING_KAFKA_BOOTSTRAP_SERVERS` | `localhost:19092` (`redpanda:9092` inside Docker) |



## Run and test

```bash
# from the repository root
mvn -pl settlement-service -am clean package -DskipTests      # build
docker compose up -d postgres redpanda redis            # start the infrastructure this service needs
mvn -pl settlement-service spring-boot:run                     # run locally
mvn -pl settlement-service -am test                            # tests (Testcontainers needs Docker)
```

Health: `http://localhost:8084/actuator/health` - Prometheus: `http://localhost:8084/actuator/prometheus`

## Observability (planned)

- `tradeflow_settlement_queue_depth` (gauge) - above 50 indicates a settlement job failure

Traces are exported over OTLP (`opentelemetry-exporter-otlp`); metrics are exposed to Prometheus through Micrometer.
