package com.tradeflow.tradecapture.service;


import com.tradeflow.common.dto.TradeRequest;
import com.tradeflow.common.dto.TradeResponse;
import com.tradeflow.tradecapture.repository.TradeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TradeCaptureService {

    private final TradeRepository tradeRepository;


    public  TradeCaptureService(TradeRepository tradeRepository) {
        this.tradeRepository = tradeRepository;
    }

    /**
     * Book a new trade. Steps:
     * <ol>
     *   <li>Reject if tradeId already exists (idempotency) — throw TradeAlreadyExistsException</li>
     *   <li>Validate via TradeValidator — throw InvalidTradeException on failure</li>
     *   <li>Build TradeEntity, set status = BOOKED</li>
     *   <li>Persist via repository.save()</li>
     *   <li>Publish TradeBookedEvent to Kafka</li>
     *   <li>Increment tradesBooked counter</li>
     * </ol>
     * Wrapped in bookingLatency.record(...) for timing.
     */
    @Transactional
    public TradeResponse bookTrade(TradeRequest request) {
        return null;
    }
}
