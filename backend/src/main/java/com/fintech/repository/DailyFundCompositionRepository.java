package com.fintech.repository;

import com.fintech.model.DailyFundComposition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Repository
public interface DailyFundCompositionRepository extends JpaRepository<DailyFundComposition, Long> {
    List<DailyFundComposition> findByDate(LocalDate date);
    boolean existsByDate(LocalDate date);
    void deleteByDate(LocalDate date);
    
    @Query("SELECT DISTINCT d.symbol FROM DailyFundComposition d")
    Set<String> findAllDistinctSymbols();

    @Query("SELECT DISTINCT d.symbol FROM DailyFundComposition d " +
           "WHERE d.date = :date")
    Set<String> findDistinctSymbolsByDate(LocalDate date);

    @Query("SELECT d FROM DailyFundComposition d " +
           "WHERE d.date BETWEEN :startDate AND :endDate " +
           "AND (:fundName = '' OR LOWER(d.fund.name) LIKE LOWER(CONCAT('%', :fundName, '%')))")
    Page<DailyFundComposition> findByDateBetweenAndFundNameContainingIgnoreCase(
        LocalDate startDate, 
        LocalDate endDate, 
        String fundName, 
        Pageable pageable
    );

    boolean existsByDateBetween(LocalDate startDate, LocalDate endDate);
} 