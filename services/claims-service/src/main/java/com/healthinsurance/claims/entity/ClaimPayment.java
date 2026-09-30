package com.healthinsurance.claims.entity;

import com.healthinsurance.claims.domain.PayeeType;
import com.healthinsurance.claims.domain.PaymentStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "claim_payment")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClaimPayment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "claim_payment_id", updatable = false, nullable = false)
    private UUID claimPaymentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "claim_id", nullable = false)
    private Claim claim;

    @Column(name = "payment_reference_number", length = 100)
    private String paymentReferenceNumber;

    @Column(name = "paid_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal paidAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "payee_type", nullable = false, length = 30)
    private PayeeType payeeType;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false, length = 30)
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    @Version
    private Long version;

    @Column(name = "settled_at")
    private Instant settledAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at")
    private Instant updatedAt = Instant.now();

    @Column(name = "created_by", length = 100)
    private String createdBy = "SYSTEM";

    @Column(name = "updated_by", length = 100)
    private String updatedBy = "SYSTEM";

    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) this.createdAt = Instant.now();
        if (this.paymentStatus == null) this.paymentStatus = PaymentStatus.PENDING;
    }
}
