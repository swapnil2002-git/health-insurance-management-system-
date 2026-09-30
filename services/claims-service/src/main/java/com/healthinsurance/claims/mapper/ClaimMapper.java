package com.healthinsurance.claims.mapper;

import com.healthinsurance.claims.dto.*;
import com.healthinsurance.claims.entity.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ClaimMapper {

    // Claim
    @Mapping(target = "claimId", ignore = true)
    @Mapping(target = "claimNumber", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "approvedAmount", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "serviceLines", ignore = true)
    @Mapping(target = "diagnoses", ignore = true)
    @Mapping(target = "documents", ignore = true)
    @Mapping(target = "validations", ignore = true)
    @Mapping(target = "adjudication", ignore = true)
    @Mapping(target = "payments", ignore = true)
    @Mapping(target = "explanationOfBenefits", ignore = true)
    Claim toEntity(ClaimCreateRequest request);

    @Mapping(target = "serviceLines", source = "serviceLines")
    @Mapping(target = "diagnoses", source = "diagnoses")
    @Mapping(target = "documents", source = "documents")
    @Mapping(target = "validations", source = "validations")
    @Mapping(target = "adjudication", source = "adjudication")
    @Mapping(target = "payments", source = "payments")
    @Mapping(target = "explanationOfBenefits", source = "explanationOfBenefits")
    ClaimResponse toResponse(Claim entity);

    List<ClaimResponse> toClaimResponseList(List<Claim> entities);

    // Claim Service
    @Mapping(target = "claimServiceId", ignore = true)
    @Mapping(target = "claim", ignore = true)
    @Mapping(target = "totalAmount", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    ClaimService toEntity(ClaimServiceRequest request);

    @Mapping(target = "claimId", source = "claim.claimId")
    ClaimServiceResponse toResponse(ClaimService entity);

    List<ClaimServiceResponse> toServiceResponseList(List<ClaimService> entities);

    // Claim Diagnosis
    @Mapping(target = "diagnosisId", ignore = true)
    @Mapping(target = "claim", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    ClaimDiagnosis toEntity(ClaimDiagnosisRequest request);

    @Mapping(target = "claimId", source = "claim.claimId")
    ClaimDiagnosisResponse toResponse(ClaimDiagnosis entity);

    List<ClaimDiagnosisResponse> toDiagnosisResponseList(List<ClaimDiagnosis> entities);

    // Claim Document
    @Mapping(target = "claimDocumentId", ignore = true)
    @Mapping(target = "claim", ignore = true)
    @Mapping(target = "uploadedAt", ignore = true)
    ClaimDocument toEntity(ClaimDocumentRequest request);

    @Mapping(target = "claimId", source = "claim.claimId")
    ClaimDocumentResponse toResponse(ClaimDocument entity);

    List<ClaimDocumentResponse> toDocumentResponseList(List<ClaimDocument> entities);

    // Claim Validation
    @Mapping(target = "claimId", source = "claim.claimId")
    ClaimValidationResponse toResponse(ClaimValidation entity);

    List<ClaimValidationResponse> toValidationResponseList(List<ClaimValidation> entities);

    // Claim Adjudication
    @Mapping(target = "claimId", source = "claim.claimId")
    ClaimAdjudicationResponse toResponse(ClaimAdjudication entity);

    // Claim Payment
    @Mapping(target = "claimId", source = "claim.claimId")
    ClaimPaymentResponse toResponse(ClaimPayment entity);

    List<ClaimPaymentResponse> toPaymentResponseList(List<ClaimPayment> entities);

    // Explanation of Benefits
    @Mapping(target = "claimId", source = "claim.claimId")
    ExplanationOfBenefitsResponse toResponse(ExplanationOfBenefits entity);
}
