package com.healthinsurance.payment.idempotency;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.healthinsurance.payment.dto.request.PaymentInitiateRequest;
import com.healthinsurance.payment.dto.response.PaymentResponse;
import com.healthinsurance.payment.entity.PaymentTransaction;
import com.healthinsurance.payment.enums.PaymentMethod;
import com.healthinsurance.payment.enums.PaymentStatus;
import com.healthinsurance.payment.event.PaymentEventProducer;
import com.healthinsurance.payment.exception.IdempotencyConflictException;
import com.healthinsurance.payment.gateway.PaymentGateway;
import com.healthinsurance.payment.gateway.PaymentGatewayResponse;
import com.healthinsurance.payment.mapper.PaymentMapper;
import com.healthinsurance.payment.outbox.OutboxEventRepository;
import com.healthinsurance.payment.repository.PaymentAllocationRepository;
import com.healthinsurance.payment.repository.PaymentTransactionRepository;
import com.healthinsurance.payment.repository.RefundTransactionRepository;
import com.healthinsurance.payment.service.impl.PaymentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentIdempotencyTest {

    @Mock
    private PaymentTransactionRepository paymentRepository;
    @Mock
    private PaymentAllocationRepository allocationRepository;
    @Mock
    private RefundTransactionRepository refundRepository;
    @Mock
    private PaymentGateway paymentGateway;
    @Mock
    private PaymentEventProducer eventProducer;
    @Mock
    private OutboxEventRepository outboxEventRepository;
    @Mock
    private PaymentMapper mapper;
    @Mock
    private IdempotencyRecordRepository idempotencyRecordRepository;
    @Mock
    private com.healthinsurance.payment.metrics.PaymentMetrics paymentMetrics;
    @Spy
    private ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @InjectMocks
    private PaymentServiceImpl paymentService;

    private UUID paymentId;
    private PaymentTransaction payment;
    private PaymentInitiateRequest request;
    private PaymentResponse paymentResponse;
    private final String idempotencyKey = "PAY-KEY-99999";

    @BeforeEach
    void setUp() {
        paymentId = UUID.randomUUID();

        payment = new PaymentTransaction();
        payment.setPaymentId(paymentId);
        payment.setPolicyId(UUID.randomUUID());
        payment.setAmount(new BigDecimal("5000.00"));
        payment.setCurrency("INR");
        payment.setPaymentMethod(PaymentMethod.CREDIT_CARD);
        payment.setStatus(PaymentStatus.PENDING);
        payment.setGatewayReference("GTW-12345");

        request = new PaymentInitiateRequest();
        request.setPolicyId(payment.getPolicyId());
        request.setInstallmentId(UUID.randomUUID());
        request.setAmount(new BigDecimal("5000.00"));
        request.setPaymentMethod(PaymentMethod.CREDIT_CARD);

        paymentResponse = new PaymentResponse();
        paymentResponse.setPaymentId(paymentId);
        paymentResponse.setPolicyId(payment.getPolicyId());
        paymentResponse.setAmount(new BigDecimal("5000.00"));
        paymentResponse.setCurrency("INR");
        paymentResponse.setPaymentMethod(PaymentMethod.CREDIT_CARD);
        paymentResponse.setStatus(PaymentStatus.PENDING);
        paymentResponse.setGatewayReference("GTW-12345");
        paymentResponse.setCreatedAt(Instant.now());
        paymentResponse.setUpdatedAt(Instant.now());
    }

    @Test
    void testInitiatePayment_FirstRequest_CallsGatewayAndSavesRecord() {
        when(idempotencyRecordRepository.findByIdempotencyKey(idempotencyKey)).thenReturn(Optional.empty());
        when(idempotencyRecordRepository.saveAndFlush(any())).thenAnswer(i -> i.getArgument(0));
        when(paymentGateway.initiatePayment(any(), any(), any()))
                .thenReturn(new PaymentGatewayResponse(true, "GTW-12345", null));
        when(paymentRepository.save(any(PaymentTransaction.class))).thenReturn(payment);
        when(mapper.toPaymentResponse(any(PaymentTransaction.class))).thenReturn(paymentResponse);

        PaymentResponse result = paymentService.initiatePayment(request, idempotencyKey);

        assertNotNull(result);
        assertEquals(paymentId, result.getPaymentId());
        verify(paymentGateway, times(1)).initiatePayment(any(), any(), any());
        verify(paymentRepository, times(1)).save(any(PaymentTransaction.class));
        verify(idempotencyRecordRepository, times(1)).save(any(IdempotencyRecord.class));
    }

    @Test
    void testInitiatePayment_DuplicateRequest_ReturnsCachedResponseWithoutNewGatewayCallOrPayment() throws Exception {
        String requestHash = RequestHashUtil.computeHash(request, objectMapper);

        IdempotencyRecord completedRecord = IdempotencyRecord.builder()
                .idempotencyKey(idempotencyKey)
                .operationType("PAYMENT_INITIATION")
                .requestHash(requestHash)
                .status(IdempotencyStatus.COMPLETED)
                .responseStatus(201)
                .responseBody(objectMapper.writeValueAsString(paymentResponse))
                .createdAt(Instant.now())
                .build();

        when(idempotencyRecordRepository.findByIdempotencyKey(idempotencyKey)).thenReturn(Optional.of(completedRecord));

        PaymentResponse result = paymentService.initiatePayment(request, idempotencyKey);

        assertNotNull(result);
        assertEquals(paymentId, result.getPaymentId());
        assertEquals("GTW-12345", result.getGatewayReference());

        // Zero duplicate financial charges and zero duplicate records in DB!
        verify(paymentGateway, never()).initiatePayment(any(), any(), any());
        verify(paymentRepository, never()).save(any(PaymentTransaction.class));
    }

    @Test
    void testInitiatePayment_SameKeyDifferentPayload_ThrowsConflictException() {
        IdempotencyRecord existingRecord = IdempotencyRecord.builder()
                .idempotencyKey(idempotencyKey)
                .operationType("PAYMENT_INITIATION")
                .requestHash("different-sha256-hash-value")
                .status(IdempotencyStatus.COMPLETED)
                .build();

        when(idempotencyRecordRepository.findByIdempotencyKey(idempotencyKey)).thenReturn(Optional.of(existingRecord));

        assertThrows(IdempotencyConflictException.class, () -> paymentService.initiatePayment(request, idempotencyKey));
        verify(paymentGateway, never()).initiatePayment(any(), any(), any());
        verify(paymentRepository, never()).save(any(PaymentTransaction.class));
    }

    @Test
    void testInitiatePayment_RequestInProgress_ThrowsConflictException() {
        String requestHash = RequestHashUtil.computeHash(request, objectMapper);

        IdempotencyRecord inProgressRecord = IdempotencyRecord.builder()
                .idempotencyKey(idempotencyKey)
                .operationType("PAYMENT_INITIATION")
                .requestHash(requestHash)
                .status(IdempotencyStatus.PROCESSING)
                .build();

        when(idempotencyRecordRepository.findByIdempotencyKey(idempotencyKey)).thenReturn(Optional.of(inProgressRecord));

        assertThrows(IdempotencyConflictException.class, () -> paymentService.initiatePayment(request, idempotencyKey));
        verify(paymentGateway, never()).initiatePayment(any(), any(), any());
        verify(paymentRepository, never()).save(any(PaymentTransaction.class));
    }
}
