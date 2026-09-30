package com.healthinsurance.reporting.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PolicySummaryDto {
    private LocalDate summaryDate;
    private Long totalPoliciesIssued;
    private Long totalPoliciesActive;
    private Long totalPoliciesExpired;
    private Long totalPoliciesCancelled;
}
