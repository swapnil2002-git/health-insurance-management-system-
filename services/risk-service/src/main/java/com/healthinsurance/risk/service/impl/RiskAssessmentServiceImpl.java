package com.healthinsurance.risk.service.impl;

import com.healthinsurance.risk.client.CustomerClient;
import com.healthinsurance.risk.client.QuotationClient;
import com.healthinsurance.risk.dto.request.CreateRiskAssessmentRequest;
import com.healthinsurance.risk.dto.response.RiskAssessmentResponse;
import com.healthinsurance.risk.entity.RiskAssessment;
import com.healthinsurance.risk.entity.RiskScore;
import com.healthinsurance.risk.enums.AssessmentStatus;
import com.healthinsurance.risk.event.RiskAssessmentCompletedEvent;
import com.healthinsurance.risk.event.RiskEventProducer;
import com.healthinsurance.risk.exception.DependencyNotFoundException;
import com.healthinsurance.risk.exception.InvalidAssessmentStatusException;
import com.healthinsurance.risk.exception.RiskAssessmentNotFoundException;
import com.healthinsurance.risk.mapper.RiskMapper;
import com.healthinsurance.risk.repository.RiskAssessmentRepository;
import com.healthinsurance.risk.service.RiskAssessmentService;
import com.healthinsurance.risk.service.RiskCalculationResult;
import com.healthinsurance.risk.service.RiskCalculationService;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashSet;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RiskAssessmentServiceImpl implements RiskAssessmentService {

    private final RiskAssessmentRepository assessmentRepository;
    private final RiskMapper mapper;
    
    private final CustomerClient customerClient;
    private final QuotationClient quotationClient;
    
    private final RiskCalculationService calculationService;
    private final RiskEventProducer eventProducer; // Injected Producer

    @Override
    @Transactional
    public RiskAssessmentResponse createAssessment(CreateRiskAssessmentRequest request) {
        log.info("Creating Risk Assessment for Quote: {}", request.getQuoteId());

        try {
            customerClient.getCustomer(request.getCustomerId());
        } catch (FeignException.NotFound e) {
            throw new DependencyNotFoundException("Customer not found in Customer Service.");
        }

        try {
            quotationClient.getQuote(request.getQuoteId());
        } catch (FeignException.NotFound e) {
            throw new DependencyNotFoundException("Quotation not found in Quotation Service.");
        }

        RiskAssessment assessment = mapper.toEntity(request);
        assessment.setStatus(AssessmentStatus.CREATED);
        assessment.setCreatedAt(Instant.now());

        if (assessment.getFactors() != null) {
            assessment.getFactors().forEach(factor -> factor.setAssessment(assessment));
        }

        RiskAssessment savedAssessment = assessmentRepository.save(assessment);
        return mapper.toResponse(savedAssessment);
    }

    @Override
    public RiskAssessmentResponse getAssessment(UUID id) {
        RiskAssessment assessment = assessmentRepository.findById(id)
                .orElseThrow(() -> new RiskAssessmentNotFoundException("Risk Assessment not found with ID: " + id));
        return mapper.toResponse(assessment);
    }

    @Override
    @Transactional
    public RiskAssessmentResponse calculateRisk(UUID id) {
        RiskAssessment assessment = assessmentRepository.findById(id)
                .orElseThrow(() -> new RiskAssessmentNotFoundException("Risk Assessment not found with ID: " + id));

        if (assessment.getStatus() == AssessmentStatus.COMPLETED) {
            throw new InvalidAssessmentStatusException("Risk Assessment is already completed.");
        }

        assessment.setStatus(AssessmentStatus.IN_PROGRESS);
        log.info("Calculating risk for Assessment ID: {}", id);

        // 1. Delegate to the decoupled Engine
        RiskCalculationResult result = calculationService.calculateRisk(assessment.getFactors());

        // 2. Create the Risk Score snapshot
        RiskScore riskScore = new RiskScore();
        riskScore.setAssessment(assessment);
        riskScore.setScore(result.getScore());
        riskScore.setClassification(result.getClassification());
        riskScore.setCalculatedAt(Instant.now());

        if (assessment.getScores() == null) assessment.setScores(new HashSet<>());
        assessment.getScores().add(riskScore);

        // 3. Update the master record and set COMPLETED
        assessment.setClassification(result.getClassification());
        assessment.setStatus(AssessmentStatus.COMPLETED);
        assessment.setUpdatedAt(Instant.now());
        assessment.setCompletedAt(Instant.now());

        RiskAssessment savedAssessment = assessmentRepository.save(assessment);
        log.info("Risk Assessment {} completed successfully.", id);

        // 4. Publish Kafka Event
        RiskAssessmentCompletedEvent event = new RiskAssessmentCompletedEvent(
                "RISK_ASSESSMENT_COMPLETED",
                savedAssessment.getAssessmentId(),
                savedAssessment.getQuoteId(),
                savedAssessment.getCustomerId(),
                result.getScore(),
                result.getClassification().name(),
                Instant.now()
        );
        eventProducer.publishRiskCompletedEvent(event);

        return mapper.toResponse(savedAssessment);
    }
}