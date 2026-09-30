package com.healthinsurance.reporting.repository;

import com.healthinsurance.reporting.entity.ClaimDailySummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ClaimDailySummaryRepository extends JpaRepository<ClaimDailySummary, Long> {

    Optional<ClaimDailySummary> findBySummaryDate(LocalDate summaryDate);

    List<ClaimDailySummary> findBySummaryDateBetweenOrderBySummaryDateAsc(LocalDate startDate, LocalDate endDate);

    @Query("SELECT COALESCE(SUM(c.totalClaimsSubmitted), 0) FROM ClaimDailySummary c")
    Long sumTotalClaimsSubmitted();

    @Query("SELECT COALESCE(SUM(c.totalClaimsApproved), 0) FROM ClaimDailySummary c")
    Long sumTotalClaimsApproved();

    @Query("SELECT COALESCE(SUM(c.totalClaimsRejected), 0) FROM ClaimDailySummary c")
    Long sumTotalClaimsRejected();

    @Query("SELECT COALESCE(SUM(c.totalClaimsSettled), 0) FROM ClaimDailySummary c")
    Long sumTotalClaimsSettled();

    @Query("SELECT COALESCE(SUM(c.totalClaimedAmount), 0) FROM ClaimDailySummary c")
    BigDecimal sumTotalClaimedAmount();

    @Query("SELECT COALESCE(SUM(c.totalApprovedAmount), 0) FROM ClaimDailySummary c")
    BigDecimal sumTotalApprovedAmount();
}
