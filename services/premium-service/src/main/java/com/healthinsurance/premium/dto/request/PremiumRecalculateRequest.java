package com.healthinsurance.premium.dto.request;

import com.healthinsurance.premium.enums.PaymentFrequency;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PremiumRecalculateRequest {

    private BigDecimal basePremium;

    private BigDecimal riderPremium;

    private BigDecimal riskLoading;

    private BigDecimal discount;

    private BigDecimal tax;

    private PaymentFrequency paymentFrequency;
}