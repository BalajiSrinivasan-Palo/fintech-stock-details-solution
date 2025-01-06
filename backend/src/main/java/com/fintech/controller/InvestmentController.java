package com.fintech.controller;



import com.fintech.repository.PortfolioHoldingRepository;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import com.fintech.model.PortfolioHolding;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/investments")
@RequiredArgsConstructor
public class InvestmentController {
    private final PortfolioHoldingRepository holdingRepository;

    @GetMapping("/portfolio/{portfolioId}")
    public List<PortfolioHolding> getPortfolioHoldings(@PathVariable Long portfolioId) {
        return holdingRepository.findByPortfolioId(portfolioId);
    }
} 