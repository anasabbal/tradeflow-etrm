package com.tradeflow.common.enums;


/**
 * Enum: Delivery shape. BASELOAD = 24h/day, PEAKLOAD = 16h weekdays only, DAY = single day, WEEKEND, MONTH, SEASON..
 *
 * <h3>Design notes</h3>
 * <p>Determines how volume is distributed across hours for MTM calculation.</p>
 *
 * <p><b>IMPLEMENTATION TASK</b>: see method-level Javadoc for what to do.
 * Method bodies are intentionally left as TODO.</p>
 */

public enum Instrument {
    BASELOAD, PEAKLOAD, DAY, WEEKEND, MONTH, SEASON
}
