package com.healthinsurance.policy.dto.request;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PolicyEndorsementRequest {
    @NotBlank(message = "Endorsement description is required")
    private String description;
}