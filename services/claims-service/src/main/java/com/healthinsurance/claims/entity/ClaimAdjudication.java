package com.healthinsurance.claims.entity;

import com.healthinsurance.claims.domain.AdjudicationDecision;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "claim_adjudication", uniqueConstraints = {
    @UniqueConstraint(name = "uk_claim_adjudication_claim_id", columnNames = {"claim_id"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClaimAdjudication {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "adjudication_id", updatable = false, nullable = false)
    private UUID adjudicationId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "claim_id", nullable = false, unique = true)
    private Claim claim;

    @Column(name = "submitted_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal submittedAmount;

    @Column(name = "allowed_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal allowedAmount;

    @Column(name = "deductible_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal deductibleAmount = BigDecimal.ZERO;

    @Column(name = "copay_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal copayAmount = BigDecimal.ZERO;

    @Column(name = "copay_percentage", precision = 5, scale = 2)
    private BigDecimal copayPercentage = BigDecimal.ZERO;

    @Column(name = "payable_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal payableAmount = BigDecimal.ZERO;

    @Column(name = "customer_responsibility", nullable = false, precision = 12, scale = 2)
    private BigDecimal customerResponsibility = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "decision", nullable = false, length = 30)
    private AdjudicationDecision decision;

    @Column(name = "reason", length = 500)
    private String reason;

    @Version
    private Long version;

    @Column(name = "adjudicated_at", nullable = false, updatable = false)
    private Instant adjudicatedAt = Instant.now();

    @Column(name = "created_by", length = 100)
    private String createdBy = "SYSTEM";

    @Column(name = "updated_by", length = 100)
    private String updatedBy = "SYSTEM";

    @PrePersist
    public void prePersist() {
        if (this.adjudicatedAt == null) this.adjudicatedAt = Instant.now();
        if (this.deductibleAmount == null) this.deductibleAmount = BigDecimal.ZERO;
        if (this.copayAmount == null) this.copayAmount = BigDecimal.ZERO;
        if (this.copayPercentage == null) this.copayPercentage = BigDecimal.ZERO;
        if (this.payableAmount == null) this.payableAmount = BigDecimal.ZERO;
        if (this.customerResponsibility == null) this.customerResponsibility = BigDecimal.ZERO;
    }
}
