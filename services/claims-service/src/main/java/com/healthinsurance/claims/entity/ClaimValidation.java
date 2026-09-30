package com.healthinsurance.claims.entity;

import com.healthinsurance.claims.domain.ValidationStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "claim_validation")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClaimValidation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "validation_id", updatable = false, nullable = false)
    private UUID validationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "claim_id", nullable = false)
    private Claim claim;

    @Column(name = "rule_name", nullable = false, length = 100)
    private String ruleName;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private ValidationStatus status;

    @Column(name = "message", length = 500)
    private String message;

    @Column(name = "validated_at", nullable = false, updatable = false)
    private Instant validatedAt = Instant.now();

    @PrePersist
    public void prePersist() {
        if (this.validatedAt == null) this.validatedAt = Instant.now();
    }
}
