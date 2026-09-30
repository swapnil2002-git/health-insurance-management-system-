package com.healthinsurance.policy.entity;

import com.healthinsurance.policy.enums.RenewalStatus;
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
@Table(name = "policy_renewal")
public class PolicyRenewal {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID renewalId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "policy_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Policy policy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RenewalStatus status;

    private UUID renewalQuoteId;

    @Column(precision = 12, scale = 2)
    private BigDecimal renewalPremium;

    private UUID paymentId;

    private Instant newEffectiveDate;
    private Instant newExpiryDate;

    private String rejectionReason;

    @Version
    private Long version;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    @Builder.Default
    private Instant updatedAt = Instant.now();

    @Column(length = 100)
    @Builder.Default
    private String createdBy = "SYSTEM";

    @Column(length = 100)
    @Builder.Default
    private String updatedBy = "SYSTEM";
}
