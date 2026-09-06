package com.healthinsurance.policy.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.Instant;
import java.util.UUID;

@Data
@Entity
@Table(name = "policy_cancellation")
public class PolicyCancellation {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID cancellationId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "policy_id", nullable = false)
    private Policy policy;

    @Column(nullable = false)
    private String reason;

    @Column(nullable = false, updatable = false)
    private Instant cancelledAt = Instant.now();
}