package com.healthinsurance.claims.dto;

import com.healthinsurance.claims.domain.PayeeType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClaimSettlementRequest {

    @NotNull(message = "Settlement amount is required")
    @Positive(message = "Settlement amount must be greater than zero")
    @jakarta.validation.constraints.DecimalMin(value = "0.01", message = "Settlement amount must be at least 0.01")
    private BigDecimal paidAmount;

    @NotNull(message = "Payee type is required")
    private PayeeType payeeType;

    @jakarta.validation.constraints.NotBlank(message = "Payment reference number is required")
    @jakarta.validation.constraints.Size(min = 3, max = 100, message = "Payment reference number must be between 3 and 100 characters")
    private String paymentReferenceNumber;
}
