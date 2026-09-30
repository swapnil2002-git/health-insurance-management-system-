package com.healthinsurance.policy.service;

import com.healthinsurance.policy.dto.request.EndorsementApprovalRequest;
import com.healthinsurance.policy.dto.request.EndorsementRejectRequest;
import com.healthinsurance.policy.dto.request.PolicyEndorsementRequest;
import com.healthinsurance.policy.dto.response.PolicyEndorsementResponse;

import java.util.List;
import java.util.UUID;

public interface EndorsementService {

    PolicyEndorsementResponse requestEndorsement(UUID policyId, PolicyEndorsementRequest request);

    PolicyEndorsementResponse getEndorsement(UUID policyId, UUID endorsementId);

    List<PolicyEndorsementResponse> getEndorsementsByPolicy(UUID policyId);

    PolicyEndorsementResponse approveEndorsement(UUID policyId, UUID endorsementId, EndorsementApprovalRequest request);

    PolicyEndorsementResponse rejectEndorsement(UUID policyId, UUID endorsementId, EndorsementRejectRequest request);
}
