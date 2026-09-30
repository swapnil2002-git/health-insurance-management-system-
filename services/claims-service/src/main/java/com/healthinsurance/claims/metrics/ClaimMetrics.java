package com.healthinsurance.claims.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Component
public class ClaimMetrics {

    private final MeterRegistry meterRegistry;

    public ClaimMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    public void recordClaimSubmitted(String claimType) {
        String safeType = (claimType != null && !claimType.isBlank()) ? claimType : "UNKNOWN";
        Counter.builder("claims_submitted_total")
                .description("Total number of claims submitted")
                .tag("claim_type", safeType)
                .register(meterRegistry)
                .increment();
    }

    public void recordClaimAdjudicated(String decision) {
        String safeDecision = (decision != null && !decision.isBlank()) ? decision : "UNKNOWN";
        Counter.builder("claims_adjudicated_total")
                .description("Total number of claims adjudicated")
                .tag("decision", safeDecision)
                .register(meterRegistry)
                .increment();
    }

    public void recordClaimSettled(String payeeType) {
        String safePayee = (payeeType != null && !payeeType.isBlank()) ? payeeType : "UNKNOWN";
        Counter.builder("claims_settled_total")
                .description("Total number of claims settled")
                .tag("payee_type", safePayee)
                .register(meterRegistry)
                .increment();
    }

    public void recordAdjudicationDuration(Duration duration, String decision) {
        String safeDecision = (decision != null && !decision.isBlank()) ? decision : "UNKNOWN";
        Timer.builder("claim_adjudication_duration_seconds")
                .description("Duration from claim creation to adjudication decision")
                .tag("decision", safeDecision)
                .publishPercentiles(0.5, 0.9, 0.95, 0.99)
                .register(meterRegistry)
                .record(duration.toMillis(), TimeUnit.MILLISECONDS);
    }

    public void recordClaimProcessingDuration(Duration duration) {
        Timer.builder("claim_processing_total_duration_seconds")
                .description("End-to-end duration from claim creation to final settlement")
                .publishPercentiles(0.5, 0.9, 0.95, 0.99)
                .register(meterRegistry)
                .record(duration.toMillis(), TimeUnit.MILLISECONDS);
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