package com.fintech.service;

import com.fintech.dto.FundValueDTO;
import com.fintech.dto.FundValueDTO.SymbolBreakdown;
import com.fintech.model.DailyFundComposition;
import com.fintech.model.Fund;
import com.fintech.model.MarketData;
import com.fintech.repository.DailyFundCompositionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FundValueService {
    private final DailyFundCompositionRepository compositionRepository;
    private final MarketDataService marketDataService;

    public List<FundValueDTO> calculateFundValues(LocalDate date) {
        List<FundValueDTO> results = new ArrayList<>();
        
        List<DailyFundComposition> compositions = compositionRepository.findByDate(date);
        Map<Fund, List<DailyFundComposition>> fundCompositions = compositions.stream()
            .collect(Collectors.groupingBy(DailyFundComposition::getFund));
        
        for (Map.Entry<Fund, List<DailyFundComposition>> entry : fundCompositions.entrySet()) {
            Fund fund = entry.getKey();
            List<DailyFundComposition> positions = entry.getValue();
            
            BigDecimal totalValue = BigDecimal.ZERO;
            Map<String, SymbolBreakdown> breakdown = new HashMap<>();
            
            for (DailyFundComposition position : positions) {
                MarketData marketData = marketDataService.getHistoricalPrice(
                    position.getSymbol(), 
                    date.atTime(16, 0)
                );
                
                BigDecimal proportion = position.getProportion();
                BigDecimal price = marketData.getPrice();
                BigDecimal value = price.multiply(proportion);
                
                totalValue = totalValue.add(value);
                breakdown.put(position.getSymbol(), new SymbolBreakdown(
                    proportion,
                    price,
                    value
                ));
            }
            
            results.add(FundValueDTO.builder()
                .date(date)
                .fundName(fund.getName())
                .totalValue(totalValue)
                .breakdown(breakdown)
                .build());
        }
        
        return results;
    }
} 