package com.healthinsurance.policy.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthinsurance.policy.client.ProductClient;
import com.healthinsurance.policy.client.QuotationClient;
import com.healthinsurance.policy.client.dto.PlanDto;
import com.healthinsurance.policy.client.dto.QuoteDto;
import com.healthinsurance.policy.dto.request.PolicyCoverageRequest;
import com.healthinsurance.policy.dto.request.PolicyCreateRequest;
import com.healthinsurance.policy.dto.request.PolicyMemberRequest;
import com.healthinsurance.policy.service.PolicyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class UnderwritingEventConsumer {

    private final PolicyService policyService;
    private final QuotationClient quotationClient;
    private final ProductClient productClient;
    private final com.healthinsurance.policy.client.CustomerClient customerClient;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "underwriting-events", groupId = "policy-group")
    public void consumeUnderwritingApproved(String message) {
        log.info("=========================================================");
        log.info("KAFKA LISTENER TRIGGERED: Underwriting Approved Event received");

        try {
            UnderwritingApprovedEvent event = objectMapper.readValue(message, UnderwritingApprovedEvent.class);
            log.info("Starting Automated Policy Creation for Quote ID: {}", event.getQuoteId());

            // 1. Fetch Quote Details
            log.info("Fetching Quote details via OpenFeign...");
            QuoteDto quote = quotationClient.getQuoteById(event.getQuoteId());

            // 2. Fetch Plan/Coverage Details
            log.info("Fetching Plan coverage details via OpenFeign...");
            PlanDto plan = productClient.getPlanById(quote.getPlanId());

            // 3. Build the Policy Payload
            PolicyCreateRequest request = new PolicyCreateRequest();
            request.setCustomerId(event.getCustomerId());
            request.setQuoteId(event.getQuoteId());
            request.setPlanId(quote.getPlanId());
            request.setEffectiveDate(Instant.now());
            request.setExpiryDate(Instant.now().plus(365, ChronoUnit.DAYS));

            // Map Members: Fetch real member IDs from Customer Service
            List<PolicyMemberRequest> policyMembers = new ArrayList<>();
            try {
                log.info("Fetching real registered members from Customer Service for Customer: {}", event.getCustomerId());
                List<com.healthinsurance.policy.client.dto.CustomerMemberDto> customerMembers = customerClient.getMembersByCustomerId(event.getCustomerId());
                if (customerMembers != null && !customerMembers.isEmpty()) {
                    for (com.healthinsurance.policy.client.dto.CustomerMemberDto cm : customerMembers) {
                        PolicyMemberRequest pmr = new PolicyMemberRequest();
                        pmr.setMemberId(cm.getMemberId());
                        policyMembers.add(pmr);
                        log.info("Enrolled real Customer Member in Policy: {} ({})", cm.getMemberId(), cm.getFirstName());
                    }
                }
            } catch (Exception ex) {
                log.warn("Could not retrieve customer members via CustomerClient: {}", ex.getMessage());
            }

            // Fallback: If no customer members were found in Customer Service, use quote members
            if (policyMembers.isEmpty() && quote.getMembers() != null) {
                for (com.healthinsurance.policy.client.dto.QuoteMemberDto m : quote.getMembers()) {
                    PolicyMemberRequest pmr = new PolicyMemberRequest();
                    pmr.setMemberId(m.getQuoteMemberId());
                    policyMembers.add(pmr);
                }
            }
            request.setMembers(policyMembers);

            // Map Coverages
            if (plan.getCoverages() != null) {
                request.setCoverages(plan.getCoverages().stream().map(c -> {
                    PolicyCoverageRequest pcr = new PolicyCoverageRequest();
                    String covName = (c.getCoverageName() != null && !c.getCoverageName().isBlank()) 
                            ? c.getCoverageName() 
                            : "Standard Comprehensive Coverage";
                    pcr.setCoverageName(covName);
                    pcr.setCoverageAmount(c.getCoverageAmount() != null ? c.getCoverageAmount() : java.math.BigDecimal.valueOf(500000.00));
                    pcr.setDeductible(c.getDeductible() != null ? c.getDeductible() : java.math.BigDecimal.ZERO);
                    return pcr;
                }).collect(Collectors.toList()));
            }

            request.setBeneficiaries(new ArrayList<>());

            // 4. Execute the Service layer
            policyService.createPolicy(request);
            log.info("SUCCESS: Automated Policy Creation completed for Quote: {}", event.getQuoteId());

        } catch (Exception e) {
            log.error("Automated Policy Creation FAILED. Error: {}", e.getMessage());
        }

        log.info("=========================================================");
    }
}