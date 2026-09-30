package com.healthinsurance.policy.dto.request;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class PolicyCoverageRequest {
    @NotBlank(message = "Coverage name is required")
    @Size(min = 2, max = 100, message = "Coverage name must be between 2 and 100 characters")
    private String coverageName;

    @NotNull(message = "Coverage amount is required")
    @DecimalMin(value = "0.01", message = "Coverage amount must be greater than zero")
    private BigDecimal coverageAmount;

    @NotNull(message = "Deductible is required")
    @DecimalMin(value = "0.0", message = "Deductible must be positive or zero")
    private BigDecimal deductible;
}