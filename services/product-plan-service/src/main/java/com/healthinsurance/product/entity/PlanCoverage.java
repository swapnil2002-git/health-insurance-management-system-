package com.healthinsurance.product.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import java.util.UUID;

@Data
@Entity
@Table(name = "plan_coverage")
public class PlanCoverage {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID planCoverageId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private InsurancePlan plan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coverage_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Coverage coverage;
}