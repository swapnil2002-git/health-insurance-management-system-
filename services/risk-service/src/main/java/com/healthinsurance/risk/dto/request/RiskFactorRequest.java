package com.healthinsurance.risk.dto.request;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RiskFactorRequest {
    @NotBlank(message = "Factor name is required (e.g., 'Age', 'Smoker')")
    private String factorName;

    @NotBlank(message = "Factor value is required (e.g., '45', 'YES')")
    private String factorValue;

    private String description;
}