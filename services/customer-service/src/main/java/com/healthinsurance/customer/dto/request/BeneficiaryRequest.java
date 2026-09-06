package com.healthinsurance.customer.dto.request;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class BeneficiaryRequest {
    @NotBlank(message = "Name is required")
    private String name;
    @Positive(message = "Allocation percentage must be positive")
    private double allocationPercentage;
    @NotBlank(message = "Relationship is required")
    private String relationship;
}