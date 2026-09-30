package com.healthinsurance.reporting.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClaimReportResponse {
    private LocalDate startDate;
    private LocalDate endDate;
    private Long overallClaimsSubmitted;
    private Long overallClaimsApproved;
    private Long overallClaimsRejected;
    private Long overallClaimsSettled;
    private BigDecimal overallClaimedAmount;
    private BigDecimal overallApprovedAmount;
    private Double approvalRatePercentage;
    private List<ClaimSummaryDto> dailySummaries;
}
