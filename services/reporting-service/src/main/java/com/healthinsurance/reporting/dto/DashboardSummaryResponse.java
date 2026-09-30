package com.healthinsurance.reporting.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardSummaryResponse {
    private Long totalPoliciesIssued;
    private Long totalPoliciesActive;
    private Long totalPoliciesExpired;
    private Long totalPoliciesCancelled;
    private Long totalClaimsSubmitted;
    private Long totalClaimsApproved;
    private Long totalClaimsRejected;
    private Long totalClaimsSettled;
    private BigDecimal totalClaimedAmount;
    private BigDecimal totalApprovedAmount;
    private Long totalPaymentsCollected;
    private BigDecimal totalPremiumAmount;
    private Double claimApprovalRate;
    private LocalDateTime generatedAt;
}
