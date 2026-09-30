package com.healthinsurance.policy.service;

import com.healthinsurance.policy.dto.request.PolicyRenewalQuoteRequest;
import com.healthinsurance.policy.dto.request.RenewalPaymentRequest;
import com.healthinsurance.policy.dto.request.RenewalRejectRequest;
import com.healthinsurance.policy.dto.response.PolicyRenewalResponse;
import com.healthinsurance.policy.dto.response.RenewalEligibilityResponse;

import java.util.List;
import java.util.UUID;

public interface RenewalService {

    RenewalEligibilityResponse checkEligibility(UUID policyId);

    PolicyRenewalResponse generateRenewalQuote(UUID policyId, PolicyRenewalQuoteRequest request);

    List<PolicyRenewalResponse> getRenewalsByPolicy(UUID policyId);

    PolicyRenewalResponse getRenewal(UUID policyId, UUID renewalId);

    PolicyRenewalResponse acceptRenewal(UUID policyId, UUID renewalId);

    PolicyRenewalResponse completeRenewal(UUID policyId, UUID renewalId, RenewalPaymentRequest request);

    PolicyRenewalResponse rejectRenewal(UUID policyId, UUID renewalId, RenewalRejectRequest request);
}
