package com.fintech.service;

import com.fintech.model.Portfolio;
import com.fintech.model.MarketData;
import com.fintech.repository.PortfolioRepository;
import com.fintech.repository.PortfolioHoldingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.math.BigDecimal;
import java.util.Map;
import java.util.HashMap;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.math.RoundingMode;
import java.time.LocalDate;
import lombok.extern.slf4j.Slf4j;
import com.fintech.model.PortfolioHolding;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
@RequiredArgsConstructor
@Slf4j
public class PortfolioService {
    private final PortfolioRepository portfolioRepository;
    private final PortfolioHoldingRepository holdingRepository;
    private final MarketDataService marketDataService;

    public Portfolio getPortfolio(Long id) {
        return portfolioRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Portfolio not found"));
    }

    public List<Portfolio> getPortfoliosByDate(LocalDate date) {
        return portfolioRepository.findByDate(date);
    }

    public List<PortfolioHolding> getPortfolioHoldings(Long portfolioId) {
        return holdingRepository.findByPortfolioId(portfolioId);
    }

    @Transactional
    public void deletePortfolio(Long id) {
        Portfolio portfolio = getPortfolio(id);
        holdingRepository.deleteAll(getPortfolioHoldings(id));
        portfolioRepository.delete(portfolio);
    }

    public BigDecimal calculatePortfolioValue(Long portfolioId) {
        List<PortfolioHolding> holdings = holdingRepository.findByPortfolioId(portfolioId);
        return holdings.stream()
            .map(holding -> {
                MarketData marketData = marketDataService.getLatestPrice(holding.getSymbol());
                return marketData.getPrice().multiply(holding.getUnits());
            })
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal calculateHistoricalValue(Long portfolioId, LocalDateTime dateTime) {
        List<PortfolioHolding> holdings = holdingRepository.findByPortfolioId(portfolioId);
        return holdings.stream()
            .map(holding -> {
                MarketData marketData = marketDataService.getHistoricalPrice(
                    holding.getSymbol(), 
                    dateTime
                );
                return marketData.getPrice().multiply(holding.getUnits());
            })
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public List<BigDecimal> getDailyReturns(Long portfolioId, int days) {
        List<BigDecimal> returns = new ArrayList<>();
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days);

        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            BigDecimal todayValue = calculateHistoricalValue(portfolioId, date.atTime(16, 0));
            BigDecimal yesterdayValue = calculateHistoricalValue(portfolioId, date.minusDays(1).atTime(16, 0));
            
            if (yesterdayValue.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal dailyReturn = todayValue.subtract(yesterdayValue)
                    .divide(yesterdayValue, 4, RoundingMode.HALF_UP);
                returns.add(dailyReturn);
            }
        }
        return returns;
    }

    public Portfolio getPortfolioByName(String name) {
        return portfolioRepository.findByName(name)
            .orElseThrow(() -> new RuntimeException("Portfolio not found: " + name));
    }

    public List<Portfolio> getAllPortfolios() {
        return portfolioRepository.findAll();
    }

    @Transactional
    public Portfolio createPortfolio(Portfolio portfolio) {
        portfolio.setDate(LocalDate.now());
        return portfolioRepository.save(portfolio);
    }

    public BigDecimal calculatePortfolioValue(String name) {
        Portfolio portfolio = getPortfolioByName(name);
        return calculatePortfolioValue(portfolio.getId());
    }

    public Map<String, Object> getPortfolioMetrics(String name) {
        Portfolio portfolio = getPortfolioByName(name);
        Map<String, Object> metrics = new HashMap<>();
        
        BigDecimal currentValue = calculatePortfolioValue(portfolio.getId());
        List<PortfolioHolding> holdings = getPortfolioHoldings(portfolio.getId());
        
        metrics.put("totalValue", currentValue);
        metrics.put("holdingCount", holdings.size());
        metrics.put("lastUpdated", portfolio.getCreatedAt());
        
        // Add holdings breakdown
        Map<String, BigDecimal> holdingsMap = holdings.stream()
            .collect(Collectors.toMap(
                PortfolioHolding::getSymbol,
                holding -> {
                    MarketData marketData = marketDataService.getLatestPrice(holding.getSymbol());
                    return marketData.getPrice().multiply(holding.getUnits());
                }
            ));
        metrics.put("holdings", holdingsMap);
        
        return metrics;
    }

    public Page<Portfolio> searchPortfolios(
        String fundName, 
        LocalDate startDate, 
        LocalDate endDate, 
        Pageable pageable
    ) {
        if (fundName != null && !fundName.trim().isEmpty()) {
            return portfolioRepository.findByNameContainingIgnoreCaseAndDateBetween(
                fundName.trim(), startDate, endDate, pageable);
        }
        return portfolioRepository.findByDateBetween(startDate, endDate, pageable);
    }

    public BigDecimal getLatestPrice(String symbol) {
        return marketDataService.getLatestPrice(symbol).getPrice();
    }
}