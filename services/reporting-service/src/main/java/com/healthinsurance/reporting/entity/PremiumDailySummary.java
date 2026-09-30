package com.healthinsurance.reporting.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "premium_daily_summary", indexes = {
    @Index(name = "idx_premium_summary_date", columnList = "summary_date", unique = true)
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PremiumDailySummary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "summary_date", nullable = false, unique = true)
    private LocalDate summaryDate;

    @Column(name = "total_payments_collected", nullable = false)
    @Builder.Default
    private Long totalPaymentsCollected = 0L;

    @Column(name = "total_premium_amount", nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal totalPremiumAmount = BigDecimal.ZERO;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (updatedAt == null) updatedAt = LocalDateTime.now();
        if (totalPaymentsCollected == null) totalPaymentsCollected = 0L;
        if (totalPremiumAmount == null) totalPremiumAmount = BigDecimal.ZERO;
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
