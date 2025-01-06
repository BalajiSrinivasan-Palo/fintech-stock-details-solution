package com.fintech.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import com.fintech.repository.DailyFundCompositionRepository;
import jakarta.annotation.PostConstruct;
import java.time.LocalDate;
import java.util.Set;

@RequiredArgsConstructor
@Slf4j
@Component
public class DataInitializationService {
    private final GitHubSyncService gitHubSyncService;
    private final MarketDataService marketDataService;
    private final DailyFundCompositionRepository compositionRepository;

    @PostConstruct
    public void initialize() {
        try {
            LocalDate today = LocalDate.now();
            if (!compositionRepository.existsByDate(today)) {
                log.info("Initializing data for date: {}", today);
                gitHubSyncService.syncDailyData();
                
                Set<String> symbols = compositionRepository.findDistinctSymbolsByDate(today);
                marketDataService.syncHistoricalData(symbols, today);
                
                log.info("Data initialization completed successfully");
            }
        } catch (Exception e) {
            log.error("Failed to initialize data", e);
            throw new RuntimeException("Data initialization failed", e);
        }
    }
} 