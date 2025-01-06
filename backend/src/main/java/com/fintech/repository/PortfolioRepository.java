package com.fintech.repository;

import com.fintech.model.Portfolio;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {
    List<Portfolio> findByDate(LocalDate date);
    boolean existsByNameAndDate(String name, LocalDate date);
    Optional<Portfolio> findByName(String name);
    Page<Portfolio> findByNameContainingIgnoreCaseAndDateBetween(
        String name, LocalDate startDate, LocalDate endDate, Pageable pageable);
    
    Page<Portfolio> findByDateBetween(
        LocalDate startDate, LocalDate endDate, Pageable pageable);
}