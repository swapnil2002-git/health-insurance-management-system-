package com.healthinsurance.claims.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "claim_diagnosis")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClaimDiagnosis {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "diagnosis_id", updatable = false, nullable = false)
    private UUID diagnosisId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "claim_id", nullable = false)
    private Claim claim;

    @Column(name = "diagnosis_code", nullable = false, length = 50)
    private String diagnosisCode;

    @Column(name = "description", nullable = false, length = 255)
    private String description;

    @Column(name = "is_primary", nullable = false)
    private boolean primary = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) this.createdAt = Instant.now();
    }
}
