package com.healthinsurance.policy.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PolicyCancellationRequest {
    @NotBlank(message = "Cancellation reason is required")
    @Size(min = 3, max = 500, message = "Cancellation reason must be between 3 and 500 characters")
    private String reason;

    @Size(max = 100, message = "Requested by cannot exceed 100 characters")
    private String requestedBy;
}