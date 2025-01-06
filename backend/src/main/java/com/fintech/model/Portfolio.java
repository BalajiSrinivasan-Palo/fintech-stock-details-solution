package com.fintech.model;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "portfolios")
@Data
public class Portfolio {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private LocalDate date;

    @CreatedDate
    private LocalDateTime createdAt;

    @Column(name = "total_units")
    private BigDecimal totalUnits;

    @OneToMany(mappedBy = "portfolio", fetch = FetchType.LAZY)
    private List<PortfolioHolding> holdings;

    // Calculate total units from holdings
    public BigDecimal getTotalUnits() {
        if (totalUnits == null) {
            totalUnits = holdings.stream()
                .map(PortfolioHolding::getUnits)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        }
        return totalUnits;
    }
}