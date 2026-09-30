package com.healthinsurance.policy.service.impl;

import com.healthinsurance.policy.dto.request.PolicyCancellationRequest;
import com.healthinsurance.policy.dto.request.PolicyCreateRequest;
import com.healthinsurance.policy.dto.request.PolicyEndorsementRequest;
import com.healthinsurance.policy.dto.response.PolicyResponse;
import com.healthinsurance.policy.entity.Policy;
import com.healthinsurance.policy.entity.PolicyCancellation;
import com.healthinsurance.policy.entity.PolicyEndorsement;
import com.healthinsurance.policy.enums.PolicyStatus;
import com.healthinsurance.policy.event.PolicyEventProducer;
import com.healthinsurance.policy.event.PolicyIssuedEvent;
import com.healthinsurance.policy.exception.InvalidPolicyStateException;
import com.healthinsurance.policy.exception.PolicyNotFoundException;
import com.healthinsurance.policy.mapper.PolicyMapper;
import com.healthinsurance.policy.repository.PolicyRepository;
import com.healthinsurance.policy.service.PolicyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.Year;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PolicyServiceImpl implements PolicyService {

    private final PolicyRepository policyRepository;
    private final PolicyMapper mapper;
    private final PolicyEventProducer eventProducer;
    private final com.healthinsurance.policy.outbox.OutboxEventRepository outboxEventRepository;
    private final com.fasterxml.jackson.databind.ObjectMapper objectMapper;
    private final com.healthinsurance.policy.client.CustomerClient customerClient;

    @Override
    @Transactional
    public PolicyResponse createPolicy(PolicyCreateRequest request) {
        log.info("Creating new Policy in DRAFT state for Customer ID: {}", request.getCustomerId());

        // If members were not supplied, automatically fetch and enroll the customer's members
        if ((request.getMembers() == null || request.getMembers().isEmpty()) && request.getCustomerId() != null) {
            try {
                List<com.healthinsurance.policy.client.dto.CustomerMemberDto> custMembers = customerClient.getMembersByCustomerId(request.getCustomerId());
                if (custMembers != null && !custMembers.isEmpty()) {
                    List<com.healthinsurance.policy.dto.request.PolicyMemberRequest> pmrList = custMembers.stream().map(cm -> {
                        com.healthinsurance.policy.dto.request.PolicyMemberRequest pmr = new com.healthinsurance.policy.dto.request.PolicyMemberRequest();
                        pmr.setMemberId(cm.getMemberId());
                        return pmr;
                    }).collect(Collectors.toList());
                    request.setMembers(pmrList);
                    log.info("Auto-enrolled {} members from Customer Service for Customer: {}", pmrList.size(), request.getCustomerId());
                }
            } catch (Exception ex) {
                log.warn("Could not auto-fetch customer members: {}", ex.getMessage());
            }
        }

        Policy policy = mapper.toEntity(request);
        policy.setStatus(PolicyStatus.DRAFT);
        policy.setCreatedAt(Instant.now());
        policy.setUpdatedAt(Instant.now());

        if (policy.getMembers() != null) policy.getMembers().forEach(m -> m.setPolicy(policy));
        if (policy.getCoverages() != null) policy.getCoverages().forEach(c -> c.setPolicy(policy));
        if (policy.getBeneficiaries() != null) policy.getBeneficiaries().forEach(b -> b.setPolicy(policy));

        return enrichPolicyResponse(mapper.toResponse(policyRepository.save(policy)), policy.getCustomerId());
    }

    @Override
    public PolicyResponse getPolicy(UUID policyId) {
        Policy policy = policyRepository.findById(policyId)
                .orElseThrow(() -> new PolicyNotFoundException("Policy not found with ID: " + policyId));
        return enrichPolicyResponse(mapper.toResponse(policy), policy.getCustomerId());
    }

    @Override
    public List<PolicyResponse> getAllPolicies() {
        return policyRepository.findAll().stream()
                .map(p -> enrichPolicyResponse(mapper.toResponse(p), p.getCustomerId()))
                .collect(Collectors.toList());
    }

    @Override
    public List<PolicyResponse> getPoliciesByCustomerId(UUID customerId) {
        return policyRepository.findByCustomerId(customerId).stream()
                .map(p -> enrichPolicyResponse(mapper.toResponse(p), customerId))
                .collect(Collectors.toList());
    }

    private PolicyResponse enrichPolicyResponse(PolicyResponse response, UUID customerId) {
        if (response == null || customerId == null) return response;

        // If members list is empty, resolve and include customer's registered members
        if (response.getMembers() == null || response.getMembers().isEmpty()) {
            try {
                List<com.healthinsurance.policy.client.dto.CustomerMemberDto> custMembers = customerClient.getMembersByCustomerId(customerId);
                if (custMembers != null && !custMembers.isEmpty()) {
                    List<com.healthinsurance.policy.dto.response.PolicyMemberResponse> pmList = custMembers.stream().map(cm -> {
                        com.healthinsurance.policy.dto.response.PolicyMemberResponse pmr = new com.healthinsurance.policy.dto.response.PolicyMemberResponse();
                        pmr.setPolicyMemberId(UUID.randomUUID());
                        pmr.setMemberId(cm.getMemberId());
                        return pmr;
                    }).collect(Collectors.toList());
                    response.setMembers(pmList);
                }
            } catch (Exception ex) {
                log.warn("Could not enrich policy members for customer {}: {}", customerId, ex.getMessage());
            }
        }
        return response;
    }

    @Override
    @Transactional
    public PolicyResponse issuePolicy(UUID policyId) {
        Policy policy = policyRepository.findById(policyId)
                .orElseThrow(() -> new PolicyNotFoundException("Policy not found"));

        if (policy.getStatus() != PolicyStatus.DRAFT) {
            throw new InvalidPolicyStateException("Only DRAFT policies can be Issued.");
        }

        if (policy.getPolicyNumber() == null) {
            policy.setPolicyNumber(generateUniquePolicyNumber());
        }

        policy.setStatus(PolicyStatus.PENDING_PAYMENT);
        policy.setUpdatedAt(Instant.now());

        Policy savedPolicy = policyRepository.save(policy);
        log.info("Policy {} issued. Saving Outbox Event.", savedPolicy.getPolicyNumber());
        
        // Outbox Pattern: Save event to database atomically
        String eventId = UUID.randomUUID().toString();
        String effectiveDateStr = savedPolicy.getEffectiveDate() != null ? savedPolicy.getEffectiveDate().toString() : Instant.now().toString();
        String expiryDateStr = savedPolicy.getExpiryDate() != null ? savedPolicy.getExpiryDate().toString() : Instant.now().plus(365, java.time.temporal.ChronoUnit.DAYS).toString();
        String payload = String.format("{\"eventType\":\"PolicyIssued\",\"eventId\":\"%s\",\"policyId\":\"%s\",\"policyNumber\":\"%s\",\"customerId\":\"%s\",\"quoteId\":\"%s\",\"planId\":\"%s\",\"effectiveDate\":\"%s\",\"expiryDate\":\"%s\"}",
                eventId, savedPolicy.getPolicyId(), savedPolicy.getPolicyNumber(), savedPolicy.getCustomerId(), savedPolicy.getQuoteId(), savedPolicy.getPlanId(), effectiveDateStr, expiryDateStr);
        
        com.healthinsurance.policy.outbox.OutboxEvent outboxEvent = com.healthinsurance.policy.outbox.OutboxEvent.builder()
                .eventId(eventId)
                .eventType("PolicyIssued")
                .aggregateId(savedPolicy.getPolicyId().toString())
                .topic("policy-events")
                .partitionKey(savedPolicy.getPolicyId().toString())
                .payload(payload)
                .status(com.healthinsurance.policy.outbox.OutboxStatus.PENDING)
                .retryCount(0)
                .maxRetries(5)
                .createdAt(Instant.now())
                .build();

        outboxEventRepository.save(outboxEvent);

        // Also dispatch directly to Kafka via PolicyEventProducer to guarantee real-time delivery
        try {
            com.healthinsurance.policy.event.PolicyIssuedEvent issuedEvent = new com.healthinsurance.policy.event.PolicyIssuedEvent();
            issuedEvent.setEventType("PolicyIssued");
            issuedEvent.setPolicyId(savedPolicy.getPolicyId());
            issuedEvent.setPolicyNumber(savedPolicy.getPolicyNumber());
            issuedEvent.setCustomerId(savedPolicy.getCustomerId());
            issuedEvent.setQuoteId(savedPolicy.getQuoteId());
            issuedEvent.setPlanId(savedPolicy.getPlanId());
            issuedEvent.setEffectiveDate(savedPolicy.getEffectiveDate());
            issuedEvent.setExpiryDate(savedPolicy.getExpiryDate());
            issuedEvent.setStatus(savedPolicy.getStatus().name());
            issuedEvent.setTimestamp(Instant.now());
            eventProducer.publishPolicyIssued(issuedEvent);
        } catch (Exception e) {
            log.error("Failed to directly dispatch PolicyIssuedEvent for policy {}: {}", savedPolicy.getPolicyNumber(), e.getMessage());
        }

        return mapper.toResponse(savedPolicy);
    }

    @Override
    @Transactional
    public PolicyResponse activatePolicy(UUID policyId) {
        Policy policy = policyRepository.findById(policyId).orElseThrow(() -> new PolicyNotFoundException("Policy not found"));
        if (policy.getStatus() != PolicyStatus.PENDING_PAYMENT) {
            throw new InvalidPolicyStateException("Policy must be PENDING_PAYMENT to be Activated.");
        }
        policy.setStatus(PolicyStatus.ACTIVE);
        policy.setUpdatedAt(Instant.now());
        return mapper.toResponse(policyRepository.save(policy));
    }

    @Override
    @Transactional
    public PolicyResponse addEndorsement(UUID policyId, PolicyEndorsementRequest request) {
        Policy policy = policyRepository.findById(policyId).orElseThrow(() -> new PolicyNotFoundException("Policy not found"));
        if (policy.getStatus() != PolicyStatus.ACTIVE) throw new InvalidPolicyStateException("Only ACTIVE policies can be endorsed.");
        
        PolicyEndorsement endorsement = new PolicyEndorsement();
        endorsement.setPolicy(policy);
        endorsement.setDescription(request.getDescription());
        endorsement.setAppliedAt(Instant.now());

        if (policy.getEndorsements() == null) policy.setEndorsements(new HashSet<>());
        policy.getEndorsements().add(endorsement);
        
        return mapper.toResponse(policyRepository.save(policy));
    }

    @Override
    @Transactional
    public PolicyResponse cancelPolicy(UUID policyId, PolicyCancellationRequest request) {
        Policy policy = policyRepository.findById(policyId).orElseThrow(() -> new PolicyNotFoundException("Policy not found"));
        if (policy.getStatus() == PolicyStatus.CANCELLED) throw new InvalidPolicyStateException("Policy is already cancelled.");
        
        PolicyCancellation cancellation = new PolicyCancellation();
        cancellation.setPolicy(policy);
        cancellation.setReason(request.getReason());
        cancellation.setCancelledAt(Instant.now());

        policy.setCancellation(cancellation);
        policy.setStatus(PolicyStatus.CANCELLED);
        policy.setUpdatedAt(Instant.now());
        
        return mapper.toResponse(policyRepository.save(policy));
    }

    @Override
    @Transactional
    public PolicyResponse renewPolicy(UUID policyId) {
        Policy policy = policyRepository.findById(policyId).orElseThrow(() -> new PolicyNotFoundException("Policy not found"));
        if (policy.getStatus() != PolicyStatus.ACTIVE && policy.getStatus() != PolicyStatus.EXPIRED) {
            throw new InvalidPolicyStateException("Only ACTIVE or EXPIRED policies can be renewed.");
        }
        
        // Simple MVP Renewal Logic: Extend expiry by 1 year, set to RENEWED
        policy.setExpiryDate(policy.getExpiryDate().plus(365, ChronoUnit.DAYS));
        policy.setStatus(PolicyStatus.RENEWED);
        policy.setUpdatedAt(Instant.now());
        
        return mapper.toResponse(policyRepository.save(policy));
    }

    @Override
    @Transactional
    public PolicyResponse compensatePolicyIssuance(UUID policyId, String reason) {
        log.warn("Executing SAGA compensation for policy {}: {}", policyId, reason);
        Policy policy = policyRepository.findById(policyId)
                .orElseThrow(() -> new PolicyNotFoundException("Policy not found: " + policyId));

        // Idempotency check: only suspend if not already ACTIVE
        if (policy.getStatus() != PolicyStatus.ACTIVE) {
            policy.setStatus(PolicyStatus.SUSPENDED);
            policy.setUpdatedAt(Instant.now());
            Policy saved = policyRepository.save(policy);
            return mapper.toResponse(saved);
        }

        return mapper.toResponse(policy);
    }

    private String generateUniquePolicyNumber() {
        String prefix = "POL-" + Year.now().getValue() + "-";
        String generatedNumber;
        do {
            generatedNumber = prefix + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        } while (policyRepository.findByPolicyNumber(generatedNumber).isPresent());
        return generatedNumber;
    }
}