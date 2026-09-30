package com.healthinsurance.reporting.repository;

import com.healthinsurance.reporting.entity.PolicyDailySummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PolicyDailySummaryRepository extends JpaRepository<PolicyDailySummary, Long> {

    Optional<PolicyDailySummary> findBySummaryDate(LocalDate summaryDate);

    List<PolicyDailySummary> findBySummaryDateBetweenOrderBySummaryDateAsc(LocalDate startDate, LocalDate endDate);

    @Query("SELECT COALESCE(SUM(p.totalPoliciesIssued), 0) FROM PolicyDailySummary p")
    Long sumTotalPoliciesIssued();

    @Query("SELECT COALESCE(SUM(p.totalPoliciesActive), 0) FROM PolicyDailySummary p")
    Long sumTotalPoliciesActive();

    @Query("SELECT COALESCE(SUM(p.totalPoliciesExpired), 0) FROM PolicyDailySummary p")
    Long sumTotalPoliciesExpired();

    @Query("SELECT COALESCE(SUM(p.totalPoliciesCancelled), 0) FROM PolicyDailySummary p")
    Long sumTotalPoliciesCancelled();
}
