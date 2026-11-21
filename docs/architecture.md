# TradeFlow ETRM - Architecture

## 1. Logical architecture

Six microservices communicate through **REST** for synchronous queries and **Kafka** for asynchronous trade and market data events.

```
                 +------------------- bff-gateway -------------------+
                 |  HTTPS | JWT (Spring Security) | Resilience4j      |
                 |  Rate limiting | Circuit breaker | WebSocket push   |
                 +----+------------+-------------+-------------+------+
                      |            |             |             |
               trade-capture   reporting    market-data    valuation        settlement
                      |            |             |             |                |
              +-------+------------+-------------+-------------+----------------+---+
              | Kafka: trades.events | market-data.prices | valuation.results |       |
              |        settlement.events                                             |
              +-------+--------------------------------------------+-----------------+
                      |                                            |
              PostgreSQL 15 (trades, prices,                Redis (latest 24h of
              valuations, settlements schemas)              prices per area, TTL 60 min)
```

- Each service owns its own schema in a shared PostgreSQL cluster locally and its own database instance in production.
- Redis is a hot cache for the latest market prices, reducing database load on the valuation service.
- The BFF gateway is the only externally exposed service.

### Single-writer principle

| Concern | Sole owner |
|---|---|
| Creating trades | trade-capture-service |
| Computing mark-to-market | valuation-service |
| Moving a trade to a terminal state | settlement-service |

This keeps the event source consistent and makes root-cause analysis tractable.

## 2. Kafka topic catalog

Topics use JSON values, named `<domain>.<entity>`. The partition key is the natural identifier of the aggregate so events for one aggregate stay ordered on one partition.

| Topic | Key | Producers | Consumers | Partitions |
|---|---|---|---|---|
| `trades.events` | `tradeId` | trade-capture, settlement | valuation, reporting | 6 |
| `market-data.prices` | `area+day` | market-data | valuation, reporting, bff (WebSocket) | 3 |
| `valuation.results` | `tradeId` | valuation | settlement, reporting, bff (WebSocket) | 6 |
| `settlement.events` | `tradeId` | settlement | reporting, bff (WebSocket) | 3 |

Event types: `TRADE_BOOKED`, `TRADE_AMENDED`, `TRADE_SETTLED`, `PRICE_UPDATED`, `VALUATION_COMPUTED`, `SETTLEMENT_PENDING`.

## 3. Trade lifecycle

```
DRAFT -> BOOKED -> VALIDATED -> CONFIRMED -> SETTLED
```

1. **DRAFT** - trade persisted by trade-capture after validation; `TRADE_BOOKED` is emitted.
2. **BOOKED** - valuation computes MTM (`(markPrice - tradePrice) * volumeMW * hours`); amendments emit `TRADE_AMENDED` and bump `version`.
3. **VALIDATED / CONFIRMED** - intermediate states named in the specification's lifecycle; the transitions between them are not detailed there and need to be defined when implementing.
4. **SETTLED** - after delivery ends, a nightly job emits `SETTLEMENT_PENDING`; on confirmation settlement emits `TRADE_SETTLED`.

## 4. Data layer

| Store | Role |
|---|---|
| PostgreSQL 15 | Durable system of record; schemas versioned with Flyway |
| Redis 7 | Latest market prices, sub-millisecond reads, 60-minute TTL |
| Kafka (Redpanda / MSK) | Immutable event log and audit trail of every state transition |

Key tables (planned):

| Table | Owner | Notes |
|---|---|---|
| `trades` | trade-capture | event-sourced, `version` increments on amend |
| `market_prices` | market-data | partitioned by `delivery_day`, PK `(area, delivery_day, hour)` |
| `valuations` | valuation | idempotent, unique `(trade_id, valuation_date)` |
| `settlements` | settlement | one row per settled trade |
| `counterparties` | trade-capture | reference data (code, name, country, active) |
| `portfolios` | trade-capture | reference data (code, name, desk, currency) |

## 5. Observability

Micrometer exposes Prometheus metrics, OpenTelemetry provides distributed traces, and Grafana is the single pane of glass. Planned business KPIs:

| Metric | Type | Meaning |
|---|---|---|
| `tradeflow_trades_booked_total` | counter | trading throughput |
| `tradeflow_valuation_latency_seconds` | timer (p99) | above 5 s blocks decision-making |
| `tradeflow_settlement_queue_depth` | gauge | above 50 indicates settlement job failure |
| `tradeflow_market_data_freshness_seconds` | gauge | stale prices break valuation |
| `tradeflow_trade_validation_failures_total` | counter | upstream data quality |
| `tradeflow_position_violation_total` | counter | risk limit breaches, should be 0 (owning service not yet assigned) |

## 6. Security (planned)

Build time: SpotBugs, SonarQube, OWASP Dependency-Check, Trivy, cosign signing. Runtime: Secrets Manager with IRSA, default-deny network policies, Kyverno/OPA admission policies. Application: Bean Validation, OAuth2 resource server with method-level authorisation, audit log of every state change.

## 7. Production target (AWS, eu-west-1)

EKS (Fargate), ECR, MSK, RDS for PostgreSQL, ElastiCache for Redis, S3, ALB, Route 53, Secrets Manager, KMS, IAM/IRSA, CloudWatch; provisioned with Terraform (`infra/terraform/`) and deployed with GitHub Actions + ArgoCD. Estimated cost: about 170-220 USD/month for a single-AZ demo, 340+ USD/month for a 3-AZ setup.
