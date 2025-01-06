package com.fintech.service;

import com.fintech.dto.PortfolioValueDTO;
import com.fintech.model.DailyFundComposition;
import com.fintech.model.Fund;
import com.fintech.model.MarketData;
import com.fintech.repository.DailyFundCompositionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PortfolioValueServiceTest {

    @Mock
    private DailyFundCompositionRepository compositionRepository;

    @Mock
    private MarketDataService marketDataService;

    @InjectMocks
    private PortfolioValueService portfolioValueService;

    @Test
    void calculateHistoricalValues_ShouldCalculateCorrectly() {
        // Arrange
        LocalDate date = LocalDate.now();
        Fund fund = new Fund();
        fund.setName("Test Fund");
        fund.setTotalUnits(BigDecimal.valueOf(100));

        DailyFundComposition composition = new DailyFundComposition();
        composition.setFund(fund);
        composition.setSymbol("AAPL");
        composition.setProportion(BigDecimal.valueOf(50));
        composition.setDate(date);

        MarketData marketData = new MarketData();
        marketData.setPrice(BigDecimal.valueOf(150));
        marketData.setTimestamp(date.atTime(16, 0));

        when(compositionRepository.findByDate(date)).thenReturn(List.of(composition));
        when(marketDataService.getHistoricalPrice(any(), any())).thenReturn(marketData);

        // Act
        List<PortfolioValueDTO> result = portfolioValueService.calculateHistoricalValues(date);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTotalValue()).isEqualByComparingTo(BigDecimal.valueOf(7500));
    }
} 