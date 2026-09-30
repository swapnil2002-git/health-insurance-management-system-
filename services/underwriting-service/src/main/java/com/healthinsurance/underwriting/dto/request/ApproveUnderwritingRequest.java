package com.healthinsurance.underwriting.dto.request;
import com.healthinsurance.underwriting.enums.UnderwritingDecisionType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ApproveUnderwritingRequest {
    @NotNull(message = "Specific approval type is required")
    private UnderwritingDecisionType decisionType; // Must be APPROVED, APPROVED_WITH_LOADING, or APPROVED_WITH_EXCLUSION
    
    @Size(max = 500, message = "Approval reason cannot exceed 500 characters")
    private String reason;

    @Size(max = 1000, message = "Approval notes cannot exceed 1000 characters")
    private String notes;
}