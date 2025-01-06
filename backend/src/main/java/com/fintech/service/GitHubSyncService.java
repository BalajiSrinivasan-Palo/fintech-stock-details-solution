package com.fintech.service;

import com.fintech.client.GitHubApiClient;
import com.fintech.dto.github.FundCompositionJson;
import com.fintech.dto.github.PortfolioJson;
import com.fintech.model.Portfolio;
import com.fintech.repository.PortfolioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.HashSet;
import java.util.Set;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import com.fintech.model.DailyFundComposition;
import com.fintech.repository.DailyFundCompositionRepository;
import com.fintech.model.Fund;
import com.fintech.repository.FundRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class GitHubSyncService {
    private final GitHubApiClient gitHubApiClient;
    private final CompositionStorageService storageService;
    private final DailyFundCompositionRepository compositionRepository;
    private final MarketDataService marketDataService;
    private final FundRepository fundRepository;
    private final PortfolioRepository portfolioRepository;

    @Transactional
    public void syncPortfolioData() {
        try {
            PortfolioJson portfolio = gitHubApiClient.getPortfolioData();
            log.info("Portfolio data: {}", portfolio);
            // Store fund information first
            portfolio.getFunds().forEach(fund -> {
                Fund dbFund = fundRepository.findByName(fund.getName())
                    .orElseGet(() -> {
                        Fund newFund = new Fund();
                        newFund.setName(fund.getName());
                        return newFund;
                    });
                
                // Update total units from portfolio data
                dbFund.setTotalUnits(fund.getUnits_owned());
                fundRepository.save(dbFund);
                
                log.info("Saved/Updated fund: {} with {} units", 
                    fund.getName(), fund.getUnits_owned());
            });
        } catch (Exception e) {
            log.error("Failed to sync portfolio data", e);
            throw new RuntimeException("Portfolio sync failed", e);
        }
    }

    @Transactional
    public void syncFundCompositions() {
        try {
            // Only sync compositions after ensuring funds exist
            FundCompositionJson compositions = gitHubApiClient.getFundCompositions();
            
            if (compositions.getFundCompositions() == null) {
                throw new RuntimeException("Invalid fund composition data");
            }
            
            storageService.storeCompositions(compositions);
            
            log.info("Successfully synced fund compositions for date: {}", 
                compositions.getDate());
        } catch (Exception e) {
            log.error("Failed to sync fund compositions", e);
            throw new RuntimeException("Fund composition sync failed", e);
        }
    }

    @Scheduled(cron = "${app.data.refresh.cron}")
    @Transactional
    public void scheduledSync() {
        try {
            log.info("Starting scheduled data sync");
            
            // 1. First sync portfolio data
            syncPortfolioData();
            
            // 2. Verify portfolio data was saved
            if (fundRepository.count() == 0) {
                throw new RuntimeException("Portfolio sync failed - no funds found");
            }
            
            // 3. Then sync fund compositions
            syncFundCompositions();
            
            log.info("Scheduled data sync completed successfully");
        } catch (Exception e) {
            log.error("Scheduled sync failed", e);
            throw new RuntimeException("Scheduled sync failed", e);
        }
    }

    public FundCompositionJson getHistoricalComposition(LocalDate date) {
        try {
            String dateStr = date.format(DateTimeFormatter.ISO_DATE);
            return gitHubApiClient.getHistoricalFundCompositionData(dateStr);
        } catch (Exception e) {
            log.error("Failed to get historical composition for date: {}", date, e);
            throw new RuntimeException("Failed to get historical data", e);
        }
    }

    @Transactional
    public void syncDailyData() {
        LocalDate today = LocalDate.now();
        
        try {
            // Check if compositions already exist for today
            if (!compositionRepository.existsByDate(today)) {
                FundCompositionJson compositions = gitHubApiClient.getFundCompositions();
                
                for (FundCompositionJson.FundComposition fundComp : compositions.getFundCompositions()) {
                    Fund fund = fundRepository.findByName(fundComp.getName())
                        .orElseGet(() -> {
                            Fund newFund = new Fund();
                            newFund.setName(fundComp.getName());
                            return fundRepository.save(newFund);
                        });

                    // Store positions
                    for (FundCompositionJson.Position position : fundComp.getPositions()) {
                        DailyFundComposition dailyComp = new DailyFundComposition();
                        dailyComp.setFund(fund);
                        dailyComp.setDate(today);
                        dailyComp.setSymbol(position.getTicker());
                        dailyComp.setProportion(position.getProportion());
                        compositionRepository.save(dailyComp);
                    }
                }

                // Sync market data for new symbols
                Set<String> symbols = compositionRepository.findDistinctSymbolsByDate(today);
                marketDataService.syncHistoricalData(symbols, today);
            } else {
                log.info("Fund compositions for {} already exist, skipping sync", today);
            }
        } catch (Exception e) {
            log.error("Failed to sync daily data", e);
            throw new RuntimeException("Failed to sync daily data", e);
        }
    }

    public Set<String> getSymbolsForDateRange(LocalDate startDate, LocalDate endDate) {
        Set<String> symbols = new HashSet<>();
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            symbols.addAll(compositionRepository.findDistinctSymbolsByDate(date));
        }
        return symbols;
    }
} 