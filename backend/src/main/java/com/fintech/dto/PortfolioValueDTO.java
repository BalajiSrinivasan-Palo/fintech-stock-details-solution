package com.fintech.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.Map;
import lombok.AllArgsConstructor;
import java.time.LocalDate;

@Data
@AllArgsConstructor
public class PortfolioValueDTO {
    private LocalDate date;
    private String fundName;
    private BigDecimal totalValue;
    private Map<String, BigDecimal> breakdown;
} 