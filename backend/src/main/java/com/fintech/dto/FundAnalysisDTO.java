package com.fintech.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FundAnalysisDTO {
    private LocalDate date;
    private String fundName;
    private BigDecimal totalValue;
    private Map<String, HoldingDetails> holdings;

    @Data
    @Builder
    public static class HoldingDetails {
        private String symbol;
        private String companyName;
        private BigDecimal proportion;
        private BigDecimal price;
        private BigDecimal value;
    }
} 