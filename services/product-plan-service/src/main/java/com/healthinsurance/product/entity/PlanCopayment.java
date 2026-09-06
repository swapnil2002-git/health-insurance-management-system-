package com.healthinsurance.product.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import java.util.UUID;

@Data
@Entity
@Table(name = "plan_copayment")
public class PlanCopayment {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID planCopaymentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private InsurancePlan plan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "copayment_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Copayment copayment;
}