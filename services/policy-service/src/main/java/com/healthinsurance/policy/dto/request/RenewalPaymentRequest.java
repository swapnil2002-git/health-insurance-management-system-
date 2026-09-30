package com.healthinsurance.policy.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RenewalPaymentRequest {
    @NotNull(message = "Payment ID is required to complete renewal")
    private UUID paymentId;
}
