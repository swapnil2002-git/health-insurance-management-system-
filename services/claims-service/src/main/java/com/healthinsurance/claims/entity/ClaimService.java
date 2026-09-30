package com.healthinsurance.claims.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "claim_service")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClaimService {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "claim_service_id", updatable = false, nullable = false)
    private UUID claimServiceId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "claim_id", nullable = false)
    private Claim claim;

    @Column(name = "service_code", nullable = false, length = 50)
    private String serviceCode;

    @Column(name = "service_description", nullable = false, length = 255)
    private String serviceDescription;

    @Column(name = "service_date", nullable = false)
    private LocalDate serviceDate;

    @Column(name = "unit_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "quantity", nullable = false)
    private Integer quantity = 1;

    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) this.createdAt = Instant.now();
        if (this.quantity == null || this.quantity < 1) this.quantity = 1;
        if (this.totalAmount == null && this.unitPrice != null) {
            this.totalAmount = this.unitPrice.multiply(BigDecimal.valueOf(this.quantity));
        }
    }
}
