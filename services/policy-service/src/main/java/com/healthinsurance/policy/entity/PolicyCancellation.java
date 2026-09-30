package com.healthinsurance.policy.entity;

import com.healthinsurance.policy.enums.CancellationStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "policy_cancellation")
public class PolicyCancellation {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID cancellationId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "policy_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Policy policy;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    @Builder.Default
    private CancellationStatus status = CancellationStatus.REQUESTED;

    @Column(nullable = false)
    private String reason;

    @Column(name = "refund_amount", precision = 12, scale = 2)
    private BigDecimal refundAmount;

    @Column(name = "refund_transaction_id")
    private UUID refundTransactionId;

    @Column(name = "requested_by")
    private String requestedBy;

    @Column(name = "approved_by")
    private String approvedBy;

    @Column(name = "rejection_reason")
    private String rejectionReason;

    @Column(name = "cancelled_at")
    private Instant cancelledAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at")
    @Builder.Default
    private Instant updatedAt = Instant.now();
}