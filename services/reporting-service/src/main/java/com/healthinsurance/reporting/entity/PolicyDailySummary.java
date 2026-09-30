package com.healthinsurance.reporting.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "policy_daily_summary", indexes = {
    @Index(name = "idx_policy_summary_date", columnList = "summary_date", unique = true)
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PolicyDailySummary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "summary_date", nullable = false, unique = true)
    private LocalDate summaryDate;

    @Column(name = "total_policies_issued", nullable = false)
    @Builder.Default
    private Long totalPoliciesIssued = 0L;

    @Column(name = "total_policies_active", nullable = false)
    @Builder.Default
    private Long totalPoliciesActive = 0L;

    @Column(name = "total_policies_expired", nullable = false)
    @Builder.Default
    private Long totalPoliciesExpired = 0L;

    @Column(name = "total_policies_cancelled", nullable = false)
    @Builder.Default
    private Long totalPoliciesCancelled = 0L;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (updatedAt == null) updatedAt = LocalDateTime.now();
        if (totalPoliciesIssued == null) totalPoliciesIssued = 0L;
        if (totalPoliciesActive == null) totalPoliciesActive = 0L;
        if (totalPoliciesExpired == null) totalPoliciesExpired = 0L;
        if (totalPoliciesCancelled == null) totalPoliciesCancelled = 0L;
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
