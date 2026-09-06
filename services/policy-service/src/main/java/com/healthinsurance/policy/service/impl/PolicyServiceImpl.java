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

    @Override
    @Transactional
    public PolicyResponse createPolicy(PolicyCreateRequest request) {
        log.info("Creating new Policy in DRAFT state for Customer ID: {}", request.getCustomerId());

        Policy policy = mapper.toEntity(request);
        policy.setStatus(PolicyStatus.DRAFT);
        policy.setCreatedAt(Instant.now());
        policy.setUpdatedAt(Instant.now());

        if (policy.getMembers() != null) policy.getMembers().forEach(m -> m.setPolicy(policy));
        if (policy.getCoverages() != null) policy.getCoverages().forEach(c -> c.setPolicy(policy));
        if (policy.getBeneficiaries() != null) policy.getBeneficiaries().forEach(b -> b.setPolicy(policy));

        return mapper.toResponse(policyRepository.save(policy));
    }

    @Override
    public PolicyResponse getPolicy(UUID policyId) {
        Policy policy = policyRepository.findById(policyId)
                .orElseThrow(() -> new PolicyNotFoundException("Policy not found with ID: " + policyId));
        return mapper.toResponse(policy);
    }

    @Override
    public List<PolicyResponse> getAllPolicies() {
        return policyRepository.findAll().stream().map(mapper::toResponse).collect(Collectors.toList());
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
        log.info("Policy {} issued. Publishing Kafka Event.", savedPolicy.getPolicyNumber());
        
        // Step 16: Publish Kafka Event
        PolicyIssuedEvent event = new PolicyIssuedEvent(
                "POLICY_ISSUED",
                savedPolicy.getPolicyId(),
                savedPolicy.getPolicyNumber(),
                savedPolicy.getCustomerId(),
                savedPolicy.getQuoteId(),
                savedPolicy.getPlanId(),
                savedPolicy.getEffectiveDate(),
                savedPolicy.getExpiryDate(),
                savedPolicy.getStatus().name(),
                Instant.now()
        );
        eventProducer.publishPolicyIssued(event);

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

    private String generateUniquePolicyNumber() {
        String prefix = "POL-" + Year.now().getValue() + "-";
        String generatedNumber;
        do {
            generatedNumber = prefix + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        } while (policyRepository.findByPolicyNumber(generatedNumber).isPresent());
        return generatedNumber;
    }
}