package com.healthinsurance.risk.service;

import com.healthinsurance.risk.client.CustomerClient;
import com.healthinsurance.risk.client.QuotationClient;
import com.healthinsurance.risk.dto.response.RiskAssessmentResponse;
import com.healthinsurance.risk.entity.RiskAssessment;
import com.healthinsurance.risk.enums.AssessmentStatus;
import com.healthinsurance.risk.enums.RiskClassification;
import com.healthinsurance.risk.event.RiskAssessmentCompletedEvent;
import com.healthinsurance.risk.event.RiskEventProducer;
import com.healthinsurance.risk.mapper.RiskMapper;
import com.healthinsurance.risk.repository.RiskAssessmentRepository;
import com.healthinsurance.risk.service.impl.RiskAssessmentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RiskAssessmentServiceImplTest {

    @Mock
    private RiskAssessmentRepository assessmentRepository;

    @Mock
    private RiskMapper mapper;

    @Mock
    private CustomerClient customerClient;

    @Mock
    private QuotationClient quotationClient;

    @Mock
    private RiskCalculationService calculationService;

    @Mock
    private RiskEventProducer eventProducer;

    @InjectMocks
    private RiskAssessmentServiceImpl riskAssessmentService;

    private RiskAssessment testAssessment;
    private UUID assessmentId;

    @BeforeEach
    void setUp() {
        assessmentId = UUID.randomUUID();
        testAssessment = new RiskAssessment();
        testAssessment.setAssessmentId(assessmentId);
        testAssessment.setQuoteId(UUID.randomUUID());
        testAssessment.setCustomerId(UUID.randomUUID());
        testAssessment.setStatus(AssessmentStatus.CREATED);
        testAssessment.setFactors(Collections.emptySet());
    }

    @Test
    void calculateRisk_PublishesStandardizedRiskAssessmentCompletedEvent() {
        when(assessmentRepository.findById(assessmentId)).thenReturn(Optional.of(testAssessment));

        RiskCalculationResult calcResult = new RiskCalculationResult(45, RiskClassification.LOW);
        when(calculationService.calculateRisk(any())).thenReturn(calcResult);

        when(assessmentRepository.save(any(RiskAssessment.class))).thenAnswer(i -> i.getArgument(0));

        RiskAssessmentResponse mockResponse = new RiskAssessmentResponse();
        mockResponse.setAssessmentId(assessmentId);
        when(mapper.toResponse(any(RiskAssessment.class))).thenReturn(mockResponse);

        RiskAssessmentResponse response = riskAssessmentService.calculateRisk(assessmentId);

        assertNotNull(response);
        assertEquals(assessmentId, response.getAssessmentId());

        ArgumentCaptor<RiskAssessmentCompletedEvent> captor = ArgumentCaptor.forClass(RiskAssessmentCompletedEvent.class);
        verify(eventProducer, times(1)).publishRiskCompletedEvent(captor.capture());

        RiskAssessmentCompletedEvent published = captor.getValue();
        assertEquals("RISK_ASSESSMENT_COMPLETED", published.getEventType());
        assertEquals(assessmentId, published.getAssessmentId());
        assertEquals(testAssessment.getQuoteId(), published.getQuoteId());
        assertEquals(testAssessment.getCustomerId(), published.getCustomerId());
        assertEquals(45, published.getRiskScore());
        assertEquals("LOW", published.getRiskClassification());
        assertNotNull(published.getTimestamp());
    }
}
