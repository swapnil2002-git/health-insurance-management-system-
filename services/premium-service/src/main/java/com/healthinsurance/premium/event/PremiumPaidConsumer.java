package com.healthinsurance.premium.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthinsurance.premium.dto.request.InstallmentPaymentRequest;
import com.healthinsurance.premium.entity.PremiumInstallment;
import com.healthinsurance.premium.enums.InstallmentStatus;
import com.healthinsurance.premium.repository.PremiumScheduleRepository;
import com.healthinsurance.premium.service.PremiumService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class PremiumPaidConsumer {

    private final PremiumService premiumService;
    private final PremiumScheduleRepository scheduleRepository;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "premium-events", groupId = "premium-payment-group")
    public void consumePremiumPaid(String message) {
        log.info("=========================================================");
        log.info("PREMIUM SERVICE KAFKA LISTENER: Received raw message on 'premium-events': {}", message);

        try {
            String cleanMessage = message;
            if (cleanMessage != null && cleanMessage.startsWith("\"") && cleanMessage.endsWith("\"")) {
                cleanMessage = objectMapper.readValue(cleanMessage, String.class);
            }
            PremiumPaidEvent event = objectMapper.readValue(cleanMessage, PremiumPaidEvent.class);

            UUID targetInstallmentId = event.getInstallmentId();
            if (targetInstallmentId == null && event.getPolicyId() != null) {
                log.info("Installment ID is null in event. Looking for pending installment for policy ID: {}", event.getPolicyId());
                targetInstallmentId = scheduleRepository.findByPolicyId(event.getPolicyId())
                        .flatMap(s -> s.getInstallments().stream()
                                .filter(i -> i.getStatus() != InstallmentStatus.PAID)
                                .findFirst()
                                .map(PremiumInstallment::getInstallmentId))
                        .orElse(null);
            }

            if (targetInstallmentId == null) {
                log.warn("Could not determine installment ID for PremiumPaidEvent. Skipping installment ledger update.");
                return;
            }

            InstallmentPaymentRequest request = new InstallmentPaymentRequest();
            request.setAmount(event.getAmount());
            request.setPaymentReference(event.getGatewayReference());

            premiumService.recordInstallmentPayment(targetInstallmentId, request);
            log.info("SUCCESS: Premium Service ledger updated for installment {}. Installment marked PAID and outstanding balance reduced.", targetInstallmentId);
        } catch (Exception e) {
            log.error("Failed to process PremiumPaidEvent in premium-service: {}", e.getMessage());
        }
        log.info("=========================================================");
    }
}