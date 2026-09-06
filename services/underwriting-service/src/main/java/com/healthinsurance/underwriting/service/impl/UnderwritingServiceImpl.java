package com.healthinsurance.underwriting.service.impl;

import com.healthinsurance.underwriting.client.QuotationClient;
import com.healthinsurance.underwriting.client.RiskAssessmentClient;
import com.healthinsurance.underwriting.dto.request.*;
import com.healthinsurance.underwriting.dto.response.UnderwritingCaseResponse;
import com.healthinsurance.underwriting.entity.UnderwritingCase;
import com.healthinsurance.underwriting.entity.UnderwritingDecision;
import com.healthinsurance.underwriting.enums.CaseStatus;
import com.healthinsurance.underwriting.enums.UnderwritingDecisionType;
import com.healthinsurance.underwriting.event.UnderwritingApprovedEvent;
import com.healthinsurance.underwriting.event.UnderwritingEventProducer;
import com.healthinsurance.underwriting.exception.DependencyNotFoundException;
import com.healthinsurance.underwriting.exception.InvalidUnderwritingStatusException;
import com.healthinsurance.underwriting.exception.UnderwritingCaseNotFoundException;
import com.healthinsurance.underwriting.mapper.UnderwritingMapper;
import com.healthinsurance.underwriting.repository.UnderwritingCaseRepository;
import com.healthinsurance.underwriting.service.UnderwritingService;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashSet;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UnderwritingServiceImpl implements UnderwritingService {

    private final UnderwritingCaseRepository caseRepository;
    private final UnderwritingMapper mapper;
    
    private final QuotationClient quotationClient;
    private final RiskAssessmentClient riskClient;
    private final UnderwritingEventProducer eventProducer;

    @Override
    @Transactional
    public UnderwritingCaseResponse createCase(CreateUnderwritingCaseRequest request) {
        log.info("Creating Underwriting Case manually for Assessment ID: {}", request.getAssessmentId());
        Optional<UnderwritingCase> existingCase = caseRepository.findByAssessmentId(request.getAssessmentId());
        if (existingCase.isPresent()) return mapper.toResponse(existingCase.get());

        try { quotationClient.getQuote(request.getQuoteId()); } 
        catch (FeignException.NotFound e) { throw new DependencyNotFoundException("Quotation not found."); }

        try { riskClient.getAssessment(request.getAssessmentId()); } 
        catch (FeignException.NotFound e) { throw new DependencyNotFoundException("Risk Assessment not found."); }

        return saveNewCase(request);
    }

    @Override
    @Transactional
    public UnderwritingCaseResponse createCaseFromEvent(CreateUnderwritingCaseRequest request) {
        log.info("Creating Underwriting Case via Kafka Event for Assessment ID: {}", request.getAssessmentId());
        Optional<UnderwritingCase> existingCase = caseRepository.findByAssessmentId(request.getAssessmentId());
        if (existingCase.isPresent()) return mapper.toResponse(existingCase.get());
        return saveNewCase(request);
    }

    private UnderwritingCaseResponse saveNewCase(CreateUnderwritingCaseRequest request) {
        UnderwritingCase uwCase = mapper.toEntity(request);
        uwCase.setStatus(CaseStatus.OPEN);
        uwCase.setCreatedAt(Instant.now());
        return mapper.toResponse(caseRepository.save(uwCase));
    }

    @Override
    public UnderwritingCaseResponse getCase(UUID caseId) {
        UnderwritingCase uwCase = caseRepository.findById(caseId)
                .orElseThrow(() -> new UnderwritingCaseNotFoundException("Case not found with ID: " + caseId));
        return mapper.toResponse(uwCase);
    }

    @Override
    @Transactional
    public UnderwritingCaseResponse approve(UUID caseId, ApproveUnderwritingRequest request) {
        UnderwritingCase uwCase = getValidCaseForDecision(caseId);
        
        if (request.getDecisionType() == UnderwritingDecisionType.REJECTED || request.getDecisionType() == UnderwritingDecisionType.REFERRED) {
            throw new InvalidUnderwritingStatusException("Cannot pass REJECTED or REFERRED to the approve endpoint.");
        }

        // Create Decision
        UnderwritingDecision decision = new UnderwritingDecision();
        decision.setUnderwritingCase(uwCase);
        decision.setDecisionType(request.getDecisionType());
        decision.setReason(request.getReason());
        decision.setNotes(request.getNotes());
        decision.setDecidedAt(Instant.now());

        if (uwCase.getDecisions() == null) uwCase.setDecisions(new HashSet<>());
        uwCase.getDecisions().add(decision);

        // Finalize Case
        uwCase.setStatus(CaseStatus.COMPLETED);
        uwCase.setUpdatedAt(Instant.now());
        uwCase.setCompletedAt(Instant.now());

        UnderwritingCase savedCase = caseRepository.save(uwCase);

        // Fire Event for Policy Service
        UnderwritingApprovedEvent event = new UnderwritingApprovedEvent(
                "UNDERWRITING_APPROVED",
                savedCase.getCaseId(),
                savedCase.getQuoteId(),
                savedCase.getCustomerId(),
                request.getDecisionType().name(),
                Instant.now()
        );
        eventProducer.publishApprovalEvent(event);

        return mapper.toResponse(savedCase);
    }

    @Override
    @Transactional
    public UnderwritingCaseResponse reject(UUID caseId, RejectUnderwritingRequest request) {
        UnderwritingCase uwCase = getValidCaseForDecision(caseId);

        UnderwritingDecision decision = new UnderwritingDecision();
        decision.setUnderwritingCase(uwCase);
        decision.setDecisionType(UnderwritingDecisionType.REJECTED);
        decision.setReason(request.getReason());
        decision.setNotes(request.getNotes());
        decision.setDecidedAt(Instant.now());

        if (uwCase.getDecisions() == null) uwCase.setDecisions(new HashSet<>());
        uwCase.getDecisions().add(decision);

        // Finalize Case
        uwCase.setStatus(CaseStatus.COMPLETED);
        uwCase.setUpdatedAt(Instant.now());
        uwCase.setCompletedAt(Instant.now());

        // Note: No Kafka event is published. The flow stops here.
        return mapper.toResponse(caseRepository.save(uwCase));
    }

    @Override
    @Transactional
    public UnderwritingCaseResponse refer(UUID caseId, ReferUnderwritingRequest request) {
        UnderwritingCase uwCase = getValidCaseForDecision(caseId);

        UnderwritingDecision decision = new UnderwritingDecision();
        decision.setUnderwritingCase(uwCase);
        decision.setDecisionType(UnderwritingDecisionType.REFERRED);
        decision.setReason(request.getReason());
        decision.setNotes(request.getNotes());
        decision.setDecidedAt(Instant.now());

        if (uwCase.getDecisions() == null) uwCase.setDecisions(new HashSet<>());
        uwCase.getDecisions().add(decision);

        // Case is NOT completed. It moves to UNDER_REVIEW
        uwCase.setStatus(CaseStatus.UNDER_REVIEW);
        uwCase.setUpdatedAt(Instant.now());

        // Note: No Kafka event is published. Awaiting further action.
        return mapper.toResponse(caseRepository.save(uwCase));
    }

    // Helper: State Transition Validation (Rule #14)
    private UnderwritingCase getValidCaseForDecision(UUID caseId) {
        UnderwritingCase uwCase = caseRepository.findById(caseId)
                .orElseThrow(() -> new UnderwritingCaseNotFoundException("Case not found: " + caseId));

        if (uwCase.getStatus() == CaseStatus.COMPLETED) {
            throw new InvalidUnderwritingStatusException("State Transition Error: This case is already COMPLETED and cannot accept new decisions.");
        }
        return uwCase;
    }
}