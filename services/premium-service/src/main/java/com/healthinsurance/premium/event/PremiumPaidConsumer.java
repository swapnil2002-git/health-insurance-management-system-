package com.healthinsurance.premium.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthinsurance.premium.dto.request.InstallmentPaymentRequest;
import com.healthinsurance.premium.service.PremiumService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PremiumPaidConsumer {

    private final PremiumService premiumService;
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
            if (event.getInstallmentId() == null) {
                log.warn("Installment ID is null in PremiumPaidEvent. Skipping installment ledger update.");
                return;
            }

            InstallmentPaymentRequest request = new InstallmentPaymentRequest();
            request.setAmount(event.getAmount());
            request.setPaymentReference(event.getGatewayReference());

            premiumService.recordInstallmentPayment(event.getInstallmentId(), request);
            log.info("SUCCESS: Premium Service ledger updated. Installment marked PAID and outstanding balance reduced.");
        } catch (Exception e) {
            log.error("Failed to process PremiumPaidEvent in premium-service: {}", e.getMessage());
        }
        log.info("=========================================================");
    }
}