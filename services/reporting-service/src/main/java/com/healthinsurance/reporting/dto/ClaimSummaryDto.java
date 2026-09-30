package com.healthinsurance.reporting.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClaimSummaryDto {
    private LocalDate summaryDate;
    private Long totalClaimsSubmitted;
    private Long totalClaimsApproved;
    private Long totalClaimsRejected;
    private Long totalClaimsSettled;
    private BigDecimal totalClaimedAmount;
    private BigDecimal totalApprovedAmount;
}
