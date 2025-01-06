package com.fintech.service;

import com.fintech.dto.github.PortfolioJson;
import com.fintech.dto.github.FundCompositionJson;
import com.fintech.exception.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Slf4j
public class ValidationService {
    private static final BigDecimal ONE = BigDecimal.ONE;
    private static final BigDecimal TOLERANCE = new BigDecimal("0.0001");

    public void validateFundComposition(List<FundCompositionJson.Position> positions) {
        if (positions == null || positions.isEmpty()) {
            throw new ValidationException("Fund composition cannot be empty");
        }

        BigDecimal totalProportion = positions.stream()
            .map(FundCompositionJson.Position::getProportion)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (ONE.subtract(totalProportion).abs().compareTo(TOLERANCE) > 0) {
            throw new ValidationException(
                String.format("Position proportions must sum to 1.0 (got: %.4f)", totalProportion));
        }
    }

    public void validatePortfolio(PortfolioJson.FundData fund) {
        if (fund.getTotalUnits().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Total units must be positive");
        }
    }
} 