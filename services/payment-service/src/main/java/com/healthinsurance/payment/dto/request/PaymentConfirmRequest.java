package com.healthinsurance.payment.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentConfirmRequest {
    private String gatewayReference;
    private boolean isSuccess = true;
    private String failureReason;
}