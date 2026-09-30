package com.healthinsurance.policy.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthinsurance.policy.client.PremiumClient;
import com.healthinsurance.policy.dto.request.EndorsementApprovalRequest;
import com.healthinsurance.policy.dto.request.EndorsementRejectRequest;
import com.healthinsurance.policy.dto.request.PolicyEndorsementRequest;
import com.healthinsurance.policy.dto.response.PolicyEndorsementResponse;
import com.healthinsurance.policy.entity.*;
import com.healthinsurance.policy.enums.EndorsementStatus;
import com.healthinsurance.policy.enums.EndorsementType;
import com.healthinsurance.policy.enums.PolicyStatus;
import com.healthinsurance.policy.event.PolicyEndorsedEvent;
import com.healthinsurance.policy.event.PolicyEventProducer;
import com.healthinsurance.policy.exception.InvalidPolicyStateException;
import com.healthinsurance.policy.repository.PolicyEndorsementRepository;
import com.healthinsurance.policy.repository.PolicyRepository;
import com.healthinsurance.policy.service.impl.EndorsementServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class EndorsementServiceTest {

    @Mock
    private PolicyRepository policyRepository;

    @Mock
    private PolicyEndorsementRepository endorsementRepository;

    @Mock
    private PremiumClient premiumClient;

    @Mock
    private PolicyEventProducer policyEventProducer;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    private EndorsementServiceImpl endorsementService;

    private Policy testPolicy;
    private UUID policyId;

    @BeforeEach
    void setUp() {
        endorsementService = new EndorsementServiceImpl(
                policyRepository,
                endorsementRepository,
                premiumClient,
                policyEventProducer,
                objectMapper
        );

        policyId = UUID.randomUUID();
        testPolicy = new Policy();
        testPolicy.setPolicyId(policyId);
        testPolicy.setPolicyNumber("POL-2026-TEST01");
        testPolicy.setCustomerId(UUID.randomUUID());
        testPolicy.setPlanId(UUID.randomUUID());
        testPolicy.setQuoteId(UUID.randomUUID());
        testPolicy.setStatus(PolicyStatus.ACTIVE);
        testPolicy.setMembers(new HashSet<>());
        testPolicy.setCoverages(new HashSet<>());
        testPolicy.setBeneficiaries(new HashSet<>());
    }

    @Test
    void testRequestEndorsement_AddMember_Success() {
        UUID newMemberId = UUID.randomUUID();
        PolicyEndorsementRequest request = PolicyEndorsementRequest.builder()
                .endorsementType(EndorsementType.ADD_MEMBER)
                .description("Add newborn child")
                .changeData(Map.of("memberId", newMemberId.toString()))
                .requestedBy("AGENT_01")
                .build();

        when(policyRepository.findById(policyId)).thenReturn(Optional.of(testPolicy));
        when(endorsementRepository.save(any(PolicyEndorsement.class))).thenAnswer(invocation -> {
            PolicyEndorsement e = invocation.getArgument(0);
            e.setEndorsementId(UUID.randomUUID());
            return e;
        });

        PolicyEndorsementResponse response = endorsementService.requestEndorsement(policyId, request);

        assertNotNull(response);
        assertEquals(EndorsementStatus.PENDING_APPROVAL, response.getStatus());
        assertEquals(EndorsementType.ADD_MEMBER, response.getEndorsementType());
        assertNotNull(response.getRevisedPremium());
        verify(endorsementRepository).save(any(PolicyEndorsement.class));
    }

    @Test
    void testRequestEndorsement_InvalidPolicyStatus_ThrowsException() {
        testPolicy.setStatus(PolicyStatus.CANCELLED);
        when(policyRepository.findById(policyId)).thenReturn(Optional.of(testPolicy));

        PolicyEndorsementRequest request = PolicyEndorsementRequest.builder()
                .endorsementType(EndorsementType.ADDRESS_CHANGE)
                .description("Change address")
                .changeData(Map.of("newAddress", "123 New St"))
                .build();

        assertThrows(InvalidPolicyStateException.class, () -> endorsementService.requestEndorsement(policyId, request));
    }

    @Test
    void testApproveEndorsement_AddMember_AppliesToPolicyAndPublishesKafka() {
        UUID endorsementId = UUID.randomUUID();
        UUID newMemberId = UUID.randomUUID();

        PolicyEndorsement endorsement = PolicyEndorsement.builder()
                .endorsementId(endorsementId)
                .policy(testPolicy)
                .endorsementType(EndorsementType.ADD_MEMBER)
                .status(EndorsementStatus.PENDING_APPROVAL)
                .description("Add member")
                .changeData("{\"memberId\":\"" + newMemberId + "\"}")
                .revisedPremium(new BigDecimal("650.00"))
                .build();

        when(endorsementRepository.findById(endorsementId)).thenReturn(Optional.of(endorsement));
        when(endorsementRepository.save(any(PolicyEndorsement.class))).thenAnswer(i -> i.getArgument(0));
        when(policyRepository.save(any(Policy.class))).thenAnswer(i -> i.getArgument(0));

        EndorsementApprovalRequest approval = EndorsementApprovalRequest.builder()
                .approvedBy("UNDERWRITER_ALICE")
                .remarks("Verified documents")
                .build();

        PolicyEndorsementResponse response = endorsementService.approveEndorsement(policyId, endorsementId, approval);

        assertNotNull(response);
        assertEquals(EndorsementStatus.APPLIED, response.getStatus());
        assertEquals("UNDERWRITER_ALICE", response.getApprovedBy());

        // Verify member was added to policy
        assertTrue(testPolicy.getMembers().stream().anyMatch(m -> m.getMemberId().equals(newMemberId)));

        // Verify Kafka event was published
        verify(policyEventProducer).publishPolicyEndorsed(any(PolicyEndorsedEvent.class));
    }

    @Test
    void testApproveEndorsement_NomineeChange_UpdatesBeneficiary() {
        UUID endorsementId = UUID.randomUUID();

        PolicyEndorsement endorsement = PolicyEndorsement.builder()
                .endorsementId(endorsementId)
                .policy(testPolicy)
                .endorsementType(EndorsementType.NOMINEE_CHANGE)
                .status(EndorsementStatus.PENDING_APPROVAL)
                .description("Change primary nominee")
                .changeData("{\"beneficiaryName\":\"Jane Doe\",\"relationship\":\"Spouse\",\"percentage\":100.00}")
                .build();

        when(endorsementRepository.findById(endorsementId)).thenReturn(Optional.of(endorsement));
        when(endorsementRepository.save(any(PolicyEndorsement.class))).thenAnswer(i -> i.getArgument(0));
        when(policyRepository.save(any(Policy.class))).thenAnswer(i -> i.getArgument(0));

        EndorsementApprovalRequest approval = EndorsementApprovalRequest.builder()
                .approvedBy("SUPERVISOR_BOB")
                .build();

        PolicyEndorsementResponse response = endorsementService.approveEndorsement(policyId, endorsementId, approval);

        assertNotNull(response);
        assertEquals(EndorsementStatus.APPLIED, response.getStatus());
        assertEquals(1, testPolicy.getBeneficiaries().size());
        assertEquals("Jane Doe", testPolicy.getBeneficiaries().iterator().next().getBeneficiaryName());
    }

    @Test
    void testRejectEndorsement_Success() {
        UUID endorsementId = UUID.randomUUID();

        PolicyEndorsement endorsement = PolicyEndorsement.builder()
                .endorsementId(endorsementId)
                .policy(testPolicy)
                .endorsementType(EndorsementType.RIDER_ADDITION)
                .status(EndorsementStatus.PENDING_APPROVAL)
                .description("Add critical illness rider")
                .build();

        when(endorsementRepository.findById(endorsementId)).thenReturn(Optional.of(endorsement));
        when(endorsementRepository.save(any(PolicyEndorsement.class))).thenAnswer(i -> i.getArgument(0));

        EndorsementRejectRequest reject = EndorsementRejectRequest.builder()
                .rejectionReason("Plan does not support requested rider")
                .rejectedBy("UNDERWRITER_CHARLIE")
                .build();

        PolicyEndorsementResponse response = endorsementService.rejectEndorsement(policyId, endorsementId, reject);

        assertNotNull(response);
        assertEquals(EndorsementStatus.REJECTED, response.getStatus());
        assertEquals("Plan does not support requested rider", response.getRejectionReason());
        verify(policyRepository, never()).save(any());
    }
}
