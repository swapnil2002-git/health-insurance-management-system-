package com.healthinsurance.payment.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentConfirmRequest {
    @NotBlank(message = "Gateway reference is required")
    @Size(min = 3, max = 100, message = "Gateway reference must be between 3 and 100 characters")
    private String gatewayReference;

    private boolean isSuccess = true;

    @Size(max = 500, message = "Failure reason cannot exceed 500 characters")
    private String failureReason;
}