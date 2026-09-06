package com.healthinsurance.payment.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentAllocationResponse {
    private UUID allocationId;
    private UUID paymentId;
    private UUID installmentId;
    private BigDecimal allocatedAmount;
    private Instant allocatedAt;
}