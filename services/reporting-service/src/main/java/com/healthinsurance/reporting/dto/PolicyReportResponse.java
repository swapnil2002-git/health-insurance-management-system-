package com.healthinsurance.reporting.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PolicyReportResponse {
    private LocalDate startDate;
    private LocalDate endDate;
    private Long overallPoliciesIssued;
    private Long overallPoliciesActive;
    private Long overallPoliciesExpired;
    private Long overallPoliciesCancelled;
    private List<PolicySummaryDto> dailySummaries;
}
