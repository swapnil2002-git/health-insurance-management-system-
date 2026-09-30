package com.healthinsurance.policy.entity;

import com.healthinsurance.policy.enums.EndorsementStatus;
import com.healthinsurance.policy.enums.EndorsementType;
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
@Table(name = "policy_endorsement")
public class PolicyEndorsement {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID endorsementId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "policy_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Policy policy;

    @Enumerated(EnumType.STRING)
    @Column(name = "endorsement_type")
    private EndorsementType endorsementType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    @Builder.Default
    private EndorsementStatus status = EndorsementStatus.REQUESTED;

    @Column(nullable = false)
    private String description;

    @Lob
    @Column(name = "change_data", columnDefinition = "LONGTEXT")
    private String changeData;

    @Column(name = "revised_premium", precision = 12, scale = 2)
    private BigDecimal revisedPremium;

    @Column(name = "requested_by")
    private String requestedBy;

    @Column(name = "approved_by")
    private String approvedBy;

    @Column(name = "rejection_reason")
    private String rejectionReason;

    @Column(name = "applied_at")
    private Instant appliedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at")
    @Builder.Default
    private Instant updatedAt = Instant.now();
}