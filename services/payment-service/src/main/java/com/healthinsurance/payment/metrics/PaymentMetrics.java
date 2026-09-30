package com.healthinsurance.payment.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class PaymentMetrics {

    private final MeterRegistry meterRegistry;

    public PaymentMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    public void recordPaymentRequested(String paymentMethod) {
        String safeMethod = (paymentMethod != null && !paymentMethod.isBlank()) ? paymentMethod : "UNKNOWN";
        Counter.builder("payment_requests_total")
                .description("Total payment transactions initiated")
                .tag("payment_method", safeMethod)
                .register(meterRegistry)
                .increment();
    }

    public void recordPaymentSuccess(String paymentMethod) {
        String safeMethod = (paymentMethod != null && !paymentMethod.isBlank()) ? paymentMethod : "UNKNOWN";
        Counter.builder("payment_success_total")
                .description("Total successful payment transactions")
                .tag("payment_method", safeMethod)
                .register(meterRegistry)
                .increment();
    }

    public void recordPaymentFailure(String paymentMethod, String failureReasonCategory) {
        String safeMethod = (paymentMethod != null && !paymentMethod.isBlank()) ? paymentMethod : "UNKNOWN";
        String safeCategory = (failureReasonCategory != null && !failureReasonCategory.isBlank()) ? failureReasonCategory : "GENERAL_FAILURE";
        Counter.builder("payment_failures_total")
                .description("Total failed payment transactions")
                .tag("payment_method", safeMethod)
                .tag("failure_reason_category", safeCategory)
                .register(meterRegistry)
                .increment();
    }

    public void recordIdempotencyConflict(String operation) {
        String safeOp = (operation != null && !operation.isBlank()) ? operation : "UNKNOWN";
        Counter.builder("idempotency_conflicts_total")
                .description("Total count of idempotency payload conflicts or concurrent access")
                .tag("operation", safeOp)
                .register(meterRegistry)
                .increment();
    }
}