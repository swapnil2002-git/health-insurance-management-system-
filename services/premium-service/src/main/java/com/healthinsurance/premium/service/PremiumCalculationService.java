package com.healthinsurance.premium.service;

import com.healthinsurance.premium.entity.PremiumInstallment;
import com.healthinsurance.premium.entity.PremiumSchedule;
import com.healthinsurance.premium.enums.PaymentFrequency;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface PremiumCalculationService {

    BigDecimal calculateTotalPremium(BigDecimal basePremium, BigDecimal riderPremium,
                                    BigDecimal riskLoading, BigDecimal discount, BigDecimal tax);

    List<PremiumInstallment> generateInstallments(PremiumSchedule schedule, BigDecimal totalPremium,
                                                 PaymentFrequency frequency, LocalDate startDate);

    int getNumberOfInstallments(PaymentFrequency frequency);
}