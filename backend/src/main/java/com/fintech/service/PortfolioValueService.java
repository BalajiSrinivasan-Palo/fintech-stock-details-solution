package com.fintech.service;

import com.fintech.dto.PortfolioValueDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;
import com.fintech.model.MarketData;
import java.time.LocalDate;
import com.fintech.model.DailyFundComposition;
import com.fintech.repository.DailyFundCompositionRepository;


@Service
@RequiredArgsConstructor
@Slf4j
public class PortfolioValueService {
    private final DailyFundCompositionRepository compositionRepository;
    private final MarketDataService marketDataService;

    public List<PortfolioValueDTO> calculateCurrentValues() {
        return calculateHistoricalValues(LocalDate.now());
    }

    public List<PortfolioValueDTO> calculateHistoricalValues(LocalDate date) {
        List<PortfolioValueDTO> results = new ArrayList<>();
        
        // Get all fund compositions for the date
        Map<String, List<DailyFundComposition>> fundCompositions = compositionRepository
            .findByDate(date).stream()
            .collect(Collectors.groupingBy(comp -> comp.getFund().getName()));
        
        // Calculate value for each fund
        for (Map.Entry<String, List<DailyFundComposition>> entry : fundCompositions.entrySet()) {
            String fundName = entry.getKey();
            List<DailyFundComposition> positions = entry.getValue();
            
            BigDecimal totalValue = BigDecimal.ZERO;
            Map<String, BigDecimal> breakdown = new HashMap<>();
            
            for (DailyFundComposition position : positions) {
                MarketData marketData = marketDataService.getHistoricalPrice(
                    position.getSymbol(), 
                    date.atTime(16, 0)
                );
                
                BigDecimal value = marketData.getPrice()
                    .multiply(position.getProportion());
                
                totalValue = totalValue.add(value);
                breakdown.put(position.getSymbol(), value);
            }
            
            results.add(new PortfolioValueDTO(date, fundName, totalValue, breakdown));
        }
        
        return results;
    }

    public List<PortfolioValueDTO> calculateHistoricalValues(LocalDate startDate, LocalDate endDate) {
        List<PortfolioValueDTO> allResults = new ArrayList<>();
        
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            allResults.addAll(calculateHistoricalValues(date));
        }
        
        return allResults;
    }
} 