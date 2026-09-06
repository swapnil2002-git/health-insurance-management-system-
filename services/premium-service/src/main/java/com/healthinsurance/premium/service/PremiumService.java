package com.healthinsurance.premium.service;

import com.healthinsurance.premium.dto.request.InstallmentPaymentRequest;
import com.healthinsurance.premium.dto.request.PremiumRecalculateRequest;
import com.healthinsurance.premium.dto.request.PremiumScheduleCreateRequest;
import com.healthinsurance.premium.dto.response.PremiumInstallmentResponse;
import com.healthinsurance.premium.dto.response.PremiumOutstandingResponse;
import com.healthinsurance.premium.dto.response.PremiumScheduleResponse;

import java.util.UUID;

public interface PremiumService {

    PremiumScheduleResponse createPremiumSchedule(PremiumScheduleCreateRequest request);

    PremiumScheduleResponse getPremiumByPolicy(UUID policyId);

    PremiumOutstandingResponse getOutstanding(UUID policyId);

    PremiumScheduleResponse recalculatePremium(UUID policyId, PremiumRecalculateRequest request);

    PremiumInstallmentResponse recordInstallmentPayment(UUID installmentId, InstallmentPaymentRequest request);
}