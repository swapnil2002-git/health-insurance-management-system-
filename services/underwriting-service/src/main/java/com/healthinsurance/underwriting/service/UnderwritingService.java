package com.healthinsurance.underwriting.service;

import com.healthinsurance.underwriting.dto.request.*;
import com.healthinsurance.underwriting.dto.response.UnderwritingCaseResponse;
import java.util.List;
import java.util.UUID;

public interface UnderwritingService {
    UnderwritingCaseResponse createCase(CreateUnderwritingCaseRequest request);
    UnderwritingCaseResponse createCaseFromEvent(CreateUnderwritingCaseRequest request);
    UnderwritingCaseResponse getCase(UUID caseId);
    List<UnderwritingCaseResponse> getAllCases();
    List<UnderwritingCaseResponse> getCasesByCustomerId(UUID customerId);
    List<UnderwritingCaseResponse> getCasesByQuoteId(UUID quoteId);
    
    UnderwritingCaseResponse approve(UUID caseId, ApproveUnderwritingRequest request);
    UnderwritingCaseResponse reject(UUID caseId, RejectUnderwritingRequest request);
    UnderwritingCaseResponse refer(UUID caseId, ReferUnderwritingRequest request);
}