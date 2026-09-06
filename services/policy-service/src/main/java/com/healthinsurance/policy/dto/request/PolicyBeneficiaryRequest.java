package com.healthinsurance.policy.dto.request;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class PolicyBeneficiaryRequest {
    @NotBlank
    private String beneficiaryName;
    @NotBlank
    private String relationship;
    @NotNull
    private BigDecimal percentage;
}