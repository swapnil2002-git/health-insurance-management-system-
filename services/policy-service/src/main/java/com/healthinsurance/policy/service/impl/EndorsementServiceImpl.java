package com.healthinsurance.policy.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
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
import com.healthinsurance.policy.exception.PolicyNotFoundException;
import com.healthinsurance.policy.repository.PolicyEndorsementRepository;
import com.healthinsurance.policy.repository.PolicyRepository;
import com.healthinsurance.policy.service.EndorsementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class EndorsementServiceImpl implements EndorsementService {

    private final PolicyRepository policyRepository;
    private final PolicyEndorsementRepository endorsementRepository;
    private final PremiumClient premiumClient;
    private final PolicyEventProducer policyEventProducer;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public PolicyEndorsementResponse requestEndorsement(UUID policyId, PolicyEndorsementRequest request) {
        log.info("Processing endorsement request for policy: {}, type: {}", policyId, request.getEndorsementType());

        Policy policy = policyRepository.findById(policyId)
                .orElseThrow(() -> new PolicyNotFoundException("Policy not found with id: " + policyId));

        if (policy.getStatus() != PolicyStatus.ACTIVE) {
            throw new InvalidPolicyStateException("Endorsements can only be requested for ACTIVE policies. Current status: " + policy.getStatus());
        }

        // Validate requested change according to type
        validateChangeData(policy, request.getEndorsementType(), request.getChangeData());

        // Serialize change data
        String changeDataJson = null;
        try {
            if (request.getChangeData() != null) {
                changeDataJson = objectMapper.writeValueAsString(request.getChangeData());
            }
        } catch (Exception e) {
            log.error("Failed to serialize change data: {}", e.getMessage());
            throw new IllegalArgumentException("Invalid change data payload: " + e.getMessage());
        }

        // Recalculate estimated premium impact if applicable
        BigDecimal revisedPremium = calculateEstimatedPremium(policy, request.getEndorsementType(), request.getChangeData());

        PolicyEndorsement endorsement = PolicyEndorsement.builder()
                .policy(policy)
                .endorsementType(request.getEndorsementType())
                .status(EndorsementStatus.PENDING_APPROVAL)
                .description(request.getDescription())
                .changeData(changeDataJson)
                .revisedPremium(revisedPremium)
                .requestedBy(request.getRequestedBy() != null ? request.getRequestedBy() : "POLICY_HOLDER")
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        PolicyEndorsement saved = endorsementRepository.save(endorsement);
        log.info("Endorsement created with ID {} and status PENDING_APPROVAL", saved.getEndorsementId());
        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PolicyEndorsementResponse getEndorsement(UUID policyId, UUID endorsementId) {
        PolicyEndorsement endorsement = endorsementRepository.findById(endorsementId)
                .orElseThrow(() -> new PolicyNotFoundException("Endorsement not found: " + endorsementId));
        if (!endorsement.getPolicy().getPolicyId().equals(policyId)) {
            throw new IllegalArgumentException("Endorsement does not belong to policy: " + policyId);
        }
        return mapToResponse(endorsement);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PolicyEndorsementResponse> getEndorsementsByPolicy(UUID policyId) {
        if (!policyRepository.existsById(policyId)) {
            throw new PolicyNotFoundException("Policy not found: " + policyId);
        }
        return endorsementRepository.findByPolicy_PolicyIdOrderByCreatedAtDesc(policyId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional
    public PolicyEndorsementResponse approveEndorsement(UUID policyId, UUID endorsementId, EndorsementApprovalRequest request) {
        log.info("Approving endorsement {} for policy {}", endorsementId, policyId);

        PolicyEndorsement endorsement = endorsementRepository.findById(endorsementId)
                .orElseThrow(() -> new PolicyNotFoundException("Endorsement not found: " + endorsementId));

        if (!endorsement.getPolicy().getPolicyId().equals(policyId)) {
            throw new IllegalArgumentException("Endorsement does not belong to policy: " + policyId);
        }

        if (endorsement.getStatus() != EndorsementStatus.PENDING_APPROVAL) {
            throw new InvalidPolicyStateException("Endorsement is not pending approval. Current status: " + endorsement.getStatus());
        }

        Policy policy = endorsement.getPolicy();
        if (policy.getStatus() != PolicyStatus.ACTIVE) {
            throw new InvalidPolicyStateException("Cannot apply endorsement because policy is not ACTIVE: " + policy.getStatus());
        }

        // Apply policy change
        applyPolicyChange(policy, endorsement.getEndorsementType(), parseChangeData(endorsement.getChangeData()));

        endorsement.setStatus(EndorsementStatus.APPLIED);
        endorsement.setApprovedBy(request.getApprovedBy());
        endorsement.setAppliedAt(Instant.now());
        endorsement.setUpdatedAt(Instant.now());

        policy.setUpdatedAt(Instant.now());
        policyRepository.save(policy);
        PolicyEndorsement updated = endorsementRepository.save(endorsement);

        // Notify Premium Service about revised premium if required
        if (endorsement.getRevisedPremium() != null) {
            try {
                Map<String, Object> recalcReq = new HashMap<>();
                recalcReq.put("basePremium", endorsement.getRevisedPremium());
                premiumClient.recalculatePremium(policyId, recalcReq);
                log.info("Premium recalculation synced with Premium Service for policy: {}", policyId);
            } catch (Exception e) {
                log.warn("Premium Service notification failed or skipped: {}", e.getMessage());
            }
        }

        // Publish Kafka Domain Event
        PolicyEndorsedEvent event = PolicyEndorsedEvent.builder()
                .endorsementId(updated.getEndorsementId())
                .policyId(policy.getPolicyId())
                .policyNumber(policy.getPolicyNumber())
                .customerId(policy.getCustomerId())
                .endorsementType(updated.getEndorsementType())
                .description(updated.getDescription())
                .revisedPremium(updated.getRevisedPremium())
                .approvedBy(updated.getApprovedBy())
                .timestamp(Instant.now())
                .build();
        policyEventProducer.publishPolicyEndorsed(event);

        log.info("Endorsement {} successfully applied and event published", endorsementId);
        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public PolicyEndorsementResponse rejectEndorsement(UUID policyId, UUID endorsementId, EndorsementRejectRequest request) {
        log.info("Rejecting endorsement {} for policy {}", endorsementId, policyId);

        PolicyEndorsement endorsement = endorsementRepository.findById(endorsementId)
                .orElseThrow(() -> new PolicyNotFoundException("Endorsement not found: " + endorsementId));

        if (!endorsement.getPolicy().getPolicyId().equals(policyId)) {
            throw new IllegalArgumentException("Endorsement does not belong to policy: " + policyId);
        }

        if (endorsement.getStatus() != EndorsementStatus.PENDING_APPROVAL) {
            throw new InvalidPolicyStateException("Endorsement cannot be rejected from status: " + endorsement.getStatus());
        }

        endorsement.setStatus(EndorsementStatus.REJECTED);
        endorsement.setRejectionReason(request.getRejectionReason());
        endorsement.setUpdatedAt(Instant.now());

        PolicyEndorsement updated = endorsementRepository.save(endorsement);
        return mapToResponse(updated);
    }

    private void validateChangeData(Policy policy, EndorsementType type, Map<String, Object> changeData) {
        if (changeData == null || changeData.isEmpty()) {
            throw new IllegalArgumentException("Change data details are required for endorsement type: " + type);
        }

        switch (type) {
            case ADD_MEMBER -> {
                if (!changeData.containsKey("memberId")) {
                    throw new IllegalArgumentException("memberId is required for ADD_MEMBER endorsement");
                }
                UUID newMemberId = UUID.fromString(changeData.get("memberId").toString());
                boolean exists = policy.getMembers() != null && policy.getMembers().stream()
                        .anyMatch(m -> m.getMemberId().equals(newMemberId));
                if (exists) {
                    throw new IllegalArgumentException("Member " + newMemberId + " is already enrolled on policy " + policy.getPolicyId());
                }
            }
            case REMOVE_MEMBER -> {
                if (!changeData.containsKey("memberId")) {
                    throw new IllegalArgumentException("memberId is required for REMOVE_MEMBER endorsement");
                }
                UUID remMemberId = UUID.fromString(changeData.get("memberId").toString());
                boolean exists = policy.getMembers() != null && policy.getMembers().stream()
                        .anyMatch(m -> m.getMemberId().equals(remMemberId));
                if (!exists) {
                    throw new IllegalArgumentException("Member " + remMemberId + " is not an active member on policy " + policy.getPolicyId());
                }
            }
            case NOMINEE_CHANGE -> {
                if (!changeData.containsKey("beneficiaryName") || !changeData.containsKey("relationship")) {
                    throw new IllegalArgumentException("beneficiaryName and relationship are required for NOMINEE_CHANGE");
                }
            }
            case ADDRESS_CHANGE -> {
                if (!changeData.containsKey("newAddress")) {
                    throw new IllegalArgumentException("newAddress is required for ADDRESS_CHANGE");
                }
            }
            case COVERAGE_CHANGE -> {
                if (!changeData.containsKey("coverageName") || !changeData.containsKey("coverageAmount")) {
                    throw new IllegalArgumentException("coverageName and coverageAmount are required for COVERAGE_CHANGE");
                }
            }
            case RIDER_ADDITION -> {
                if (!changeData.containsKey("riderName") || !changeData.containsKey("riderAmount")) {
                    throw new IllegalArgumentException("riderName and riderAmount are required for RIDER_ADDITION");
                }
            }
        }
    }

    private BigDecimal calculateEstimatedPremium(Policy policy, EndorsementType type, Map<String, Object> changeData) {
        BigDecimal base = BigDecimal.valueOf(1100.00); // Standard policy baseline default
        try {
            Map<String, Object> premiumResp = premiumClient.getPremiumByPolicy(policy.getPolicyId());
            if (premiumResp != null && premiumResp.get("totalPremium") != null) {
                base = new BigDecimal(premiumResp.get("totalPremium").toString());
            }
        } catch (Exception e) {
            log.warn("Could not retrieve current premium from PremiumService for policy {}: {}. Defaulting to: {}",
                    policy.getPolicyId(), e.getMessage(), base);
        }

        switch (type) {
            case ADD_MEMBER -> {
                return base.add(BigDecimal.valueOf(150.00)); // +150 per added dependent member
            }
            case REMOVE_MEMBER -> {
                return base.subtract(BigDecimal.valueOf(100.00)).max(BigDecimal.valueOf(200.00));
            }
            case RIDER_ADDITION -> {
                Object amount = changeData != null ? changeData.get("riderAmount") : null;
                BigDecimal riderCost = (amount != null) ? new BigDecimal(amount.toString()).multiply(new BigDecimal("0.05")) : BigDecimal.valueOf(50.00);
                return base.add(riderCost);
            }
            case COVERAGE_CHANGE -> {
                Object amount = changeData != null ? changeData.get("coverageAmount") : null;
                BigDecimal additional = (amount != null) ? new BigDecimal(amount.toString()).multiply(new BigDecimal("0.02")) : BigDecimal.valueOf(80.00);
                return base.add(additional);
            }
            default -> {
                return null; // Non-financial endorsements (Address, Nominee) do not impact premium
            }
        }
    }

    private void applyPolicyChange(Policy policy, EndorsementType type, Map<String, Object> changeData) {
        if (changeData == null) return;

        switch (type) {
            case ADD_MEMBER -> {
                UUID memberId = UUID.fromString(changeData.get("memberId").toString());
                PolicyMember member = new PolicyMember();
                member.setPolicy(policy);
                member.setMemberId(memberId);
                if (policy.getMembers() == null) policy.setMembers(new HashSet<>());
                policy.getMembers().add(member);
            }
            case REMOVE_MEMBER -> {
                UUID remMemberId = UUID.fromString(changeData.get("memberId").toString());
                if (policy.getMembers() != null) {
                    policy.getMembers().removeIf(m -> m.getMemberId().equals(remMemberId));
                }
            }
            case NOMINEE_CHANGE -> {
                String name = changeData.get("beneficiaryName").toString();
                String rel = changeData.get("relationship").toString();
                BigDecimal pct = changeData.containsKey("percentage")
                        ? new BigDecimal(changeData.get("percentage").toString())
                        : BigDecimal.valueOf(100.00);

                if (policy.getBeneficiaries() == null) policy.setBeneficiaries(new HashSet<>());
                policy.getBeneficiaries().clear(); // Replace current nominee

                PolicyBeneficiary ben = new PolicyBeneficiary();
                ben.setPolicy(policy);
                ben.setBeneficiaryName(name);
                ben.setRelationship(rel);
                ben.setPercentage(pct);
                policy.getBeneficiaries().add(ben);
            }
            case COVERAGE_CHANGE -> {
                String name = changeData.get("coverageName").toString();
                BigDecimal amount = new BigDecimal(changeData.get("coverageAmount").toString());
                BigDecimal deductible = changeData.containsKey("deductible")
                        ? new BigDecimal(changeData.get("deductible").toString())
                        : BigDecimal.ZERO;

                if (policy.getCoverages() == null) policy.setCoverages(new HashSet<>());
                policy.getCoverages().removeIf(c -> c.getCoverageName().equalsIgnoreCase(name));

                PolicyCoverage coverage = new PolicyCoverage();
                coverage.setPolicy(policy);
                coverage.setCoverageName(name);
                coverage.setCoverageAmount(amount);
                coverage.setDeductible(deductible);
                policy.getCoverages().add(coverage);
            }
            case RIDER_ADDITION -> {
                String riderName = changeData.get("riderName").toString();
                BigDecimal riderAmount = new BigDecimal(changeData.get("riderAmount").toString());

                if (policy.getCoverages() == null) policy.setCoverages(new HashSet<>());
                PolicyCoverage riderCoverage = new PolicyCoverage();
                riderCoverage.setPolicy(policy);
                riderCoverage.setCoverageName("RIDER: " + riderName);
                riderCoverage.setCoverageAmount(riderAmount);
                riderCoverage.setDeductible(BigDecimal.ZERO);
                policy.getCoverages().add(riderCoverage);
            }
            case ADDRESS_CHANGE -> {
                log.info("Policy address change noted for policy: {} -> {}", policy.getPolicyId(), changeData.get("newAddress"));
            }
        }
    }

    private Map<String, Object> parseChangeData(String changeDataJson) {
        if (changeDataJson == null || changeDataJson.isBlank()) return Collections.emptyMap();
        try {
            return objectMapper.readValue(changeDataJson, new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Failed to deserialize changeData JSON: {}", e.getMessage());
            return Collections.emptyMap();
        }
    }

    private PolicyEndorsementResponse mapToResponse(PolicyEndorsement entity) {
        return PolicyEndorsementResponse.builder()
                .endorsementId(entity.getEndorsementId())
                .policyId(entity.getPolicy().getPolicyId())
                .policyNumber(entity.getPolicy().getPolicyNumber())
                .endorsementType(entity.getEndorsementType())
                .status(entity.getStatus())
                .description(entity.getDescription())
                .changeData(parseChangeData(entity.getChangeData()))
                .revisedPremium(entity.getRevisedPremium())
                .requestedBy(entity.getRequestedBy())
                .approvedBy(entity.getApprovedBy())
                .rejectionReason(entity.getRejectionReason())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .appliedAt(entity.getAppliedAt())
                .build();
    }
}
