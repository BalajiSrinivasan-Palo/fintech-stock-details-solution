package com.fintech.service;

import com.fintech.model.Investment;
import com.fintech.model.MarketData;
import com.fintech.repository.InvestmentRepository;
import com.fintech.repository.MarketDataRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.context.annotation.DependsOn;
import com.fintech.dto.alphavantage.TimeSeriesResponse;

@Component
@RequiredArgsConstructor
@Slf4j
@Order(2)
@DependsOn("dataInitializationService")
public class HistoricalDataInitializationService implements CommandLineRunner {
    private final InvestmentRepository investmentRepository;
    private final MarketDataService marketDataService;
    private final MarketDataRepository marketDataRepository;
    private static final int DAYS_OF_HISTORY = 100;

    @Override
    public void run(String... args) throws InterruptedException {
        log.info("HistoricalDataInitializationService starting...");
        // Give a small delay to ensure data is committed
        Thread.sleep(2000);

        if (marketDataRepository.count() == 0) {
            log.info("Initializing historical market data...");
            fetchHistoricalData();
            log.info("Historical data initialization completed");
        } else {
            log.info("Market data already exists, skipping initialization");
        }
    }

    private void fetchHistoricalData() throws InterruptedException {
        Set<String> symbols = investmentRepository.findAll().stream()
            .map(Investment::getSymbol)
            .collect(Collectors.toSet());

        if (symbols.isEmpty()) {
            log.error("No symbols found in investment table. Ensure DataInitializationService has run.");
            return;
        }

        log.info("Found {} symbols to fetch: {}", symbols.size(), symbols);

        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(DAYS_OF_HISTORY);
        log.info("Fetching data from {} to {}", startDate, endDate);

        for (String symbol : symbols) {
            try {
                log.info("Fetching historical data for symbol: {}", symbol);
                Map<String, TimeSeriesResponse.DailyData> timeSeriesData = 
                    marketDataService.fetchTimeSeriesDaily(symbol, startDate, endDate);
                log.info("Received {} days of data for {}", timeSeriesData.size(), symbol);

                timeSeriesData.forEach((date, priceData) -> {
                    MarketData marketData = new MarketData();
                    marketData.setSymbol(symbol);
                    marketData.setPrice(new BigDecimal(priceData.getClose()));
                    marketData.setTimestamp(LocalDateTime.parse(date + "T16:00:00"));
                    marketDataRepository.save(marketData);
                    log.info("Saved price {} for {} on {}", 
                        priceData.getClose(), symbol, date);
                });

                // Sleep to respect API rate limits
                Thread.sleep(15000); // 15 seconds between API calls
            } catch (Exception e) {
                log.error("Error fetching data for symbol: " + symbol, e);
            }
        }
    }
} 