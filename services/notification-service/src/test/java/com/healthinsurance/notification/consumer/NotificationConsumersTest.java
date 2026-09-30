package com.healthinsurance.notification.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthinsurance.notification.dto.NotificationRequest;
import com.healthinsurance.notification.event.ClaimEvent;
import com.healthinsurance.notification.event.PolicyIssuedEvent;
import com.healthinsurance.notification.event.PremiumPaidEvent;
import com.healthinsurance.notification.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationConsumersTest {

    @Mock
    private NotificationService notificationService;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void testPolicyEventConsumer() {
        PolicyEventConsumer consumer = new PolicyEventConsumer(notificationService, objectMapper);
        PolicyIssuedEvent event = new PolicyIssuedEvent();
        event.setPolicyId(UUID.randomUUID());
        event.setPolicyNumber("POL-999");
        event.setCustomerId(UUID.randomUUID());
        event.setTimestamp(Instant.now());

        // Pass event directly
        consumer.consumePolicyEvent(event);

        verify(notificationService, times(2)).sendNotification(any(NotificationRequest.class));
    }

    @Test
    void testPremiumEventConsumer() {
        PremiumEventConsumer consumer = new PremiumEventConsumer(notificationService, objectMapper);
        PremiumPaidEvent event = new PremiumPaidEvent();
        event.setPaymentId(UUID.randomUUID());
        event.setPolicyId(UUID.randomUUID());
        event.setAmount(new BigDecimal("250.00"));
        event.setGatewayReference("REF-XYZ");
        event.setTimestamp(Instant.now());

        // Pass event directly
        consumer.consumePremiumEvent(event);

        verify(notificationService, times(2)).sendNotification(any(NotificationRequest.class));
    }

    @Test
    void testClaimEventConsumer() {
        ClaimEventConsumer consumer = new ClaimEventConsumer(notificationService, objectMapper);
        ClaimEvent event = new ClaimEvent();
        event.setClaimId(UUID.randomUUID());
        event.setClaimNumber("CLM-100");
        event.setEventType("CLAIM_APPROVED");
        event.setStatus("APPROVED");
        event.setTotalClaimAmount(new BigDecimal("1500.00"));
        event.setApprovedAmount(new BigDecimal("1200.00"));

        // Pass event directly
        consumer.consumeClaimEvent(event);

        verify(notificationService, times(2)).sendNotification(any(NotificationRequest.class));
    }
}
