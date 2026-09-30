package com.healthinsurance.claims.entity;

import com.healthinsurance.claims.domain.ClaimStatus;
import com.healthinsurance.claims.domain.ClaimType;
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
@Table(name = "claim", uniqueConstraints = {
    @UniqueConstraint(name = "uk_claim_number", columnNames = {"claim_number"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Claim {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "claim_id", updatable = false, nullable = false)
    private UUID claimId;

    @Column(name = "claim_number", nullable = false, unique = true, length = 50)
    private String claimNumber;

    @Column(name = "policy_id", nullable = false)
    private UUID policyId;

    @Column(name = "member_id", nullable = false)
    private UUID memberId;

    @Column(name = "provider_id", nullable = false)
    private UUID providerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "claim_type", nullable = false, length = 30)
    private ClaimType claimType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private ClaimStatus status = ClaimStatus.SUBMITTED;

    @Column(name = "service_date", nullable = false)
    private LocalDate serviceDate;

    @Column(name = "admission_date")
    private LocalDate admissionDate;

    @Column(name = "discharge_date")
    private LocalDate dischargeDate;

    @Column(name = "total_claim_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalClaimAmount = BigDecimal.ZERO;

    @Column(name = "approved_amount", precision = 12, scale = 2)
    private BigDecimal approvedAmount = BigDecimal.ZERO;

    @Column(name = "remarks", length = 1000)
    private String remarks;

    @Version
    private Long version;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at")
    private Instant updatedAt = Instant.now();

    @Column(name = "created_by", length = 100)
    private String createdBy = "SYSTEM";

    @Column(name = "updated_by", length = 100)
    private String updatedBy = "SYSTEM";

    // Relationships to the other 7 tables
    @OneToMany(mappedBy = "claim", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ClaimService> serviceLines = new ArrayList<>();

    @OneToMany(mappedBy = "claim", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ClaimDiagnosis> diagnoses = new ArrayList<>();

    @OneToMany(mappedBy = "claim", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ClaimDocument> documents = new ArrayList<>();

    @OneToMany(mappedBy = "claim", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ClaimValidation> validations = new ArrayList<>();

    @OneToOne(mappedBy = "claim", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private ClaimAdjudication adjudication;

    @OneToMany(mappedBy = "claim", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ClaimPayment> payments = new ArrayList<>();

    @OneToOne(mappedBy = "claim", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private ExplanationOfBenefits explanationOfBenefits;

    // Helper methods for bidirectional relationships
    public void addServiceLine(ClaimService service) {
        serviceLines.add(service);
        service.setClaim(this);
    }

    public void addDiagnosis(ClaimDiagnosis diagnosis) {
        diagnoses.add(diagnosis);
        diagnosis.setClaim(this);
    }

    public void addDocument(ClaimDocument document) {
        documents.add(document);
        document.setClaim(this);
    }

    public void addValidation(ClaimValidation validation) {
        validations.add(validation);
        validation.setClaim(this);
    }

    public void setAdjudication(ClaimAdjudication adjudication) {
        this.adjudication = adjudication;
        if (adjudication != null) {
            adjudication.setClaim(this);
        }
    }

    public void addPayment(ClaimPayment payment) {
        payments.add(payment);
        payment.setClaim(this);
    }

    public void setExplanationOfBenefits(ExplanationOfBenefits eob) {
        this.explanationOfBenefits = eob;
        if (eob != null) {
            eob.setClaim(this);
        }
    }

    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) this.createdAt = Instant.now();
        if (this.updatedAt == null) this.updatedAt = Instant.now();
        if (this.status == null) this.status = ClaimStatus.SUBMITTED;
        if (this.totalClaimAmount == null) this.totalClaimAmount = BigDecimal.ZERO;
        if (this.approvedAmount == null) this.approvedAmount = BigDecimal.ZERO;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = Instant.now();
    }
}
