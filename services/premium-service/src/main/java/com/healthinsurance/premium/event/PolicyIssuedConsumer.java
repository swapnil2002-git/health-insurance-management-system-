package com.healthinsurance.premium.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthinsurance.premium.client.QuotationClient;
import com.healthinsurance.premium.client.dto.QuoteResponseDto;
import com.healthinsurance.premium.dto.request.PremiumScheduleCreateRequest;
import com.healthinsurance.premium.enums.PaymentFrequency;
import com.healthinsurance.premium.repository.PremiumScheduleRepository;
import com.healthinsurance.premium.service.PremiumService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneOffset;

@Slf4j
@Component
@RequiredArgsConstructor
public class PolicyIssuedConsumer {

    private final PremiumService premiumService;
    private final PremiumScheduleRepository scheduleRepository;
    private final QuotationClient quotationClient;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "policy-events", groupId = "premium-group")
    public void consumePolicyIssued(String message) {
        log.info("=========================================================");
        log.info("KAFKA LISTENER TRIGGERED: PolicyIssuedEvent received");

        try {
            String cleanMessage = message;
            if (cleanMessage != null && cleanMessage.startsWith("\"") && cleanMessage.endsWith("\"")) {
                cleanMessage = objectMapper.readValue(cleanMessage, String.class);
            }
            PolicyIssuedEvent event = objectMapper.readValue(cleanMessage, PolicyIssuedEvent.class);
            log.info("Policy Number: {}, Policy ID: {}, Quote ID: {}",
                    event.getPolicyNumber(), event.getPolicyId(), event.getQuoteId());

            if (event.getPolicyId() == null) {
                log.warn("Invalid PolicyIssuedEvent: policyId is null. Skipping processing.");
                return;
            }

            if (scheduleRepository.existsByPolicyId(event.getPolicyId())) {
                log.info("Idempotency Guard: Premium schedule already exists for policy ID {}. Skipping.", event.getPolicyId());
                log.info("=========================================================");
                return;
            }

            BigDecimal calculatedBasePremium = BigDecimal.valueOf(5000.00);

            if (event.getQuoteId() != null) {
                try {
                    log.info("Fetching Quote details via OpenFeign for Quote ID: {}", event.getQuoteId());
                    QuoteResponseDto quote = quotationClient.getQuote(event.getQuoteId());
                    if (quote != null && quote.getTotalPremium() != null && quote.getTotalPremium().compareTo(BigDecimal.ZERO) > 0) {
                        calculatedBasePremium = quote.getTotalPremium();
                        log.info("Retrieved total premium: {} from Quote Number: {}", calculatedBasePremium, quote.getQuoteNumber());
                    }
                } catch (Exception e) {
                    log.warn("Could not fetch quote details via Feign ({}). Using default premium of {}.", e.getMessage(), calculatedBasePremium);
                }
            }

            LocalDate startDate = event.getEffectiveDate() != null
                    ? event.getEffectiveDate().atZone(ZoneOffset.UTC).toLocalDate()
                    : LocalDate.now();

            LocalDate endDate = event.getExpiryDate() != null
                    ? event.getExpiryDate().atZone(ZoneOffset.UTC).toLocalDate()
                    : startDate.plusYears(1);

            PremiumScheduleCreateRequest request = new PremiumScheduleCreateRequest();
            request.setPolicyId(event.getPolicyId());
            request.setPaymentFrequency(PaymentFrequency.ANNUAL);
            request.setStartDate(startDate);
            request.setEndDate(endDate);
            request.setBasePremium(calculatedBasePremium);
            request.setRiderPremium(BigDecimal.ZERO);
            request.setRiskLoading(BigDecimal.ZERO);
            request.setDiscount(BigDecimal.ZERO);
            request.setTax(BigDecimal.ZERO);

            premiumService.createPremiumSchedule(request);
            log.info("SUCCESS: Automated Premium Schedule & Installments created for Policy: {}", event.getPolicyNumber());

        } catch (Exception e) {
            log.error("Failed to process PolicyIssuedEvent: {}", e.getMessage());
        }

        log.info("=========================================================");
    }
}