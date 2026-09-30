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
public class PremiumSummaryDto {
    private LocalDate summaryDate;
    private Long totalPaymentsCollected;
    private BigDecimal totalPremiumAmount;
}
