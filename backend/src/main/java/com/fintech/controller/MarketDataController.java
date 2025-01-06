package com.fintech.controller;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.fintech.service.MarketDataService;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.math.BigDecimal;
import java.util.HashSet;

@RestController
@RequestMapping("/api/market-data")
@Tag(name = "Market Data", description = "APIs for managing market data")
@RequiredArgsConstructor
@Slf4j
public class MarketDataController {
    private final MarketDataService marketDataService;

    @PostMapping("/sync")
    @Operation(summary = "Sync historical market data for a date range")
    public ResponseEntity<String> syncMarketData(
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
        @RequestParam(required = false) Set<String> symbols,
        @RequestParam(defaultValue = "false") boolean useScheduledSync
    ) {
        try {
            if (useScheduledSync) {
                // Use the scheduled sync logic (past 30 days)
                marketDataService.scheduledMarketDataSync();
                return ResponseEntity.ok("Scheduled market data sync completed successfully");
            }

            // If dates not provided, default to last 90 days
            LocalDate today = LocalDate.now();
            startDate = startDate != null ? startDate : today.minusDays(90);
            endDate = endDate != null ? endDate : today;

            // If no symbols provided, get from fund compositions
            if (symbols == null || symbols.isEmpty()) {
                symbols = marketDataService.getActiveSymbols();
            }

            log.info("Starting market data sync for {} fund symbols from {} to {}", 
                symbols.size(), startDate, endDate);

            for (String symbol : symbols) {
                marketDataService.syncHistoricalData(symbol, startDate, endDate);
            }

            return ResponseEntity.ok("Market data sync completed successfully");
        } catch (Exception e) {
            log.error("Failed to sync market data", e);
            return ResponseEntity.internalServerError()
                .body("Failed to sync market data: " + e.getMessage());
        }
    }

    @GetMapping("/status")
    @Operation(summary = "Get market data status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("symbolCount", marketDataService.findDistinctSymbols().size());
        status.put("symbols", marketDataService.findDistinctSymbols());
        return ResponseEntity.ok(status);
    }

    @GetMapping("/historical")
    @Operation(summary = "Get historical market data for all tracked symbols")
    public ResponseEntity<Map<String, Object>> getHistoricalData(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        try {
            // Get all symbols from both portfolios and fund compositions
            Set<String> allSymbols = new HashSet<>();
            allSymbols.addAll(marketDataService.findDistinctSymbols());
            allSymbols.addAll(marketDataService.getInvestmentSymbols());

            Map<String, Object> response = new HashMap<>();
            Map<String, Map<LocalDate, BigDecimal>> historicalData = new HashMap<>();

            for (String symbol : allSymbols) {
                Map<LocalDate, BigDecimal> prices = marketDataService.getHistoricalPrices(
                    symbol, startDate, endDate);
                historicalData.put(symbol, prices);
            }

            response.put("startDate", startDate);
            response.put("endDate", endDate);
            response.put("symbolCount", allSymbols.size());
            response.put("data", historicalData);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to fetch historical data", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("error", e.getMessage()));
        }
    }
} 