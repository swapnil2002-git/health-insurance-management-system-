package com.healthinsurance.payment.dto.response;

import com.healthinsurance.payment.enums.PaymentMethod;
import com.healthinsurance.payment.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {
    private UUID paymentId;
    private UUID policyId;
    private BigDecimal amount;
    private String currency;
    private PaymentMethod paymentMethod;
    private PaymentStatus status;
    private String gatewayReference;
    private String failureReason;
    private Instant createdAt;
    private Instant updatedAt;
}