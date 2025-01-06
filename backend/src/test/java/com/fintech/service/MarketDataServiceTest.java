package com.fintech.service;

import com.fintech.client.AlphaVantageClient;
import com.fintech.repository.MarketDataRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class MarketDataServiceTest {

    @Mock
    private MarketDataRepository marketDataRepository;

    @Mock
    private AlphaVantageClient alphaVantageClient;

    @InjectMocks
    private MarketDataService marketDataService;

    @Test
    void findDistinctSymbols_ShouldReturnFromRepository() {
        // Arrange
        Set<String> expectedSymbols = Set.of("AAPL", "GOOGL");
        when(marketDataRepository.findDistinctSymbols()).thenReturn(expectedSymbols);

        // Act
        Set<String> result = marketDataService.findDistinctSymbols();

        // Assert
        assertThat(result).isEqualTo(expectedSymbols);
    }
}