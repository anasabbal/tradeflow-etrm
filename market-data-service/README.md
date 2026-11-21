# Market Data Service (`market-data-service`)

> Scaffold only: this module contains the Spring Boot entry point (`com.tradeflow.marketdata.MarketDataApplication`), dependencies and configuration. The behaviour below is **planned**, as described in the engineering specification.

## Purpose and responsibility

Inbound feed handler. A scheduled job pulls day-ahead electricity prices from the ENTSO-E Transparency Platform for the Portuguese, German and French bidding zones, converts the XML response to canonical JSON (one price per hour), stores it, caches the latest prices in Redis (60-minute TTL) and publishes them to Kafka. A daily synthetic generator covers TTF-style gas products that ENTSO-E does not provide.

## Planned REST endpoints

- `GET /api/v1/prices/{area}/{day}` - hourly prices for an area and day
- `GET /api/v1/prices/latest?area=...` - latest prices for an area

## Kafka

**Produces**

- `market-data.prices` (key = `area|day`, e.g. `10YPT-REN------W|2026-12-01`)

**Consumes**

- none

## Data owned

- `market_prices` - partitioned by `delivery_day`, primary key `(area, delivery_day, hour)`

Flyway migrations: Yes - empty `db/migration/` folder.

## Dependencies

| Dependency | Why |
|---|---|
| spring-boot-starter-webflux | reactive `WebClient` for the ENTSO-E API |
| jackson-dataformat-xml | parse ENTSO-E XML |
| spring-boot-starter-data-redis | hot cache of latest prices |
| spring-kafka | publish price events |
| spring-boot-starter-data-jpa + flyway + postgresql | partitioned time-series storage |
| `@EnableScheduling` on the main class | hourly ingestion job |
| actuator, micrometer-registry-prometheus, micrometer-tracing-bridge-otel, opentelemetry-exporter-otlp | metrics and distributed tracing |
| spring-boot-starter-test, Testcontainers | testing |

## Configuration

Port: **8082**. Settings live in `src/main/resources/application.yml` and can be overridden with environment variables:

| Variable | Default |
|---|---|
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://localhost:5432/tradeflow?currentSchema=prices` |
| `SPRING_DATASOURCE_USERNAME` / `SPRING_DATASOURCE_PASSWORD` | `tradeflow` / `tradeflow` |
| `SPRING_KAFKA_BOOTSTRAP_SERVERS` | `localhost:19092` (`redpanda:9092` inside Docker) |
| `SPRING_DATA_REDIS_HOST` | `localhost` |
| `SPRING_DATA_REDIS_PORT` | `6379` |
| `ENTSOE_API_TOKEN` | *(planned - not read yet)* |

Requires an ENTSO-E security token (`ENTSOE_API_TOKEN`) once the ingestion job is implemented - see `SETUP.md`.

## Run and test

```bash
# from the repository root
mvn -pl market-data-service -am clean package -DskipTests      # build
docker compose up -d postgres redpanda redis            # start the infrastructure this service needs
mvn -pl market-data-service spring-boot:run                     # run locally
mvn -pl market-data-service -am test                            # tests (Testcontainers needs Docker)
```

Health: `http://localhost:8082/actuator/health` - Prometheus: `http://localhost:8082/actuator/prometheus`

## Observability (planned)

- `tradeflow_market_data_freshness_seconds` (gauge) - stale prices break valuation correctness

Traces are exported over OTLP (`opentelemetry-exporter-otlp`); metrics are exposed to Prometheus through Micrometer.
