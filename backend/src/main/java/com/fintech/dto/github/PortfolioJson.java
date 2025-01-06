package com.fintech.dto.github;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PortfolioJson {
    private String date;
    private List<Fund> funds;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Fund {
        private String name;
        private BigDecimal units_owned;
    }

    public Map<String, FundData> getFundsAsMap() {
        return funds.stream()
            .collect(Collectors.toMap(
                fund -> fund.getName(),
                fund -> {
                    FundData data = new FundData();
                    data.setTotalUnits(fund.getUnits_owned());
                    data.setDate(this.date);
                    return data;
                }
            ));
    }

    @Data
    public static class FundData {
        private BigDecimal totalUnits;
        private String date;
    }
} 