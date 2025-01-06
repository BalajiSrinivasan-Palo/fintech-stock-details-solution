package com.fintech.controller;

import com.fintech.service.GitHubSyncService;
import com.fintech.service.MarketDataService;
import com.fintech.service.DataIntegrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import com.fintech.dto.github.FundCompositionJson;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestParam;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/sync")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Data Synchronization")
public class SyncController {
    private final GitHubSyncService gitHubSyncService;
    private final MarketDataService marketDataService;
    private final DataIntegrationService dataIntegrationService;

    @PostMapping
    public ResponseEntity<String> syncAll() {
        // 1. First sync portfolio data (contains fund names and total units)
        gitHubSyncService.syncPortfolioData();
        
        // 2. Then sync fund compositions
        gitHubSyncService.syncFundCompositions();
        
        return ResponseEntity.ok("Sync completed successfully");
    }

    @GetMapping("/compositions/{date}")
    public ResponseEntity<?> getHistoricalCompositions(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        try {
            FundCompositionJson composition = gitHubSyncService.getHistoricalComposition(date);
            return ResponseEntity.ok(composition);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body("Failed to fetch historical compositions: " + e.getMessage());
        }
    }

    @PostMapping("/daily")
    @Operation(summary = "Sync today's fund compositions and market data")
    public ResponseEntity<String> syncDailyData() {
        try {
            gitHubSyncService.syncDailyData();
            return ResponseEntity.ok("Daily sync completed");
        } catch (Exception e) {
            log.error("Failed to sync daily data", e);
            return ResponseEntity.internalServerError()
                .body("Failed to sync daily data: " + e.getMessage());
        }
    }

    @PostMapping("/market-data/historical")
    @Operation(summary = "Sync historical market data for symbols")
    public ResponseEntity<String> syncHistoricalMarketData(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) List<String> symbols) {
        try {
            log.info("Starting historical data sync from {} to {}", startDate, endDate);
            
            if (symbols == null || symbols.isEmpty()) {
                // Get symbols from daily compositions for the date range
                symbols = new ArrayList<>(gitHubSyncService.getSymbolsForDateRange(startDate, endDate));
            }

            for (String symbol : symbols) {
                marketDataService.syncHistoricalData(symbol, startDate, endDate);
                Thread.sleep(12000); // Respect Alpha Vantage rate limits
            }

            return ResponseEntity.ok("Historical data sync completed");
        } catch (Exception e) {
            log.error("Failed to sync historical data", e);
            return ResponseEntity.internalServerError()
                .body("Failed to sync historical data: " + e.getMessage());
        }
    }

    @PostMapping("/refresh")
    @Operation(summary = "Manually trigger data refresh scheduler")
    public ResponseEntity<String> triggerDataRefresh() {
        try {
            log.info("Manually triggering data refresh");
            dataIntegrationService.scheduledDataRefresh();
            return ResponseEntity.ok("Manual data refresh completed successfully");
        } catch (Exception e) {
            log.error("Failed to execute manual data refresh", e);
            return ResponseEntity.internalServerError()
                .body("Failed to execute manual data refresh: " + e.getMessage());
        }
    }
} 