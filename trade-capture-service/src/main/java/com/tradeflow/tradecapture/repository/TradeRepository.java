package com.tradeflow.tradecapture.repository;

import com.tradeflow.tradecapture.model.TradeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface TradeRepository extends JpaRepository<TradeEntity, String> {
}
