package com.healthinsurance.policy.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.util.UUID;

@Data
@Entity
@Table(name = "policy_coverage")
public class PolicyCoverage {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID policyCoverageId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "policy_id", nullable = false)
    private Policy policy;

    // Snapshot of the coverage at the time of policy issuance (Rule #19)
    @Column(nullable = false)
    private String coverageName;
    
    private BigDecimal coverageAmount;
    private BigDecimal deductible;
}