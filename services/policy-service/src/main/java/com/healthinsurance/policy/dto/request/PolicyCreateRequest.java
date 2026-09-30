package com.healthinsurance.policy.dto.request;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
public class PolicyCreateRequest {
    @NotNull(message = "Customer ID is required")
    private UUID customerId;
    @NotNull(message = "Quote ID is required")
    private UUID quoteId;
    @NotNull(message = "Plan ID is required")
    private UUID planId;
    
    @NotNull(message = "Effective date is required")
    private Instant effectiveDate;
    @NotNull(message = "Expiry date is required")
    private Instant expiryDate;

    @NotEmpty(message = "At least one member is required")
    @jakarta.validation.constraints.Size(min = 1, max = 20, message = "Members count cannot exceed 20")
    @Valid
    private List<PolicyMemberRequest> members;

    // @NotEmpty removed for MVP testing
    @jakarta.validation.constraints.Size(max = 20, message = "Coverages count cannot exceed 20")
    @Valid
    private List<PolicyCoverageRequest> coverages;

    @jakarta.validation.constraints.Size(max = 20, message = "Beneficiaries count cannot exceed 20")
    @Valid
    private List<PolicyBeneficiaryRequest> beneficiaries;
}