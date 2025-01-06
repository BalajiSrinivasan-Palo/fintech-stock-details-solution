package com.fintech.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fintech.model.MarketData;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

@Service
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.kafka.enabled", havingValue = "true", matchIfMissing = false)
public class MarketDataProducerService {
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${kafka.topic.market-data}")
    private String marketDataTopic;

    public void publishMarketData(MarketData data) {
        try {
            String message = objectMapper.writeValueAsString(data);
            kafkaTemplate.send(marketDataTopic, message);
            log.info("Published market data for symbol: {}", data.getSymbol());
        } catch (Exception e) {
            log.error("Failed to publish market data", e);
        }
    }
}
