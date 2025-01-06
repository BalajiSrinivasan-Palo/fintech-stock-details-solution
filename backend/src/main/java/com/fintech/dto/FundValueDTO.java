package com.fintech.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import lombok.Builder;
import lombok.Data;
import lombok.AllArgsConstructor;

@Data
@Builder
public class FundValueDTO {
    private LocalDate date;
    private String fundName;
    private BigDecimal totalValue;
    private Map<String, SymbolBreakdown> breakdown;

    @Data
    @Builder
    @AllArgsConstructor
    public static class SymbolBreakdown {
        private BigDecimal proportion;
        private BigDecimal price;
        private BigDecimal value;
    }
} 