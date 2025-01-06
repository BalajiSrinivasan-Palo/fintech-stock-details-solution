package com.fintech.service;

import com.fintech.client.GitHubApiClient;
import com.fintech.dto.github.FundCompositionJson;
import com.fintech.dto.github.PortfolioJson;
import com.fintech.model.Fund;
import com.fintech.repository.FundRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GitHubSyncServiceTest {

    @Mock
    private GitHubApiClient gitHubApiClient;

    @Mock
    private CompositionStorageService storageService;

    @Mock
    private FundRepository fundRepository;

    @InjectMocks
    private GitHubSyncService gitHubSyncService;

    @Test
    void syncPortfolioData_ShouldSaveNewFund() {
        // Arrange
        PortfolioJson.Fund fundData = new PortfolioJson.Fund();
        fundData.setName("Test Fund");
        fundData.setUnits_owned(BigDecimal.TEN);

        PortfolioJson portfolioJson = new PortfolioJson();
        portfolioJson.setFunds(List.of(fundData));

        when(gitHubApiClient.getPortfolioData()).thenReturn(portfolioJson);
        when(fundRepository.findByName(anyString())).thenReturn(Optional.empty());
        when(fundRepository.save(any(Fund.class))).thenReturn(new Fund());

        // Act
        gitHubSyncService.syncPortfolioData();

        // Assert
        verify(fundRepository).save(any(Fund.class));
    }

    @Test
    void syncFundCompositions_ShouldSaveCompositions() {
        // Arrange
        FundCompositionJson compositions = new FundCompositionJson();
        
        // Create a fund with positions
        FundCompositionJson.Fund fund = new FundCompositionJson.Fund();
        fund.setDate("2024-01-01");
        
        FundCompositionJson.Position position = new FundCompositionJson.Position();
        position.setTicker("AAPL");
        position.setProportion(BigDecimal.valueOf(100));
        fund.setPositions(List.of(position));

        // Add fund to compositions
        Map<String, FundCompositionJson.Fund> funds = new HashMap<>();
        funds.put("Test Fund", fund);
        compositions.setFunds(funds);

        when(gitHubApiClient.getFundCompositions()).thenReturn(compositions);
        doNothing().when(storageService).storeCompositions(any());

        // Act
        gitHubSyncService.syncFundCompositions();

        // Assert
        verify(storageService).storeCompositions(compositions);
    }
} 