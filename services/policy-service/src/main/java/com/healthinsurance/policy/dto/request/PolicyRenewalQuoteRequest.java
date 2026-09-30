package com.healthinsurance.policy.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PolicyRenewalQuoteRequest {
    private BigDecimal customizedSumInsured;
    private String remarks;
}
