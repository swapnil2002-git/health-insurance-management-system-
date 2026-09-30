package com.healthinsurance.claims.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "explanation_of_benefits", uniqueConstraints = {
    @UniqueConstraint(name = "uk_eob_claim_id", columnNames = {"claim_id"}),
    @UniqueConstraint(name = "uk_eob_number", columnNames = {"eob_number"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExplanationOfBenefits {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "eob_id", updatable = false, nullable = false)
    private UUID eobId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "claim_id", nullable = false, unique = true)
    private Claim claim;

    @Column(name = "eob_number", nullable = false, unique = true, length = 50)
    private String eobNumber;

    @Column(name = "claim_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal claimAmount;

    @Column(name = "allowed_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal allowedAmount;

    @Column(name = "deductible", nullable = false, precision = 12, scale = 2)
    private BigDecimal deductible = BigDecimal.ZERO;

    @Column(name = "copay", nullable = false, precision = 12, scale = 2)
    private BigDecimal copay = BigDecimal.ZERO;

    @Column(name = "insurance_payment", nullable = false, precision = 12, scale = 2)
    private BigDecimal insurancePayment = BigDecimal.ZERO;

    @Column(name = "customer_responsibility", nullable = false, precision = 12, scale = 2)
    private BigDecimal customerResponsibility = BigDecimal.ZERO;

    @Column(name = "remarks", length = 1000)
    private String remarks;

    @Column(name = "generated_at", nullable = false, updatable = false)
    private Instant generatedAt = Instant.now();

    @PrePersist
    public void prePersist() {
        if (this.generatedAt == null) this.generatedAt = Instant.now();
        if (this.deductible == null) this.deductible = BigDecimal.ZERO;
        if (this.copay == null) this.copay = BigDecimal.ZERO;
        if (this.insurancePayment == null) this.insurancePayment = BigDecimal.ZERO;
        if (this.customerResponsibility == null) this.customerResponsibility = BigDecimal.ZERO;
    }
}
