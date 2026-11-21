# Valuation Service (`valuation-service`)

> Scaffold only: this module contains the Spring Boot entry point (`com.tradeflow.valuation.ValuationApplication`), dependencies and configuration. The behaviour below is **planned**, as described in the engineering specification.

## Purpose and responsibility

Compute-heavy service and the **only** one that computes mark-to-market. For each trade entering `BOOKED` or `AMENDED` state it fetches the matching market price (Redis for the latest day, PostgreSQL for history) and computes `MTM = (markPrice - tradePrice) * volumeMW * hours`. Valuation is idempotent: re-running for the same `tradeId` and `valuationDate` yields the same result without creating duplicate rows.

## Planned REST endpoints

- `GET /api/v1/trades/{id}/valuation?asOf=...`
- `GET /api/v1/valuations?portfolio=...&date=...`

## Kafka

**Produces**

- `valuation.results` (key = `tradeId`): `VALUATION_COMPUTED`

**Consumes**

- `trades.events` (`TRADE_BOOKED`, `TRADE_AMENDED`)
- `market-data.prices` (`PRICE_UPDATED`)

## Data owned

- `valuations` - unique `(trade_id, valuation_date)`

Flyway migrations: Yes - empty `db/migration/` folder.

## Dependencies

| Dependency | Why |
|---|---|
| spring-kafka | consume trade and price events, publish results |
| spring-boot-starter-data-redis | read latest prices |
| spring-boot-starter-data-jpa + flyway + postgresql | persist valuations |
| spring-boot-starter-web | REST API and actuator HTTP server (added so the service stays up and is scrapeable) |
| actuator, micrometer-registry-prometheus, micrometer-tracing-bridge-otel, opentelemetry-exporter-otlp | metrics and distributed tracing |
| spring-boot-starter-test, Testcontainers | testing |

## Configuration

Port: **8083**. Settings live in `src/main/resources/application.yml` and can be overridden with environment variables:

| Variable | Default |
|---|---|
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://localhost:5432/tradeflow?currentSchema=valuations` |
| `SPRING_DATASOURCE_USERNAME` / `SPRING_DATASOURCE_PASSWORD` | `tradeflow` / `tradeflow` |
| `SPRING_KAFKA_BOOTSTRAP_SERVERS` | `localhost:19092` (`redpanda:9092` inside Docker) |
| `SPRING_DATA_REDIS_HOST` | `localhost` |
| `SPRING_DATA_REDIS_PORT` | `6379` |



## Run and test

```bash
# from the repository root
mvn -pl valuation-service -am clean package -DskipTests      # build
docker compose up -d postgres redpanda redis            # start the infrastructure this service needs
mvn -pl valuation-service spring-boot:run                     # run locally
mvn -pl valuation-service -am test                            # tests (Testcontainers needs Docker)
```

Health: `http://localhost:8083/actuator/health` - Prometheus: `http://localhost:8083/actuator/prometheus`

## Observability (planned)

- `tradeflow_valuation_latency_seconds` (timer, p99) - above 5 s blocks trader decision-making

Traces are exported over OTLP (`opentelemetry-exporter-otlp`); metrics are exposed to Prometheus through Micrometer.
