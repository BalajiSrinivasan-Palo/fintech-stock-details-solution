package com.fintech.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.Map;

@Data
public class FundCompositionData {
    private Map<String, Map<String, BigDecimal>> funds;
} 