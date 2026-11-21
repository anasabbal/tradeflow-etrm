package com.tradeflow.common.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.math.BigDecimal;
import com.tradeflow.common.event.TradeSnapshot;
/**
 * Response payload for all trade-capture endpoints. Includes computed fields (status, version, timestamps).
 *
 *
 * <p><b>IMPLEMENTATION TASK</b>: see method-level Javadoc for what to do.
 * Method bodies are intentionally left as TODO.</p>
 */

public record TradeResponse(
    /** The tradeId. */
    String tradeId,
    /** BOOKED, VALIDATED, etc. */
    String status,
    /** JPA @Version value. */
    Long version,
    /** createdAt from entity. */
    java.time.Instant bookedAt,
    /** updatedAt from entity. */
    java.time.Instant updatedAt,
    /** Full snapshot of trade fields. */
    TradeSnapshot trade
) {}
