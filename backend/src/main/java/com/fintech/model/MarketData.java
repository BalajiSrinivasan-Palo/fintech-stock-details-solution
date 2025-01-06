package com.fintech.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.springframework.data.annotation.CreatedDate;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "market_data",
    indexes = @Index(name = "idx_symbol_date", columnList = "symbol,timestamp")
)
public class MarketData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String symbol;
    private BigDecimal price;
    
    @Column(columnDefinition = "TIMESTAMP")
    private LocalDateTime timestamp;

    @CreatedDate
    private LocalDateTime createdAt;
    
    public MarketData(String symbol, BigDecimal price, LocalDateTime timestamp) {
        this.symbol = symbol;
        this.price = price;
        this.timestamp = timestamp;
    }
} 