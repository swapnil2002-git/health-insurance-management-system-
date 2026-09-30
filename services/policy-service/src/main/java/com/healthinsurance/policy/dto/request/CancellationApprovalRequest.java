package com.healthinsurance.policy.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CancellationApprovalRequest {

    @NotBlank(message = "Approved by is required")
    private String approvedBy;

    private String remarks;
}
