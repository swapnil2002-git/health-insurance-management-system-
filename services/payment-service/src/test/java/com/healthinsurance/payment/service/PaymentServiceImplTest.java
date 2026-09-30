package com.healthinsurance.payment.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.healthinsurance.payment.dto.request.PaymentConfirmRequest;
import com.healthinsurance.payment.dto.response.PaymentResponse;
import com.healthinsurance.payment.entity.PaymentAllocation;
import com.healthinsurance.payment.entity.PaymentTransaction;
import com.healthinsurance.payment.enums.PaymentStatus;
import com.healthinsurance.payment.gateway.PaymentGateway;
import com.healthinsurance.payment.gateway.PaymentGatewayResponse;
import com.healthinsurance.payment.mapper.PaymentMapper;
import com.healthinsurance.payment.outbox.OutboxEvent;
import com.healthinsurance.payment.outbox.OutboxEventRepository;
import com.healthinsurance.payment.outbox.OutboxStatus;
import com.healthinsurance.payment.repository.PaymentTransactionRepository;
import com.healthinsurance.payment.service.impl.PaymentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    @Mock
    private PaymentTransactionRepository paymentRepository;

    @Mock
    private PaymentMapper mapper;

    @Mock
    private PaymentGateway paymentGateway;

    @Mock
    private OutboxEventRepository outboxEventRepository;

    @Mock
    private com.healthinsurance.payment.metrics.PaymentMetrics paymentMetrics;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @InjectMocks
    private PaymentServiceImpl paymentService;

    private PaymentTransaction testPayment;
    private UUID paymentId;
    private UUID policyId;
    private UUID installmentId;

    @BeforeEach
    void setUp() {
        paymentId = UUID.randomUUID();
        policyId = UUID.randomUUID();
        installmentId = UUID.randomUUID();

        testPayment = new PaymentTransaction();
        testPayment.setPaymentId(paymentId);
        testPayment.setPolicyId(policyId);
        testPayment.setAmount(new BigDecimal("150.00"));
        testPayment.setStatus(PaymentStatus.INITIATED);
        testPayment.setGatewayReference("REF-12345");

        PaymentAllocation alloc = new PaymentAllocation();
        alloc.setInstallmentId(installmentId);
        testPayment.setAllocations(Collections.singletonList(alloc));
    }

    @Test
    void confirmPayment_SavesPremiumPaidToTransactionalOutbox() {
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(testPayment));
        PaymentGatewayResponse gwResp = new PaymentGatewayResponse(true, "REF-12345", null);
        when(paymentGateway.confirmPayment(any(), eq(true), any())).thenReturn(gwResp);
        when(paymentRepository.save(any(PaymentTransaction.class))).thenAnswer(i -> i.getArgument(0));

        PaymentResponse mockResponse = new PaymentResponse();
        mockResponse.setPaymentId(paymentId);
        when(mapper.toPaymentResponse(any(PaymentTransaction.class))).thenReturn(mockResponse);

        PaymentConfirmRequest request = new PaymentConfirmRequest();
        request.setSuccess(true);

        PaymentResponse response = paymentService.confirmPayment(paymentId, request);

        assertNotNull(response);
        assertEquals(paymentId, response.getPaymentId());

        ArgumentCaptor<OutboxEvent> captor = ArgumentCaptor.forClass(OutboxEvent.class);
        verify(outboxEventRepository, times(1)).save(captor.capture());

        OutboxEvent outboxEvent = captor.getValue();
        assertNotNull(outboxEvent.getEventId());
        assertEquals("PremiumPaid", outboxEvent.getEventType());
        assertEquals(paymentId.toString(), outboxEvent.getAggregateId());
        assertEquals("premium-events", outboxEvent.getTopic());
        assertEquals(paymentId.toString(), outboxEvent.getPartitionKey());
        assertEquals(OutboxStatus.PENDING, outboxEvent.getStatus());
        assertNotNull(outboxEvent.getPayload());
        assertTrue(outboxEvent.getPayload().contains("PremiumPaid"));
        assertTrue(outboxEvent.getPayload().contains(paymentId.toString()));
        assertTrue(outboxEvent.getPayload().contains("150.00"));
        assertNotNull(outboxEvent.getCreatedAt());
    }
}

