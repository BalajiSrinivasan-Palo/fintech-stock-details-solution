package com.fintech.repository;

import com.fintech.model.MarketData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.time.LocalDate;

@Repository
public interface MarketDataRepository extends JpaRepository<MarketData, Long> {
    Optional<MarketData> findBySymbolAndTimestamp(String symbol, LocalDateTime timestamp);
    
    @Query("SELECT DISTINCT m.symbol FROM MarketData m")
    Set<String> findDistinctSymbols();
    
    boolean existsBySymbolAndTimestamp(String symbol, LocalDateTime timestamp);
    
    default boolean existsBySymbolAndDate(String symbol, LocalDate date) {
        return existsBySymbolAndTimestamp(symbol, date.atTime(16, 0));
    }

    @Query("SELECT DISTINCT m.symbol, DATE(m.timestamp) as date FROM MarketData m " +
           "WHERE DATE(m.timestamp) = :date")
    Set<String> findExistingSymbolsForDate(LocalDate date);

    List<MarketData> findBySymbolAndTimestampBetween(
        String symbol, 
        LocalDateTime startTime, 
        LocalDateTime endTime
    );
} 