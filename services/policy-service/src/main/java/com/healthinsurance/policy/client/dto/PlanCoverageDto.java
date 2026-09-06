package com.healthinsurance.policy.client.dto;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class PlanCoverageDto {
    private String coverageName;
    private BigDecimal coverageAmount;
    private BigDecimal deductible;
}