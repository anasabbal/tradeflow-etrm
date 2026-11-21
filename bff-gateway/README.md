# BFF Gateway (`bff-gateway`)

> Scaffold only: this module contains the Spring Boot entry point (`com.tradeflow.bffgateway.BffGatewayApplication`), dependencies and configuration. The behaviour below is **planned**, as described in the engineering specification.

## Purpose and responsibility

The only externally exposed service. It terminates HTTPS (an AWS ALB in production), validates JWTs, applies Resilience4j circuit breaker and rate limiter policies to upstream calls, pushes live trade and market data events over WebSocket to the React trading blotter, and aggregates responses: one `GET /api/v1/dashboard` call fans out to trade-capture, valuation, reporting and market-data in parallel (4 round trips become 1).

## Planned REST endpoints

- `POST /auth/login`
- `GET /api/v1/dashboard`
- `WS /ws/events`
- proxied: `/api/v1/trades/**`, `/api/v1/prices/**`, `/api/v1/reports/**`

## Kafka

**Produces**

- none

**Consumes**

- spec: `market-data.prices`, `valuation.results`, `settlement.events` for WebSocket fan-out (not wired in this scaffold)

## Data owned

- `users`, `sessions` (per the spec's service matrix)

Flyway migrations: No.

## Dependencies

| Dependency | Why |
|---|---|
| spring-cloud-starter-gateway | routing (reactive) |
| spring-boot-starter-webflux | reactive stack |
| spring-boot-starter-security + oauth2-resource-server | JWT authentication |
| resilience4j-spring-boot3 + resilience4j-reactor + spring-boot-starter-aop | circuit breaker and rate limiting (the spec's `spring-boot2` artifact is incompatible with Boot 3) |
| spring-boot-starter-websocket | live event push |
| actuator, micrometer-registry-prometheus, micrometer-tracing-bridge-otel, opentelemetry-exporter-otlp | metrics and distributed tracing |
| spring-boot-starter-test, Testcontainers | testing |

## Configuration

Port: **8080**. Settings live in `src/main/resources/application.yml` and can be overridden with environment variables:

No environment variables are required yet (the `application.yml` only sets the name, port and actuator exposure).
**Security note:** until a `SecurityConfig` exists, `/actuator/**` returns 401 (the Docker health check accepts that as "up"). Gateway routes and upstream URLs are not configured yet.

## Run and test

```bash
# from the repository root
mvn -pl bff-gateway -am clean package -DskipTests      # build
docker compose up -d postgres redpanda redis            # start the infrastructure this service needs
mvn -pl bff-gateway spring-boot:run                     # run locally
mvn -pl bff-gateway -am test                            # tests (Testcontainers needs Docker)
```

Health: `http://localhost:8080/actuator/health` - Prometheus: `http://localhost:8080/actuator/prometheus`

## Observability (planned)

- Resilience4j circuit breaker and rate limiter metrics (via Micrometer)
- gateway route metrics

Traces are exported over OTLP (`opentelemetry-exporter-otlp`); metrics are exposed to Prometheus through Micrometer.
