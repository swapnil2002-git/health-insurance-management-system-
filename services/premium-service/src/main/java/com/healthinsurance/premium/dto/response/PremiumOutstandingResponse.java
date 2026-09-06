package com.healthinsurance.premium.dto.response;

import com.healthinsurance.premium.enums.PremiumStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PremiumOutstandingResponse {
    private UUID policyId;
    private UUID scheduleId;
    private BigDecimal totalPremium;
    private BigDecimal paidAmount;
    private BigDecimal outstandingAmount;
    private PremiumStatus status;
}