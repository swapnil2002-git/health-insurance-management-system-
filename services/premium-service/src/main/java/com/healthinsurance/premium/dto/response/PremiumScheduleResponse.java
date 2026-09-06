package com.healthinsurance.premium.dto.response;

import com.healthinsurance.premium.enums.PaymentFrequency;
import com.healthinsurance.premium.enums.PremiumStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PremiumScheduleResponse {
    private UUID scheduleId;
    private UUID policyId;
    private BigDecimal totalPremium;
    private PaymentFrequency paymentFrequency;
    private Integer numberOfInstallments;
    private BigDecimal paidAmount;
    private BigDecimal outstandingAmount;
    private PremiumStatus premiumStatus;
    private LocalDate startDate;
    private LocalDate endDate;
    private Instant createdAt;
    private Instant updatedAt;
    private List<PremiumInstallmentResponse> installments;
}