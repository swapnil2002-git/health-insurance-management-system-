package com.healthinsurance.policy.dto.response;
import lombok.Data;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
public class PolicyResponse {
    private UUID policyId;
    private String policyNumber;
    private UUID customerId;
    private UUID planId;
    private UUID quoteId;
    private String status;
    private Instant effectiveDate;
    private Instant expiryDate;
    private Instant createdAt;
    
    private List<PolicyMemberResponse> members;
    private List<PolicyCoverageResponse> coverages;
    private List<PolicyBeneficiaryResponse> beneficiaries;
}