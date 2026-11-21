package com.tradeflow.tradecapture.model;



import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

import com.tradeflow.common.enums.*;


@Entity
@Table(name = "trades",
    indexes = {
        @Index(name = "idx_trades_portfolio",    columnList = "portfolio"),
        @Index(name = "idx_trades_status",       columnList = "status"),
        @Index(name = "idx_trades_delivery",     columnList = "delivery_start,delivery_end"),
        @Index(name = "idx_trades_counterparty", columnList = "counterparty")
    })
public class TradeEntity {

    @Id
    @Column(name = "trade_id", length = 32, nullable = false)
    private String tradeId;                              // business key, e.g. TRD-2026-0001

    @Column(name = "trade_date", nullable = false)
    private LocalDate tradeDate;

    @Column(name = "counterparty", length = 32, nullable = false)
    private String counterparty;                         // FK to counterparties.code (no JPA rel for simplicity)

    @Column(name = "portfolio", length = 32, nullable = false)
    private String portfolio;                            // FK to portfolios.code

    @Enumerated(EnumType.STRING)
    @Column(name = "product", length = 16, nullable = false)
    private Product product;                             // POWER, GAS, CO2

    @Enumerated(EnumType.STRING)
    @Column(name = "instrument", length = 16, nullable = false)
    private Instrument instrument;                       // BASELOAD, PEAKLOAD, DAY, WEEKEND, MONTH, SEASON

    @Column(name = "delivery_start", nullable = false)
    private Instant deliveryStart;

    @Column(name = "delivery_end", nullable = false)
    private Instant deliveryEnd;

    @Column(name = "delivery_area", length = 16, nullable = false)
    private String deliveryArea;                         // EIC code, e.g. 10YPT-REN------W

    @Column(name = "volume_mw", nullable = false, precision = 12, scale = 3)
    private BigDecimal volumeMW;                         // BigDecimal — never double for money/volume

    @Column(name = "price", nullable = false, precision = 12, scale = 6)
    private BigDecimal price;                            // EUR/MWh

    @Column(name = "currency", length = 3, nullable = false)
    private String currency;                             // ISO 4217, e.g. EUR

    @Enumerated(EnumType.STRING)
    @Column(name = "direction", length = 4, nullable = false)
    private Direction direction;                         // BUY, SELL

    @Column(name = "trader", length = 64, nullable = false)
    private String trader;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 16, nullable = false)
    private TradeStatus status;                          // DRAFT, BOOKED, VALIDATED, CONFIRMED, SETTLED, CANCELLED

    @Version
    @Column(name = "version", nullable = false)
    private Long version;                                // JPA optimistic locking — auto-increments on amend

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.status == null)  this.status = TradeStatus.BOOKED;
        if (this.version == null) this.version = 0L;
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = Instant.now();
    }
}
