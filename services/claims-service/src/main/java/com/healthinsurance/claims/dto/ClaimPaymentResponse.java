package com.healthinsurance.claims.dto;

import com.healthinsurance.claims.domain.PayeeType;
import com.healthinsurance.claims.domain.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClaimPaymentResponse {

    private UUID claimPaymentId;
    private UUID claimId;
    private String paymentReferenceNumber;
    private BigDecimal paidAmount;
    private PayeeType payeeType;
    private PaymentStatus paymentStatus;
    private Instant settledAt;
    private Instant createdAt;
}
