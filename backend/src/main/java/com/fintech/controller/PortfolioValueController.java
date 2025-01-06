package com.fintech.controller;

import com.fintech.dto.PortfolioValueDTO;
import com.fintech.service.PortfolioValueService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.time.LocalDate;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/portfolio-values")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Portfolio Values")
public class PortfolioValueController {
    private final PortfolioValueService portfolioValueService;

    @GetMapping("/current")
    @Operation(summary = "Get current portfolio values")
    public ResponseEntity<List<PortfolioValueDTO>> getCurrentValues() {
        try {
            return ResponseEntity.ok(portfolioValueService.calculateCurrentValues());
        } catch (Exception e) {
            log.error("Error calculating current values", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/historical")
    @Operation(summary = "Get historical portfolio values for a specific date")
    public ResponseEntity<List<PortfolioValueDTO>> getHistoricalValues(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        try {
            return ResponseEntity.ok(portfolioValueService.calculateHistoricalValues(date));
        } catch (Exception e) {
            log.error("Error calculating historical values for date: {}", date, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/historical/range")
    @Operation(summary = "Get historical portfolio values for date range")
    public ResponseEntity<List<PortfolioValueDTO>> getHistoricalValuesRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            return ResponseEntity.ok(portfolioValueService.calculateHistoricalValues(startDate, endDate));
        } catch (Exception e) {
            log.error("Error calculating historical values for range: {} to {}", startDate, endDate, e);
            return ResponseEntity.internalServerError().build();
        }
    }
} 