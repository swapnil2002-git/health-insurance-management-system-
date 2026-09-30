package com.healthinsurance.claims.dto;

import com.healthinsurance.claims.domain.ClaimStatus;
import com.healthinsurance.claims.domain.ClaimType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClaimResponse {

    private UUID claimId;
    private String claimNumber;
    private UUID policyId;
    private UUID memberId;
    private UUID providerId;
    private ClaimType claimType;
    private ClaimStatus status;
    private LocalDate serviceDate;
    private LocalDate admissionDate;
    private LocalDate dischargeDate;
    private BigDecimal totalClaimAmount;
    private BigDecimal approvedAmount;
    private String remarks;
    private Instant createdAt;
    private Instant updatedAt;

    private List<ClaimServiceResponse> serviceLines;
    private List<ClaimDiagnosisResponse> diagnoses;
    private List<ClaimDocumentResponse> documents;
    private List<ClaimValidationResponse> validations;
    private ClaimAdjudicationResponse adjudication;
    private List<ClaimPaymentResponse> payments;
    private ExplanationOfBenefitsResponse explanationOfBenefits;
}
