package com.fintech.service;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fintech.model.MarketData;
import com.fintech.repository.MarketDataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

@Service
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.kafka.enabled", havingValue = "true", matchIfMissing = false)
public class MarketDataConsumerService {
    private final MarketDataRepository repository;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "${kafka.topic.market-data}")
    public void consumeMarketData(String message) {
        try {
            MarketData data = objectMapper.readValue(message, MarketData.class);
            repository.save(data);
            log.info("Processed market data for symbol: {}", data.getSymbol());
        } catch (Exception e) {
            log.error("Failed to process market data", e);
        }
    }
} 
