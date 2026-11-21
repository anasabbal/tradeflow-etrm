package com.tradeflow.common.enums;

/**
 * Enum: Lifecycle of a trade. Transitions are forward-only except CANCELLED which can be set from any non-terminal state..
 *
 * <h3>Design notes</h3>
 * <p>Stored as STRING in PostgreSQL via @Enumerated(EnumType.STRING) so audit rows are readable.</p>
 *
 * <p><b>IMPLEMENTATION TASK</b>: see method-level Javadoc for what to do.
 * Method bodies are intentionally left as TODO.</p>
 */

public enum TradeStatus {
    DRAFT, BOOKED, VALIDATED, CONFIRMED, SETTLED, CANCELLED
}

