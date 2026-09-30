package com.healthinsurance.reporting.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthinsurance.reporting.event.ClaimEvent;
import com.healthinsurance.reporting.event.PolicyIssuedEvent;
import com.healthinsurance.reporting.event.PremiumPaidEvent;
import com.healthinsurance.reporting.service.ReportingService;
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
class ReportingConsumersTest {

    @Mock
    private ReportingService reportingService;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper().registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());

    @Test
    void testPolicyReportingConsumer() throws Exception {
        PolicyReportingConsumer consumer = new PolicyReportingConsumer(reportingService, objectMapper);
        PolicyIssuedEvent event = new PolicyIssuedEvent();
        event.setPolicyId(UUID.randomUUID());
        event.setPolicyNumber("POL-101");
        event.setEffectiveDate(Instant.now());
        event.setStatus("ACTIVE");
        String json = objectMapper.writeValueAsString(event);
        org.apache.kafka.clients.consumer.ConsumerRecord<String, String> record =
                new org.apache.kafka.clients.consumer.ConsumerRecord<>("policy-events", 0, 0L, event.getPolicyId().toString(), json);

        consumer.consumePolicyEvent(record);

        verify(reportingService).recordPolicyIssued(anyString(), any(), eq("ACTIVE"));
    }

    @Test
    void testPremiumReportingConsumer() throws Exception {
        PremiumReportingConsumer consumer = new PremiumReportingConsumer(reportingService, objectMapper);
        PremiumPaidEvent event = new PremiumPaidEvent();
        event.setPaymentId(UUID.randomUUID());
        event.setAmount(new BigDecimal("300.00"));
        event.setPaymentDate(Instant.now());
        String json = objectMapper.writeValueAsString(event);
        org.apache.kafka.clients.consumer.ConsumerRecord<String, String> record =
                new org.apache.kafka.clients.consumer.ConsumerRecord<>("premium-events", 0, 0L, event.getPaymentId().toString(), json);

        consumer.consumePremiumEvent(record);

        verify(reportingService).recordPremiumPayment(anyString(), any(), eq(new BigDecimal("300.00")));
    }

    @Test
    void testClaimReportingConsumer() throws Exception {
        ClaimReportingConsumer consumer = new ClaimReportingConsumer(reportingService, objectMapper);
        ClaimEvent event = new ClaimEvent();
        event.setClaimId(UUID.randomUUID());
        event.setEventType("CLAIM_APPROVED");
        event.setStatus("APPROVED");
        event.setTotalClaimAmount(new BigDecimal("1000.00"));
        event.setApprovedAmount(new BigDecimal("800.00"));
        event.setTimestamp(Instant.now());
        String json = objectMapper.writeValueAsString(event);
        org.apache.kafka.clients.consumer.ConsumerRecord<String, String> record =
                new org.apache.kafka.clients.consumer.ConsumerRecord<>("claim-events", 0, 0L, event.getClaimId().toString(), json);

        consumer.consumeClaimEvent(record);

        verify(reportingService).recordClaimEvent(
                anyString(),
                any(),
                eq("CLAIM_APPROVED"),
                eq("APPROVED"),
                eq(new BigDecimal("1000.00")),
                eq(new BigDecimal("800.00"))
        );
    }
}
