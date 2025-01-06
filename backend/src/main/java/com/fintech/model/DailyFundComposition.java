package com.fintech.model;

import lombok.Data;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Data
public class DailyFundComposition {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "fund_id")
    private Fund fund;

    private LocalDate date;
    private String symbol;
    private BigDecimal proportion;  // Stored as percentage (e.g., 25.5 for 25.5%)
    private BigDecimal unitPrice;   // Price per unit for this composition
} 