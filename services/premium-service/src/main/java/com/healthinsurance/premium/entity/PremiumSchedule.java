package com.healthinsurance.premium.entity;

import com.healthinsurance.premium.enums.PaymentFrequency;
import com.healthinsurance.premium.enums.PremiumStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "premium_schedule", uniqueConstraints = {
    @UniqueConstraint(name = "uk_premium_schedule_policy_id", columnNames = {"policy_id"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PremiumSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "schedule_id", updatable = false, nullable = false)
    private UUID scheduleId;

    @Column(name = "policy_id", nullable = false, unique = true)
    private UUID policyId;

    @Column(name = "total_premium", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalPremium;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_frequency", nullable = false, length = 30)
    private PaymentFrequency paymentFrequency;

    @Column(name = "number_of_installments", nullable = false)
    private Integer numberOfInstallments;

    @Column(name = "paid_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal paidAmount = BigDecimal.ZERO;

    @Column(name = "outstanding_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal outstandingAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "premium_status", nullable = false, length = 30)
    private PremiumStatus premiumStatus = PremiumStatus.PENDING;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at")
    private Instant updatedAt = Instant.now();

    @OneToMany(mappedBy = "premiumSchedule", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<PremiumInstallment> installments = new ArrayList<>();

    public void addInstallment(PremiumInstallment installment) {
        installments.add(installment);
        installment.setPremiumSchedule(this);
    }

    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) this.createdAt = Instant.now();
        if (this.updatedAt == null) this.updatedAt = Instant.now();
        if (this.paidAmount == null) this.paidAmount = BigDecimal.ZERO;
        if (this.outstandingAmount == null) this.outstandingAmount = this.totalPremium;
        if (this.premiumStatus == null) this.premiumStatus = PremiumStatus.PENDING;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = Instant.now();
    }
}