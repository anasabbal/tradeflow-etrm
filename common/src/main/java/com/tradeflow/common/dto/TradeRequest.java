package com.tradeflow.common.dto;

public record TradeRequest (
    /** Business trade ID, format TRD-YYYY-NNNN. Unique. */
    String tradeId,
    /** The date the trade was struck. */
    java.time.LocalDate tradeDate,
    /** Code from counterparties table (e.g. EDP). */
    String counterparty,
    /** Code from portfolios table (e.g. PT-PWR-2026). */
    String portfolio,
    /** POWER, GAS or CO2. */
    String product,
    /** BASELOAD, PEAKLOAD, DAY, WEEKEND, MONTH, SEASON. */
    String instrument,
    /** Inclusive. UTC. */
    java.time.Instant deliveryStart,
    /** Exclusive. UTC. Must be after deliveryStart. */
    java.time.Instant deliveryEnd,
    /** EIC code, e.g. 10YPT-REN------W. */
    String deliveryArea,
    /** Positive. For gas, MWh equivalent. */
    java.math.BigDecimal volumeMW,
    /** Positive. EUR per MWh. */
    java.math.BigDecimal price,
    /** ISO 4217, e.g. EUR. */
    String currency,
    /** BUY or SELL. */
    String direction,
    /** Username of the trader. */
    String trader
) {}
