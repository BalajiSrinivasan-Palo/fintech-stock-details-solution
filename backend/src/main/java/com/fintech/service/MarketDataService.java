package com.fintech.service;

import com.fintech.client.AlphaVantageClient;
import com.fintech.model.MarketData;
import com.fintech.repository.MarketDataRepository;
import com.fintech.repository.InvestmentRepository;
import com.fintech.repository.DailyFundCompositionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import com.fintech.model.Investment;
import com.fintech.dto.alphavantage.TimeSeriesResponse;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.scheduling.annotation.Scheduled;


@Service
@RequiredArgsConstructor
@Slf4j
public class MarketDataService {
    private final AlphaVantageClient alphaVantageClient;
    private final MarketDataRepository marketDataRepository;
    private final InvestmentRepository investmentRepository;
    private final DailyFundCompositionRepository compositionRepository;
    private final Map<String, MarketData> marketDataCache = new ConcurrentHashMap<>();

    public Map<String, TimeSeriesResponse.DailyData> fetchTimeSeriesDaily(String symbol, 
            LocalDate startDate, LocalDate endDate) {
        Map<String, TimeSeriesResponse.DailyData> allData = alphaVantageClient.getTimeSeriesDaily(symbol);
        return allData.entrySet().stream()
            .filter(entry -> {
                LocalDate date = LocalDate.parse(entry.getKey());
                return !date.isBefore(startDate) && !date.isAfter(endDate);
            })
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public MarketData getLatestPrice(String symbol) {
        return marketDataCache.computeIfAbsent(symbol, this::fetchAndStoreMarketData);
    }

    public MarketData getHistoricalPrice(String symbol, LocalDateTime dateTime) {
        try {
            // 1. Try to get from repository first
            Optional<MarketData> storedData = marketDataRepository.findBySymbolAndTimestamp(symbol, dateTime);
            if (storedData.isPresent()) {
                return storedData.get();
            }

            // 2. If not found, fetch from Alpha Vantage
            Map<String, TimeSeriesResponse.DailyData> data = alphaVantageClient.getTimeSeriesDaily(symbol);
            String dateStr = dateTime.toLocalDate().toString();
            TimeSeriesResponse.DailyData historicalData = data.get(dateStr);

            if (historicalData == null) {
                log.warn("No historical data found for {} on {}", symbol, dateStr);
                // Return a default MarketData with zero price instead of null
                MarketData defaultData = new MarketData();
                defaultData.setSymbol(symbol);
                defaultData.setPrice(BigDecimal.ZERO);
                defaultData.setTimestamp(dateTime);
                return marketDataRepository.save(defaultData);
            }

            MarketData marketData = new MarketData();
            marketData.setSymbol(symbol);
            marketData.setPrice(new BigDecimal(historicalData.getClose()));
            marketData.setTimestamp(dateTime);
            return marketDataRepository.save(marketData);

        } catch (Exception e) {
            log.error("Error fetching historical price for {} at {}: {}", symbol, dateTime, e.getMessage());
            // Return a default MarketData with zero price in case of error
            MarketData defaultData = new MarketData();
            defaultData.setSymbol(symbol);
            defaultData.setPrice(BigDecimal.ZERO);
            defaultData.setTimestamp(dateTime);
            return marketDataRepository.save(defaultData);
        }
    }

    @Transactional
    public void syncHistoricalData(Set<String> symbols, LocalDate date) {
        for (String symbol : symbols) {
        
            // Check if we already have data for this symbol and date
            if (!marketDataRepository.existsBySymbolAndDate(symbol, date)) {
                try {
                    // Fetch historical data from Alpha Vantage
                    MarketData marketData = alphaVantageClient.getHistoricalData(symbol, date);
                    
                    // Save to database
                    marketDataRepository.save(marketData);
                    
                    // Alpha Vantage has rate limits, so add a small delay
                    Thread.sleep(1500); // 1.5 second delay between API calls
                    
                } catch (Exception e) {
                    log.error("Failed to fetch historical data for symbol: {} on date: {}", 
                        symbol, date, e);
                }
            } else {
                log.debug("Market data already exists for symbol: {} on date: {}", 
                    symbol, date);
            }
        }
    }

    @Transactional
    public void syncHistoricalData(String symbol, LocalDate startDate, LocalDate endDate) {
        log.info("Syncing historical data for {} from {} to {}", symbol, startDate, endDate);
        
        try {
            // Get all existing dates for this symbol to avoid duplicates
            Set<LocalDate> existingDates = marketDataRepository
                .findBySymbolAndTimestampBetween(symbol, 
                    startDate.atStartOfDay(), 
                    endDate.atTime(23, 59))
                .stream()
                .map(md -> md.getTimestamp().toLocalDate())
                .collect(Collectors.toSet());

            // Fetch 90 days of data at once
            Map<String, TimeSeriesResponse.DailyData> data = alphaVantageClient.getTimeSeriesDaily(symbol);
            List<MarketData> newDataPoints = new ArrayList<>();
            
            data.forEach((dateStr, dailyData) -> {
                LocalDate date = LocalDate.parse(dateStr);
                if (!date.isBefore(startDate) && !date.isAfter(endDate) 
                    && !existingDates.contains(date)) {
                    MarketData marketData = new MarketData();
                    marketData.setSymbol(symbol);
                    marketData.setPrice(new BigDecimal(dailyData.getClose()));
                    marketData.setTimestamp(date.atTime(16, 0)); // Market close time
                    newDataPoints.add(marketData);
                }
            });

            if (!newDataPoints.isEmpty()) {
                marketDataRepository.saveAll(newDataPoints);
                log.info("Saved {} new data points for {}", newDataPoints.size(), symbol);
            } else {
                log.info("No new data points to save for {}", symbol);
            }
            
        } catch (Exception e) {
            log.error("Error syncing historical data for {}", symbol, e);
            throw new RuntimeException("Failed to sync historical data for " + symbol, e);
        }
    }

    private MarketData fetchAndStoreMarketData(String symbol) {
        MarketData marketData = alphaVantageClient.getQuote(symbol);
        return marketDataRepository.save(marketData);
    }

    public List<MarketData> getDailyValues(LocalDate startDate, LocalDate endDate) {
        Set<String> symbols = investmentRepository.findAll().stream()
            .map(Investment::getSymbol)
            .collect(Collectors.toSet());
        
        return symbols.stream()
            .flatMap(symbol -> fetchDailyValuesForSymbol(symbol, startDate, endDate).stream())
            .collect(Collectors.toList());
    }

    private List<MarketData> fetchDailyValuesForSymbol(String symbol, 
            LocalDate startDate, LocalDate endDate) {
        try {
            log.info("Fetching daily values for symbol: {}", symbol);
            Map<String, TimeSeriesResponse.DailyData> timeSeriesData = 
                alphaVantageClient.getTimeSeriesDaily(symbol);

            if (timeSeriesData == null) {
                log.error("No data found for symbol: {}", symbol);
                return Collections.emptyList();
            }

            log.info("Time series data: {}", timeSeriesData);
            List<MarketData> marketDataList = new ArrayList<>();
            timeSeriesData.forEach((date, priceData) -> {
                LocalDate dataDate = LocalDate.parse(date);
                log.info("Data date: {}", dataDate);
                log.info("Start date: {}", startDate);
                log.info("End date: {}", endDate);
                if (!dataDate.isBefore(startDate) && !dataDate.isAfter(endDate)) {
                    MarketData marketData = new MarketData();
                    marketData.setSymbol(symbol);
                    marketData.setPrice(new BigDecimal(priceData.getClose()));
                    marketData.setTimestamp(LocalDateTime.parse(date + "T16:00:00"));
                    marketDataList.add(marketData);
                }
                log.info("Market data list: {}", marketDataList);
            });

            Thread.sleep(15000); // Respect API rate limits
            return marketDataList;
        } catch (Exception e) {
            log.error("Error fetching data for symbol: " + symbol, e);
            return Collections.emptyList();
        }
    }

    public Map<String, BigDecimal> getHistoricalPrices(String symbol, LocalDate date) {
        Map<String, TimeSeriesResponse.DailyData> timeSeriesData = 
            alphaVantageClient.getTimeSeriesDaily(symbol);
        var dailyData = timeSeriesData.get(date.toString());
        
        return Map.of(
            "open", new BigDecimal(dailyData.getOpen()),
            "high", new BigDecimal(dailyData.getHigh()),
            "low", new BigDecimal(dailyData.getLow()),
            "close", new BigDecimal(dailyData.getClose())
        );
    }

    public void refreshMarketData() {
        log.info("Refreshing market data");
        marketDataCache.clear();
        // Fetch and store new data for all tracked symbols
        Set<String> symbols = marketDataRepository.findDistinctSymbols();
        for (String symbol : symbols) {
            try {
                fetchAndStoreMarketData(symbol);
                Thread.sleep(12000); // Respect Alpha Vantage rate limits
            } catch (Exception e) {
                log.error("Error refreshing data for {}", symbol, e);
            }
        }
    }

    public Set<String> findDistinctSymbols() {
        return marketDataRepository.findDistinctSymbols();
    }

    public Set<String> getInvestmentSymbols() {
        return investmentRepository.findDistinctSymbols();
    }

    public Map<LocalDate, BigDecimal> getHistoricalPrices(String symbol, LocalDate startDate, LocalDate endDate) {
        Map<LocalDate, BigDecimal> prices = new TreeMap<>();
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            MarketData data = getHistoricalPrice(symbol, date.atTime(16, 0));
            if (data != null) {
                prices.put(date, data.getPrice());
            }
        }
        return prices;
    }

    @Scheduled(cron = "0 0 10 * * MON-FRI")
    @Transactional
    public void scheduledMarketDataSync() {
        try {
            LocalDate today = LocalDate.now();
            LocalDate startDate = today.minusDays(30); // Past 30 days excluding today
            Set<String> symbols = getActiveSymbols();

            log.info("Starting scheduled market data sync for {} symbols", symbols.size());

            for (String symbol : symbols) {
                syncHistoricalData(symbol, startDate, today.minusDays(1));
            }

            log.info("Completed scheduled market data sync");
        } catch (Exception e) {
            log.error("Scheduled market data sync failed", e);
        }
    }

    public Set<String> getActiveSymbols() {
        // Get symbols from fund compositions
        return compositionRepository.findAllDistinctSymbols();
    }

    public Set<String> getActiveSymbolsForDate(LocalDate date) {
        return compositionRepository.findDistinctSymbolsByDate(date);
    }
} 