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
@Table(name = "claim_daily_summary", indexes = {
    @Index(name = "idx_claim_summary_date", columnList = "summary_date", unique = true)
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClaimDailySummary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "summary_date", nullable = false, unique = true)
    private LocalDate summaryDate;

    @Column(name = "total_claims_submitted", nullable = false)
    @Builder.Default
    private Long totalClaimsSubmitted = 0L;

    @Column(name = "total_claims_approved", nullable = false)
    @Builder.Default
    private Long totalClaimsApproved = 0L;

    @Column(name = "total_claims_rejected", nullable = false)
    @Builder.Default
    private Long totalClaimsRejected = 0L;

    @Column(name = "total_claims_settled", nullable = false)
    @Builder.Default
    private Long totalClaimsSettled = 0L;

    @Column(name = "total_claimed_amount", nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal totalClaimedAmount = BigDecimal.ZERO;

    @Column(name = "total_approved_amount", nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal totalApprovedAmount = BigDecimal.ZERO;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (updatedAt == null) updatedAt = LocalDateTime.now();
        if (totalClaimsSubmitted == null) totalClaimsSubmitted = 0L;
        if (totalClaimsApproved == null) totalClaimsApproved = 0L;
        if (totalClaimsRejected == null) totalClaimsRejected = 0L;
        if (totalClaimsSettled == null) totalClaimsSettled = 0L;
        if (totalClaimedAmount == null) totalClaimedAmount = BigDecimal.ZERO;
        if (totalApprovedAmount == null) totalApprovedAmount = BigDecimal.ZERO;
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
