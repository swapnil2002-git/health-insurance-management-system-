package com.healthinsurance.policy.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.util.UUID;

@Data
@Entity
@Table(name = "policy_beneficiary")
public class PolicyBeneficiary {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID policyBeneficiaryId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "policy_id", nullable = false)
    private Policy policy;

    @Column(nullable = false)
    private String beneficiaryName;
    
    @Column(nullable = false)
    private String relationship;
    
    @Column(nullable = false)
    private BigDecimal percentage; // e.g., 50.00 for 50%
}