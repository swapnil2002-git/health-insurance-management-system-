package com.healthinsurance.policy.service;

import com.healthinsurance.policy.dto.request.CancellationApprovalRequest;
import com.healthinsurance.policy.dto.request.CancellationRejectRequest;
import com.healthinsurance.policy.dto.request.PolicyCancellationRequest;
import com.healthinsurance.policy.dto.response.PolicyCancellationResponse;

import java.util.UUID;

public interface CancellationService {

    PolicyCancellationResponse requestCancellation(UUID policyId, PolicyCancellationRequest request);

    PolicyCancellationResponse getCancellation(UUID policyId);

    PolicyCancellationResponse approveCancellation(UUID policyId, CancellationApprovalRequest request);

    PolicyCancellationResponse rejectCancellation(UUID policyId, CancellationRejectRequest request);
}
