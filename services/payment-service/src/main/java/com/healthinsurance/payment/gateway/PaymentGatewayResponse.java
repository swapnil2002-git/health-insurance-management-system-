package com.healthinsurance.payment.gateway;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentGatewayResponse {
    private boolean isSuccess;
    private String gatewayReference;
    private String errorMessage;
}