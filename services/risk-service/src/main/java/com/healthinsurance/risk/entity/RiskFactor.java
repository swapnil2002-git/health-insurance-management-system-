package com.healthinsurance.risk.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import java.util.UUID;

@Data
@Entity
@Table(name = "risk_factor")
public class RiskFactor {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID riskFactorId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assessment_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private RiskAssessment assessment;

    @Column(nullable = false)
    private String factorName;

    @Column(nullable = false)
    private String factorValue;

    private String description;
}