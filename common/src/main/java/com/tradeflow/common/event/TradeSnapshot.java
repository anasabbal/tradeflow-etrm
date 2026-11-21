package com.tradeflow.common.event;

import java.time.Instant;
import com.tradeflow.common.enums.*;
/**
 * Immutable snapshot of a trade used inside all trade-related Kafka events. Keeping a snapshot in the event (instead of just a tradeId) lets downstream consumers process the event without an extra DB round-trip.
 *
 * <h3>Design notes</h3>
 * <p>Java 17 record — immutable and serialisable by Jackson out of the box. All monetary fields use BigDecimal.</p>
 *
 * <p><b>IMPLEMENTATION TASK</b>: see method-level Javadoc for what to do.
 * Method bodies are intentionally left as TODO.</p>
 */

public record TradeSnapshot(
    String tradeId,
    String counterparty,
    String portfolio,
    Product product,
    Instrument instrument,
    Instant deliveryStart,
    Instant deliveryEnd,
    String deliveryArea,
    java.math.BigDecimal volumeMW,
    java.math.BigDecimal price,
    String currency,
    Direction direction,
    String trader,
    String status,
    Long version
) { }
