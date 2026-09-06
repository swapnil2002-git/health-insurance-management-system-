package com.healthinsurance.underwriting.entity;

import com.healthinsurance.underwriting.enums.CaseStatus;
import jakarta.persistence.*;
import lombok.Data;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Data
@Entity
@Table(name = "underwriting_case")
public class UnderwritingCase {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID caseId;

    // External References (No JPA mappings!)
    @Column(nullable = false)
    private UUID quoteId;

    @Column(nullable = false)
    private UUID customerId;

    // UNIQUE constraint guarantees idempotency: One risk assessment -> One underwriting case
    @Column(nullable = false, unique = true)
    private UUID assessmentId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CaseStatus status = CaseStatus.OPEN;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    private Instant updatedAt = Instant.now();
    private Instant completedAt;

    @OneToMany(mappedBy = "underwritingCase", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<UnderwritingDecision> decisions;
}