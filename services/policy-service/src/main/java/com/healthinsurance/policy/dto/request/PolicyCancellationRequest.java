package com.healthinsurance.policy.dto.request;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PolicyCancellationRequest {
    @NotBlank(message = "Cancellation reason is required")
    private String reason;
}