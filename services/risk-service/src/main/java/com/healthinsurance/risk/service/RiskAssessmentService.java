package com.healthinsurance.risk.service;

import com.healthinsurance.risk.dto.request.CreateRiskAssessmentRequest;
import com.healthinsurance.risk.dto.response.RiskAssessmentResponse;
import java.util.UUID;

public interface RiskAssessmentService {
    RiskAssessmentResponse createAssessment(CreateRiskAssessmentRequest request);
    RiskAssessmentResponse getAssessment(UUID id);
    RiskAssessmentResponse calculateRisk(UUID id);
}