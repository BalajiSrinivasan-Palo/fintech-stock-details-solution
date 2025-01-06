package com.fintech.repository;

import com.fintech.model.Investment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Set;

@Repository
public interface InvestmentRepository extends JpaRepository<Investment, Long> {
    List<Investment> findByPortfolioId(Long portfolioId);
    List<Investment> findBySymbol(String symbol);
    @Query("SELECT DISTINCT i.symbol FROM Investment i")
    Set<String> findDistinctSymbols();
}