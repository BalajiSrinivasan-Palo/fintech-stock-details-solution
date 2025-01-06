package com.fintech.client;

import com.fintech.dto.alphavantage.TimeSeriesResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import java.util.Map;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.fasterxml.jackson.databind.JsonNode;
import com.fintech.model.MarketData;
import java.time.LocalDate;

@Slf4j
@Component
@RequiredArgsConstructor
public class AlphaVantageClient {
    private final RestTemplate restTemplate;
    
    @Value("${alphavantage.api.key}")
    private String apiKey;

    @Value("${alphavantage.api.url}")
    private String apiUrl;

    public MarketData getQuote(String symbol) {
        String url = String.format("%s/query?function=GLOBAL_QUOTE&symbol=%s&apikey=%s",
            apiUrl, symbol, apiKey);
            
        JsonNode response = restTemplate.getForObject(url, JsonNode.class);
        JsonNode quote = response.get("Global Quote");
        
        MarketData marketData = new MarketData();
        marketData.setSymbol(symbol);
        marketData.setPrice(new BigDecimal(quote.get("05. price").asText()));
        marketData.setTimestamp(LocalDateTime.now());
        
        return marketData;
    }

    public Map<String, TimeSeriesResponse.DailyData> getTimeSeriesDaily(String symbol) {
        String url = String.format("%s/query?function=TIME_SERIES_DAILY&symbol=%s&outputsize=full&apikey=%s",
            apiUrl, symbol, apiKey);
            log.info("Alpha Vantage API URL: {}", url);
        
        log.info("Fetching time series data for symbol: {}", symbol);
        var response = restTemplate.getForObject(url, TimeSeriesResponse.class);
        
        if (response == null || response.getTimeSeries() == null) {
            throw new RuntimeException("Failed to fetch time series data for symbol: " + symbol);
        }
        
        return response.getTimeSeries();
    }

    public MarketData getHistoricalData(String symbol, LocalDate date) {
        String url = String.format("%s/query?function=TIME_SERIES_DAILY&symbol=%s&outputsize=full&apikey=%s",
            apiUrl, symbol, apiKey);
            
        TimeSeriesResponse response = restTemplate.getForObject(url, TimeSeriesResponse.class);
        //get the date for the past 30 from current date without including today
        LocalDate pastDate = date.minusDays(31);

        TimeSeriesResponse.DailyData dailyData = response.getTimeSeries().get(pastDate.toString());
        
        MarketData marketData = new MarketData();
        marketData.setSymbol(symbol);
        marketData.setTimestamp(date.atTime(16, 0));
        marketData.setPrice(new BigDecimal(dailyData.getClose()));
        
        return marketData;
    }
} 