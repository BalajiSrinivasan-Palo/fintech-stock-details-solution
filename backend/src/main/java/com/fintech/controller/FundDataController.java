package com.fintech.controller;

import com.fintech.service.MarketDataService;
import com.fintech.model.MarketData;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/funds")
@RequiredArgsConstructor
public class FundDataController {
    private final MarketDataService marketDataService;

    @GetMapping("/daily-values")
    public List<MarketData> getDailyValues(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        // This endpoint would use MarketDataService to fetch from Alpha Vantage
        return marketDataService.getDailyValues(startDate, endDate);
    }

    @GetMapping("/price/{symbol}")
    public MarketData getStockPrice(@PathVariable String symbol) {
        // This endpoint calls Alpha Vantage API through MarketDataService
        return marketDataService.getLatestPrice(symbol);
    }
} 