package com.tradeflow.common.enums;

/**
 * Enum: Traded commodities. POWER = electricity, GAS = natural gas, CO2 = carbon allowances..
 *
 * <h3>Design notes</h3>
 * <p>Used by trade-capture, market-data, valuation, reporting — must stay in sync across services.</p>
 *
 * <p><b>IMPLEMENTATION TASK</b>: see method-level Javadoc for what to do.
 * Method bodies are intentionally left as TODO.</p>
 */

public enum Product {
    POWER, GAS, CO2
}
