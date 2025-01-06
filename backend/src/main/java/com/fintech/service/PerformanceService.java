package com.fintech.service;

import com.fintech.model.Portfolio;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Service
@RequiredArgsConstructor
public class PerformanceService {
    private final PortfolioService portfolioService;

    public Map<String, Object> calculatePerformanceMetrics(Long portfolioId) {
        Portfolio portfolio = portfolioService.getPortfolio(portfolioId);
        Map<String, Object> metrics = new HashMap<>();
        
        // Calculate current value
        BigDecimal currentValue = portfolioService.calculatePortfolioValue(portfolioId);
        
        // Calculate historical performance
        LocalDateTime oneMonthAgo = LocalDateTime.now().minusMonths(1);
        BigDecimal monthlyReturn = calculateReturn(portfolio, oneMonthAgo);
        
        // Risk metrics
        BigDecimal volatility = calculateVolatility(portfolio);
        BigDecimal sharpeRatio = calculateSharpeRatio(portfolio);
        
        metrics.put("currentValue", currentValue);
        metrics.put("monthlyReturn", monthlyReturn);
        metrics.put("volatility", volatility);
        metrics.put("sharpeRatio", sharpeRatio);
        
        return metrics;
    }
    
    private BigDecimal calculateReturn(Portfolio portfolio, LocalDateTime from) {
        BigDecimal initialValue = portfolioService.calculateHistoricalValue(portfolio.getId(), from);
        BigDecimal currentValue = portfolioService.calculatePortfolioValue(portfolio.getId());
        
        return currentValue.subtract(initialValue)
            .divide(initialValue, 4, BigDecimal.ROUND_HALF_UP)
            .multiply(new BigDecimal("100"));
    }
    
    private BigDecimal calculateVolatility(Portfolio portfolio) {
        List<BigDecimal> returns = portfolioService.getDailyReturns(portfolio.getId(), 30); // 30 days
        
        BigDecimal mean = returns.stream()
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .divide(new BigDecimal(returns.size()), 4, BigDecimal.ROUND_HALF_UP);
        
        BigDecimal variance = returns.stream()
            .map(r -> r.subtract(mean).pow(2))
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .divide(new BigDecimal(returns.size() - 1), 4, BigDecimal.ROUND_HALF_UP);
        
        return sqrt(variance);
    }
    
    private BigDecimal calculateSharpeRatio(Portfolio portfolio) {
        BigDecimal riskFreeRate = new BigDecimal("0.02");
        BigDecimal portfolioReturn = calculateReturn(portfolio, LocalDateTime.now().minusYears(1));
        BigDecimal volatility = calculateVolatility(portfolio);
        
        return portfolioReturn.subtract(riskFreeRate)
            .divide(volatility, 4, RoundingMode.HALF_UP);
    }
    
    private BigDecimal sqrt(BigDecimal value) {
        return new BigDecimal(Math.sqrt(value.doubleValue()));
    }
} 