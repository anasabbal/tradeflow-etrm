# Reporting Service (`reporting-service`)

> Scaffold only: this module contains the Spring Boot entry point (`com.tradeflow.reporting.ReportingApplication`), dependencies and configuration. The behaviour below is **planned**, as described in the engineering specification.

## Purpose and responsibility

The Openlink Endur *Report Builder* mimic. It exposes parameterised report endpoints aggregating trade, valuation, settlement and market price data. Reports return JSON by default and CSV via the `Accept` header. Heavy aggregation uses prepared SQL views, and results are filtered by role from the JWT claims (a trader sees only their portfolio, a risk officer sees all).

## Planned REST endpoints

- `GET /api/v1/reports/positions`
- `GET /api/v1/reports/pnl`
- `GET /api/v1/reports/blotter`
- `GET /api/v1/reports/exposure`

## Kafka

**Produces**

- none

**Consumes**

- none in this scaffold (reads shared read-only views; the spec also lists `trades.events`, `valuation.results`, `market-data.prices` and `settlement.events` as sources)

## Data owned

- read-only views over trades, valuations, settlements and market prices; owns no tables

Flyway migrations: No.

## Dependencies

| Dependency | Why |
|---|---|
| spring-boot-starter-web | REST API |
| spring-boot-starter-jdbc + postgresql | SQL views and aggregation |
| spring-boot-starter-oauth2-resource-server | JWT validation and role-based filtering |
| jackson-dataformat-csv | CSV output |
| actuator, micrometer-registry-prometheus, micrometer-tracing-bridge-otel, opentelemetry-exporter-otlp | metrics and distributed tracing |
| spring-boot-starter-test, Testcontainers | testing |

## Configuration

Port: **8085**. Settings live in `src/main/resources/application.yml` and can be overridden with environment variables:

| Variable | Default |
|---|---|
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://localhost:5432/tradeflow?currentSchema=public` |
| `SPRING_DATASOURCE_USERNAME` / `SPRING_DATASOURCE_PASSWORD` | `tradeflow` / `tradeflow` |

**Security note:** with the resource-server starter on the classpath and no `SecurityConfig`, `/actuator/**` returns 401. Docker health checks accept 401 as "up"; Prometheus scraping needs a permit rule or credentials.

## Run and test

```bash
# from the repository root
mvn -pl reporting-service -am clean package -DskipTests      # build
docker compose up -d postgres redpanda redis            # start the infrastructure this service needs
mvn -pl reporting-service spring-boot:run                     # run locally
mvn -pl reporting-service -am test                            # tests (Testcontainers needs Docker)
```

Health: `http://localhost:8085/actuator/health` - Prometheus: `http://localhost:8085/actuator/prometheus`

## Observability (planned)

- report generation latency (proposed - not in the original spec)

Traces are exported over OTLP (`opentelemetry-exporter-otlp`); metrics are exposed to Prometheus through Micrometer.
