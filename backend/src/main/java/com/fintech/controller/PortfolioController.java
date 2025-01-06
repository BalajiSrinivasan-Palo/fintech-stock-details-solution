package com.fintech.controller;

import com.fintech.model.Portfolio;
import com.fintech.service.PortfolioService;
import com.fintech.service.PerformanceService;
import com.fintech.service.CompositionStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import java.util.stream.Collectors;
import lombok.Data;
import lombok.AllArgsConstructor;
import java.math.RoundingMode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.fintech.model.PortfolioHolding;
import org.springframework.data.domain.PageRequest;
import java.util.AbstractMap;
import com.fintech.model.DailyFundComposition;
import com.fintech.service.MarketDataService;
import com.fintech.repository.DailyFundCompositionRepository;
import com.fintech.model.Fund;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fintech.model.MarketData;
import com.fasterxml.jackson.annotation.JsonFormat;


@Tag(name = "Portfolio Management", description = "APIs for managing investment portfolios")
@RestController
@RequestMapping("/api/portfolios")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class PortfolioController {
    private static final Logger log = LoggerFactory.getLogger(PortfolioController.class);
    private final PortfolioService portfolioService;
    private final PerformanceService performanceService;
    private final CompositionStorageService storageService;
    private final MarketDataService marketDataService;
    private final DailyFundCompositionRepository compositionRepository;

    @Operation(summary = "Get all portfolios")
    @GetMapping
    public List<Portfolio> getAllPortfolios() {
        return portfolioService.getAllPortfolios();
    }

    @Operation(summary = "Get portfolio by ID")
    @GetMapping("/{id}")
    public Portfolio getPortfolioById(@PathVariable Long id) {
        return portfolioService.getPortfolio(id);
    }

    @Operation(summary = "Get portfolio by name")
    @GetMapping("/name/{name}")
    public ResponseEntity<Portfolio> getPortfolioByName(@PathVariable String name) {
        return ResponseEntity.ok(portfolioService.getPortfolioByName(name));
    }

    @Operation(summary = "Get portfolio value")
    @GetMapping("/name/{name}/value")
    public BigDecimal getPortfolioValue(@PathVariable String name) {
        return portfolioService.calculatePortfolioValue(name);
    }

    @Operation(summary = "Get portfolio metrics")
    @GetMapping("/name/{name}/metrics")
    public Map<String, Object> getPortfolioMetrics(@PathVariable String name) {
        return portfolioService.getPortfolioMetrics(name);
    }

    @Operation(summary = "Create new portfolio")
    @PostMapping
    public Portfolio createPortfolio(@RequestBody Portfolio portfolio) {
        return portfolioService.createPortfolio(portfolio);
    }

    @Operation(summary = "Get performance metrics")
    @GetMapping("/{id}/performance")
    public Map<String, Object> getPerformanceMetrics(@PathVariable Long id) {
        return performanceService.calculatePerformanceMetrics(id);
    }

    @GetMapping("/date/{date}")
    public ResponseEntity<Set<Portfolio>> getPortfoliosByDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDateTime date) {
        try {
            Set<Portfolio> portfolios = storageService.getPortfoliosByDate(date);
            return ResponseEntity.ok(portfolios);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/current")
    public ResponseEntity<List<PortfolioSummaryDTO>> getCurrentPortfolios() {
        LocalDate today = LocalDate.now();
        List<Portfolio> portfolios = portfolioService.getPortfoliosByDate(today);
        
        List<PortfolioSummaryDTO> summaries = portfolios.stream()
            .map(portfolio -> {
                BigDecimal currentValue = portfolioService.calculatePortfolioValue(portfolio.getId());
                List<HoldingDTO> holdings = portfolioService.getPortfolioHoldings(portfolio.getId())
                    .stream()
                    .map(holding -> new HoldingDTO(
                        holding.getSymbol(),
                        holding.getUnits(),
                        portfolioService.getLatestPrice(holding.getSymbol())
                    ))
                    .collect(Collectors.toList());

                return new PortfolioSummaryDTO(
                    portfolio.getId(),
                    portfolio.getName(),
                    currentValue,
                    holdings
                );
            })
            .collect(Collectors.toList());

        return ResponseEntity.ok(summaries);
    }

    @GetMapping("/funds/values")
    public ResponseEntity<List<FundValueDTO>> getFundValues() {
        LocalDate today = LocalDate.now();
        List<Portfolio> portfolios = portfolioService.getPortfoliosByDate(today);
        
        List<FundValueDTO> fundValues = portfolios.stream()
            .map(portfolio -> {
                BigDecimal currentValue = portfolioService.calculatePortfolioValue(portfolio.getId());
                BigDecimal previousValue = portfolioService.calculateHistoricalValue(
                    portfolio.getId(), 
                    LocalDateTime.now().minusDays(1)
                );
                
                BigDecimal change = currentValue.subtract(previousValue);
                BigDecimal percentChange = change.divide(previousValue, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));

                return new FundValueDTO(
                    portfolio.getName(),
                    currentValue,
                    change,
                    percentChange,
                    portfolio.getDate()
                );
            })
            .collect(Collectors.toList());

        return ResponseEntity.ok(fundValues);
    }

    @GetMapping("/funds/details")
    public ResponseEntity<List<FundDetailDTO>> getFundDetails(
        @RequestParam(required = false) 
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate requestDate
    ) {
        final LocalDate date = requestDate != null ? requestDate : LocalDate.now();
        List<Portfolio> portfolios = portfolioService.getPortfoliosByDate(date);
        
        List<FundDetailDTO> fundDetails = portfolios.stream()
            .map(portfolio -> {
                Map<String, BigDecimal> breakdown = portfolioService.getPortfolioHoldings(portfolio.getId())
                    .stream()
                    .collect(Collectors.toMap(
                        PortfolioHolding::getSymbol,
                        holding -> {
                            BigDecimal price = portfolioService.getLatestPrice(holding.getSymbol());
                            return price.multiply(holding.getUnits()).setScale(2, RoundingMode.HALF_UP);
                        }
                    ));

                return new FundDetailDTO(
                    date,
                    portfolio.getName(),
                    portfolioService.calculatePortfolioValue(portfolio.getId()),
                    breakdown
                );
            })
            .collect(Collectors.toList());

        return ResponseEntity.ok(fundDetails);
    }

    @GetMapping("/funds/detailed-breakdown")
    public ResponseEntity<List<FundDetailedDTO>> getFundDetailedBreakdown(
        @RequestParam(required = false) 
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate requestDate
    ) {
        final LocalDate date = requestDate != null ? requestDate : LocalDate.now();
        List<Portfolio> portfolios = portfolioService.getPortfoliosByDate(date);
        
        List<FundDetailedDTO> fundDetails = portfolios.stream()
            .map(portfolio -> {
                List<HoldingBreakdownDTO> breakdowns = portfolioService.getPortfolioHoldings(portfolio.getId())
                    .stream()
                    .map(holding -> {
                        BigDecimal price = portfolioService.getLatestPrice(holding.getSymbol());
                        BigDecimal proportion = holding.getUnits()
                            .divide(portfolio.getTotalUnits(), 4, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100));
                        BigDecimal value = price.multiply(holding.getUnits());

                        return new HoldingBreakdownDTO(
                            holding.getSymbol(),
                            proportion,
                            price,
                            value
                        );
                    })
                    .collect(Collectors.toList());

                return new FundDetailedDTO(
                    date,
                    portfolio.getName(),
                    portfolioService.calculatePortfolioValue(portfolio.getId()),
                    breakdowns
                );
            })
            .collect(Collectors.toList());

        return ResponseEntity.ok(fundDetails);
    }

    @GetMapping("/funds/breakdown/search")
    public ResponseEntity<PageResponse<FundDetailedDTO>> searchFundBreakdown(
        @RequestParam(required = false) String fundName,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            
            Page<DailyFundComposition> compositions = compositionRepository
                .findByDateBetweenAndFundNameContainingIgnoreCase(
                    startDate, endDate, 
                    fundName != null ? fundName : "", 
                    pageable);

            List<FundDetailedDTO> fundDetails = compositions.getContent().stream()
                .filter(comp -> comp != null && comp.getFund() != null)  // Filter out nulls
                .collect(Collectors.groupingBy(
                    comp -> new AbstractMap.SimpleEntry<>(
                        comp.getFund().getName(), 
                        comp.getDate()),
                    Collectors.toList()
                ))
                .entrySet().stream()
                .map(entry -> {
                    try {
                        String name = entry.getKey().getKey();
                        LocalDate date = entry.getKey().getValue();
                        List<DailyFundComposition> fundComps = entry.getValue();
                        Fund fund = fundComps.get(0).getFund();

                        List<HoldingBreakdownDTO> breakdowns = fundComps.stream()
                            .filter(comp -> comp.getSymbol() != null)  // Filter out null symbols
                            .map(comp -> {
                                try {
                                    MarketData marketData = marketDataService.getHistoricalPrice(
                                        comp.getSymbol(), 
                                        date.atTime(16, 0)
                                    );
                                    
                                    if (marketData == null || marketData.getPrice() == null) {
                                        log.warn("No price data for symbol: {} on date: {}", 
                                            comp.getSymbol(), date);
                                        return null;
                                    }

                                    BigDecimal proportionalUnits = fund.getTotalUnits()
                                        .multiply(comp.getProportion())
                                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                                    
                                    return new HoldingBreakdownDTO(
                                        comp.getSymbol(),
                                        comp.getProportion(),
                                        marketData.getPrice(),
                                        marketData.getPrice().multiply(proportionalUnits)
                                    );
                                } catch (Exception e) {
                                    log.warn("Skipping breakdown for symbol: {} due to error", 
                                        comp.getSymbol());
                                    return null;
                                }
                            })
                            .filter(breakdown -> breakdown != null)  // Filter out failed calculations
                            .collect(Collectors.toList());

                        if (breakdowns.isEmpty()) {
                            log.warn("No valid breakdowns for fund: {} on date: {}", name, date);
                            return null;
                        }

                        BigDecimal totalValue = breakdowns.stream()
                            .map(HoldingBreakdownDTO::getValue)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                        return new FundDetailedDTO(date, name, totalValue, breakdowns);
                    } catch (Exception e) {
                        log.warn("Skipping fund entry due to error", e);
                        return null;
                    }
                })
                .filter(dto -> dto != null)  // Filter out failed fund calculations
                .collect(Collectors.toList());

            return ResponseEntity.ok(new PageResponse<>(
                fundDetails,
                compositions.getTotalElements(),
                compositions.getTotalPages(),
                compositions.getNumber()
            ));
        } catch (Exception e) {
            log.error("Error in searchFundBreakdown", e);
            return ResponseEntity.ok(new PageResponse<>(List.of(), 0L, 0, 0));
        }
    }

    @Data
    @AllArgsConstructor
    public static class PortfolioSummaryDTO {
        private Long id;
        private String name;
        private BigDecimal totalValue;
        private List<HoldingDTO> holdings;
    }

    @Data
    @AllArgsConstructor
    public static class HoldingDTO {
        private String symbol;
        private BigDecimal units;
        private BigDecimal price;
    }

    @Data
    @AllArgsConstructor
    public static class FundValueDTO {
        private String fundName;
        private BigDecimal currentValue;
        private BigDecimal valueChange;
        private BigDecimal percentageChange;
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate date;
    }

    @Data
    @AllArgsConstructor
    public static class FundDetailDTO {
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate date;
        private String fundName;
        private BigDecimal totalValue;
        private Map<String, BigDecimal> breakdown;
    }

    @Data
    @AllArgsConstructor
    public static class FundDetailedDTO {
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate date;
        private String fundName;
        private BigDecimal totalValue;
        private List<HoldingBreakdownDTO> breakdown;
    }

    @Data
    @AllArgsConstructor
    public static class HoldingBreakdownDTO {
        private String symbol;
        private BigDecimal proportion;  // as percentage
        private BigDecimal price;
        private BigDecimal value;
    }

    @Data
    @AllArgsConstructor
    public static class PageResponse<T> {
        private List<T> content;
        private long totalElements;
        private int totalPages;
        private int currentPage;
    }
} 