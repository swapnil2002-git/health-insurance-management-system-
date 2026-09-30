package com.healthinsurance.reporting.service;

import com.healthinsurance.reporting.dto.ClaimReportResponse;
import com.healthinsurance.reporting.dto.DashboardSummaryResponse;
import com.healthinsurance.reporting.dto.PolicyReportResponse;
import com.healthinsurance.reporting.dto.PremiumReportResponse;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface ReportingService {

    void recordPolicyIssued(String eventId, LocalDate eventDate, String status);

    void recordPremiumPayment(String eventId, LocalDate eventDate, BigDecimal amount);

    void recordClaimEvent(String eventId, LocalDate eventDate, String eventType, String status, BigDecimal claimedAmount, BigDecimal approvedAmount);

    PolicyReportResponse getPolicyReport(LocalDate startDate, LocalDate endDate);

    ClaimReportResponse getClaimReport(LocalDate startDate, LocalDate endDate);

    PremiumReportResponse getPremiumReport(LocalDate startDate, LocalDate endDate);

    DashboardSummaryResponse getDashboardSummary();
}
