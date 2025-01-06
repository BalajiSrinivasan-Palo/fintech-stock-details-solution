package com.fintech.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.Map;

@Data
public class AlphaVantageResponse {
    private GlobalQuote globalQuote;
    private Map<String, DailyData> timeSeriesDaily;

    @Data
    public static class GlobalQuote {
        private BigDecimal price;
    }

    @Data
    public static class DailyData {
        private BigDecimal open;
        private BigDecimal high;
        private BigDecimal low;
        private BigDecimal close;
    }
} 