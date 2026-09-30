package com.healthinsurance.reporting.repository;

import com.healthinsurance.reporting.entity.PremiumDailySummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PremiumDailySummaryRepository extends JpaRepository<PremiumDailySummary, Long> {

    Optional<PremiumDailySummary> findBySummaryDate(LocalDate summaryDate);

    List<PremiumDailySummary> findBySummaryDateBetweenOrderBySummaryDateAsc(LocalDate startDate, LocalDate endDate);

    @Query("SELECT COALESCE(SUM(p.totalPaymentsCollected), 0) FROM PremiumDailySummary p")
    Long sumTotalPaymentsCollected();

    @Query("SELECT COALESCE(SUM(p.totalPremiumAmount), 0) FROM PremiumDailySummary p")
    BigDecimal sumTotalPremiumAmount();
}
