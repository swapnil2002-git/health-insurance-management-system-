package com.healthinsurance.premium.dto.request;

import com.healthinsurance.premium.enums.PaymentFrequency;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PremiumScheduleCreateRequest {

    @NotNull(message = "Policy ID is required")
    private UUID policyId;

    @NotNull(message = "Payment frequency is required")
    private PaymentFrequency paymentFrequency;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    private LocalDate endDate;

    @NotNull(message = "Base premium is required")
    @Positive(message = "Base premium must be greater than zero")
    @DecimalMin(value = "1.00", message = "Base premium must be at least 1.00")
    private BigDecimal basePremium;

    @DecimalMin(value = "0.0", message = "Rider premium cannot be negative")
    private BigDecimal riderPremium = BigDecimal.ZERO;

    @DecimalMin(value = "0.0", message = "Risk loading cannot be negative")
    private BigDecimal riskLoading = BigDecimal.ZERO;

    @DecimalMin(value = "0.0", message = "Discount cannot be negative")
    private BigDecimal discount = BigDecimal.ZERO;

    @DecimalMin(value = "0.0", message = "Tax cannot be negative")
    private BigDecimal tax = BigDecimal.ZERO;
}