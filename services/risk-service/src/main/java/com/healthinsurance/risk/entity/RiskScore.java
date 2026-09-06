package com.healthinsurance.risk.entity;

import com.healthinsurance.risk.enums.RiskClassification;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import java.time.Instant;
import java.util.UUID;

@Data
@Entity
@Table(name = "risk_score")
public class RiskScore {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID riskScoreId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assessment_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private RiskAssessment assessment;

    @Column(nullable = false)
    private Integer score;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RiskClassification classification;

    @Column(nullable = false, updatable = false)
    private Instant calculatedAt = Instant.now();
}