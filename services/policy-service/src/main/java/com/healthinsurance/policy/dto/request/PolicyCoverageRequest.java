package com.healthinsurance.policy.dto.request;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class PolicyCoverageRequest {
    @NotBlank(message = "Coverage name is required")
    private String coverageName;
    @NotNull
    private BigDecimal coverageAmount;
    @NotNull
    private BigDecimal deductible;
}