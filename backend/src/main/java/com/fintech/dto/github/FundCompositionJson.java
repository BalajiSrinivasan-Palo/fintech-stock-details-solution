package com.fintech.dto.github;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
public class FundCompositionJson {
    private Map<String, Fund> funds;

    @Data
    public static class Fund {
        private String date;
        private List<Position> positions;
    }

    @Data
    public static class Position {
        private String ticker;
        private BigDecimal proportion;
    }

    // Helper method to convert to the format expected by the service
    public List<FundComposition> getFundCompositions() {
        if (funds == null) {
            return List.of(); // or return null based on your preference
        }
        
        return funds.entrySet().stream()
            .map(entry -> {
                FundComposition comp = new FundComposition();
                comp.setName(entry.getKey());
                comp.setPositions(entry.getValue().getPositions());
                return comp;
            })
            .toList();
    }

    @Data
    public static class FundComposition {
        private String name;
        private BigDecimal totalUnits;
        private List<Position> positions;
    }

    public String getDate() {
        return funds.values().stream()
            .findFirst()
            .map(Fund::getDate)
            .orElse(null);
    }
} 