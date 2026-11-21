# ADR-0001: Event sourcing for the trade lifecycle

- **Status:** Accepted
- **Date:** 2026-11-20
- **Deciders:** project author

## Context

An energy trading platform needs a complete, tamper-evident history of every trade state change for audit, post-incident root-cause analysis and regulatory reporting. Several services (valuation, settlement, reporting, gateway) react to trade changes and must stay decoupled from the service that creates trades.

## Decision

Model the trade lifecycle as an immutable stream of events on Kafka (`trades.events`, keyed by `tradeId`). Only the owning service writes each kind of event (single-writer principle): trade-capture creates and amends trades, valuation computes MTM, settlement moves trades to a terminal state. The `trades` table carries a `version` that increments on every amendment, and consumers must be idempotent.

## Consequences

- Full audit trail and replayability; new consumers can be added without touching producers.
- Ordering is guaranteed per trade through the partition key.
- Consumers become eventually consistent and must handle duplicates and redelivery (for example the unique `(trade_id, valuation_date)` constraint on valuations).
- Event schemas must be versioned and documented (`docs/schemas/`).

## Alternatives considered

- **Shared database between services:** simpler, but couples services and loses the event history.
- **Plain CRUD with change-data-capture:** keeps current state only and makes intent harder to recover.
