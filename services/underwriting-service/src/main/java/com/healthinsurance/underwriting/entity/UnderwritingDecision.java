package com.healthinsurance.underwriting.entity;

import com.healthinsurance.underwriting.enums.UnderwritingDecisionType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import java.time.Instant;
import java.util.UUID;

@Data
@Entity
@Table(name = "underwriting_decision")
public class UnderwritingDecision {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID decisionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "case_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private UnderwritingCase underwritingCase;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UnderwritingDecisionType decisionType;

    private String reason;
    private String notes;

    @Column(nullable = false, updatable = false)
    private Instant decidedAt = Instant.now();
}