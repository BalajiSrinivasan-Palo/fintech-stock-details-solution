package com.fintech.service;

import com.fintech.dto.github.FundCompositionJson;
import com.fintech.model.Portfolio;
import com.fintech.repository.PortfolioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.fintech.repository.DailyFundCompositionRepository;
import com.fintech.repository.FundRepository;
import com.fintech.model.DailyFundComposition;
import com.fintech.model.Fund;
import java.time.LocalDate;
import java.time.LocalDateTime;

import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class CompositionStorageService {
    private final PortfolioRepository portfolioRepository;
    private final DailyFundCompositionRepository compositionRepository;
    private final FundRepository fundRepository;

    @Transactional
    public void storeCompositions(FundCompositionJson compositions) {
        LocalDate date = LocalDate.parse(compositions.getDate());
        
        for (FundCompositionJson.FundComposition fundComp : compositions.getFundCompositions()) {
            Fund fund = fundRepository.findByName(fundComp.getName())
                .orElseGet(() -> {
                    Fund newFund = new Fund();
                    newFund.setName(fundComp.getName());
                    newFund.setTotalUnits(fundComp.getTotalUnits());
                    return fundRepository.save(newFund);
                });

            for (FundCompositionJson.Position position : fundComp.getPositions()) {
                DailyFundComposition composition = new DailyFundComposition();
                composition.setFund(fund);
                composition.setSymbol(position.getTicker());
                composition.setProportion(position.getProportion());
                composition.setDate(date);
                compositionRepository.save(composition);
            }
        }
    }

    @Transactional(readOnly = true)
    public Set<Portfolio> getPortfoliosByDate(LocalDateTime dateTime) {
        LocalDate date = dateTime.toLocalDate();
        return new HashSet<>(portfolioRepository.findByDate(date));
    }
} 