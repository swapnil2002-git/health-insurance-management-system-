package com.healthinsurance.underwriting.service;

import com.healthinsurance.underwriting.client.QuotationClient;
import com.healthinsurance.underwriting.client.RiskAssessmentClient;
import com.healthinsurance.underwriting.dto.request.ApproveUnderwritingRequest;
import com.healthinsurance.underwriting.dto.response.UnderwritingCaseResponse;
import com.healthinsurance.underwriting.entity.UnderwritingCase;
import com.healthinsurance.underwriting.enums.CaseStatus;
import com.healthinsurance.underwriting.enums.UnderwritingDecisionType;
import com.healthinsurance.underwriting.event.UnderwritingApprovedEvent;
import com.healthinsurance.underwriting.event.UnderwritingEventProducer;
import com.healthinsurance.underwriting.mapper.UnderwritingMapper;
import com.healthinsurance.underwriting.repository.UnderwritingCaseRepository;
import com.healthinsurance.underwriting.service.impl.UnderwritingServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UnderwritingServiceImplTest {

    @Mock
    private UnderwritingCaseRepository caseRepository;

    @Mock
    private UnderwritingMapper mapper;

    @Mock
    private QuotationClient quotationClient;

    @Mock
    private RiskAssessmentClient riskClient;

    @Mock
    private UnderwritingEventProducer eventProducer;

    @InjectMocks
    private UnderwritingServiceImpl underwritingService;

    private UnderwritingCase testCase;
    private UUID caseId;
    private UUID quoteId;
    private UUID customerId;

    @BeforeEach
    void setUp() {
        caseId = UUID.randomUUID();
        quoteId = UUID.randomUUID();
        customerId = UUID.randomUUID();

        testCase = new UnderwritingCase();
        testCase.setCaseId(caseId);
        testCase.setQuoteId(quoteId);
        testCase.setCustomerId(customerId);
        testCase.setStatus(CaseStatus.OPEN);
    }

    @Test
    void approve_PublishesStandardizedUnderwritingApprovedEvent() {
        when(caseRepository.findById(caseId)).thenReturn(Optional.of(testCase));
        when(caseRepository.save(any(UnderwritingCase.class))).thenAnswer(i -> i.getArgument(0));

        UnderwritingCaseResponse mockResponse = new UnderwritingCaseResponse();
        mockResponse.setCaseId(caseId);
        when(mapper.toResponse(any(UnderwritingCase.class))).thenReturn(mockResponse);

        ApproveUnderwritingRequest request = new ApproveUnderwritingRequest();
        request.setDecisionType(UnderwritingDecisionType.APPROVED);
        request.setReason("Risk within acceptable bounds");
        request.setNotes("Approved standard tier");

        UnderwritingCaseResponse response = underwritingService.approve(caseId, request);

        assertNotNull(response);
        assertEquals(caseId, response.getCaseId());

        ArgumentCaptor<UnderwritingApprovedEvent> captor = ArgumentCaptor.forClass(UnderwritingApprovedEvent.class);
        verify(eventProducer, times(1)).publishApprovalEvent(captor.capture());

        UnderwritingApprovedEvent published = captor.getValue();
        assertEquals("UNDERWRITING_APPROVED", published.getEventType());
        assertEquals(caseId, published.getCaseId());
        assertEquals(quoteId, published.getQuoteId());
        assertEquals(customerId, published.getCustomerId());
        assertEquals("APPROVED", published.getDecisionType());
        assertNotNull(published.getTimestamp());
    }
}
