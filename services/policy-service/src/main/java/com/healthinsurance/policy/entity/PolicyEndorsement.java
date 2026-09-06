package com.healthinsurance.policy.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.Instant;
import java.util.UUID;

@Data
@Entity
@Table(name = "policy_endorsement")
public class PolicyEndorsement {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID endorsementId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "policy_id", nullable = false)
    private Policy policy;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false, updatable = false)
    private Instant appliedAt = Instant.now();
}