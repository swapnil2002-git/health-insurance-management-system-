package com.healthinsurance.premium.service.impl;

import com.healthinsurance.premium.entity.PremiumInstallment;
import com.healthinsurance.premium.entity.PremiumSchedule;
import com.healthinsurance.premium.enums.InstallmentStatus;
import com.healthinsurance.premium.enums.PaymentFrequency;
import com.healthinsurance.premium.service.PremiumCalculationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class PremiumCalculationServiceImpl implements PremiumCalculationService {

    @Override
    public BigDecimal calculateTotalPremium(BigDecimal basePremium, BigDecimal riderPremium,
                                            BigDecimal riskLoading, BigDecimal discount, BigDecimal tax) {
        BigDecimal base = basePremium != null ? basePremium : BigDecimal.ZERO;
        BigDecimal rider = riderPremium != null ? riderPremium : BigDecimal.ZERO;
        BigDecimal risk = riskLoading != null ? riskLoading : BigDecimal.ZERO;
        BigDecimal disc = discount != null ? discount : BigDecimal.ZERO;
        BigDecimal tx = tax != null ? tax : BigDecimal.ZERO;

        BigDecimal total = base.add(rider).add(risk).subtract(disc).add(tx);

        if (total.compareTo(BigDecimal.ZERO) <= 0) {
            log.warn("Calculated total premium is non-positive: {}. Defaulting to base premium.", total);
            return base.setScale(2, RoundingMode.HALF_UP);
        }

        return total.setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public int getNumberOfInstallments(PaymentFrequency frequency) {
        if (frequency == null) {
            return 1;
        }
        switch (frequency) {
            case MONTHLY:
                return 12;
            case QUARTERLY:
                return 4;
            case ANNUAL:
            default:
                return 1;
        }
    }

    @Override
    public List<PremiumInstallment> generateInstallments(PremiumSchedule schedule, BigDecimal totalPremium,
                                                        PaymentFrequency frequency, LocalDate startDate) {
        int count = getNumberOfInstallments(frequency);
        List<PremiumInstallment> installments = new ArrayList<>(count);

        BigDecimal countBigDecimal = BigDecimal.valueOf(count);
        BigDecimal regularAmount = totalPremium.divide(countBigDecimal, 2, RoundingMode.HALF_UP);

        BigDecimal runningTotal = BigDecimal.ZERO;

        for (int i = 1; i <= count; i++) {
            PremiumInstallment installment = new PremiumInstallment();
            installment.setPremiumSchedule(schedule);
            installment.setInstallmentNumber(i);

            LocalDate dueDate;
            if (frequency == PaymentFrequency.MONTHLY) {
                dueDate = startDate.plusMonths(i - 1);
            } else if (frequency == PaymentFrequency.QUARTERLY) {
                dueDate = startDate.plusMonths((long) (i - 1) * 3);
            } else {
                dueDate = startDate;
            }
            installment.setDueDate(dueDate);

            BigDecimal installmentAmount;
            if (i == count) {
                // Adjust the final installment for any rounding difference
                installmentAmount = totalPremium.subtract(runningTotal);
            } else {
                installmentAmount = regularAmount;
                runningTotal = runningTotal.add(installmentAmount);
            }

            installment.setAmount(installmentAmount);
            installment.setPaidAmount(BigDecimal.ZERO);
            installment.setOutstandingAmount(installmentAmount);
            installment.setStatus(InstallmentStatus.PENDING);
            installment.setCreatedAt(Instant.now());
            installment.setUpdatedAt(Instant.now());

            installments.add(installment);
        }

        log.info("Generated {} installments for schedule with frequency {}, total: {}", count, frequency, totalPremium);
        return installments;
    }
}