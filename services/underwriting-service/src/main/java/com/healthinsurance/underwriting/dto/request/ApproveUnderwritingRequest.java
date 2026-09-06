package com.healthinsurance.underwriting.dto.request;
import com.healthinsurance.underwriting.enums.UnderwritingDecisionType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ApproveUnderwritingRequest {
    @NotNull(message = "Specific approval type is required")
    private UnderwritingDecisionType decisionType; // Must be APPROVED, APPROVED_WITH_LOADING, or APPROVED_WITH_EXCLUSION
    
    private String reason;
    private String notes;
}