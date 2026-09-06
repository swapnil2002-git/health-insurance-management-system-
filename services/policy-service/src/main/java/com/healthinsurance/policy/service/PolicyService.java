package com.healthinsurance.policy.service;

import com.healthinsurance.policy.dto.request.PolicyCancellationRequest;
import com.healthinsurance.policy.dto.request.PolicyCreateRequest;
import com.healthinsurance.policy.dto.request.PolicyEndorsementRequest;
import com.healthinsurance.policy.dto.response.PolicyResponse;

import java.util.List;
import java.util.UUID;

public interface PolicyService {
    PolicyResponse createPolicy(PolicyCreateRequest request);
    PolicyResponse getPolicy(UUID policyId);
    List<PolicyResponse> getAllPolicies();
    
    PolicyResponse issuePolicy(UUID policyId);
    PolicyResponse activatePolicy(UUID policyId);

    PolicyResponse addEndorsement(UUID policyId, PolicyEndorsementRequest request);
    PolicyResponse cancelPolicy(UUID policyId, PolicyCancellationRequest request);
    PolicyResponse renewPolicy(UUID policyId);
}