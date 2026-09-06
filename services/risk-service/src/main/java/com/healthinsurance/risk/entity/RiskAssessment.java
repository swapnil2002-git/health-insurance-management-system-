package com.healthinsurance.risk.entity;

import com.healthinsurance.risk.enums.AssessmentStatus;
import com.healthinsurance.risk.enums.RiskClassification;
import jakarta.persistence.*;
import lombok.Data;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Data
@Entity
@Table(name = "risk_assessment")
public class RiskAssessment {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID assessmentId;

    // External References (No JPA mappings!)
    @Column(nullable = false)
    private UUID customerId;

    @Column(nullable = false)
    private UUID quoteId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AssessmentStatus status = AssessmentStatus.CREATED;

    @Enumerated(EnumType.STRING)
    private RiskClassification classification;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    private Instant updatedAt = Instant.now();
    private Instant completedAt;

    @OneToMany(mappedBy = "assessment", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<RiskFactor> factors;

    @OneToMany(mappedBy = "assessment", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<RiskScore> scores;
}